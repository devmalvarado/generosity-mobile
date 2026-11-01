<?php

namespace App\Services;

use App\Exceptions\ApiProblem;
use App\Models\User;
use Illuminate\Support\Facades\Hash;

class AccountLogin
{
    // Call inside a transaction so password resets cannot race token issuance.
    public function password(string $email, string $password): User
    {
        $user = User::where('email', strtolower($email))->lockForUpdate()->first();
        $hash = $user?->password ?? '$2y$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.';
        if (strlen($password) > 72 || ! Hash::check($password, $hash) || ! $user) {
            throw new ApiProblem('invalid_credentials', 401);
        }
        if (! $user->hasVerifiedEmail()) {
            throw new ApiProblem('email_not_verified', 403);
        }
        if (Hash::needsRehash($user->password)) {
            $user->update(['password' => $password]);
        }

        return $user;
    }

    public function token(User $user, string $device): array
    {
        if (! User::whereKey($user->id)->exists()) {
            throw new ApiProblem('invalid_credentials', 401);
        }
        $expires = now()->addDays(7);
        $token = $user->createToken($device, ['*'], $expires);

        return ['token' => $token->plainTextToken, 'token_type' => 'Bearer', 'expires_at' => $expires->toISOString(), 'user' => $user];
    }
}
