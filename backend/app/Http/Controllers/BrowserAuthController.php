<?php

namespace App\Http\Controllers;

use App\Exceptions\ApiProblem;
use App\Models\User;
use App\Services\AccountLogin;
use App\Services\AccountPolicy;
use App\Services\SocialIdentity;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\MessageBag;
use Illuminate\Support\Str;
use Illuminate\Support\ViewErrorBag;
use Illuminate\Validation\ValidationException;

class BrowserAuthController extends Controller
{
    public function __construct(private SocialIdentity $identity, private AccountLogin $accounts) {}

    public function page(Request $request)
    {
        $context = $this->context($request);

        return $this->view($context);
    }

    public function session(Request $request)
    {
        return response()->json(['user' => $request->user(), 'csrf_token' => csrf_token()]);
    }

    public function start(Request $request, string $provider)
    {
        $context = $this->context($request);
        if (! $this->identity->enabled($provider)) {
            return $this->view($context, 'provider_unavailable', 503);
        }
        // Apple posts across sites; its separate Secure binding cookie survives that callback.
        abort_unless($request->isSecure() || app()->runningUnitTests(), 400, 'HTTPS is required for social sign-in.');
        $linkUserId = null;
        if ($request->boolean('link')) {
            if (! $request->user() || $request->session()->get('auth_at', 0) < time() - 300) {
                return $this->view($context, 'reauthentication_required', 403);
            }
            $linkUserId = $request->user()->id;
        }
        if ($linkUserId === null) {
            try {
                AccountPolicy::acceptance($request->all());
                $context['policy'] = $request->only(['adult_confirmed', 'terms_accepted', 'privacy_acknowledged', 'policy_version']);
            } catch (ValidationException $error) {
                return $this->view($context, null, 422, $error->validator->errors(), $request);
            }
        }
        $state = Str::random(64);
        $binding = Str::random(64);
        $nonce = Str::random(64);
        DB::table('auth_attempts')->insert([
            'id' => hash('sha256', $state), 'provider' => $provider,
            'binding_hash' => hash('sha256', $binding), 'nonce' => $nonce,
            'context' => json_encode($context), 'link_user_id' => $linkUserId, 'expires_at' => now()->addMinutes(5),
        ]);

        return redirect()->away($this->identity->authorizationUrl($provider, $state, $nonce))
            ->cookie($this->cookieName($state), $binding, 5, '/auth', null, true, true, false, 'none');
    }

    public function callback(Request $request, string $provider)
    {
        $state = $request->input('state');
        abort_unless(is_string($state) && preg_match('/^[a-zA-Z0-9]{64}$/D', $state), 400);
        $binding = $request->cookie($this->cookieName($state), '');
        // Consume before contacting the provider; retries require a fresh authorization.
        $attempt = DB::transaction(function () use ($state, $binding, $provider) {
            $attempt = DB::table('auth_attempts')->where('id', hash('sha256', $state))->lockForUpdate()->first();
            if (! $attempt || $attempt->provider !== $provider || $attempt->expires_at <= now()
                || ! is_string($binding) || ! hash_equals($attempt->binding_hash, hash('sha256', $binding))) {
                return null;
            }
            DB::table('auth_attempts')->where('id', $attempt->id)->delete();

            return $attempt;
        });
        if (! $attempt) {
            return $this->view(['client' => 'web', 'locale' => 'en'], 'authorization_expired', 400);
        }
        $context = json_decode($attempt->context, true);
        try {
            if ($request->has('error')) {
                throw new ApiProblem('authorization_cancelled', 400);
            }
            $code = $request->input('code');
            if (! is_string($code) || $code === '' || strlen($code) > 4096) {
                throw new ApiProblem('identity_rejected', 401);
            }
            $claims = $this->identity->claims($provider, $code, $attempt->nonce);
            $user = $this->identity->user($provider, $claims, $attempt->link_user_id, $context['policy'] ?? []);
            unset($context['policy']);
            $response = $this->finish($request, $user, $context);
        } catch (\Throwable $error) {
            unset($context['policy']);
            // Provider responses and JWTs must never enter logs or browser error pages.
            $response = $this->view($context, $error instanceof ApiProblem ? $error->errorCode : 'identity_rejected', 400);
        }

        return $response->withoutCookie($this->cookieName($state), '/auth');
    }

    public function password(Request $request)
    {
        $context = $this->context($request);
        try {
            $data = $request->validate(['email' => 'required|email|max:254', 'password' => 'required|string|max:72']);

            return DB::transaction(fn () => $this->finish($request, $this->accounts->password($data['email'], $data['password']), $context));
        } catch (ValidationException $error) {
            return $this->view($context, null, 422, $error->validator->errors(), $request);
        } catch (ApiProblem $error) {
            return $this->view($context, $error->errorCode, $error->status, null, $request);
        }
    }

    public function accountAction(Request $request, string $action)
    {
        $context = $this->context($request);
        app()->setLocale($context['locale']);
        $controller = app(AuthController::class);
        $context['screen'] = $action;
        $mailContext = array_diff_key($context, ['screen' => true]);
        try {
            match ($action) {
                'register' => $controller->register($request, $mailContext),
                'recover' => $controller->forgot($request, $mailContext),
                'verify' => $controller->resend($request, $mailContext),
                default => abort(404),
            };
        } catch (ValidationException $error) {
            return $this->view($context, null, 422, $error->validator->errors(), $request);
        }

        return $this->result($mailContext, 'email_requested', 202);
    }

