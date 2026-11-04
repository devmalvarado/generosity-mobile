<?php

namespace Tests\Feature;

use App\Exceptions\ApiProblem;
use App\Models\Center;
use App\Models\Contribution;
use App\Models\Need;
use App\Models\User;
use App\Notifications\ResetPassword;
use App\Notifications\VerifyEmail;
use App\Services\AccountPolicy;
use App\Services\CatalogImport;
use Illuminate\Foundation\Testing\DatabaseMigrations;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Log;
use Illuminate\Support\Facades\Notification;
use Illuminate\Support\Facades\Password;
use Illuminate\Support\Facades\Route;
use Illuminate\Support\Facades\URL;
use Illuminate\Support\Str;
use Illuminate\Testing\TestResponse;
use Illuminate\Validation\ValidationException;
use Symfony\Component\Process\Process;
use Tests\TestCase;

class ApiTest extends TestCase
{
    use DatabaseMigrations;

    protected function setUp(): void
    {
        parent::setUp();
        $this->assertSame('generosity_test', DB::connection()->getDatabaseName());
        app(CatalogImport::class)->run($this->catalog(), 'test');
    }

    private function catalog(): array
    {
        $center = [
            'id' => 'center-a', 'name' => ['es' => 'Centro de apoyo', 'en' => 'Support center'],
            'description' => ['es' => 'Niñas, niños y familias'], 'address' => ['es' => 'Ciudad de México'],
            'categories' => ['health'], 'coordinates' => ['latitude' => 19.4, 'longitude' => -99.1],
            'contact' => ['email' => 'test@example.test'], 'verificationStatus' => 'imported',
            'importedAtIsoDate' => '2026-10-31',
            'needs' => [['id' => 'need-a', 'title' => ['es' => 'Útiles'], 'description' => ['es' => 'Lápices'],
                'helpMethods' => ['goods', 'time', 'money'], 'urgent' => true, 'quantityRequired' => 2,
                'expiresOnIsoDate' => now()->addYear()->toDateString(), 'unit' => 'kit']],
        ];

        return ['schemaVersion' => 1, 'centers' => [$center, array_merge($center, ['id' => 'center-b', 'needs' => []])]];
    }

    private function actor(?string $center = null): array
    {
        $user = User::factory()->create();
        if ($center) {
            DB::table('center_memberships')->insert(['user_id' => $user->id, 'center_id' => $center]);
        }

        return [$user, $user->createToken('test', ['*'], now()->addDays(7))->plainTextToken];
    }

    private function callApi(string $method, string $path, ?string $token = null, array $data = [], ?string $key = null)
    {
        $this->app['auth']->forgetGuards();

        return $this->json($method, '/api/v1/'.$path, $data, array_filter([
            'Authorization' => $token ? 'Bearer '.$token : null,
            'Idempotency-Key' => $key ?? (string) Str::uuid(),
        ]));
    }

    private function offer(string $token, int $quantity = 1)
    {
        return $this->callApi('POST', 'contributions', $token, ['need_id' => 'need-a', 'method' => 'goods', 'quantity' => $quantity]);
    }

    public function test_partial_box_progress_distinguishes_reserved_and_received_and_preserves_legacy_sync(): void
    {
        $need = Need::findOrFail('need-a');
        $need->update(['quantity_required' => 10, 'details' => array_merge($need->details, ['unit' => 'cajas de 10 piezas'])]);
        [, $donor] = $this->actor();
        [, $other] = $this->actor();
        [, $staff] = $this->actor('center-a');
        $this->callApi('POST', 'contributions', $donor, ['need_id' => 'need-a', 'method' => 'goods', 'quantity' => 1, 'quantity_unit' => 'piezas'])->assertUnprocessable()->assertJsonPath('error.code', 'unit_mismatch');
        $first = $this->offer($donor, 1)->assertCreated()->assertJsonPath('quantity_unit', 'cajas de 10 piezas')->json();
        $this->callApi('GET', 'needs/need-a')->assertJsonPath('quantityRequired', 10)->assertJsonPath('quantityCommitted', 1)->assertJsonPath('quantityReceived', 0);
        $path = 'contributions/'.$first['id'].'/transition';
        $this->callApi('POST', $path, $donor, ['action' => 'fulfill', 'version' => 1])->assertForbidden();
        $this->callApi('POST', $path, $staff, ['action' => 'confirm', 'version' => 1])->assertOk();
        $this->callApi('GET', 'needs/need-a')->assertJsonPath('quantityReceived', 0);
        $key = (string) Str::uuid();
        for ($i = 0; $i < 2; $i++) {
            $this->callApi('POST', $path, $staff, ['action' => 'fulfill', 'version' => 2], $key)->assertOk();
        }
        $this->callApi('GET', 'needs')->assertJsonPath('data.0.quantityReceived', 1);
        $this->callApi('GET', 'staff/centers/center-a/needs', $staff)->assertJsonPath('data.0.quantityReceived', 1);
        $rest = $this->offer($other, 9)->assertCreated()->json();
        $this->offer($other, 1)->assertConflict()->assertJsonPath('error.code', 'capacity_exceeded');
        $modern = $this->callApi('GET', 'sync/catalog?progress_version=1')->assertOk();
        $modern->assertJsonPath('catalog.centers.0.needs.0.quantityCommitted', 10)->assertJsonPath('catalog.centers.0.needs.0.quantityReceived', 1)->assertJsonPath('catalog.centers.0.needs.0.status', 'covered');
        $legacy = $this->callApi('GET', 'sync/catalog')->assertOk();
        $this->assertArrayNotHasKey('quantityReceived', $legacy->json('catalog.centers.0.needs.0'));
        $this->callApi('POST', 'contributions/'.$rest['id'].'/transition', $other, ['action' => 'cancel', 'version' => 1])->assertOk();
        $this->callApi('GET', 'sync/catalog?progress_version=1')->assertJsonPath('catalog.centers.0.needs.0.quantityCommitted', 1)->assertJsonPath('catalog.centers.0.needs.0.quantityReceived', 1)->assertJsonPath('catalog.centers.0.needs.0.status', 'active');
        $this->callApi('GET', 'sync/catalog?progress_version=2')->assertUnprocessable();
        $this->callApi('POST', 'contributions', $donor, ['need_id' => 'need-a', 'method' => 'money', 'amount' => 10000])->assertUnprocessable();
    }

