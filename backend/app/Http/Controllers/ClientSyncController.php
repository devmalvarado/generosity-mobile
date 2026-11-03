<?php

namespace App\Http\Controllers;

use App\Exceptions\ApiProblem;
use App\Models\Center;
use App\Models\Contribution;
use App\Models\Need;
use Illuminate\Http\Request;
use Illuminate\Support\Arr;
use Illuminate\Support\Facades\DB;

class ClientSyncController extends Controller
{
    public function catalog(Request $request)
    {
        $request->validate(['category_version' => 'sometimes|integer|in:1,2', 'progress_version' => 'sometimes|integer|in:1']);
        $bloodCategory = $request->integer('category_version', 1) === 2;
        $progress = $request->integer('progress_version') === 1;

        return DB::transaction(function () use ($bloodCategory, $progress) {
            // shortcut: bounded full snapshots; add cursor-based deltas before exceeding these limits.
            if (Center::count() > 2000 || Need::where('status', 'published')->count() > 10000) {
                throw new ApiProblem('snapshot_limit', 503);
            }
            $needs = Need::withProgress()->where('status', 'published')->orderBy('id')->get()->groupBy('center_id');
            $centers = Center::orderBy('id')->get()->map(function ($center) use ($needs, $bloodCategory, $progress) {
                $public = Arr::only($center->details, ['name', 'description', 'categories', 'coordinates', 'address', 'contact', 'verificationStatus', 'importedAtIsoDate']);
                // Older native readers reject the entire snapshot on an unknown category.
                if (! $bloodCategory) {
                    $public['categories'] = array_values(array_unique(array_map(
                        fn ($category) => $category === 'blood_donation' ? 'health' : $category, $public['categories']
                    )));
                }
                $public['id'] = $center->id;
                $public['needs'] = ($needs[$center->id] ?? collect())->map(function ($need) use ($progress) {
                    $row = Arr::only($need->publicData(), ['id', 'centerId', 'title', 'description', 'helpMethods', 'urgent', 'quantityRequired', 'quantityCommitted', 'expiresOnIsoDate', 'unit']);
                    $row['status'] = $need->expires_on && $need->expires_on->lt(today()) ? 'expired'
                        : ($need->quantity_required !== null && $need->quantity_committed >= $need->quantity_required ? 'covered' : 'active');
                    // Older mobile readers reject unknown keys; progress is explicitly negotiated.
                    if ($progress && $need->quantity_required !== null) {
                        $row['quantityReceived'] = (int) $need->quantity_received;
                    }
                    if ($need->quantity_required === null) {
                        unset($row['quantityCommitted']);
                    }

                    return $row;
                })->values()->all();

                return $public;
            })->all();
            $catalog = ['schemaVersion' => 1, 'centers' => $centers];

            return ['catalog' => $catalog, 'revision' => hash('sha256', json_encode($catalog, JSON_THROW_ON_ERROR)), 'updatedAt' => now()->toISOString()];
        });
    }

    public function account(Request $request)
    {
        return DB::transaction(function () use ($request) {
            $owned = Contribution::where('user_id', $request->user()->id);
            $visits = DB::table('visit_signals')->where('user_id', $request->user()->id);
            if ((clone $owned)->count() > 5000 || (clone $visits)->count() > 5000) {
                throw new ApiProblem('snapshot_limit', 503);
            }

            return ['user' => $request->user()->only(['id', 'name', 'email']),
                'contributions' => $owned->orderByDesc('created_at')->orderBy('id')->get(),
                'visits' => $visits->orderBy('received_at')->orderBy('id')->get(),
                'notices' => NotificationController::snapshot($request->user()->id),
                'updatedAt' => now()->toISOString()];
        });
    }

    public function staffCenters(Request $request)
    {
        return ['centers' => Center::whereIn('id', DB::table('center_memberships')->where('user_id', $request->user()->id)->select('center_id'))
            ->orderBy('id')->get()->map(fn ($center) => ['id' => $center->id, 'name' => $center->details['name']])];
    }
}
