<?php

namespace Tests\Feature;

use App\Models\User;
use Firebase\JWT\JWT;
use Illuminate\Foundation\Testing\DatabaseMigrations;
use Illuminate\Http\Client\Factory;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Password;
use Tests\TestCase;

class SocialAuthTest extends TestCase
{
    use DatabaseMigrations;

    private string $privateKey = '';

    private array $jwk;

    protected function setUp(): void
    {
        parent::setUp();
        $key = openssl_pkey_new(['private_key_bits' => 2048, 'private_key_type' => OPENSSL_KEYTYPE_RSA]);
        openssl_pkey_export($key, $this->privateKey);
        $rsa = openssl_pkey_get_details($key)['rsa'];
        $this->jwk = ['kty' => 'RSA', 'kid' => 'test-key', 'alg' => 'RS256', 'use' => 'sig', 'n' => $this->url64($rsa['n']), 'e' => $this->url64($rsa['e'])];
        config(['identity.google.client_id' => 'generosity-client', 'identity.google.client_secret' => 'test-only', 'app.url' => 'https://api.generosity.test']);
        Http::preventStrayRequests();
    }

    public function test_hosted_registration_and_recovery_preserve_native_context(): void
    {
        \Illuminate\Support\Facades\Notification::fake();
        $context = ['client' => 'ios', 'locale' => 'es', 'state' => str_repeat('s', 43), 'code_challenge' => str_repeat('c', 43)];
        $this->get('/auth/sign-in?'.http_build_query($context))->assertOk()->assertSee('Crear cuenta')->assertSee('Recuperar contraseña');
        $registration = $context + ['screen' => 'register'];
        $this->get('/auth/sign-in?'.http_build_query($registration))->assertOk()
            ->assertSee('id="new-name"', false)->assertDontSee('id="email"', false)
            ->assertSee('client=ios')->assertSee('locale=es')
            ->assertSee('state='.$context['state'])->assertSee('code_challenge='.$context['code_challenge']);
        $this->post('/auth/account/register', $context + ['adult_confirmed' => true, 'terms_accepted' => true, 'privacy_acknowledged' => true, 'policy_version' => \App\Services\AccountPolicy::VERSION, 'name' => 'María Peña', 'email' => 'native@example.test', 'password' => 'StrongPassword123'])->assertStatus(202)->assertSee('siguientes pasos')->assertSee(str_repeat('c', 43));
        $this->assertDatabaseHas('users', ['email' => 'native@example.test', 'email_verified_at' => null]);
        $this->post('/auth/account/recover', $context + ['email' => 'unknown@example.test'])->assertStatus(202)->assertSee('siguientes pasos');
        $this->assertGuest();
    }

    public function test_hosted_entry_exposes_registration_and_local_provider_branding(): void
    {
        config(['identity.google.client_secret' => null]);
        foreach (['en' => 'Create an account', 'es' => 'Crear cuenta'] as $locale => $label) {
            $response = $this->get('/auth/sign-in?locale='.$locale);
            $response->assertOk()->assertSee($label)->assertSee('screen=register')
                ->assertSee('class="account-modes"', false)
                ->assertSee('class="provider provider-google"', false)
                ->assertSee('class="provider provider-apple"', false)
                ->assertSee('/auth-assets/google.png')->assertSee('/auth-assets/apple.png')
                ->assertDontSee('id="new-name"', false);
            $this->assertStringContainsString("img-src 'self'; font-src 'self'", $response->headers->get('Content-Security-Policy'));
        }
    }

    public function test_missing_configuration_disables_providers_and_does_not_create_attempts(): void
    {
        config(['identity.google.client_secret' => null]);
        $this->get('/auth/sign-in?locale=es')->assertOk()->assertSee('Disponible cuando se configure');
        $this->post('/auth/google/start')->assertStatus(503);
        $this->assertDatabaseCount('auth_attempts', 0);
        $this->assertDatabaseCount('users', 0);
    }