    public function test_public_catalog_is_paginated_unicode_and_hides_drafts(): void
    {
        $this->callApi('GET', 'centers')->assertOk()->assertJsonPath('data.0.name.es', 'Centro de apoyo')->assertJsonPath('per_page', 25);
        $this->callApi('GET', 'needs')->assertOk()->assertJsonPath('data.0.title.es', 'Útiles');
        Need::whereKey('need-a')->update(['status' => 'draft']);
        $this->callApi('GET', 'needs/need-a')->assertNotFound();
        $this->callApi('GET', 'needs')->assertJsonCount(0, 'data');
        $this->callApi('GET', 'me')->assertUnauthorized()->assertJsonPath('error.code', 'unauthenticated');
        $this->get('/api/v1/me')->assertUnauthorized()->assertJsonPath('error.code', 'unauthenticated');
    }

    public function test_client_snapshots_preserve_catalog_contract_and_account_isolation(): void
    {
        [$owner, $token] = $this->actor();
        [, $otherToken] = $this->actor();
        $snapshot = $this->callApi('GET', 'sync/catalog')->assertOk();
        $snapshot->assertJsonPath('catalog.schemaVersion', 1)->assertJsonPath('catalog.centers.0.needs.0.status', 'active');
        $revision = $snapshot->json('revision');
        $offer = $this->offer($token)->assertCreated()->json();
        $this->assertNotSame($revision, $this->callApi('GET', 'sync/catalog')->json('revision'));
        $this->callApi('GET', 'sync/account', $token)->assertJsonPath('user.id', $owner->id)
            ->assertJsonPath('contributions.0.title.es', 'Útiles')->assertJsonPath('contributions.0.quantity_unit', 'kit');
        $this->callApi('GET', 'sync/account', $otherToken)->assertJsonCount(0, 'contributions');
        $this->callApi('GET', 'sync/account')->assertUnauthorized();
        Need::whereKey('need-a')->update(['status' => 'closed']);
        $this->callApi('GET', 'sync/catalog')->assertJsonCount(0, 'catalog.centers.0.needs');
        $this->callApi('GET', 'sync/account', $token)->assertJsonPath('contributions.0.id', $offer['id']);
    }

    public function test_browser_session_writes_use_csrf_session_authority_and_recheck_center_membership(): void
    {
        [$user] = $this->actor('center-a');
        config(['sanctum.stateful' => ['portal.generosity.test']]);
        $this->actingAs($user, 'web')->withSession(['_token' => 'test-csrf']);
        $headers = ['Origin' => 'https://portal.generosity.test', 'X-CSRF-TOKEN' => 'test-csrf', 'Idempotency-Key' => (string) Str::uuid()];
        $body = ['need_id' => 'need-a', 'method' => 'goods', 'quantity' => 1];
        $this->postJson('/api/v1/contributions', $body, $headers)->assertCreated();
        $this->postJson('/api/v1/contributions', $body, $headers)->assertCreated();
        $this->assertDatabaseCount('contributions', 1);
        $this->getJson('/api/v1/staff/centers', $headers)->assertJsonCount(1, 'centers')->assertJsonPath('centers.0.id', 'center-a');
        DB::table('center_memberships')->where('user_id', $user->id)->delete();
        $this->getJson('/api/v1/staff/centers', $headers)->assertJsonCount(0, 'centers');
        $this->getJson('/api/v1/staff/centers/center-a/needs', $headers)->assertForbidden();
    }

