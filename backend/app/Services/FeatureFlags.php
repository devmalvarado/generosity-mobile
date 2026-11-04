<?php

namespace App\Services;

use App\Exceptions\ApiProblem;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class FeatureFlags
{
    public const PREVIEW_NAMES = ['preview.discovery', 'preview.contributions', 'preview.impact', 'preview.community', 'preview.assistance', 'preview.organizations', 'preview.communication', 'preview.operations', 'preview.account', 'preview.payments'];

    public const PARTICIPATION_NAMES = [
        'events.publish', 'events.enroll', 'events.validate', 'campaigns.publish',
        'meals.plan', 'meals.commit', 'assistance.record', 'outcomes.publish', 'follows.update',
    ];

    public const NAMES = ['contributions.create', 'needs.publish', 'needs.progress', ...self::PREVIEW_NAMES, ...self::PARTICIPATION_NAMES];

    public static function allowed(?object $flag, ?int $userId): bool
    {
        return $flag && ($flag->mode === 'all' || ($flag->mode === 'testers' && $userId !== null
            && in_array($userId, json_decode($flag->tester_ids, true, 512, JSON_THROW_ON_ERROR), true)));
    }

    public static function snapshot(?int $userId): array
    {
        $rows = DB::table('feature_flags')->whereIn('name', self::NAMES)->get()->keyBy('name');
        $flags = [];
        foreach (self::NAMES as $name) {
            $flags[$name] = self::allowed($rows->get($name), $userId);
        }

        return ['channel' => config('generosity.channel'), 'features' => $flags];
    }

    public static function require(string $name, int $userId): void
    {
        // Read inside the business transaction so an off switch serializes with new writes.
        $flag = DB::table('feature_flags')->where('name', $name)->sharedLock()->first();
        if (! self::allowed($flag, $userId)) {
            throw new ApiProblem('feature_disabled', 409, ['feature' => $name]);
        }
    }

    public static function authorizeAdmin(Request $request): void
    {
        $user = $request->user();
        abort_unless($user && $user->hasVerifiedEmail() && User::whereKey($user->id)->exists()
            && in_array((string) $user->id, config('generosity.feature_admin_ids', []), true), 403);
    }

    public static function change(Request $request, string $name, array $data): void
    {
        DB::transaction(function () use ($request, $name, $data) {
            User::whereKey($request->user()->id)->lockForUpdate()->firstOrFail();
            self::authorizeAdmin($request);
            abort_unless(in_array($name, self::NAMES, true), 404);
            $row = DB::table('feature_flags')->where('name', $name)->lockForUpdate()->first() ?? abort(404);
            if ($row->version !== (int) $data['version']) {
                throw new ApiProblem('version_conflict', 409);
            }
            $ids = $data['mode'] === 'testers' ? array_values(array_unique(array_map('intval', $data['tester_ids'] ?? []))) : [];
            if (User::whereIn('id', $ids)->whereNotNull('email_verified_at')->count() !== count($ids)) {
                throw new ApiProblem('tester_unavailable', 422);
            }
            $next = ['mode' => $data['mode'], 'tester_ids' => json_encode($ids, JSON_THROW_ON_ERROR), 'version' => $row->version + 1];
            DB::table('feature_flags')->where('name', $name)->update($next + ['updated_at' => now()]);
            DB::table('operator_audits')->insert([
                'operator' => 'user:'.$request->user()->id, 'action' => 'feature.changed', 'created_at' => now(),
                'details' => json_encode(['feature' => $name, 'reason' => $data['reason'],
                    'before' => ['mode' => $row->mode, 'tester_ids' => json_decode($row->tester_ids), 'version' => $row->version],
                    'after' => ['mode' => $data['mode'], 'tester_ids' => $ids, 'version' => $next['version']]], JSON_THROW_ON_ERROR),
            ]);
        }, 5);
    }
}
