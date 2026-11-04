<?php

namespace Tests\Feature;

use App\Models\Center;
use App\Models\Need;
use App\Models\User;
use App\Services\DeploymentEnvironment;
use App\Services\FeatureFlags;
use Illuminate\Foundation\Testing\DatabaseMigrations;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;
use Tests\TestCase;

class FeatureFlagsTest extends TestCase
{
    use DatabaseMigrations;

    public function test_native_preview_flags_default_off_and_are_independently_scoped(): void
    {
        $flags = $this->getJson('/api/v1/features')->assertOk()->json('features');
        foreach (FeatureFlags::PREVIEW_NAMES as $key) {
            $this->assertFalse($flags[$key]);
        }
        $user = User::factory()->create();
        $this->mode('preview.community', 'testers', [$user->id]);
        $this->getJson('/api/v1/features')->assertJsonPath('features', fn ($flags) => $flags['preview.community'] === false);
        $this->actingAs($user, 'sanctum')->getJson('/api/v1/me/features')->assertJsonPath('features', fn ($flags) => $flags['preview.community'] === true && $flags['preview.payments'] === false);
        $this->mode('preview.community', 'off');
        $this->getJson('/api/v1/me/features')->assertJsonPath('features', fn ($flags) => $flags['preview.community'] === false);
    }

    private function mode(string $name, string $mode, array $testers = []): void
    {
        DB::table('feature_flags')->where('name', $name)->update(['mode' => $mode, 'tester_ids' => json_encode($testers)]);
    }

    public function test_guest_and_account_results_are_scoped_and_do_not_expose_testers(): void
    {
        $user = User::factory()->create();
        $this->mode('needs.progress', 'testers', [$user->id]);
        $guest = $this->getJson('/api/v1/features')->assertOk()->assertJsonPath('features', fn ($flags) => $flags['needs.progress'] === false);
        $this->assertStringNotContainsString('tester_ids', $guest->getContent());
        $this->actingAs($user, 'sanctum')->getJson('/api/v1/me/features')->assertOk()->assertJsonPath('features', fn ($flags) => $flags['needs.progress'] === true);
        $this->actingAs(User::factory()->create(), 'sanctum')->getJson('/api/v1/me/features')->assertJsonPath('features', fn ($flags) => $flags['needs.progress'] === false);
        $this->getJson('/api/v1/features')->assertJsonPath('features', fn ($flags) => $flags['needs.progress'] === false);
        $this->mode('needs.progress', 'off', [$user->id]);
        $this->actingAs($user, 'sanctum')->getJson('/api/v1/me/features')->assertJsonPath('features', fn ($flags) => $flags['needs.progress'] === false);
    }

    public function test_switch_blocks_new_offers_but_allows_acknowledged_replay_and_cancellation(): void
    {
        $user = User::factory()->create();
        $token = $user->createToken('test')->plainTextToken;
        $headers = ['Authorization' => 'Bearer '.$token, 'Idempotency-Key' => (string) Str::uuid()];
        $body = ['method' => 'goods', 'quantity' => 1, 'quantity_unit' => 'box'];
        $offer = $this->postJson('/api/v1/contributions', $body, $headers)->assertCreated()->json();
        $this->mode('contributions.create', 'off');
        $this->postJson('/api/v1/contributions', $body, $headers)->assertCreated()->assertJsonPath('id', $offer['id']);
        $this->postJson('/api/v1/contributions', $body, array_replace($headers, ['Idempotency-Key' => (string) Str::uuid()]))->assertConflict()->assertJsonPath('error.code', 'feature_disabled');
        $this->postJson('/api/v1/contributions/'.$offer['id'].'/transition', ['action' => 'cancel', 'version' => 1], array_replace($headers, ['Idempotency-Key' => (string) Str::uuid()]))->assertOk();
        $this->getJson('/api/v1/me/contributions', $headers)->assertOk()->assertJsonPath('data.0.status', 'cancelled');
        $this->mode('contributions.create', 'testers', [$user->id]);
        $this->postJson('/api/v1/contributions', $body, array_replace($headers, ['Idempotency-Key' => (string) Str::uuid()]))->assertCreated();
    }

    public function test_paused_creation_still_allows_center_confirmation_and_receipt(): void
    {
        Center::create(['id' => 'c', 'details' => []]);
        $staff = User::factory()->create();
        $donor = User::factory()->create();
        DB::table('center_memberships')->insert(['center_id' => 'c', 'user_id' => $staff->id]);
        $staffHeader = ['Authorization' => 'Bearer '.$staff->createToken('staff')->plainTextToken];
        $need = $this->postJson('/api/v1/staff/centers/c/needs', [
            'title' => ['es' => 'Cajas'], 'description' => ['es' => 'Apoyo'], 'helpMethods' => ['goods'],
            'urgent' => false, 'status' => 'published', 'quantityRequired' => 10, 'unit' => 'cajas',
        ], $staffHeader + ['Idempotency-Key' => (string) Str::uuid()])->assertCreated()->json();
        auth()->forgetGuards();
        $offer = $this->postJson('/api/v1/contributions', ['need_id' => $need['id'], 'method' => 'goods', 'quantity' => 1], [
            'Authorization' => 'Bearer '.$donor->createToken('donor')->plainTextToken,
            'Idempotency-Key' => (string) Str::uuid(),
        ])->assertCreated()->json();
        $this->mode('contributions.create', 'off');
        $this->mode('needs.publish', 'off');
        $this->mode('needs.progress', 'off');
        foreach (['confirm', 'fulfill'] as $index => $action) {
            auth()->forgetGuards();
            $this->postJson('/api/v1/contributions/'.$offer['id'].'/transition', ['action' => $action, 'version' => $index + 1],
                $staffHeader + ['Idempotency-Key' => (string) Str::uuid()])->assertOk();
        }
        $this->assertDatabaseHas('contributions', ['id' => $offer['id'], 'status' => 'fulfilled']);
        $this->getJson('/api/v1/needs/'.$need['id'])->assertOk()->assertJsonPath('quantityReceived', 1);
    }