    public function test_staff_cookie_writes_require_valid_csrf_in_production(): void
    {
        try {
            $browser = $this->staffBrowser();
            $before = $this->staffWriteSnapshot();
            foreach ($browser['writes'] as [$method, $path, $body, $status]) {
                foreach ([null, 'incorrect-csrf'] as $csrf) {
                    $headers = ['Idempotency-Key' => (string) Str::uuid()];
                    if ($csrf !== null) {
                        $headers['X-CSRF-TOKEN'] = $csrf;
                    }
                    $this->browserRequest($method, $path, $browser['cookie'], $body, $headers)->assertStatus(419);
                    $this->assertSame($before, $this->staffWriteSnapshot());
                }
            }

            foreach ($browser['writes'] as [$method, $path, $body, $status]) {
                $this->browserRequest($method, $path, $browser['cookie'], $body, [
                    'X-CSRF-TOKEN' => $browser['csrf'], 'Idempotency-Key' => (string) Str::uuid(),
                ])->assertStatus($status)->assertHeader('Access-Control-Allow-Origin', 'https://portal.generosity.test');
            }
        } finally {
            $this->app->instance('env', 'testing');
        }
    }

    public function test_foreign_origin_cannot_authorize_staff_writes_with_a_valid_cookie(): void
    {
        try {
            $browser = $this->staffBrowser();
            $before = $this->staffWriteSnapshot();
            foreach ($browser['writes'] as [$method, $path, $body]) {
                $this->browserRequest($method, $path, $browser['cookie'], $body, [
                    'Origin' => 'https://foreign.example.test', 'Referer' => 'https://foreign.example.test/',
                    'X-CSRF-TOKEN' => $browser['csrf'], 'Idempotency-Key' => (string) Str::uuid(),
                ])->assertUnauthorized()->assertJsonPath('error.code', 'unauthenticated')
                    // The single-origin CORS policy always names the trusted origin, never the caller.
                    ->assertHeader('Access-Control-Allow-Origin', 'https://portal.generosity.test');
                $this->assertSame($before, $this->staffWriteSnapshot());
            }

            // A trusted origin still cannot grant membership in a different center.
            $this->browserRequest('POST', '/api/v1/staff/centers/center-b/needs', $browser['cookie'], $browser['writes'][0][2], [
                'X-CSRF-TOKEN' => $browser['csrf'], 'Idempotency-Key' => (string) Str::uuid(),
            ])->assertForbidden();
            $this->assertSame($before, $this->staffWriteSnapshot());
            $this->browserRequest('GET', '/api/v1/staff/centers', $browser['cookie'])
                ->assertOk()->assertJsonPath('centers.0.id', 'center-a');
        } finally {
            $this->app->instance('env', 'testing');
        }
    }

    public function test_revoked_browser_cookie_cannot_write_even_with_fresh_guest_csrf(): void
    {
        try {
            $browser = $this->staffBrowser();
            $before = $this->staffWriteSnapshot();
            $this->browserRequest('POST', '/auth/logout', $browser['cookie'], [], ['X-CSRF-TOKEN' => $browser['csrf']])
                ->assertNoContent();
            $this->assertDatabaseMissing('sessions', ['id' => $browser['sessionId']]);
            foreach ($browser['writes'] as [$method, $path, $body]) {
                $this->browserRequest($method, $path, $browser['cookie'], $body, [
                    'X-CSRF-TOKEN' => $browser['csrf'], 'Idempotency-Key' => (string) Str::uuid(),
                ])->assertStatus(419);
                $this->assertSame($before, $this->staffWriteSnapshot());
            }

            // Pass CSRF with the old cookie's new guest session, so authentication must reject the write.
            $guest = $this->browserRequest('GET', '/auth/session', $browser['cookie'])
                ->assertOk()->assertJsonPath('user', null);
            $this->assertNotEmpty($guest->json('csrf_token'));
            $this->assertNotSame($browser['csrf'], $guest->json('csrf_token'));
            foreach ($browser['writes'] as [$method, $path, $body]) {
                $this->browserRequest($method, $path, $browser['cookie'], $body, [
                    'X-CSRF-TOKEN' => $guest->json('csrf_token'), 'Idempotency-Key' => (string) Str::uuid(),
                ])->assertUnauthorized()->assertJsonPath('error.code', 'unauthenticated');
                $this->assertSame($before, $this->staffWriteSnapshot());
            }
        } finally {
            $this->app->instance('env', 'testing');
        }
    }

