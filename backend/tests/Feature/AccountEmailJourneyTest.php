<?php

namespace Tests\Feature;

use App\Models\User;
use App\Notifications\ResetPassword;
use App\Notifications\VerifyEmail;
use Illuminate\Foundation\Testing\DatabaseMigrations;
use Illuminate\Notifications\ChannelManager;
use Illuminate\Notifications\SendQueuedNotifications;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Mail;
use Illuminate\Support\Facades\Notification;
use Illuminate\Support\Facades\Password;
use Illuminate\Support\Facades\Queue;
use Illuminate\Support\Facades\URL;
use Tests\TestCase;

class AccountEmailJourneyTest extends TestCase
{
    use DatabaseMigrations;

    public function test_serialized_queue_job_sends_spanish_mail_under_an_english_worker(): void
    {
        Queue::fake();
        $context = $this->context('ios');
        $this->post('/auth/account/register', $context + ['adult_confirmed' => true, 'terms_accepted' => true, 'privacy_acknowledged' => true, 'policy_version' => \App\Services\AccountPolicy::VERSION, 
            'name' => 'Lucía', 'email' => 'queue@example.test', 'password' => 'ContraseñaÚnica123',
        ])->assertStatus(202);
        $this->post('/auth/account/recover', $context + ['email' => 'queue@example.test'])->assertStatus(202);
        $jobs = Queue::pushed(SendQueuedNotifications::class);
        $this->assertCount(2, $jobs);
        app()->setLocale('en');
        foreach ($jobs as $job) {
            unserialize(serialize($job))->handle(app(ChannelManager::class));
        }
        $messages = Mail::mailer()->getSymfonyTransport()->messages();
        $this->assertCount(2, $messages);
        foreach ($messages as $sent) {
            $email = $sent->getOriginalMessage();
            $this->assertStringContainsString('¡Hola!', $email->getHtmlBody());
            $this->assertStringNotContainsString('Hello!', $email->getHtmlBody());
            $this->assertStringContainsString('locale=es', $email->getHtmlBody());
        }
        $this->assertSame('Verifica tu correo electrónico', $messages[0]->getOriginalMessage()->getSubject());
        $this->assertSame('Restablece tu contraseña', $messages[1]->getOriginalMessage()->getSubject());
        $this->assertSame('en', app()->getLocale());
    }

    public function test_queued_emails_keep_language_and_native_context_when_rendered_later(): void
    {
        Notification::fake();
        foreach (['es', 'en'] as $locale) {
            $context = $this->context('ios', $locale);
            $this->post('/auth/account/register', $context + ['adult_confirmed' => true, 'terms_accepted' => true, 'privacy_acknowledged' => true, 'policy_version' => \App\Services\AccountPolicy::VERSION, 
                'name' => 'María Peña', 'email' => $locale.'@example.test', 'password' => 'ContraseñaÚnica123',
            ])->assertStatus(202)->assertSee($locale === 'es' ? 'Revisa tu correo' : 'Check your email');
            $user = User::where('email', $locale.'@example.test')->firstOrFail();
            $this->post('/auth/account/recover', $context + ['email' => $user->email])->assertStatus(202);
            foreach ([VerifyEmail::class, ResetPassword::class] as $type) {
                $notification = unserialize(serialize(Notification::sent($user, $type)->first()));
                $this->assertSame($locale, $notification->locale);
                app()->setLocale($notification->locale);
                $mail = $notification->toMail($user);
                $html = (string) $mail->render();
                app()->setLocale('en');
                $this->assertStringContainsString($locale === 'es' ? '¡Hola!' : 'Hello!', $html);
                $this->assertStringNotContainsString($locale === 'es' ? 'Regards,' : 'Saludos,', $html);
                $this->assertStringContainsString('locale='.$locale, $mail->actionUrl);
                $this->assertStringContainsString('code_challenge='.$context['code_challenge'], $mail->actionUrl);
                $this->assertStringContainsString('client=ios', $mail->actionUrl);
                $this->assertStringNotContainsString('Contraseña', $mail->actionUrl);
                if ($type === VerifyEmail::class) {
                    $this->get($mail->actionUrl)->assertOk()->assertSee($locale === 'es' ? 'Correo verificado' : 'Email verified');
                } else {
                    $this->get($mail->actionUrl)->assertOk()->assertSee($locale === 'es' ? 'Nueva contraseña' : 'New password');
                }
            }
        }
    }

    public function test_registration_and_sign_in_validation_is_visible_without_flashing_passwords(): void
    {
        $context = $this->context('android');
        $password = 'lowercaseonly';
        $this->post('/auth/account/register', $context + ['adult_confirmed' => true, 'terms_accepted' => true, 'privacy_acknowledged' => true, 'policy_version' => \App\Services\AccountPolicy::VERSION, 'name' => 'José', 'email' => 'jose@example.test', 'password' => $password])
            ->assertStatus(422)->assertSee('mayúsculas y minúsculas')->assertSee('al menos un número')
            ->assertSee('id="new-name"', false)->assertSee('José')->assertSee($context['state'])
            ->assertSee('role="alert"', false)->assertDontSee($password);
        $this->assertDatabaseCount('users', 0);
        $this->assertFalse(session()->has('_old_input.password'));
        $this->post('/auth/password', $context + ['email' => 'not-an-email', 'password' => 'private-invalid-password'])
            ->assertStatus(422)->assertSee('correo electrónico válido')->assertDontSee('private-invalid-password');
        $this->post('/auth/account/register', $context + ['adult_confirmed' => true, 'terms_accepted' => true, 'privacy_acknowledged' => true, 'policy_version' => \App\Services\AccountPolicy::VERSION, 'name' => ['malformed'], 'email' => ['malformed'], 'password' => []])
            ->assertStatus(422)->assertSee('role="alert"', false);
    }

