<?php

namespace Tests\Feature;

use App\Exceptions\ApiProblem;
use App\Models\Center;
use App\Models\Contribution;
use App\Models\Need;
use App\Models\User;
use App\Notifications\VerifyEmail;
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

    public function test_registration_verification_login_and_revocation(): void
    {
        Notification::fake();
        $body = ['name' => 'María', 'email' => 'MARIA@example.test', 'password' => 'LongPassword123', 'role' => 'admin'];
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
        $this->callApi('POST', 'auth/register', null, ['name' => 'José', 'email' => 'jose@example.test', 'password' => $password])->assertStatus(202);
        $this->assertTrue(Hash::check($password, User::where('email', 'jose@example.test')->firstOrFail()->password));
        $this->callApi('POST', 'auth/register', null, ['name' => 'José', 'email' => 'oversized@example.test', 'password' => str_repeat('ñ', 36).'Aa1'])->assertUnprocessable();
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
        $this->callApi('GET', 'me/impact', $token)->assertJsonPath('groups.0.provenance', 'self_reported')->assertJsonPath('groups.0.minutes', '90');
        $this->callApi('POST', 'contributions', $token, ['need_id' => 'need-a', 'method' => 'money', 'quantity' => 10])->assertUnprocessable();
        $this->callApi('PUT', 'contributions/'.$id, $token, ['need_id' => 'need-a'])->assertStatus(405);
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
