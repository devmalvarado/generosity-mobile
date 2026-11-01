<?php

namespace App\Notifications;

use App\Models\User;
use Illuminate\Bus\Queueable;
use Illuminate\Contracts\Queue\ShouldQueue;
use Illuminate\Support\Facades\URL;

class VerifyEmail extends \Illuminate\Auth\Notifications\VerifyEmail implements ShouldQueue
{
    use Queueable;

    public function __construct(public array $context = []) {}

    public function shouldSend($notifiable, string $channel): bool
    {
        return User::whereKey($notifiable->id)->exists();
    }

    protected function verificationUrl($notifiable)
    {
        return URL::temporarySignedRoute('verification.browser', now()->addMinutes(config('auth.verification.expire', 60)), [
            'id' => $notifiable->getKey(), 'hash' => sha1($notifiable->getEmailForVerification()),
            ...$this->context, 'locale' => $this->locale ?? 'en',
        ]);
    }
}