    private function staffBrowser(): array
    {
        $staff = User::factory()->create();
        DB::table('center_memberships')->insert(['user_id' => $staff->id, 'center_id' => 'center-a']);
        [, $donor] = $this->actor();
        $offer = $this->offer($donor)->assertCreated()->json('id');
        $body = $this->catalog()['centers'][0]['needs'][0];
        unset($body['id']);
        $body['status'] = 'draft';

        config([
            'session.driver' => 'database', 'session.secure' => true, 'session.domain' => null, 'session.lottery' => [0, 100],
            'sanctum.stateful' => ['portal.generosity.test'], 'cors.allowed_origins' => ['https://portal.generosity.test'],
            'cors.supports_credentials' => true,
        ]);
        $this->app->instance('env', 'production');
        $this->assertFalse($this->app->runningUnitTests());
        $session = $this->browserRequest('GET', '/auth/session')->assertOk()->assertJsonPath('user', null);
        $login = $this->browserRequest('POST', '/auth/password', $session->getCookie(config('session.cookie'), false)->getValue(), [
            'email' => $staff->email, 'password' => 'password', 'client' => 'web', 'locale' => 'en',
        ], ['X-CSRF-TOKEN' => $session->json('csrf_token')])->assertRedirect(config('identity.portal_url'));
        $cookie = $login->getCookie(config('session.cookie'), false)->getValue();
        $sessionId = $login->getCookie(config('session.cookie'))->getValue();
        $this->assertDatabaseHas('sessions', ['id' => $sessionId, 'user_id' => $staff->id]);
        $session = $this->browserRequest('GET', '/auth/session', $cookie)->assertOk()->assertJsonPath('user.id', $staff->id);
        $this->browserRequest('GET', '/api/v1/staff/centers', $cookie)->assertOk()->assertJsonCount(1, 'centers');

        return ['cookie' => $cookie, 'csrf' => $session->json('csrf_token'), 'sessionId' => $sessionId, 'writes' => [
            ['POST', '/api/v1/staff/centers/center-a/needs', $body, 201],
            ['PUT', '/api/v1/staff/centers/center-a/needs/need-a', $body + ['version' => Need::findOrFail('need-a')->version], 200],
            ['POST', '/api/v1/contributions/'.$offer.'/transition', ['action' => 'confirm', 'version' => 1], 200],
        ]];
    }

    private function browserRequest(string $method, string $path, ?string $cookie = null, array $body = [], array $headers = []): TestResponse
    {
        // Each request must reload the cookie from storage, never reuse actingAs or an in-memory session.
        $this->app['auth']->forgetGuards();
        $this->app['auth']->shouldUse('web');
        $this->app->forgetInstance('auth.driver');
        $this->app['session']->forgetDrivers();
        $this->app->forgetInstance('session.store');

        return $this->call($method, 'https://api.generosity.test'.$path, [],
            $cookie === null ? [] : [config('session.cookie') => $cookie], [],
            $this->transformHeadersToServerVars($headers + ['Accept' => 'application/json', 'Content-Type' => 'application/json', 'Origin' => 'https://portal.generosity.test']),
            json_encode($body, JSON_THROW_ON_ERROR));
    }

    private function staffWriteSnapshot(): array
    {
        $snapshot = [];
        foreach (['needs' => 'id', 'contributions' => 'id', 'contribution_events' => 'id', 'operations' => 'user_id', 'jobs' => 'id'] as $table => $key) {
            $query = DB::table($table)->orderBy($key);
            if ($table === 'operations') {
                $query->orderBy('key');
            }
            $snapshot[$table] = $query->get()->map(fn ($row) => (array) $row)->all();
        }

        return $snapshot;
    }

    public function test_registration_verification_login_and_revocation(): void
    {
        Notification::fake();
        $body = ['adult_confirmed' => true, 'terms_accepted' => true, 'privacy_acknowledged' => true, 'policy_version' => AccountPolicy::VERSION, 'name' => 'María', 'email' => 'MARIA@example.test', 'password' => 'LongPassword123', 'role' => 'admin'];
        $this->callApi('POST', 'auth/register', null, $body)->assertStatus(202);
        $user = User::where('email', 'maria@example.test')->firstOrFail();
        $this->assertTrue(Hash::check($body['password'], $user->password));
        $this->assertDatabaseCount('center_memberships', 0);
        Notification::assertSentTo($user, VerifyEmail::class);
        $login = ['email' => $body['email'], 'password' => $body['password'], 'device_name' => 'iPhone'];
        $this->callApi('POST', 'auth/login', null, $login)->assertForbidden()->assertJsonPath('error.code', 'email_not_verified');
        $url = URL::temporarySignedRoute('verification.verify', now()->addMinutes(60), ['id' => $user->id, 'hash' => sha1($user->email)]);
        $this->getJson($url.'&tampered=1')->assertForbidden();
        $this->getJson($url)->assertOk();
        $response = $this->callApi('POST', 'auth/login', null, $login)->assertOk();
        $token = $response->json('token');
        $this->assertDatabaseMissing('personal_access_tokens', ['token' => $token]);
        $this->callApi('GET', 'me', $token)->assertOk()->assertJsonMissingPath('user.password');
        $this->callApi('POST', 'auth/logout', $token)->assertNoContent();
        $this->callApi('GET', 'me', $token)->assertUnauthorized();
    }

    public function test_expired_tokens_and_cookie_only_requests_are_rejected(): void
    {
        [$user, $token] = $this->actor();
        $user->tokens()->update(['expires_at' => now()->subMinute()]);
        $this->callApi('GET', 'me', $token)->assertUnauthorized();
        $this->withCookie('laravel_session', 'untrusted')->callApi('POST', 'contributions', null, ['method' => 'goods', 'quantity' => 1])->assertUnauthorized();
        $this->withHeader('Origin', 'https://evil.example')->callApi('GET', 'centers')->assertHeaderMissing('Access-Control-Allow-Origin');
    }

