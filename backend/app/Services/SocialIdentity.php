<?php

namespace App\Services;

use App\Exceptions\ApiProblem;
use App\Models\User;
use Firebase\JWT\JWK;
use Firebase\JWT\JWT;
use Illuminate\Support\Facades\Cache;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Str;

class SocialIdentity
{
    private const ENDPOINTS = [
        'google' => ['authorize' => 'https://accounts.google.com/o/oauth2/v2/auth', 'token' => 'https://oauth2.googleapis.com/token', 'keys' => 'https://www.googleapis.com/oauth2/v3/certs', 'issuer' => 'https://accounts.google.com'],
        'apple' => ['authorize' => 'https://appleid.apple.com/auth/authorize', 'token' => 'https://appleid.apple.com/auth/token', 'keys' => 'https://appleid.apple.com/auth/keys', 'issuer' => 'https://appleid.apple.com'],
    ];

    public function enabled(string $provider): bool
    {
        if (! isset(self::ENDPOINTS[$provider])) {
            return false;
        }
        $config = config('identity.'.$provider);
        if (in_array(null, $config, true) || in_array('', $config, true)) {
            return false;
        }

        return $provider !== 'apple' || is_readable($config['private_key_path']);
    }

    public function redirectUri(string $provider): string
    {
        return rtrim(config('app.url'), '/').'/auth/'.$provider.'/callback';
    }

    public function authorizationUrl(string $provider, string $state, string $nonce): string
    {
        if (! $this->enabled($provider)) {
            throw new ApiProblem('provider_unavailable', 503);
        }

        return self::ENDPOINTS[$provider]['authorize'].'?'.http_build_query([
            'client_id' => config('identity.'.$provider.'.client_id'),
            'redirect_uri' => $this->redirectUri($provider), 'response_type' => 'code',
            'scope' => $provider === 'google' ? 'openid email profile' : 'name email',
            'response_mode' => $provider === 'apple' ? 'form_post' : 'query',
            'state' => $state, 'nonce' => $nonce,
        ], '', '&', PHP_QUERY_RFC3986);
    }

    public function claims(string $provider, string $code, string $nonce): object
    {
        if (! $this->enabled($provider)) {
            throw new ApiProblem('provider_unavailable', 503);
        }
        $config = config('identity.'.$provider);
        $secret = $provider === 'google' ? $config['client_secret'] : JWT::encode([
            'iss' => $config['team_id'], 'sub' => $config['client_id'], 'aud' => 'https://appleid.apple.com',
            'iat' => time(), 'exp' => time() + 300,
        ], file_get_contents($config['private_key_path']), 'ES256', $config['key_id']);
        $response = Http::asForm()->timeout(10)->withOptions(['allow_redirects' => false])->post(self::ENDPOINTS[$provider]['token'], [
            'grant_type' => 'authorization_code', 'code' => $code, 'redirect_uri' => $this->redirectUri($provider),
            'client_id' => $config['client_id'], 'client_secret' => $secret,
        ]);
        if (! $response->successful() || ! is_string($response->json('id_token')) || strlen($response->body()) > 65536) {
            throw new ApiProblem('identity_rejected', 401);
        }
        $jwt = $response->json('id_token');
        $header = json_decode(base64_decode(strtr(explode('.', $jwt)[0], '-_', '+/')), true);
        if (($header['alg'] ?? '') !== 'RS256' || ! is_string($header['kid'] ?? null)) {
            throw new ApiProblem('identity_rejected', 401);
        }
        $loadKeys = fn () => Http::timeout(10)->withOptions(['allow_redirects' => false])->get(self::ENDPOINTS[$provider]['keys'])->throw()->json();
        $keys = Cache::remember('oidc.keys.'.$provider, 3600, $loadKeys);
        // Providers rotate signing keys; refresh once for an unknown key ID.
        if (! in_array($header['kid'], array_column($keys['keys'] ?? [], 'kid'), true)) {
            $keys = $loadKeys();
            Cache::put('oidc.keys.'.$provider, $keys, 3600);
        }
        $keys['keys'] = array_values(array_filter($keys['keys'] ?? [], fn ($key) => ($key['kty'] ?? '') === 'RSA' && ($key['alg'] ?? 'RS256') === 'RS256' && ($key['use'] ?? 'sig') === 'sig'));
        $claims = JWT::decode($jwt, JWK::parseKeySet($keys, 'RS256'));
        $issuers = $provider === 'google' ? ['https://accounts.google.com', 'accounts.google.com'] : [self::ENDPOINTS[$provider]['issuer']];
        $audiences = (array) ($claims->aud ?? []);
        if (! in_array($claims->iss ?? '', $issuers, true)
            || ! in_array($config['client_id'], $audiences, true)
            || (isset($claims->azp) && $claims->azp !== $config['client_id'])
            || (count($audiences) > 1 && ! isset($claims->azp))
            || ! is_numeric($claims->exp ?? null) || $claims->exp <= time()
            || ! is_numeric($claims->iat ?? null) || $claims->iat > time() + 30
            || ! is_string($claims->sub ?? null) || $claims->sub === '' || strlen($claims->sub) > 255
            || ! is_string($claims->nonce ?? null) || ! hash_equals($nonce, $claims->nonce)) {
            throw new ApiProblem('identity_rejected', 401);
        }

        return $claims;
    }

    public function user(string $provider, object $claims, ?int $linkUserId, array $acceptance = []): User
    {
        return DB::transaction(function () use ($provider, $claims, $linkUserId, $acceptance) {
            $identity = DB::table('social_identities')->where('provider', $provider)->where('subject', $claims->sub)->lockForUpdate()->first();
            if ($identity) {
                if ($linkUserId !== null && $identity->user_id !== $linkUserId) {
                    throw new ApiProblem('identity_already_linked', 409);
                }

                return User::whereKey($identity->user_id)->lockForUpdate()->firstOrFail();
            }
            if ($linkUserId !== null) {
                $user = User::whereKey($linkUserId)->lockForUpdate()->firstOrFail();
                if (DB::table('social_identities')->where('user_id', $user->id)->where('provider', $provider)->exists()) {
                    throw new ApiProblem('identity_already_linked', 409);
                }
            } else {
                if (! in_array($claims->email_verified ?? false, [true, 'true'], true)
                    || ! filter_var($claims->email ?? '', FILTER_VALIDATE_EMAIL) || strlen($claims->email) > 254) {
                    throw new ApiProblem('verified_email_required', 403);
                }
                $email = strtolower($claims->email);
                if (User::withoutGlobalScope('active')->where('email', $email)->exists()) {
                    throw new ApiProblem('account_link_required', 409);
                }
                $user = User::create([
                    'name' => mb_substr(is_string($claims->name ?? null) ? $claims->name : 'Generosity', 0, 120),
                    'email' => $email, 'password' => Str::random(64),
                ]);
                $user->forceFill(AccountPolicy::acceptance($acceptance))->save();
                $user->markEmailAsVerified();
            }
            DB::table('social_identities')->insert([
                'user_id' => $user->id, 'provider' => $provider, 'subject' => $claims->sub,
                'created_at' => now(), 'updated_at' => now(),
            ]);

            return $user;
        }, 3);
    }
}