    public function test_publication_pause_allows_drafts_and_resolution_of_existing_needs(): void
    {
        Center::create(['id' => 'c', 'details' => []]);
        $user = User::factory()->create();
        DB::table('center_memberships')->insert(['center_id' => 'c', 'user_id' => $user->id]);
        $headers = ['Authorization' => 'Bearer '.$user->createToken('staff')->plainTextToken];
        $body = ['title' => ['es' => 'Cajas'], 'description' => ['es' => 'Apoyo'], 'helpMethods' => ['goods'], 'urgent' => false, 'status' => 'draft', 'quantityRequired' => 10, 'unit' => 'cajas'];
        $this->mode('needs.publish', 'off');
        $draft = $this->postJson('/api/v1/staff/centers/c/needs', $body, $headers + ['Idempotency-Key' => (string) Str::uuid()])->assertCreated()->json();
        $this->putJson('/api/v1/staff/centers/c/needs/'.$draft['id'], array_replace($body, ['status' => 'published', 'version' => 1]), $headers + ['Idempotency-Key' => (string) Str::uuid()])->assertConflict()->assertJsonPath('error.code', 'feature_disabled');
        Need::whereKey($draft['id'])->update(['status' => 'published']);
        $this->putJson('/api/v1/staff/centers/c/needs/'.$draft['id'], $body + ['version' => 1], $headers + ['Idempotency-Key' => (string) Str::uuid()])->assertOk();
    }

    public function test_only_configured_verified_admin_can_change_flags_with_version_and_audit(): void
    {
        $user = User::factory()->create();
        $this->actingAs($user)->get('/admin/features')->assertForbidden();
        config(['generosity.feature_admin_ids' => [(string) $user->id]]);
        $this->get('/admin/features?locale=es')->assertOk()->assertSee('Control de funciones')->assertHeader('Cache-Control', 'no-store, private');
        $body = ['mode' => 'testers', 'tester_ids' => (string) $user->id, 'version' => 1, 'reason' => 'Pilot'];
        $this->post('/admin/features/contributions.create', $body)->assertRedirect();
        $this->assertDatabaseHas('feature_flags', ['name' => 'contributions.create', 'mode' => 'testers', 'version' => 2]);
        $this->assertDatabaseHas('operator_audits', ['operator' => 'user:'.$user->id, 'action' => 'feature.changed']);
        $this->post('/admin/features/contributions.create', $body)->assertConflict();
        $this->post('/admin/features/payments', $body)->assertNotFound();
        $this->postJson('/admin/features/needs.progress', array_replace($body, ['tester_ids' => '999999999']))->assertUnprocessable();
        $this->assertSame(1, DB::table('operator_audits')->count());
    }

    public function test_admin_requires_verification_and_csrf_even_for_allowlisted_accounts(): void
    {
        $user = User::factory()->unverified()->create();
        config(['generosity.feature_admin_ids' => [(string) $user->id]]);
        $this->actingAs($user)->get('/admin/features')->assertForbidden();
        $user->forceFill(['email_verified_at' => now()])->save();
        $this->app->instance('env', 'production');
        $this->post('https://api.generosity.test/admin/features/needs.progress', ['mode' => 'off', 'version' => 1, 'tester_ids' => '', 'reason' => 'Pause'])->assertStatus(419);
        $this->assertDatabaseHas('feature_flags', ['name' => 'needs.progress', 'mode' => 'all', 'version' => 1]);
        $this->app->instance('env', 'testing');
    }

    public function test_preview_requires_isolated_database_hosts_mail_and_push(): void
    {
        $dbKey = 'database.connections.'.config('database.default').'.database';
        $safe = ['generosity.channel' => 'preview', $dbKey => 'generosity_preview',
            'app.url' => 'https://api-preview.generosity.test', 'identity.portal_url' => 'https://preview.generosity.test',
            'session.domain' => null, 'push.enabled' => false, 'mail.default' => 'log'];
        $original = collect($safe)->mapWithKeys(fn ($value, $key) => [$key => config($key)])->all();
        try {
            foreach ([[$dbKey, 'generosity'], ['app.url', 'https://api.generosity.global'],
                ['identity.portal_url', 'https://generosity.global'], ['app.url', 'https://API.GENEROSITY.GLOBAL.'], ['session.domain', '.generosity.global'],
                ['mail.default', 'sendmail'], ['app.url', 'http://api-preview.generosity.test'],
                ['push.enabled', true]] as [$key, $value]) {
                config($safe + ['push.project_id' => 'generosity-v01']);
                DeploymentEnvironment::validate();
                config([$key => $value]);
                try {
                    DeploymentEnvironment::validate();
                    $this->fail('Unsafe preview setting accepted: '.$key);
                } catch (\LogicException) {
                    $this->addToAssertionCount(1);
                }
            }
        } finally {
            config($original);
        }
    }

    public function test_channel_mismatch_is_rejected_and_preview_configuration_is_fail_closed(): void
    {
        $this->getJson('/api/v1/features', ['X-Generosity-Channel' => 'preview'])->assertConflict()->assertJsonPath('error.code', 'environment_mismatch');
        $this->getJson('/auth/session', ['X-Generosity-Channel' => 'preview'])->assertConflict();
        config(['generosity.channel' => 'preview']);
        $this->expectException(\LogicException::class);
        DeploymentEnvironment::validate();
    }
}
