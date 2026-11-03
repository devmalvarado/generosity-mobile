<?php

namespace Tests\Feature;

use App\Jobs\PublishUrgentNeed;
use App\Jobs\SendPush;
use App\Models\Center;
use App\Models\Need;
use App\Models\User;
use App\Services\PushMessaging;
use Illuminate\Foundation\Testing\DatabaseMigrations;
use Illuminate\Support\Facades\Crypt;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Str;
use Tests\TestCase;

class PushNotificationTest extends TestCase
{
    use DatabaseMigrations;

    private User $user;
    private string $token;
    private string $installation;
    private string $secret;

    protected function setUp(): void
    {
        parent::setUp();
        Http::preventStrayRequests();
        $this->user = User::factory()->create();
        $this->token = $this->user->createToken('push-test', ['*'], now()->addDay())->plainTextToken;
        $this->installation = (string) Str::uuid();
        $this->secret = (string) Str::uuid();
        Center::create(['id' => 'center-push', 'details' => ['categories' => ['health']]]);
        Need::create(['id' => 'need-push', 'center_id' => 'center-push', 'status' => 'published',
            'details' => ['urgent' => true, 'title' => ['en' => 'School kits', 'es' => 'Kits escolares']], 'quantity_required' => 20]);
    }

    private function api(string $path, array $body = [], ?string $key = null)
    {
        $this->app['auth']->forgetGuards();
        return $this->postJson('/api/v1/'.$path, $body, ['Authorization' => 'Bearer '.$this->token, 'Idempotency-Key' => $key ?? (string) Str::uuid()]);
    }

    private function subscribe(bool $authorized = true): void
    {
        $this->api('me/notification-preferences', ['urgent' => true, 'all' => false, 'categories' => ['health']])->assertOk();
        $this->api('me/push-installations/'.$this->installation, ['token' => str_repeat('test-token-', 5), 'secret' => $this->secret,
            'platform' => 'android', 'locale' => 'es', 'authorized' => $authorized])->assertOk();
    }

    private function publish(): PublishUrgentNeed
    {
        $event = new PublishUrgentNeed('need-push', (string) Str::uuid(), now()->toDateTimeString());
        $event->handle();
        return $event;
    }

    public function test_owned_inbox_survives_denied_push_and_read_retries(): void
    {
        $this->subscribe(false);
        $this->publish()->handle();
        $this->assertDatabaseCount('inbox_notices', 1);
        $this->assertDatabaseCount('push_deliveries', 0);
        $notice = DB::table('inbox_notices')->first();
        $key = (string) Str::uuid();
        $this->api('me/notifications/'.$notice->id.'/read', [], $key)->assertOk();
        $this->api('me/notifications/'.$notice->id.'/read', [], $key)->assertOk();
        $this->assertNotNull(DB::table('inbox_notices')->first()->read_at);
        $other = User::factory()->create();
        $this->token = $other->createToken('other')->plainTextToken;
        $this->api('me/notifications/'.$notice->id.'/read')->assertNotFound();
    }

    public function test_device_binding_requires_secret_and_logout_revokes_delivery(): void
    {
        $this->subscribe();
        $stored = DB::table('push_installations')->first();
        $this->assertNotSame(str_repeat('test-token-', 5), $stored->token);
        $this->assertSame(str_repeat('test-token-', 5), Crypt::decryptString($stored->token));
        $other = User::factory()->create();
        $original = $this->token;
        $this->token = $other->createToken('other')->plainTextToken;
        $this->api('me/push-installations/'.$this->installation, ['token' => str_repeat('test-token-', 5), 'secret' => (string) Str::uuid(),
            'platform' => 'android', 'locale' => 'es', 'authorized' => true])->assertConflict();
        $this->token = $original;
        $this->publish();
        $this->api('auth/logout')->assertSuccessful();
        $this->assertDatabaseCount('push_installations', 0);
        $this->assertDatabaseCount('push_deliveries', 0);
        $this->assertDatabaseCount('inbox_notices', 1);
    }

    public function test_opt_out_closure_and_expiry_suppress_queued_delivery(): void
    {
        $this->subscribe();
        $event = $this->publish();
        $event->handle();
        $this->assertDatabaseCount('push_deliveries', 1);
        $this->api('me/notification-preferences', ['urgent' => false, 'all' => false, 'categories' => ['health']])->assertOk();
        $messaging = $this->mock(PushMessaging::class);
        $messaging->shouldNotReceive('send');
        (new SendPush(DB::table('push_deliveries')->value('id')))->handle($messaging);
        $this->assertDatabaseHas('push_deliveries', ['status' => 'suppressed']);
        $this->subscribe();
        Need::whereKey('need-push')->update(['status' => 'closed']);
        $this->publish();
        $this->assertDatabaseCount('inbox_notices', 1);
        Need::whereKey('need-push')->update(['status' => 'published', 'expires_on' => now()->subDay()->toDateString()]);
        $this->publish();
        $this->assertDatabaseCount('inbox_notices', 1);
    }