    public function verify(Request $request, string $id, string $hash)
    {
        // Check the whole signed URL before trusting its native return context.
        if (! $request->hasValidSignature()) {
            return $this->result(['client' => 'web', 'locale' => app()->getLocale()], 'invalid_verification', 403);
        }
        $context = $this->context($request);
        $user = User::find($id);
        if (! $user || ! hash_equals(sha1($user->getEmailForVerification()), $hash)) {
            return $this->result($context, 'invalid_verification', 403);
        }
        app(AuthController::class)->verify($request, $id, $hash);

        return $this->result($context, 'email_verified');
    }

    public function resetPage(Request $request, string $token)
    {
        $context = $this->context($request);
        $email = $request->input('email');
        if (! is_string($email) || ! filter_var($email, FILTER_VALIDATE_EMAIL) || strlen($email) > 254 || strlen($token) > 255) {
            return $this->result($context, 'invalid_reset_token', 422);
        }

        return response()->view('reset-password', compact('context', 'token', 'email'));
    }

    public function reset(Request $request)
    {
        $context = $this->context($request);
        try {
            app(AuthController::class)->reset($request);
        } catch (ValidationException $error) {
            $email = is_string($request->input('email')) ? $request->input('email') : '';
            $token = is_string($request->input('token')) ? $request->input('token') : '';
            $errors = (new ViewErrorBag)->put('default', $error->validator->errors());

            return response()->view('reset-password', compact('context', 'token', 'email', 'errors'), 422);
        } catch (ApiProblem $error) {
            return $this->result($context, $error->errorCode, $error->status);
        }

        return $this->result($context, 'password_reset');
    }

    private function result(array $context, string $outcome, int $status = 200)
    {
        return response()->view('account-result', compact('context', 'outcome'), $status);
    }

    public function exchange(Request $request)
    {
        $data = $request->validate([
            'code' => 'required|string|size:64|regex:/^[a-zA-Z0-9]+$/D',
            'code_verifier' => 'required|string|min:43|max:128|regex:/^[a-zA-Z0-9._~-]+$/D',
            'client' => 'required|in:android,ios',
        ]);

        return DB::transaction(function () use ($data) {
            $handoff = DB::table('auth_handoffs')->where('id', hash('sha256', $data['code']))->lockForUpdate()->first();
            $challenge = rtrim(strtr(base64_encode(hash('sha256', $data['code_verifier'], true)), '+/', '-_'), '=');
            if (! $handoff || $handoff->expires_at <= now() || $handoff->client !== $data['client'] || ! hash_equals($handoff->challenge, $challenge)) {
                throw new ApiProblem('invalid_handoff', 401);
            }
            DB::table('auth_handoffs')->where('id', $handoff->id)->delete();
            $user = User::whereKey($handoff->user_id)->lockForUpdate()->firstOrFail();

            return response()->json($this->accounts->token($user, $data['client']));
        });
    }

    public function logout(Request $request)
    {
        Auth::guard('web')->logout();
        $request->session()->invalidate();
        $request->session()->regenerateToken();

        return response()->noContent();
    }

    private function finish(Request $request, User $user, array $context)
    {
        return DB::transaction(function () use ($request, $user, $context) {
            $user = User::whereKey($user->id)->lockForUpdate()->firstOrFail();
            if ($context['client'] === 'web') {
                Auth::guard('web')->login($user);
                $request->session()->regenerate();
                $request->session()->put('auth_at', time());

                return redirect()->away(config('identity.portal_url'));
            }
            $code = Str::random(64);
            DB::table('auth_handoffs')->insert([
                'id' => hash('sha256', $code), 'user_id' => $user->id, 'client' => $context['client'],
                'challenge' => $context['code_challenge'], 'expires_at' => now()->addSeconds(60),
            ]);

            return redirect()->away(config('identity.clients.'.$context['client']).'?'.http_build_query(['code' => $code, 'state' => $context['state']]));
        });
    }

    private function context(Request $request): array
    {
        $data = $request->validate([
            'client' => 'sometimes|in:web,android,ios', 'locale' => 'sometimes|in:es,en', 'screen' => 'sometimes|in:register,recover,verify',
            'code_challenge' => 'required_if:client,android,ios|string|size:43|regex:/^[a-zA-Z0-9_-]+$/D',
            'state' => 'required_if:client,android,ios|string|min:32|max:128|regex:/^[a-zA-Z0-9_-]+$/D',
        ]);

        $context = $data + ['client' => 'web', 'locale' => app()->getLocale()];
        app()->setLocale($context['locale']);

        return $context;
    }

    private function cookieName(string $state): string
    {
        return 'generosity_auth_'.substr(hash('sha256', $state), 0, 16);
    }

    private function view(array $context, ?string $error = null, int $status = 200, ?MessageBag $validationErrors = null, ?Request $request = null)
    {
        app()->setLocale($context['locale']);
        $providers = ['google' => $this->identity->enabled('google'), 'apple' => $this->identity->enabled('apple')];

        $input = array_filter($request?->only(['name', 'email']) ?? [], 'is_string');
        $errors = (new ViewErrorBag)->put('default', $validationErrors ?? new MessageBag);

        return response()->view('sign-in', compact('context', 'error', 'providers', 'errors', 'input'), $status);
    }
}
