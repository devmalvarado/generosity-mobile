<?php

namespace App\Notifications;

use App\Models\User;
use Illuminate\Bus\Queueable;
use Illuminate\Contracts\Queue\ShouldQueue;

class ResetPassword extends \Illuminate\Auth\Notifications\ResetPassword implements ShouldQueue
{
    use Queueable;

    public function __construct(#[\SensitiveParameter] $token, public array $context = [])
    {
        parent::__construct($token);
    }

    protected function resetUrl($notifiable)
    {
        return route('password.reset', [
            'token' => $this->token, 'email' => $notifiable->getEmailForPasswordReset(),
            ...$this->context, 'locale' => $this->locale ?? 'en',
        ]);
    }

    public function shouldSend($notifiable, string $channel): bool
    {
        return User::whereKey($notifiable->id)->exists();
    }
}
