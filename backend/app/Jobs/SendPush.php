<?php

namespace App\Jobs;

use App\Models\Need;
use App\Models\User;
use App\Services\PushMessaging;
use Illuminate\Contracts\Queue\ShouldQueue;
use Illuminate\Foundation\Queue\Queueable;
use Illuminate\Support\Facades\DB;

class SendPush implements ShouldQueue
{
    use Queueable;

    public int $tries = 5;
    public int $timeout = 20;
    public function backoff(): array { return [60, 300, 900, 3600]; }

    public function __construct(public int $deliveryId)
    {
        $this->onConnection('database')->beforeCommit();
    }

    public function handle(PushMessaging $messaging): void
    {
        DB::transaction(function () use ($messaging) {
            $delivery = DB::table('push_deliveries')->where('id', $this->deliveryId)->lockForUpdate()->first();
            if (! $delivery || $delivery->status !== 'pending') { return; }
            $installation = DB::table('push_installations')->where('id', $delivery->installation_id)->first();
            $notice = DB::table('inbox_notices')->where('id', $delivery->notice_id)->first();
            $need = $notice ? Need::find($notice->need_id) : null;
            $preferences = $notice ? DB::table('notification_preferences')->where('user_id', $notice->user_id)->first() : null;
            $token = $installation ? DB::table('personal_access_tokens')->where('id', $installation->access_token_id)->first() : null;
            $eligible = $installation && $notice && $installation->user_id === $notice->user_id && $installation->authorized
                && hash_equals($installation->token_hash, $delivery->token_hash)
                && $token && (! $token->expires_at || now()->lt($token->expires_at))
                && User::whereKey($notice->user_id)->whereNotNull('email_verified_at')->exists()
                && now()->subDay()->lt($notice->created_at) && PushMessaging::eligible($need)
                && $preferences && PushMessaging::interested($preferences, $need);
            $status = $eligible ? $messaging->send($installation, $notice) : 'suppressed';
            DB::table('push_deliveries')->where('id', $delivery->id)->update([
                'status' => $status, 'accepted_at' => $status === 'accepted' ? now() : null,
            ]);
            if ($status === 'invalid_token') {
                DB::table('push_installations')->where('id', $installation->id)->update(['authorized' => false]);
            }
        });
    }

    public function failed(?\Throwable $exception): void
    {
        DB::table('push_deliveries')->where('id', $this->deliveryId)->where('status', 'pending')->update(['status' => 'failed']);
    }
}
