<?php

namespace App\Services;

use App\Models\Center;
use App\Models\Need;
use Firebase\JWT\JWT;
use Illuminate\Support\Facades\Cache;
use Illuminate\Support\Facades\Crypt;
use Illuminate\Support\Facades\Http;

class PushMessaging
{
    public static function eligible(?Need $need): bool
    {
        return $need && $need->status === 'published' && ($need->details['urgent'] ?? false)
            && (! $need->expires_on || ! $need->expires_on->lt(today()))
            && ($need->quantity_required === null || $need->quantity_committed < $need->quantity_required);
    }

    public static function interested(object $preferences, Need $need): bool
    {
        return $preferences->urgent && ($preferences->all_categories || count(array_intersect(
            json_decode($preferences->categories, true, 512, JSON_THROW_ON_ERROR),
            Center::find($need->center_id)?->details['categories'] ?? []
        )) > 0);
    }

    /** Returns provider acceptance, never proof that a phone displayed or read the alert. */
    public function send(object $installation, object $notice): string
    {
        if (! config('push.enabled') || ($installation->platform === 'ios' && ! config('push.ios_enabled'))) {
            return 'disabled';
        }
        $project = config('push.project_id');
        if (! is_string($project) || ! preg_match('/^[a-z][a-z0-9-]{4,62}$/', $project)) {
            throw new \RuntimeException('Invalid FCM project configuration');
        }
        $credentials = json_decode(file_get_contents(config('push.credentials')), true, 512, JSON_THROW_ON_ERROR);
        if (($credentials['project_id'] ?? null) !== $project || ($credentials['type'] ?? null) !== 'service_account') {
            throw new \RuntimeException('FCM credential project mismatch');
        }
        $cacheKey = 'fcm-access-'.hash('sha256', $credentials['client_email'].$credentials['private_key_id']);
        $access = Cache::remember($cacheKey, 3000, function () use ($credentials) {
            $assertion = JWT::encode([
                'iss' => $credentials['client_email'], 'scope' => 'https://www.googleapis.com/auth/firebase.messaging',
                'aud' => 'https://oauth2.googleapis.com/token', 'iat' => time(), 'exp' => time() + 3600,
            ], $credentials['private_key'], 'RS256');
            $response = Http::asForm()->connectTimeout(3)->timeout(8)->post('https://oauth2.googleapis.com/token', [
                'grant_type' => 'urn:ietf:params:oauth:grant-type:jwt-bearer', 'assertion' => $assertion,
            ]);
            if (! $response->successful() || ! is_string($response->json('access_token'))) {
                throw new \RuntimeException('FCM authorization unavailable');
            }

            return $response->json('access_token');
        });
        $es = $installation->locale === 'es';
        $data = ['notice_id' => $notice->id, 'event_id' => $notice->event_id, 'kind' => 'urgent_need',
            'center_id' => $notice->center_id, 'need_id' => $notice->need_id, 'account_id' => (string) $notice->user_id];
        // Generic lock-screen text avoids exposing the account or beneficiary/medical details.
        $response = Http::withToken($access)->connectTimeout(3)->timeout(8)
            ->post("https://fcm.googleapis.com/v1/projects/{$project}/messages:send", ['message' => [
                'token' => Crypt::decryptString($installation->token), 'data' => $data,
                'notification' => ['title' => 'Generosity', 'body' => $es ? 'Hay una necesidad urgente que puedes ayudar a cubrir.' : 'There is an urgent need you can help meet.'],
                'android' => ['priority' => 'high', 'ttl' => '3600s', 'notification' => ['channel_id' => 'urgent_needs', 'tag' => $notice->id]],
                'apns' => ['headers' => ['apns-push-type' => 'alert', 'apns-priority' => '10', 'apns-expiration' => (string) (time() + 3600), 'apns-collapse-id' => $notice->id],
                    'payload' => ['aps' => ['sound' => 'default']]],
            ]]);
        if ($response->successful() && is_string($response->json('name'))) {
            return 'accepted';
        }
        $codes = array_column($response->json('error.details', []), 'errorCode');
        if (in_array('UNREGISTERED', $codes, true)) {
            return 'invalid_token';
        }
        if ($response->status() === 401) {
            Cache::forget($cacheKey);
        }
        // Do not persist provider responses: they can contain the registration token.
        if (in_array($response->status(), [400, 403, 404], true)) {
            return 'rejected';
        }
        throw new \RuntimeException('FCM delivery temporarily unavailable');
    }
}