    public function test_verification_expiry_and_reopening_a_valid_link(): void
    {
        $user = User::factory()->unverified()->create();
        $parameters = ['id' => $user->id, 'hash' => sha1($user->email)];
        $expired = URL::temporarySignedRoute('verification.verify', now()->subMinute(), $parameters);
        $this->getJson($expired)->assertForbidden();
        $this->assertFalse($user->fresh()->hasVerifiedEmail());

        $valid = URL::temporarySignedRoute('verification.verify', now()->addHour(), $parameters);
        $this->getJson($valid)->assertOk()->assertJsonPath('status', 'email_verified');
        $verifiedAt = $user->fresh()->email_verified_at;
        $this->getJson($valid)->assertOk();
        $this->assertEquals($verifiedAt, $user->fresh()->email_verified_at);
    }

    public function test_expired_or_wrong_account_reset_token_preserves_password_and_sessions(): void
    {
        [$user, $accessToken] = $this->actor();
        [$other] = $this->actor();
        $originalPassword = $user->password;
        $otherPassword = $other->password;
        $token = Password::createToken($user);
        $data = ['email' => $other->email, 'token' => $token, 'password' => 'AnotherLongPassword123', 'password_confirmation' => 'AnotherLongPassword123'];
        $this->callApi('POST', 'auth/reset-password', null, $data)->assertUnprocessable()->assertJsonPath('error.code', 'invalid_reset_token');
        $this->assertSame($otherPassword, $other->fresh()->password);

        DB::table('password_reset_tokens')->where('email', $user->email)->update(['created_at' => now()->subMinutes(config('auth.passwords.users.expire') + 1)]);
        $data['email'] = $user->email;
        $this->callApi('POST', 'auth/reset-password', null, $data)->assertUnprocessable()->assertJsonPath('error.code', 'invalid_reset_token');
        $this->assertSame($originalPassword, $user->fresh()->password);
        $this->callApi('GET', 'me', $accessToken)->assertOk();
    }

    public function test_recovery_and_resend_do_not_disclose_unknown_accounts(): void
    {
        Notification::fake();
        $user = User::factory()->unverified()->create();
        foreach (['forgot-password' => 'reset_requested', 'verification' => 'verification_requested'] as $action => $status) {
            $this->callApi('POST', 'auth/'.$action, null, ['email' => 'missing@example.test'])->assertStatus(202)->assertExactJson(['status' => $status]);
            Notification::assertNothingSent();
            $this->callApi('POST', 'auth/'.$action, null, ['email' => $user->email])->assertStatus(202)->assertExactJson(['status' => $status]);
            Notification::assertSentTo($user, $action === 'verification' ? VerifyEmail::class : ResetPassword::class);
            Notification::fake();
        }
    }

    public function test_password_reset_revokes_tokens_and_cannot_be_reused(): void
    {
        [$user, $token] = $this->actor();
        $reset = Password::createToken($user);
        $data = ['email' => $user->email, 'token' => $reset, 'password' => 'AnotherLongPassword123', 'password_confirmation' => 'AnotherLongPassword123'];
        $this->callApi('POST', 'auth/reset-password', null, $data)->assertOk();
        $this->callApi('GET', 'me', $token)->assertUnauthorized();
        $this->callApi('POST', 'auth/reset-password', null, $data)->assertStatus(422);
        $this->assertTrue(Hash::check($data['password'], $user->fresh()->password));
    }

    public function test_accented_passwords_are_accepted_and_oversized_utf8_passwords_are_validated(): void
    {
        Notification::fake();
        $password = 'ContraseñaÚnica123';
        $this->callApi('POST', 'auth/register', null, ['adult_confirmed' => true, 'terms_accepted' => true, 'privacy_acknowledged' => true, 'policy_version' => AccountPolicy::VERSION, 'name' => 'José', 'email' => 'jose@example.test', 'password' => $password])->assertStatus(202);
        $this->assertTrue(Hash::check($password, User::where('email', 'jose@example.test')->firstOrFail()->password));
        $this->callApi('POST', 'auth/register', null, ['adult_confirmed' => true, 'terms_accepted' => true, 'privacy_acknowledged' => true, 'policy_version' => AccountPolicy::VERSION, 'name' => 'José', 'email' => 'oversized@example.test', 'password' => str_repeat('ñ', 36).'Aa1'])->assertUnprocessable();
        $this->assertDatabaseMissing('users', ['email' => 'oversized@example.test']);
    }

    public function test_login_rate_limit_and_errors_do_not_expose_secrets(): void
    {
        for ($i = 0; $i < 5; $i++) {
            $this->callApi('POST', 'auth/login', null, ['email' => 'nobody@example.test', 'password' => 'invalid', 'device_name' => 'test'])->assertUnauthorized();
        }
        $this->callApi('POST', 'auth/login', null, ['email' => 'nobody@example.test', 'password' => 'invalid', 'device_name' => 'test'])
            ->assertStatus(429)->assertJsonPath('error.code', 'rate_limited')->assertJsonMissingPath('trace');
    }

