<?php

namespace Tests\Feature;

use App\Models\User;
use App\Notifications\ResetPassword;
use App\Notifications\VerifyEmail;
use App\Services\AccountPolicy;
use App\Services\SocialIdentity;
use Illuminate\Database\Eloquent\ModelNotFoundException;
use Illuminate\Foundation\Testing\DatabaseMigrations;
use Illuminate\Routing\Middleware\ThrottleRequests;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Notification;
use Illuminate\Support\Facades\Password;
use Illuminate\Support\Facades\URL;
use Tests\TestCase;

class AccountPolicyTest extends TestCase
{
    use DatabaseMigrations;

    private function registration(): array
    {
        return ['name' => 'María Peña', 'email' => 'adult@example.test', 'password' => 'StrongPassword123',
            'adult_confirmed' => true, 'terms_accepted' => true, 'privacy_acknowledged' => true,
            'policy_version' => AccountPolicy::VERSION];
    }

    public function test_registration_requires_current_policy_and_explicit_adult_declaration(): void
    {
        $this->withoutMiddleware(ThrottleRequests::class);
        Notification::fake();
        foreach (['adult_confirmed', 'terms_accepted', 'privacy_acknowledged', 'policy_version'] as $field) {
            $data = $this->registration();
            unset($data[$field]);
            $this->postJson('/api/v1/auth/register', $data)->assertUnprocessable();
        }
        $this->postJson('/api/v1/auth/register', array_replace($this->registration(), ['adult_confirmed' => false]))->assertUnprocessable();
        $this->postJson('/api/v1/auth/register', array_replace($this->registration(), ['policy_version' => 'old']))->assertUnprocessable();
        $this->assertDatabaseCount('users', 0);
        Notification::assertNothingSent();
        $this->postJson('/api/v1/auth/register', $this->registration() + ['policy_accepted_at' => '2000-01-01'])->assertStatus(202);
        $user = User::firstOrFail();
        $this->assertSame(AccountPolicy::VERSION, $user->policy_version);
        $this->assertTrue($user->policy_accepted_at->isToday());
        $this->assertTrue($user->adult_confirmed_at->isToday());
    }

    public function test_social_start_requires_policy_before_redirect_or_attempt_creation(): void
    {
        config(['identity.google.client_id' => 'test', 'identity.google.client_secret' => 'test']);
        $this->post('/auth/google/start', ['locale' => 'es'])->assertStatus(422)->assertSee('18');
        $this->assertDatabaseCount('auth_attempts', 0);
        $this->post('/auth/google/start', $this->registration() + ['locale' => 'es'])->assertRedirect();
        $context = json_decode(DB::table('auth_attempts')->value('context'), true);
        $this->assertSame(AccountPolicy::VERSION, $context['policy']['policy_version']);
        $this->assertArrayNotHasKey('password', $context['policy']);
    }