    public function test_accepted_delivery_is_idempotent_and_invalid_token_retires_installation(): void
    {
        $this->subscribe();
        $this->publish();
        $messaging = $this->mock(PushMessaging::class);
        $messaging->shouldReceive('send')->once()->andReturn('accepted');
        $job = new SendPush(DB::table('push_deliveries')->value('id'));
        $job->handle($messaging);
        $job->handle($messaging);
        $this->assertDatabaseHas('push_deliveries', ['status' => 'accepted']);
        $this->assertNull(DB::table('inbox_notices')->first()->read_at);
        $this->publish();
        $messaging->shouldReceive('send')->once()->andReturn('invalid_token');
        (new SendPush(DB::table('push_deliveries')->max('id')))->handle($messaging);
        $this->assertDatabaseHas('push_installations', ['id' => $this->installation, 'authorized' => false]);
    }

    public function test_domain_write_and_job_are_atomic_and_quantity_edits_do_not_alert(): void
    {
        DB::table('center_memberships')->insert(['user_id' => $this->user->id, 'center_id' => 'center-push']);
        $body = ['title' => ['es' => 'Útiles'], 'description' => ['es' => 'Material para clases'], 'helpMethods' => ['goods'],
            'quantityRequired' => 10, 'urgent' => true, 'status' => 'published'];
        $key = (string) Str::uuid();
        $response = $this->api('staff/centers/center-push/needs', $body, $key)->assertCreated();
        $this->api('staff/centers/center-push/needs', $body, $key)->assertCreated();
        $this->assertDatabaseCount('jobs', 1);
        $id = $response->json('id');
        $body['quantityRequired'] = 20;
        $this->putJson('/api/v1/staff/centers/center-push/needs/'.$id, $body + ['version' => 1],
            ['Authorization' => 'Bearer '.$this->token, 'Idempotency-Key' => (string) Str::uuid()])->assertOk();
        $this->assertDatabaseCount('jobs', 1);
        DB::beginTransaction();
        PublishUrgentNeed::dispatch($id, (string) Str::uuid(), now()->toDateTimeString());
        $this->assertDatabaseCount('jobs', 2);
        DB::rollBack();
        $this->assertDatabaseCount('jobs', 1);
    }

    public function test_rotation_revocation_and_deactivation_prevent_stale_delivery(): void
    {
        $this->subscribe();
        $this->publish();
        $stale = DB::table('push_deliveries')->value('id');
        $this->api('me/push-installations/'.$this->installation, ['token' => str_repeat('rotated-token-', 5), 'secret' => $this->secret,
            'platform' => 'android', 'locale' => 'en', 'authorized' => true])->assertOk();
        $messaging = $this->mock(PushMessaging::class);
        $messaging->shouldNotReceive('send');
        (new SendPush($stale))->handle($messaging);
        $this->assertDatabaseHas('push_deliveries', ['id' => $stale, 'status' => 'suppressed']);
        $this->publish();
        $revoked = DB::table('push_deliveries')->max('id');
        $this->api('me/push-installations/'.$this->installation, ['token' => str_repeat('rotated-token-', 5), 'secret' => $this->secret,
            'platform' => 'android', 'locale' => 'en', 'authorized' => false])->assertOk();
        (new SendPush($revoked))->handle($messaging);
        $this->assertDatabaseHas('push_deliveries', ['id' => $revoked, 'status' => 'suppressed']);
        $this->user->deactivate('test');
        $this->publish();
        $this->assertDatabaseCount('inbox_notices', 2);
        $this->assertDatabaseCount('push_installations', 0);
    }

    public function test_transient_failure_can_retry_without_duplicate_notice_or_false_acceptance(): void
    {
        $this->subscribe();
        $this->publish();
        $job = new SendPush(DB::table('push_deliveries')->value('id'));
        $messaging = $this->mock(PushMessaging::class);
        $messaging->shouldReceive('send')->once()->andThrow(new \RuntimeException('Transient provider failure'));
        try {
            $job->handle($messaging);
            $this->fail('The worker must retry a transient provider failure');
        } catch (\RuntimeException $error) {
            $this->assertSame('Transient provider failure', $error->getMessage());
        }
        $this->assertDatabaseHas('push_deliveries', ['status' => 'pending', 'accepted_at' => null]);
        $messaging->shouldReceive('send')->once()->andReturn('accepted');
        $job->handle($messaging);
        $this->assertDatabaseCount('inbox_notices', 1);
        $this->assertDatabaseCount('push_deliveries', 1);
        $this->assertDatabaseHas('push_deliveries', ['status' => 'accepted']);
    }

    public function test_category_interests_and_retention_bound_the_owned_inbox(): void
    {
        $this->api('me/notification-preferences', ['urgent' => true, 'all' => false, 'categories' => ['education']])->assertOk();
        $this->publish();
        $this->assertDatabaseCount('inbox_notices', 0);
        $this->api('me/notification-preferences', ['urgent' => true, 'all' => true, 'categories' => []])->assertOk();
        $this->publish();
        $notice = DB::table('inbox_notices')->first();
        $this->assertDatabaseCount('inbox_notices', 1);
        DB::table('inbox_notices')->where('id', $notice->id)->update(['created_at' => now()->subDays(91)]);
        $snapshot = \App\Http\Controllers\NotificationController::snapshot($this->user->id);
        $this->assertCount(0, $snapshot['items']);
        $this->assertSame(0, $snapshot['unread']);
        $this->api('me/notifications/'.$notice->id.'/read')->assertNotFound();
    }
}