    public function test_offer_idempotency_capacity_and_cancellation_release_exactly_once(): void
    {
        [$user, $token] = $this->actor();
        $key = (string) Str::uuid();
        $payload = ['need_id' => 'need-a', 'method' => 'goods', 'quantity' => 2];
        $offer = $this->callApi('POST', 'contributions', $token, $payload, $key)->assertCreated()->json();
        $this->callApi('POST', 'contributions', $token, $payload, $key)->assertCreated()->assertExactJson($offer);
        $this->callApi('POST', 'contributions', $token, array_merge($payload, ['quantity' => 1]), $key)->assertConflict()->assertJsonPath('error.code', 'operation_reused');
        $this->offer($token)->assertConflict()->assertJsonPath('error.code', 'capacity_exceeded');
        Need::whereKey('need-a')->update(['status' => 'closed']);
        $cancelKey = (string) Str::uuid();
        $this->callApi('POST', 'contributions/'.$offer['id'].'/transition', $token, ['action' => 'cancel', 'version' => 1], $cancelKey)->assertOk();
        $this->callApi('POST', 'contributions/'.$offer['id'].'/transition', $token, ['action' => 'cancel', 'version' => 1], $cancelKey)->assertOk();
        $this->assertSame(0, Need::find('need-a')->quantity_committed);
        $this->assertSame('closed', Need::find('need-a')->status);
        $this->assertDatabaseCount('contribution_events', 2);
        $this->callApi('GET', 'me/impact', $token)->assertJsonCount(0, 'groups');
    }

    public function test_scoped_staff_transitions_owner_isolation_and_terminal_state(): void
    {
        [$owner, $token] = $this->actor('center-a');
        [$staff, $staffToken] = $this->actor('center-a');
        [$other, $otherToken] = $this->actor('center-b');
        $id = $this->offer($token)->assertCreated()->json('id');
        $path = 'contributions/'.$id.'/transition';
        $this->callApi('POST', $path, $token, ['action' => 'confirm', 'version' => 1])->assertForbidden();
        $this->callApi('POST', $path, $otherToken, ['action' => 'confirm', 'version' => 1])->assertForbidden();
        $this->callApi('GET', 'contributions/'.$id, $otherToken)->assertForbidden();
        $this->callApi('POST', $path, $otherToken, ['action' => 'cancel', 'version' => 1])->assertForbidden();
        $this->callApi('POST', $path, $staffToken, ['action' => 'confirm', 'version' => 1])->assertOk()->assertJsonPath('version', 2);
        $this->callApi('POST', $path, $staffToken, ['action' => 'fulfill', 'version' => 1])->assertConflict();
        $this->callApi('POST', $path, $staffToken, ['action' => 'fulfill', 'version' => 2])->assertOk();
        $this->callApi('POST', $path, $token, ['action' => 'cancel', 'version' => 3])->assertConflict();
        $this->callApi('GET', 'contributions/'.$id, $token)->assertJsonCount(3, 'events');
        $this->callApi('GET', 'me/impact', $token)->assertJsonPath('groups.0.provenance', 'center_offer')->assertJsonPath('groups.0.status', 'fulfilled');
        $this->assertSame(1, Need::find('need-a')->quantity_committed);
    }

    public function test_revoked_staff_cannot_replay_a_previously_accepted_operation(): void
    {
        [, $ownerToken] = $this->actor();
        [$staff, $token] = $this->actor('center-a');
        $id = $this->offer($ownerToken)->assertCreated()->json('id');
        $key = (string) Str::uuid();
        $this->callApi('POST', 'contributions/'.$id.'/transition', $token, ['action' => 'confirm', 'version' => 1], $key)->assertOk();
        $this->artisan('center:staff', ['center' => 'center-a', 'email' => $staff->email, '--revoke' => true, '--operator' => 'test'])->assertSuccessful();
        $this->callApi('POST', 'contributions/'.$id.'/transition', $token, ['action' => 'confirm', 'version' => 1], $key)->assertForbidden();
        $this->callApi('GET', 'staff/centers/center-a/contributions', $token)->assertForbidden();
        $this->assertSame('imported', Center::find('center-a')->details['verificationStatus']);
    }

    public function test_personal_help_is_separate_and_money_is_disabled(): void
    {
        [, $token] = $this->actor();
        [, $other] = $this->actor();
        $id = $this->callApi('POST', 'contributions', $token, ['method' => 'time', 'minutes' => 90, 'activity' => 'Acompañamiento'])->assertCreated()->json('id');
        $this->callApi('POST', 'contributions/'.$id.'/transition', $other, ['action' => 'complete', 'version' => 1])->assertForbidden();
        $this->callApi('POST', 'contributions/'.$id.'/transition', $token, ['action' => 'complete', 'version' => 1])->assertOk();
        $this->callApi('GET', 'me/impact', $token)->assertJsonPath('groups.0.provenance', 'self_reported')->assertJsonPath('groups.0.minutes', '0')->assertJsonPath('groups.0.planned_minutes', '90');
        $this->callApi('POST', 'contributions', $token, ['need_id' => 'need-a', 'method' => 'money', 'quantity' => 10])->assertUnprocessable();
        $this->callApi('PUT', 'contributions/'.$id, $token, ['need_id' => 'need-a'])->assertStatus(405);
    }

