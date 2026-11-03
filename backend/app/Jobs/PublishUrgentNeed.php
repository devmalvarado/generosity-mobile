<?php

namespace App\Jobs;

use App\Models\Need;
use App\Models\User;
use App\Services\PushMessaging;
use Illuminate\Contracts\Queue\ShouldQueue;
use Illuminate\Foundation\Queue\Queueable;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;

class PublishUrgentNeed implements ShouldQueue
{
    use Queueable;

    public int $tries = 5;
    public function backoff(): array { return [60, 300, 900, 3600]; }

    public function __construct(public string $needId, public string $eventId, public string $occurredAt)
    {
        // The database job is the transactional outbox, on the same DB as the need.
        $this->onConnection('database')->beforeCommit();
    }

    public function handle(): void
    {
        $need = Need::find($this->needId);
        if (! PushMessaging::eligible($need)) { return; }
        DB::table('notification_preferences')->where('urgent', true)->where('updated_at', '<=', $this->occurredAt)
            ->orderBy('user_id')->chunkById(100, function ($rows) use ($need) {
                foreach ($rows as $preference) {
                    if (! User::whereKey($preference->user_id)->whereNotNull('email_verified_at')->exists()
                        || ! PushMessaging::interested($preference, $need)) { continue; }
                    DB::transaction(function () use ($preference, $need) {
                        $id = (string) Str::uuid();
                        $inserted = DB::table('inbox_notices')->insertOrIgnore([
                            'id' => $id, 'event_id' => $this->eventId, 'user_id' => $preference->user_id,
                            'need_id' => $need->id, 'center_id' => $need->center_id,
                            'title' => json_encode($need->details['title'], JSON_THROW_ON_ERROR), 'created_at' => $this->occurredAt,
                        ]);
                        if (! $inserted) { return; }
                        foreach (DB::table('push_installations')->where('user_id', $preference->user_id)->where('authorized', true)->get() as $installation) {
                            $delivery = DB::table('push_deliveries')->insertGetId([
                                'notice_id' => $id, 'installation_id' => $installation->id,
                                'token_hash' => $installation->token_hash, 'created_at' => now(),
                            ]);
                            SendPush::dispatch($delivery);
                        }
                    });
                }
            }, 'user_id');
    }
}
