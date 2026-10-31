<?php

namespace App\Http\Controllers;

use App\Exceptions\ApiProblem;
use App\Models\User;
use Illuminate\Auth\Events\PasswordReset;
use Illuminate\Auth\Events\Verified;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Password;
use Illuminate\Support\Str;
use Illuminate\Validation\Rules\Password as PasswordRule;

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
            $user = User::where('email', strtolower($data['email']))->lockForUpdate()->first();
            // Hash::check also runs for unknown accounts to avoid the fast failure path.
            $hash = $user?->password ?? '$2y$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.';
            if (! Hash::check($data['password'], $hash) || ! $user) {
                throw new ApiProblem('invalid_credentials', 401);
            }
            if (! $user->hasVerifiedEmail()) {
                throw new ApiProblem('email_not_verified', 403);
            }
            if (Hash::needsRehash($user->password)) {
                $user->update(['password' => $data['password']]);
            }
            $expires = now()->addDays(7);
            $token = $user->createToken($data['device_name'], ['*'], $expires);

            return response()->json(['token' => $token->plainTextToken, 'token_type' => 'Bearer', 'expires_at' => $expires->toISOString(), 'user' => $user]);
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
                event(new PasswordReset($user));
            });
        });
        if ($status !== Password::PasswordReset) {
            throw new ApiProblem('invalid_reset_token', 422);
        }

        return response()->json(['status' => 'password_reset']);
    }

    public function logout(Request $request)
    {
        DB::transaction(function () use ($request) {
            User::whereKey($request->user()->id)->lockForUpdate()->firstOrFail();
            $request->user()->currentAccessToken()->delete();
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
