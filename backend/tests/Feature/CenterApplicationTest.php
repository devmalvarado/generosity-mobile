<?php

namespace Tests\Feature;

use App\Models\User;
use Illuminate\Foundation\Testing\DatabaseMigrations;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;
use Tests\TestCase;

class CenterApplicationTest extends TestCase
{
    use DatabaseMigrations;

    private function details(): array
    {
        return ['organization_name' => 'Asociación de Apoyo', 'site_name' => 'Sede Niñez',
            'activities' => 'Comedor y capacitación', 'country' => 'México', 'city' => 'Monterrey',
            'contact_name' => 'María Peña', 'contact_role' => 'Coordinadora',
            'representation_confirmed' => true, 'contact_consent' => true];
    }

    private function headers(User $user): array
    {
        $this->app['auth']->forgetGuards();

        return ['Authorization' => 'Bearer '.$user->createToken('test', ['*'], now()->addDay())->plainTextToken,
            'Idempotency-Key' => (string) Str::uuid()];
    }

    public function test_verified_submission_is_private_idempotent_and_grants_no_rights(): void
    {
        $this->assertSame('generosity_test', DB::connection()->getDatabaseName());
        $this->postJson('/api/v1/center-applications', $this->details())->assertUnauthorized();
        $this->postJson('/api/v1/center-applications', $this->details(), $this->headers(User::factory()->unverified()->create()))->assertForbidden();
        $owner = User::factory()->create();
        $headers = $this->headers($owner);
        $first = $this->postJson('/api/v1/center-applications', $this->details(), $headers)->assertCreated()
            ->assertJsonPath('application.status', 'pending')->assertJsonPath('application.site_name', 'Sede Niñez')
            ->assertJsonMissingPath('application.contact_email');
        $legacy = $this->details() + array_fill_keys(['legal_name', 'region', 'postal_code', 'address', 'phone', 'website'], null);
        ksort($legacy);
        $this->assertDatabaseHas('operations', ['key' => $headers['Idempotency-Key'],
            'request_hash' => hash('sha256', 'POST api/v1/center-applications '.json_encode($legacy, JSON_THROW_ON_ERROR))]);
        $this->postJson('/api/v1/center-applications', $this->details() + ['latitude' => null, 'longitude' => null, 'google_maps_url' => null], $headers)
            ->assertCreated()->assertExactJson($first->json());
        $this->postJson('/api/v1/center-applications', $this->details(), $headers)->assertCreated()->assertExactJson($first->json());
        $this->postJson('/api/v1/center-applications', $this->details(), $this->headers($owner))->assertOk()->assertExactJson($first->json());
        $this->postJson('/api/v1/center-applications', array_replace($this->details(), ['site_name' => 'Otra sede']), $headers)->assertConflict();
        $this->assertDatabaseCount('center_applications', 1);
        $this->assertDatabaseHas('center_applications', ['user_id' => $owner->id, 'contact_email' => $owner->email, 'consent_version' => 'site-application-v1']);
        $this->assertDatabaseCount('centers', 0);
        $this->assertDatabaseCount('center_memberships', 0);
        $this->getJson('/api/v1/sync/catalog')->assertJsonCount(0, 'catalog.centers');
    }

    public function test_validation_rejects_missing_consent_privilege_claims_and_unsafe_values(): void
    {
        $owner = User::factory()->create();
        foreach ([['contact_consent' => false], ['representation_confirmed' => false],
            ['status' => 'approved', 'verified' => true, 'user_id' => 1],
            ['website' => 'javascript:alert(1)', 'phone' => '<script>', 'activities' => str_repeat('x', 2001)]] as $invalid) {
            $this->postJson('/api/v1/center-applications', array_replace($this->details(), $invalid), $this->headers($owner))
                ->assertUnprocessable()->assertJsonPath('error.code', 'validation_failed');
        }
        $headers = $this->headers($owner);
        unset($headers['Idempotency-Key']);
        $this->postJson('/api/v1/center-applications', $this->details(), $headers)->assertUnprocessable();
        $this->assertDatabaseCount('center_applications', 0);
    }