    public function test_google_sign_in_creates_verified_user_without_center_roles_and_rejects_replay(): void
    {
        [$state, $nonce] = $this->begin();
        $this->provider($nonce);
        $this->get('/auth/google/callback?state='.$state.'&code=test')->assertRedirect(config('identity.portal_url'));
        $this->getJson('/auth/session')->assertJsonPath('user.email', 'helper@example.test');
        $this->assertDatabaseCount('social_identities', 1);
        $this->assertDatabaseCount('center_memberships', 0);
        $this->assertNotNull(User::first()->email_verified_at);
        $this->get('/auth/google/callback?state='.$state.'&code=test')->assertStatus(400);
        $this->assertDatabaseCount('users', 1);
    }

    public function test_wrong_browser_binding_cannot_consume_attempt(): void
    {
        [$state] = $this->begin();
        $this->withCookie($this->cookieName($state), 'attacker');
        $this->get('/auth/google/callback?state='.$state.'&code=test')->assertStatus(400);
        $this->assertDatabaseCount('auth_attempts', 1);
        Http::assertNothingSent();
    }

    public function test_expired_cancelled_and_cross_provider_attempts_cannot_authenticate(): void
    {
        [$state] = $this->begin();
        $this->post('/auth/apple/callback', ['state' => $state, 'code' => 'test'])->assertStatus(400);
        DB::table('auth_attempts')->update(['expires_at' => now()->subSecond()]);
        $this->get('/auth/google/callback?state='.$state.'&code=test')->assertStatus(400);
        [$state] = $this->begin();
        $this->get('/auth/google/callback?state='.$state.'&error=access_denied')->assertStatus(400)->assertSee('cancelled');
        $this->assertDatabaseCount('users', 0);
        Http::assertNothingSent();
    }

    public function test_bad_signature_issuer_audience_expiry_nonce_and_unverified_email_are_rejected(): void
    {
        foreach ([['iss' => 'https://attacker.test'], ['aud' => 'another-app'], ['exp' => time() - 10], ['nonce' => 'wrong'], ['email_verified' => false], ['sub' => ''], ['iat' => time() + 100], ['azp' => 'another-app']] as $claims) {
            [$state, $nonce] = $this->begin();
            $this->provider($nonce, $claims);
            $this->get('/auth/google/callback?state='.$state.'&code=test')->assertStatus(400);
        }
        [$state, $nonce] = $this->begin();
        $this->provider($nonce, [], 'invalid-signature');
        $this->get('/auth/google/callback?state='.$state.'&code=test')->assertStatus(400);
        $this->assertDatabaseCount('users', 0);
    }

    public function test_existing_email_requires_explicit_recent_account_link_and_subject_is_identity(): void
    {
        $user = User::factory()->create(['email' => 'helper@example.test']);
        [$state, $nonce] = $this->begin();
        $this->provider($nonce);
        $this->get('/auth/google/callback?state='.$state.'&code=test')->assertStatus(400)->assertSee('already uses this email');
        $this->assertDatabaseCount('social_identities', 0);
        $this->actingAs($user)->withSession(['auth_at' => time() - 600]);
        $this->post('/auth/google/start', ['link' => '1'])->assertStatus(403);
        $this->withSession(['auth_at' => time()]);
        [$state, $nonce] = $this->begin(['link' => '1']);
        $this->provider($nonce);
        $this->get('/auth/google/callback?state='.$state.'&code=test')->assertRedirect();
        $this->assertDatabaseHas('social_identities', ['user_id' => $user->id, 'subject' => 'stable-subject']);
        auth()->logout();
        [$state, $nonce] = $this->begin();
        $this->provider($nonce, ['email' => 'changed@example.test']);
        $this->get('/auth/google/callback?state='.$state.'&code=test')->assertRedirect();
        $this->assertDatabaseCount('users', 1);
        $this->assertAuthenticatedAs($user);
    }