    public function test_visits_are_owned_idempotent_and_never_verify_centers(): void
    {
        [$user, $token] = $this->actor();
        [, $other] = $this->actor();
        $body = ['center_id' => 'center-a', 'occurred_at' => now()->toISOString(), 'explicit_action' => true];
        $key = (string) Str::uuid();
        $this->callApi('POST', 'visits', null, $body)->assertUnauthorized();
        $this->callApi('POST', 'visits', $token, array_replace($body, ['explicit_action' => false]))->assertUnprocessable();
        $first = $this->callApi('POST', 'visits', $token, $body, $key)->assertCreated();
        $this->callApi('POST', 'visits', $token, $body, $key)->assertCreated()->assertJsonPath('id', $first->json('id'));
        $this->callApi('GET', 'sync/account', $token)->assertJsonCount(1, 'visits')->assertJsonPath('visits.0.user_id', $user->id);
        $this->callApi('GET', 'sync/account', $other)->assertJsonCount(0, 'visits');
        $this->assertDatabaseCount('visit_signals', 1);
        $this->assertSame('imported', Center::find('center-a')->details['verificationStatus']);
    }

    public function test_actual_time_is_explicit_and_only_recorded_on_completion(): void
    {
        [, $token] = $this->actor();
        $id = $this->callApi('POST', 'contributions', $token, ['method' => 'time', 'minutes' => 90, 'activity' => 'Tutoría'])->assertCreated()->json('id');
        $this->callApi('POST', 'contributions/'.$id.'/transition', $token, ['action' => 'cancel', 'version' => 1, 'actual_minutes' => 30])->assertUnprocessable();
        $this->callApi('POST', 'contributions/'.$id.'/transition', $token, ['action' => 'complete', 'version' => 1, 'actual_minutes' => 30])->assertOk()->assertJsonPath('actual_minutes', 30);
        $this->callApi('GET', 'me/impact', $token)->assertJsonPath('groups.0.minutes', '30')->assertJsonPath('groups.0.planned_minutes', '90');
    }

    public function test_invalid_and_expired_offers_have_no_side_effects(): void
    {
        [, $token] = $this->actor();
        $this->offer($token, -1)->assertUnprocessable();
        $this->callApi('POST', 'contributions', $token, ['method' => 'goods', 'quantity' => 1, 'user_id' => 42])->assertUnprocessable();
        Need::whereKey('need-a')->update(['expires_on' => now()->subDay()->toDateString()]);
        $this->offer($token)->assertConflict()->assertJsonPath('error.code', 'need_unavailable');
        $this->assertDatabaseCount('contributions', 0);
        $this->assertDatabaseCount('operations', 0);
    }

    public function test_invalid_imports_and_duplicate_ids_leave_the_previous_catalog_untouched(): void
    {
        $cases = [];
        $bad = $this->catalog();
        $bad['centers'][0]['coordinates']['latitude'] = 100;
        $cases[] = $bad;
        $bad = $this->catalog();
        $bad['centers'][1]['needs'] = $bad['centers'][0]['needs'];
        $cases[] = $bad;
        $bad = $this->catalog();
        $bad['centers'][0]['needs'][0]['centerId'] = 'center-b';
        $cases[] = $bad;
        $bad = $this->catalog();
        $bad['centers'][0]['needs'][0]['quantityCommitted'] = 1;
        $cases[] = $bad;
        foreach ($cases as $catalog) {
            try {
                app(CatalogImport::class)->run($catalog, 'test');
                $this->fail('Invalid catalog accepted');
            } catch (ValidationException $e) {
                $this->assertNotEmpty($e->errors());
            }
        }
        $this->assertDatabaseCount('centers', 2);
        $this->assertDatabaseCount('needs', 1);
        $this->assertDatabaseCount('operator_audits', 1);
    }

    public function test_private_error_details_are_redacted_in_responses_and_logs(): void
    {
        Log::spy();
        Route::get('/api/v1/test-failure', fn () => throw new \RuntimeException('secret-canary'));
        $response = $this->callApi('GET', 'test-failure')->assertStatus(500)->assertJsonPath('error.code', 'service_unavailable');
        $this->assertStringNotContainsString('secret-canary', $response->getContent());
        Log::shouldHaveReceived('error')->once()->with('backend_failure', ['type' => 'RuntimeException']);
    }

    public function test_openapi_covers_every_implemented_versioned_route_and_core_response_fields(): void
    {
        $schema = json_decode(file_get_contents(base_path('openapi.json')), true, 512, JSON_THROW_ON_ERROR);
        foreach (Route::getRoutes() as $route) {
            if (! str_starts_with($route->uri(), 'api/v1/')) {
                continue;
            }
            $path = substr($route->uri(), strlen('api/v1'));
            foreach (array_diff($route->methods(), ['HEAD']) as $method) {
                $this->assertArrayHasKey(strtolower($method), $schema['paths'][$path] ?? [], $method.' '.$path);
            }
        }
        $this->assertSame('3.1.0', $schema['openapi']);
        $payload = $this->callApi('GET', 'needs/need-a')->assertOk()->json();
        foreach ($schema['components']['schemas']['Need']['required'] as $property) {
            $this->assertArrayHasKey($property, $payload);
        }
    }

