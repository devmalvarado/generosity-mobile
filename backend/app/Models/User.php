<?php

namespace App\Models;

use App\Notifications\ResetPassword;
use App\Notifications\VerifyEmail;
use Database\Factories\UserFactory;
use Illuminate\Contracts\Auth\MustVerifyEmail;
use Illuminate\Database\Eloquent\Attributes\Fillable;
use Illuminate\Database\Eloquent\Attributes\Hidden;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;
use Laravel\Sanctum\HasApiTokens;

#[Fillable(['name', 'email', 'password'])]
#[Hidden(['password', 'remember_token', 'deactivated_at', 'policy_version', 'policy_accepted_at', 'adult_confirmed_at'])]
class User extends Authenticatable implements MustVerifyEmail
{
    /** @use HasFactory<UserFactory> */
    use HasApiTokens, HasFactory, Notifiable;

    protected static function booted(): void
    {
        static::addGlobalScope('active', fn (Builder $query) => $query->whereNull('users.deactivated_at'));
    }

    public function deactivate(string $operator): void
    {
        DB::transaction(function () use ($operator) {
            $user = static::withoutGlobalScope('active')->whereKey($this->id)->lockForUpdate()->firstOrFail();
            if ($user->deactivated_at) {
                return;
            }
            // shortcut: retain restricted records; automated erasure is a separate release.
            $user->forceFill(['deactivated_at' => now(), 'password' => Str::random(64), 'remember_token' => null])->save();
            $user->tokens()->delete();
            DB::table('sessions')->where('user_id', $user->id)->delete();
            DB::table('password_reset_tokens')->where('email', $user->email)->delete();
            DB::table('auth_handoffs')->where('user_id', $user->id)->delete();
            DB::table('auth_attempts')->where('link_user_id', $user->id)->delete();
            DB::table('operator_audits')->insert([
                'operator' => $operator, 'action' => 'account_deactivated',
                'details' => json_encode(['user_id' => $user->id], JSON_THROW_ON_ERROR), 'created_at' => now(),
            ]);
        }, 5);
    }

    public function sendEmailVerificationNotification(array $context = []): void
    {
        $this->notify((new VerifyEmail($context))->locale(app()->getLocale()));
    }

    public function sendPasswordResetNotification($token, array $context = []): void
    {
        $this->notify((new ResetPassword($token, $context))->locale(app()->getLocale()));
    }

    /**
     * Get the attributes that should be cast.
     *
     * @return array<string, string>
     */
    protected function casts(): array
    {
        return [
            'email_verified_at' => 'datetime',
            'password' => 'hashed',
            'deactivated_at' => 'datetime',
            'policy_accepted_at' => 'datetime',
            'adult_confirmed_at' => 'datetime',
        ];
    }
}