    public function test_operator_deactivation_revokes_access_without_erasing_or_reactivating_records(): void
    {
        $this->withoutMiddleware(ThrottleRequests::class);
        Notification::fake();
        $user = User::factory()->create(['email' => 'adult@example.test', 'password' => 'StrongPassword123']);
        $access = $user->createToken('old-device')->plainTextToken;
        $reset = Password::createToken($user);
        $verify = URL::temporarySignedRoute('verification.verify', now()->addHour(), ['id' => $user->id, 'hash' => sha1($user->email)]);
        DB::table('sessions')->insert(['id' => 'old-browser', 'user_id' => $user->id, 'payload' => '', 'last_activity' => time()]);
        DB::table('auth_handoffs')->insert(['id' => hash('sha256', str_repeat('c', 64)), 'user_id' => $user->id,
            'client' => 'ios', 'challenge' => str_repeat('a', 43), 'expires_at' => now()->addMinute()]);
        DB::table('auth_attempts')->insert(['id' => str_repeat('b', 64), 'provider' => 'google', 'binding_hash' => str_repeat('d', 64),
            'nonce' => str_repeat('n', 64), 'context' => '{}', 'link_user_id' => $user->id, 'expires_at' => now()->addMinute()]);
        $this->artisan('account:deactivate', ['user' => (string) $user->id])->assertFailed();
        $this->assertNotNull(User::find($user->id));
        $this->artisan('account:deactivate', ['user' => (string) $user->id, '--confirmed' => true, '--operator' => 'test-operator'])->assertSuccessful();
        $this->artisan('account:deactivate', ['user' => (string) $user->id, '--confirmed' => true, '--operator' => 'test-operator'])->assertSuccessful();
        $this->assertNull(User::find($user->id));
        $this->assertNotNull(User::withoutGlobalScope('active')->findOrFail($user->id)->deactivated_at);
        foreach (['personal_access_tokens', 'sessions', 'password_reset_tokens', 'auth_handoffs', 'auth_attempts'] as $table) {
            $this->assertDatabaseCount($table, 0);
        }
        $this->assertDatabaseCount('operator_audits', 1);
        $this->withToken($access)->getJson('/api/v1/me')->assertUnauthorized();
        $this->postJson('/api/v1/auth/login', ['email' => $user->email, 'password' => 'StrongPassword123', 'device_name' => 'old'])->assertUnauthorized();
        $this->postJson('/api/v1/auth/reset-password', ['email' => $user->email, 'token' => $reset, 'password' => 'NewPassword12345', 'password_confirmation' => 'NewPassword12345'])->assertUnprocessable();
        $this->getJson($verify)->assertNotFound();
        $this->postJson('/api/v1/auth/register', $this->registration())->assertStatus(202);
        $this->postJson('/api/v1/auth/forgot-password', ['email' => $user->email])->assertStatus(202);
        $this->postJson('/api/v1/auth/verification', ['email' => $user->email])->assertStatus(202);
        Notification::assertNothingSent();
        $this->assertFalse((new VerifyEmail)->shouldSend($user, 'mail'));
        $this->assertFalse((new ResetPassword($reset))->shouldSend($user, 'mail'));
        $this->assertDatabaseCount('users', 1);
        $this->assertNull(User::first());
    }

    public function test_delayed_social_subject_cannot_reopen_closed_account(): void
    {
        $user = User::factory()->create();
        DB::table('social_identities')->insert(['user_id' => $user->id, 'provider' => 'google', 'subject' => 'old-subject', 'created_at' => now(), 'updated_at' => now()]);
        $user->deactivate('test');
        $this->expectException(ModelNotFoundException::class);
        app(SocialIdentity::class)->user('google', (object) ['sub' => 'old-subject'], null, $this->registration());
    }

    public function test_stale_browser_session_and_new_social_subject_cannot_restore_access(): void
    {
        $user = User::factory()->create(['password' => 'StrongPassword123']);
        $this->post('/auth/password', ['email' => $user->email, 'password' => 'StrongPassword123'])->assertRedirect();
        $this->getJson('/auth/session')->assertJsonPath('user.id', $user->id);
        $user->deactivate('test');
        app('auth')->forgetGuards();
        $this->getJson('/auth/session')->assertJsonPath('user', null);
        $this->expectException(\App\Exceptions\ApiProblem::class);
        app(SocialIdentity::class)->user('google', (object) [
            'sub' => 'different-subject', 'email' => $user->email, 'email_verified' => true,
        ], null, $this->registration());
    }

    public function test_legal_pages_are_localized_and_available_without_an_account(): void
    {
        foreach (['terms', 'privacy'] as $document) {
            foreach (['es', 'en'] as $locale) {
                $this->get('/legal/'.$document.'?locale='.$locale)->assertOk()
                    ->assertSee('lang="'.$locale.'"', false)->assertSee('IDD190329L69')
                    ->assertSee('notificaciones@generosity.global')->assertSee(AccountPolicy::VERSION)
                    ->assertDontSee('PENDIENTE')->assertHeader('X-Content-Type-Options', 'nosniff');
            }
        }
        $this->get('/legal/unknown')->assertNotFound();
        $this->get('/legal/privacy?locale=fr')->assertNotFound();
    }
}