    public function test_native_handoff_is_bound_to_pkce_client_and_can_be_exchanged_only_once(): void
    {
        $verifier = str_repeat('v', 64);
        [$state, $nonce] = $this->begin(['client' => 'android', 'code_challenge' => $this->url64(hash('sha256', $verifier, true)), 'state' => str_repeat('s', 32)]);
        $this->provider($nonce);
        $response = $this->get('/auth/google/callback?state='.$state.'&code=test')->assertRedirect();
        $url = $response->headers->get('Location');
        $this->assertStringStartsWith('org.generosity.app.android:/oauth/callback?', $url);
        $this->assertStringNotContainsString('token', $url);
        parse_str(parse_url($url, PHP_URL_QUERY), $query);
        $this->assertSame(str_repeat('s', 32), $query['state']);
        $body = ['code' => $query['code'], 'code_verifier' => $verifier, 'client' => 'android'];
        $this->postJson('/api/v1/auth/exchange', array_replace($body, ['code_verifier' => str_repeat('x', 64)]))->assertUnauthorized();
        $this->postJson('/api/v1/auth/exchange', array_replace($body, ['client' => 'ios']))->assertUnauthorized();
        $exchange = $this->postJson('/api/v1/auth/exchange', $body)->assertOk()->assertJsonPath('user.email', 'helper@example.test');
        $this->postJson('/api/v1/auth/exchange', $body)->assertUnauthorized();
        $token = $exchange->json('token');
        auth()->forgetGuards();
        $this->withToken($token)->getJson('/api/v1/me')->assertOk();
        $this->withToken($token)->postJson('/api/v1/auth/logout')->assertNoContent();
        auth()->forgetGuards();
        $this->withToken($token)->getJson('/api/v1/me')->assertUnauthorized();
    }

    public function test_apple_form_post_verifies_signed_token_and_private_relay_email(): void
    {
        $key = openssl_pkey_new(['private_key_type' => OPENSSL_KEYTYPE_EC, 'curve_name' => 'prime256v1']);
        openssl_pkey_export($key, $pem);
        $path = tempnam(sys_get_temp_dir(), 'apple-test-');
        file_put_contents($path, $pem);
        try {
            config(['identity.apple' => ['client_id' => 'generosity-client', 'team_id' => 'team', 'key_id' => 'key', 'private_key_path' => $path]]);
            [$state, $nonce] = $this->begin([], 'apple');
            $this->provider($nonce, ['iss' => 'https://appleid.apple.com', 'email' => 'hidden@privaterelay.appleid.com', 'email_verified' => 'true']);
            $this->post('/auth/apple/callback', ['state' => $state, 'code' => 'apple-code'])->assertRedirect();
            $this->assertDatabaseHas('users', ['email' => 'hidden@privaterelay.appleid.com']);
            Http::assertSent(fn ($request) => $request->url() === 'https://appleid.apple.com/auth/token' && count(explode('.', $request['client_secret'])) === 3);
        } finally {
            unlink($path);
        }
    }

    public function test_csrf_protects_logout_and_provider_start_and_cors_is_explicit(): void
    {
        $this->app->instance('env', 'production');
        config(['cors.allowed_origins' => ['https://generosity.global', 'https://www.generosity.global']]);
        $this->post('https://api.generosity.test/auth/logout')->assertStatus(419);
        $this->post('https://api.generosity.test/auth/google/start')->assertStatus(419);
        foreach (['register', 'recover', 'verify'] as $action) {
            $this->post('https://api.generosity.test/auth/account/'.$action)->assertStatus(419);
        }
        $this->post('https://api.generosity.test/reset-password')->assertStatus(419);
        $this->withHeader('Origin', 'https://unrelated-institute.test')->getJson('https://api.generosity.test/auth/session')->assertHeaderMissing('Access-Control-Allow-Origin');
        $this->withHeader('Origin', 'https://generosity.global')->getJson('https://api.generosity.test/auth/session')->assertHeader('Access-Control-Allow-Origin', 'https://generosity.global')->assertHeader('Access-Control-Allow-Credentials', 'true');
        $this->app->instance('env', 'testing');
    }

    public function test_an_identity_cannot_be_moved_to_another_account(): void
    {
        $owner = User::factory()->create();
        DB::table('social_identities')->insert(['user_id' => $owner->id, 'provider' => 'google', 'subject' => 'stable-subject', 'created_at' => now(), 'updated_at' => now()]);
        $other = User::factory()->create();
        $this->actingAs($other)->withSession(['auth_at' => time()]);
        [$state, $nonce] = $this->begin(['link' => '1']);
        $this->provider($nonce);
        $this->get('/auth/google/callback?state='.$state.'&code=test')->assertStatus(400);
        $this->assertDatabaseHas('social_identities', ['user_id' => $owner->id, 'subject' => 'stable-subject']);
        $this->assertDatabaseCount('social_identities', 1);
    }

