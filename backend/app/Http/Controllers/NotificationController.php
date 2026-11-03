<?php

namespace App\Http\Controllers;

use App\Services\Mutation;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Crypt;
use Illuminate\Support\Facades\DB;
use Laravel\Sanctum\TransientToken;

class NotificationController extends Controller
{
    public function preferences(Request $request)
    {
        $data = $request->validate(['urgent' => 'required|boolean', 'all' => 'required|boolean',
            'categories' => 'present|array|max:30', 'categories.*' => 'string|max:60|regex:/^[a-z_]+$/']);
        return Mutation::run($request, $data, fn () => null, function () use ($request, $data) {
            DB::table('notification_preferences')->updateOrInsert(['user_id' => $request->user()->id], [
                'urgent' => $data['urgent'], 'all_categories' => $data['all'],
                'categories' => json_encode(array_values(array_unique($data['categories'])), JSON_THROW_ON_ERROR),
                'created_at' => now(), 'updated_at' => now(),
            ]);
            return response()->json(['saved' => true]);
        });
    }

    public function register(Request $request, string $installation)
    {
        $data = $request->validate(['token' => 'required|string|min:20|max:4096', 'secret' => 'required|uuid',
            'platform' => 'required|in:android,ios', 'locale' => 'required|in:en,es', 'authorized' => 'required|boolean']);
        return Mutation::run($request, $data, fn () => null, function () use ($request, $installation, $data) {
            $access = $request->user()->currentAccessToken();
            abort_if($access instanceof TransientToken, 403);
            $existing = DB::table('push_installations')->where('id', $installation)->lockForUpdate()->first();
            $secret = hash('sha256', $data['secret']);
            abort_if($existing && ! hash_equals($existing->secret_hash, $secret), 409);
            $tokenHash = hash('sha256', $data['token']);
            abort_if(DB::table('push_installations')->where('token_hash', $tokenHash)->where('id', '!=', $installation)->exists(), 409);
            DB::table('push_installations')->updateOrInsert(['id' => $installation], [
                'user_id' => $request->user()->id, 'access_token_id' => $access->getKey(), 'secret_hash' => $secret,
                'token' => Crypt::encryptString($data['token']), 'token_hash' => $tokenHash,
                'platform' => $data['platform'], 'locale' => $data['locale'], 'authorized' => $data['authorized'],
                'created_at' => now(), 'updated_at' => now(),
            ]);
            return response()->json(['registered' => true, 'deliveryConfigured' => config('push.enabled') && ($data['platform'] !== 'ios' || config('push.ios_enabled'))]);
        });
    }

    public function read(Request $request, string $notice)
    {
        return Mutation::run($request, [], fn () => null, function () use ($request, $notice) {
            $owned = DB::table('inbox_notices')->where('id', $notice)->where('user_id', $request->user()->id)->where('created_at', '>=', now()->subDays(90));
            abort_unless((clone $owned)->exists(), 404);
            $owned->whereNull('read_at')->update(['read_at' => now()]);
            return response()->json(['read' => true]);
        });
    }

    public static function snapshot(int $userId): array
    {
        $owned = DB::table('inbox_notices')->where('user_id', $userId)->where('created_at', '>=', now()->subDays(90));
        // shortcut: last 100 notices for the pilot; add cursor UI before larger inboxes.
        return ['items' => (clone $owned)->orderByDesc('created_at')->orderBy('id')->limit(100)->get()->map(fn ($row) => [
            'id' => $row->id, 'eventId' => $row->event_id, 'centerId' => $row->center_id, 'needId' => $row->need_id,
            'title' => json_decode($row->title, true, 512, JSON_THROW_ON_ERROR), 'createdAt' => $row->created_at,
            'read' => $row->read_at !== null,
        ]), 'unread' => (clone $owned)->whereNull('read_at')->count(), 'hasMore' => (clone $owned)->count() > 100];
    }
}