    public function test_verification_is_localized_idempotent_and_rejects_changed_or_expired_links(): void
    {
        $user = User::factory()->unverified()->create();
        $params = ['id' => $user->id, 'hash' => sha1($user->email), ...$this->context('android')];
        $url = URL::temporarySignedRoute('verification.browser', now()->addHour(), $params);
        $this->get($url.'&changed=1')->assertForbidden()->assertSee('enlace de verificación no es válido')->assertSee('screen=verify');
        $this->assertFalse($user->fresh()->hasVerifiedEmail());
        $this->get(URL::temporarySignedRoute('verification.browser', now()->subMinute(), $params))
            ->assertForbidden()->assertSee('Reenviar correo de verificación');
        $this->get($url)->assertOk()->assertSee('Correo verificado')->assertSee('client=android')
            ->assertHeader('Referrer-Policy', 'no-referrer')->assertHeader('X-Content-Type-Options', 'nosniff');
        $verifiedAt = $user->fresh()->email_verified_at;
        $this->get($url)->assertOk();
        $this->assertEquals($verifiedAt, $user->fresh()->email_verified_at);
        $this->assertGuest();
    }

    public function test_browser_reset_errors_retry_success_and_replay_preserve_security(): void
    {
        $user = User::factory()->create();
        $access = $user->createToken('before-reset')->plainTextToken;
        $token = Password::createToken($user);
        $data = $this->context('ios') + ['email' => $user->email, 'token' => $token,
            'password' => 'NuevaContraseña123', 'password_confirmation' => 'DoesNotMatch123'];
        $this->post('/reset-password', $data)->assertStatus(422)->assertSee('confirmación de la contraseña no coincide')
            ->assertSee('aria-invalid="true"', false)->assertSee('name="client" value="ios"', false)
            ->assertDontSee($data['password'])->assertDontSee($data['password_confirmation']);
        $this->assertFalse(session()->has('_old_input.password'));
        $this->assertDatabaseCount('personal_access_tokens', 1);
        $data['password_confirmation'] = $data['password'];
        $this->post('/reset-password', $data)->assertOk()->assertSee('Contraseña actualizada')->assertSee('client=ios');
        $this->assertTrue(Hash::check($data['password'], $user->fresh()->password));
        $this->assertDatabaseCount('personal_access_tokens', 0);
        $this->withToken($access)->getJson('/api/v1/me')->assertUnauthorized();
        $this->withHeader('Accept', 'text/html')->post('/reset-password', $data)
            ->assertStatus(422)->assertSee('enlace de recuperación no es válido')->assertSee('screen=recover');
    }

    public function test_expired_browser_reset_and_malformed_inputs_do_not_change_password(): void
    {
        $user = User::factory()->create();
        $password = $user->password;
        $token = Password::createToken($user);
        DB::table('password_reset_tokens')->update(['created_at' => now()->subMinutes(61)]);
        $this->post('/reset-password', ['locale' => 'es', 'email' => $user->email, 'token' => $token,
            'password' => 'NewPassword12345', 'password_confirmation' => 'NewPassword12345'])
            ->assertStatus(422)->assertSee('Solicitar un nuevo enlace');
        $this->assertSame($password, $user->fresh()->password);
        $this->get('/reset-password/token?email[]=invalid&locale=es')->assertStatus(422)->assertSee('enlace de recuperación');
        $this->post('/reset-password', ['locale' => 'es', 'email' => [], 'token' => [], 'password' => []])
            ->assertStatus(422)->assertSee('Corrige los siguientes campos');
    }

    public function test_email_follow_up_preserves_pkce_return_for_both_native_clients(): void
    {
        foreach (['ios', 'android'] as $client) {
            $user = User::factory()->unverified()->create();
            $context = $this->context($client);
            $url = URL::temporarySignedRoute('verification.browser', now()->addHour(), [
                'id' => $user->id, 'hash' => sha1($user->email), ...$context,
            ]);
            $this->get($url)->assertOk()->assertSee('state='.$context['state']);
            $response = $this->post('/auth/password', $context + ['email' => $user->email, 'password' => 'password']);
            $response->assertRedirect();
            $this->assertStringStartsWith(config('identity.clients.'.$client), $response->headers->get('Location'));
            parse_str(parse_url($response->headers->get('Location'), PHP_URL_QUERY), $query);
            $this->assertSame($context['state'], $query['state']);
            $this->postJson('/api/v1/auth/exchange', ['code' => $query['code'], 'client' => $client, 'code_verifier' => str_repeat('v', 64)])
                ->assertOk()->assertJsonPath('user.id', $user->id);
        }
    }

    public function test_expired_form_session_and_throttling_have_localized_retry_pages(): void
    {
        $this->app->instance('env', 'production');
        $this->post('https://api.generosity.test/reset-password', ['locale' => 'es'])->assertStatus(419)
            ->assertSee('sesión del formulario venció')->assertHeader('Referrer-Policy', 'no-referrer');
        $this->app->instance('env', 'testing');
        for ($i = 0; $i < 21; $i++) {
            $response = $this->get('/auth/sign-in?locale=es');
        }
        $response->assertStatus(429)->assertSee('Espera unos minutos')->assertHeader('Retry-After');
    }

    private function context(string $client, string $locale = 'es'): array
    {
        return ['client' => $client, 'locale' => $locale, 'state' => str_repeat('s', 43),
            'code_challenge' => rtrim(strtr(base64_encode(hash('sha256', str_repeat('v', 64), true)), '+/', '-_'), '=')];
    }
}