    public function test_need_management_preserves_reservations_and_uses_versions(): void
    {
        [, $ownerToken] = $this->actor();
        [, $staffToken] = $this->actor('center-a');
        $data = $this->catalog()['centers'][0]['needs'][0];
        $data['status'] = 'draft';
        $this->callApi('POST', 'staff/centers/center-a/needs', $ownerToken, $data)->assertForbidden();
        $this->callApi('POST', 'staff/centers/center-a/needs', $staffToken, $data)->assertCreated();
        $this->offer($ownerToken, 2)->assertCreated();
        $data['version'] = 2;
        $data['status'] = 'published';
        $data['quantityRequired'] = 1;
        $this->callApi('PUT', 'staff/centers/center-a/needs/need-a', $staffToken, $data)->assertConflict();
        $data['quantityRequired'] = 3;
        $this->callApi('PUT', 'staff/centers/center-a/needs/need-a', $staffToken, $data)->assertOk()->assertJsonPath('quantityCommitted', 2);
        $this->callApi('PUT', 'staff/centers/center-a/needs/need-a', $staffToken, $data)->assertConflict();
    }

    public function test_import_is_atomic_and_does_not_reset_capacity_or_reassign_needs(): void
    {
        [, $token] = $this->actor();
        $this->offer($token)->assertCreated();
        app(CatalogImport::class)->run($this->catalog(), 'test');
        $this->assertSame(1, Need::find('need-a')->quantity_committed);
        $bad = $this->catalog();
        $bad['centers'][0]['name']['es'] = 'Should roll back';
        $bad['centers'][1]['needs'] = $bad['centers'][0]['needs'];
        $bad['centers'][0]['needs'] = [];
        try {
            app(CatalogImport::class)->run($bad, 'test');
            $this->fail('Reassignment accepted');
        } catch (ApiProblem $e) {
            $this->assertSame('need_center_immutable', $e->errorCode);
        }
        $this->assertSame('Centro de apoyo', Center::find('center-a')->details['name']['es']);
        $this->assertDatabaseCount('operator_audits', 2);
        DB::purge();
        $this->assertSame(1, Contribution::count());
        $this->assertSame(1, Need::find('need-a')->quantity_committed);
    }

    private function race(array $requests): array
    {
        $processes = [];
        $start = microtime(true) + 1;
        foreach ($requests as $request) {
            $process = new Process([PHP_BINARY, 'tests/request-worker.php'], base_path(), ['APP_ENV' => 'testing', 'DB_DATABASE' => 'generosity_test']);
            $process->setInput(json_encode($request + ['start' => $start, 'key' => (string) Str::uuid()], JSON_THROW_ON_ERROR));
            $process->start();
            $processes[] = $process;
        }
        $results = [];
        foreach ($processes as $process) {
            $process->wait();
            $this->assertSame(0, $process->getExitCode(), $process->getErrorOutput().$process->getOutput());
            $results[] = json_decode($process->getOutput(), true, 512, JSON_THROW_ON_ERROR);
        }

        return $results;
    }

    public function test_concurrent_offers_cannot_reserve_the_last_unit_twice(): void
    {
        [, $one] = $this->actor();
        [, $two] = $this->actor();
        Need::whereKey('need-a')->update(['quantity_required' => 1]);
        $request = ['path' => 'contributions', 'body' => ['need_id' => 'need-a', 'method' => 'goods', 'quantity' => 1]];
        $results = $this->race([$request + ['token' => $one], $request + ['token' => $two]]);
        $codes = array_column($results, 'status');
        sort($codes);
        $this->assertSame([201, 409], $codes, json_encode($results));
        $this->assertDatabaseCount('contributions', 1);
        $this->assertSame(1, Need::find('need-a')->quantity_committed);
    }

    public function test_concurrent_cancel_and_fulfillment_commit_one_transition(): void
    {
        [, $owner] = $this->actor();
        [, $staff] = $this->actor('center-a');
        $id = $this->offer($owner)->assertCreated()->json('id');
        $path = 'contributions/'.$id.'/transition';
        $this->callApi('POST', $path, $staff, ['action' => 'confirm', 'version' => 1])->assertOk();
        $results = $this->race([
            ['path' => $path, 'token' => $owner, 'body' => ['action' => 'cancel', 'version' => 2]],
            ['path' => $path, 'token' => $staff, 'body' => ['action' => 'fulfill', 'version' => 2]],
        ]);
        $codes = array_column($results, 'status');
        sort($codes);
        $this->assertSame([200, 409], $codes, json_encode($results));
        $this->assertDatabaseCount('contribution_events', 3);
        $this->assertSame(Contribution::find($id)->status === 'cancelled' ? 0 : 1, Need::find('need-a')->quantity_committed);
    }
}