    public function test_expired_handoff_and_arbitrary_return_urls_are_rejected(): void
    {
        $user = User::factory()->create();
        $verifier = str_repeat('v', 64);
        $code = str_repeat('c', 64);
        DB::table('auth_handoffs')->insert(['id' => hash('sha256', $code), 'user_id' => $user->id, 'client' => 'ios', 'challenge' => $this->url64(hash('sha256', $verifier, true)), 'expires_at' => now()->subSecond()]);
        $this->postJson('/api/v1/auth/exchange', ['client' => 'ios', 'code' => $code, 'code_verifier' => $verifier])->assertUnauthorized();
        [$state, $nonce] = $this->begin(['redirect_uri' => 'https://unrelated-institute.test/steal']);
        $this->provider($nonce);
        $this->get('/auth/google/callback?state='.$state.'&code=test')->assertRedirect(config('identity.portal_url'));
    }

    public function test_password_reset_revokes_browser_sessions_and_pending_mobile_handoffs(): void
    {
        $user = User::factory()->create();
        DB::table('sessions')->insert(['id' => 'browser-test', 'user_id' => $user->id, 'payload' => '', 'last_activity' => time()]);
        DB::table('auth_handoffs')->insert(['id' => str_repeat('h', 64), 'user_id' => $user->id, 'client' => 'ios', 'challenge' => str_repeat('v', 43), 'expires_at' => now()->addMinute()]);
        $user->createToken('ios');
        $token = Password::createToken($user);
        $this->postJson('/api/v1/auth/reset-password', ['email' => $user->email, 'token' => $token, 'password' => 'NewPassword12345', 'password_confirmation' => 'NewPassword12345'])->assertOk();
        $this->assertDatabaseCount('auth_handoffs', 0);
        $this->assertDatabaseCount('personal_access_tokens', 0);
        $this->assertDatabaseMissing('sessions', ['user_id' => $user->id]);
    }

    private function begin(array $context = [], string $provider = 'google'): array
    {
        $response = $this->post('/auth/'.$provider.'/start', $context + ['adult_confirmed' => true, 'terms_accepted' => true, 'privacy_acknowledged' => true, 'policy_version' => \App\Services\AccountPolicy::VERSION, ])->assertRedirect();
        parse_str(parse_url($response->headers->get('Location'), PHP_URL_QUERY), $query);
        $cookie = $response->getCookie($this->cookieName($query['state']));
        $this->assertTrue($cookie->isSecure());
        $this->assertTrue($cookie->isHttpOnly());
        $this->assertSame('none', $cookie->getSameSite());
        $this->withCookie($cookie->getName(), $cookie->getValue());

        return [$query['state'], $query['nonce']];
    }

    private function provider(string $nonce, array $overrides = [], ?string $bad = null): void
    {
        $claims = array_replace(['iss' => 'https://accounts.google.com', 'aud' => 'generosity-client', 'sub' => 'stable-subject', 'nonce' => $nonce, 'iat' => time(), 'exp' => time() + 300, 'email' => 'helper@example.test', 'email_verified' => true, 'name' => 'María Peña'], $overrides);
        $jwt = JWT::encode($claims, $this->privateKey, 'RS256', 'test-key');
        if ($bad) {
            $jwt = substr($jwt, 0, strrpos($jwt, '.') + 1).$this->url64(str_repeat('x', 256));
        }
        Http::swap(new Factory);
        Http::preventStrayRequests();
        Http::fake(['*token' => Http::response(['id_token' => $jwt]), '*certs' => Http::response(['keys' => [$this->jwk]]), '*keys' => Http::response(['keys' => [$this->jwk]])]);
    }

    private function cookieName(string $state): string
    {
        return 'generosity_auth_'.substr(hash('sha256', $state), 0, 16);
    }

    private function url64(string $value): string
    {
        return rtrim(strtr(base64_encode($value), '+/', '-_'), '=');
    }
}