    public function test_applicants_only_see_their_own_paginated_receipts(): void
    {
        $owner = User::factory()->create();
        $other = User::factory()->create();
        $first = $this->postJson('/api/v1/center-applications', $this->details(), $this->headers($owner))->assertCreated()->json('application.id');
        $second = $this->postJson('/api/v1/center-applications', $this->details(), $this->headers($other))->assertCreated()->json('application.id');
        $this->assertNotSame($first, $second);
        $this->getJson('/api/v1/me/center-applications', $this->headers($owner))->assertOk()
            ->assertJsonCount(1, 'data')->assertJsonPath('data.0.id', $first)->assertJsonPath('per_page', 10)
            ->assertJsonMissingPath('data.0.details')->assertJsonMissingPath('data.0.contact_email');
        $this->getJson('/api/v1/me/center-applications', $this->headers($other))->assertJsonPath('data.0.id', $second);
        $this->app['auth']->forgetGuards();
        $this->getJson('/api/v1/me/center-applications')->assertUnauthorized();
    }

    public function test_location_is_validated_persisted_privately_and_normalized_for_retries(): void
    {
        $owner = User::factory()->create();
        $details = $this->details() + ['address' => 'Calle Pública 10, Coyoacán',
            'google_maps_url' => 'https://maps.app.goo.gl/example',
            'latitude' => '19.350000', 'longitude' => '-99.160000'];
        $first = $this->postJson('/api/v1/center-applications', $details, $this->headers($owner))->assertCreated();
        $saved = json_decode(DB::table('center_applications')->value('details'), true);
        $this->assertSame(19.35, $saved['latitude']);
        $this->assertSame(-99.16, $saved['longitude']);
        $this->assertSame($details['google_maps_url'], $saved['google_maps_url']);
        $details['latitude'] = 19.35;
        $details['longitude'] = -99.16;
        $this->postJson('/api/v1/center-applications', $details, $this->headers($owner))
            ->assertOk()->assertExactJson($first->json());
        $this->assertDatabaseCount('center_applications', 1);
        $this->getJson('/api/v1/me/center-applications', $this->headers($owner))
            ->assertJsonMissingPath('data.0.latitude')->assertJsonMissingPath('data.0.google_maps_url');
        $this->getJson('/api/v1/sync/catalog')->assertJsonCount(0, 'catalog.centers');
    }

    public function test_invalid_coordinate_pairs_and_non_maps_urls_are_rejected(): void
    {
        foreach ([['latitude' => 19], ['longitude' => -99], ['latitude' => 0, 'longitude' => null],
            ['latitude' => 91, 'longitude' => -99], ['latitude' => 19, 'longitude' => -181],
            ['latitude' => 'NaN', 'longitude' => -99],
            ['google_maps_url' => 'https://google.com.evil.test/maps/place/example'],
            ['google_maps_url' => 'https://evil.test@www.google.com/maps/place/example'],
            ['google_maps_url' => 'https://www.google.com/search?q=example'],
            ['google_maps_url' => 'javascript:alert(1)']] as $invalid) {
            $this->postJson('/api/v1/center-applications', array_replace($this->details(), $invalid), $this->headers(User::factory()->create()))
                ->assertUnprocessable()->assertJsonPath('error.code', 'validation_failed');
        }
        $this->assertDatabaseCount('center_applications', 0);
    }

    public function test_stateful_requests_require_csrf_and_new_submission_rate_is_bounded(): void
    {
        config(['sanctum.stateful' => ['generosity.global']]);
        $this->app->instance('env', 'production');
        $this->postJson('https://api.generosity.test/api/v1/center-applications', $this->details(), ['Origin' => 'https://generosity.global'])->assertStatus(419);
        $this->app->instance('env', 'testing');
        $owner = User::factory()->create();
        for ($i = 0; $i < 5; $i++) {
            $this->postJson('/api/v1/center-applications', array_replace($this->details(), ['site_name' => 'Sede '.$i]), $this->headers($owner))->assertCreated();
        }
        $this->postJson('/api/v1/center-applications', $this->details(), $this->headers($owner))->assertStatus(429);
    }

    public function test_verified_browser_session_can_submit_without_becoming_staff(): void
    {
        $owner = User::factory()->create();
        config(['sanctum.stateful' => ['generosity.global']]);
        $this->actingAs($owner, 'web')->withSession(['_token' => 'test-csrf']);
        $headers = ['Origin' => 'https://generosity.global', 'X-CSRF-TOKEN' => 'test-csrf', 'Idempotency-Key' => (string) Str::uuid()];
        $this->postJson('/api/v1/center-applications', $this->details(), $headers)->assertCreated();
        $this->getJson('/api/v1/me/center-applications', $headers)->assertJsonCount(1, 'data');
        $this->getJson('/api/v1/staff/centers', $headers)->assertJsonCount(0, 'centers');
    }
}
