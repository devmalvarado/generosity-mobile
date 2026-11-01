<?php

namespace App\Http\Controllers;

use App\Exceptions\ApiProblem;
use App\Models\User;
use App\Services\AccountLogin;
use Illuminate\Auth\Events\PasswordReset;
use Illuminate\Auth\Events\Verified;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Password;
use Illuminate\Support\Str;
use Illuminate\Validation\Rules\Password as PasswordRule;
use Laravel\Sanctum\PersonalAccessToken;

class AuthController extends Controller
{
    public function register(Request $request)
    {
        $data = $request->validate([
            'name' => 'required|string|max:120', 'email' => 'required|email|max:254',
            'password' => $this->passwordRules(true),
        ]);
        $data['email'] = strtolower($data['email']);
        $data['password'] = Hash::make($data['password']);
        // The same response for an existing account avoids exposing registrations.
        DB::transaction(function () use ($data) {
            $user = User::firstOrCreate(['email' => $data['email']], $data);
            if ($user->wasRecentlyCreated) {
                $user->sendEmailVerificationNotification();
            }
        });

        return response()->json(['status' => 'verification_requested'], 202);
    }

    public function login(Request $request)
    {
        $data = $request->validate(['email' => 'required|email|max:254', 'password' => $this->passwordRules(false), 'device_name' => 'required|string|max:100']);

        return DB::transaction(function () use ($data) {
            $accounts = app(AccountLogin::class);
            $user = $accounts->password($data['email'], $data['password']);

            return response()->json($accounts->token($user, $data['device_name']));
        });
    }

    public function verify(Request $request, string $id, string $hash)
    {
        $user = User::findOrFail($id);
        abort_unless(hash_equals(sha1($user->getEmailForVerification()), $hash), 403);
        if (! $user->hasVerifiedEmail() && $user->markEmailAsVerified()) {
            event(new Verified($user));
        }

        return response()->json(['status' => 'email_verified']);
    }

    public function resend(Request $request)
    {
        $data = $request->validate(['email' => 'required|email|max:254']);
        $user = User::where('email', strtolower($data['email']))->first();
        if ($user && ! $user->hasVerifiedEmail()) {
            $user->sendEmailVerificationNotification();
        }

        return response()->json(['status' => 'verification_requested'], 202);
    }

    public function forgot(Request $request)
    {
        $data = $request->validate(['email' => 'required|email|max:254']);
        Password::sendResetLink(['email' => strtolower($data['email'])]);

        return response()->json(['status' => 'reset_requested'], 202);
    }

    public function reset(Request $request)
    {
        $data = $request->validate([
            'email' => 'required|email|max:254', 'token' => 'required|string|max:255',
            'password' => [...$this->passwordRules(true), 'confirmed'],
        ]);
        $data['email'] = strtolower($data['email']);
        $status = DB::transaction(function () use ($data) {
            // Serialize reset-token consumption, password replacement and token revocation.
            User::where('email', $data['email'])->lockForUpdate()->first();

            return Password::reset($data, function (User $user, string $password) {
                $user->forceFill(['password' => $password, 'remember_token' => Str::random(60)])->save();
                $user->tokens()->delete();
                DB::table('sessions')->where('user_id', $user->id)->delete();
                DB::table('auth_handoffs')->where('user_id', $user->id)->delete();
                DB::table('auth_attempts')->where('link_user_id', $user->id)->delete();
                event(new PasswordReset($user));
            });
        });
        if ($status !== Password::PasswordReset) {
            throw new ApiProblem('invalid_reset_token', 422);
        }

        if ($request->hasSession()) {
            auth('web')->logout();
            $request->session()->invalidate();
            $request->session()->regenerateToken();
        }

        return response()->json(['status' => 'password_reset']);
    }

    public function logout(Request $request)
    {
        DB::transaction(function () use ($request) {
            User::whereKey($request->user()->id)->lockForUpdate()->firstOrFail();
            $token = $request->user()->currentAccessToken();
            if ($token instanceof PersonalAccessToken) {
                $token->delete();
            } elseif ($request->hasSession()) {
                auth('web')->logout();
                $request->session()->invalidate();
                $request->session()->regenerateToken();
            }
        });

        return response()->noContent();
    }

    private function passwordRules(bool $newPassword): array
    {
        return array_filter(['required', 'string', 'max:72', function ($attribute, $value, $fail) {
            // Bcrypt limits bytes, so a character count alone fails for accented passwords.
            if (is_string($value) && strlen($value) > 72) {
                $fail('The password must not exceed 72 UTF-8 bytes.');
            }
        }, $newPassword ? PasswordRule::min(12)->mixedCase()->numbers() : null]);
    }
}
