<?php

namespace Tests\Feature;

use App\Services\PushMessaging;
use Firebase\JWT\JWT;
use Firebase\JWT\Key;
use Illuminate\Support\Facades\Cache;
use Illuminate\Support\Facades\Crypt;
use Illuminate\Support\Facades\Http;
use Tests\TestCase;

class PushMessagingTest extends TestCase
{
    public function test_signed_authorization_bounded_payload_and_invalid_token(): void
    {
        Http::preventStrayRequests();
        Cache::flush();
        $key = openssl_pkey_new(['private_key_bits' => 2048, 'private_key_type' => OPENSSL_KEYTYPE_RSA]);
        openssl_pkey_export($key, $pem);
        $path = tempnam(sys_get_temp_dir(), 'fcm-test-');
        file_put_contents($path, json_encode(['type' => 'service_account', 'project_id' => 'generosity-test',
            'client_email' => 'push@generosity-test.iam.gserviceaccount.com', 'private_key_id' => 'test', 'private_key' => $pem]));
        config(['push.enabled' => true, 'push.ios_enabled' => false, 'push.project_id' => 'generosity-test', 'push.credentials' => $path]);
        $install = (object) ['platform' => 'android', 'locale' => 'es', 'token' => Crypt::encryptString('device-registration-token')];
        $notice = (object) ['id' => 'notice', 'event_id' => 'event', 'center_id' => 'center', 'need_id' => 'need', 'user_id' => 1];
        Http::fake([
            'oauth2.googleapis.com/token' => Http::response(['access_token' => 'test-access', 'expires_in' => 3600]),
            'fcm.googleapis.com/*' => Http::sequence()->push(['name' => 'projects/generosity-test/messages/test'])
                ->push(['error' => ['details' => [['errorCode' => 'UNREGISTERED']]]], 404)->push([], 503),
        ]);
        try {
            $service = new PushMessaging;
            $this->assertSame('accepted', $service->send($install, $notice));
            $this->assertSame('invalid_token', $service->send($install, $notice));
            Http::assertSent(function ($request) use ($key) {
                if ($request->url() !== 'https://oauth2.googleapis.com/token') { return false; }
                $claims = JWT::decode($request['assertion'], new Key(openssl_pkey_get_details($key)['key'], 'RS256'));
                $this->assertSame('https://www.googleapis.com/auth/firebase.messaging', $claims->scope);
                $this->assertSame(3600, $claims->exp - $claims->iat);
                return true;
            });
            Http::assertSent(function ($request) {
                if (! str_contains($request->url(), '/messages:send')) { return false; }
                $message = $request['message'];
                $this->assertSame('device-registration-token', $message['token']);
                $this->assertSame('1', $message['data']['account_id']);
                $this->assertSame('3600s', $message['android']['ttl']);
                $this->assertSame('urgent_needs', $message['android']['notification']['channel_id']);
                $this->assertSame('alert', $message['apns']['headers']['apns-push-type']);
                $this->assertSame('notice', $message['apns']['headers']['apns-collapse-id']);
                $this->assertArrayNotHasKey('title', $message['data']);
                return true;
            });
            $install->platform = 'ios';
            $this->assertSame('disabled', $service->send($install, $notice));
            Http::assertSentCount(3);
            $install->platform = 'android';
            $this->expectExceptionMessage('FCM delivery temporarily unavailable');
            $service->send($install, $notice);
        } finally {
            unlink($path);
        }
    }
}
