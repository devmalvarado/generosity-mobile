<?php

namespace App\Http\Controllers;

use App\Exceptions\ApiProblem;
use App\Models\Center;
use App\Models\Contribution;
use App\Models\Need;
use App\Services\Mutation;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;

class ContributionController extends Controller
{
    public function store(Request $request)
    {
        $data = $request->validate([
            'need_id' => 'nullable|string|max:100', 'method' => 'required|in:goods,time',
            'quantity' => 'required_if:method,goods|prohibited_if:method,time|integer|min:1|max:1000000',
            'minutes' => 'required_if:method,time|prohibited_if:method,goods|integer|min:1|max:525600',
            'activity' => 'required_if:method,time|string|max:500',
            'availability' => 'nullable|string|max:500', 'notes' => 'nullable|string|max:2000',
            'quantity_unit' => 'nullable|string|max:60',
            'center_id' => 'prohibited', 'user_id' => 'prohibited', 'status' => 'prohibited', 'provenance' => 'prohibited',
        ]);

        return Mutation::run($request, $data, fn () => null, function () use ($request, $data) {
            $need = ! empty($data['need_id']) ? Need::whereKey($data['need_id'])->lockForUpdate()->firstOrFail() : null;
            if ($need) {
                if ($need->status !== 'published' || ($need->expires_on && $need->expires_on->lt(today()))) {
                    throw new ApiProblem('need_unavailable');
                }
                if (! in_array($data['method'], $need->details['helpMethods'], true)) {
                    throw new ApiProblem('method_unavailable', 422);
                }
                if ($data['method'] === 'goods') {
                    if ($need->quantity_required !== null && $need->quantity_committed + $data['quantity'] > $need->quantity_required) {
                        throw new ApiProblem('capacity_exceeded', 409, ['available' => $need->quantity_required - $need->quantity_committed]);
                    }
                    $need->quantity_committed += $data['quantity'];
                    $need->version++;
                    $need->save();
                }
            }
            $contribution = Contribution::create(array_merge($data, [
                'id' => (string) Str::uuid(), 'user_id' => $request->user()->id,
                'need_id' => $need?->id, 'center_id' => $need?->center_id,
                'provenance' => $need ? 'center_offer' : 'self_reported', 'status' => 'offered',
                'title' => $need?->details['title'] ?? ['es' => 'Ayuda personal', 'en' => 'Personal help'],
                'quantity_unit' => $need ? ($need->details['unit'] ?? null) : ($data['quantity_unit'] ?? null),
            ]));
            $contribution->refresh();
            $this->event($contribution, $request->user()->id, null);

            return response()->json($contribution, 201);
        });
    }

    public function transition(Request $request, Contribution $contribution)
    {
        $data = $request->validate(['action' => 'required|in:confirm,fulfill,cancel,complete', 'version' => 'required|integer|min:1',
            'actual_minutes' => 'sometimes|integer|min:1|max:525600']);
        $authorize = function () use ($request, $contribution, $data) {
            if (in_array($data['action'], ['confirm', 'fulfill'], true)) {
                abort_unless($contribution->center_id && $contribution->user_id !== $request->user()->id, 403);
                Mutation::staff($request->user()->id, $contribution->center_id);
            } else {
                abort_unless($contribution->user_id === $request->user()->id, 403);
            }
        };

        return Mutation::run($request, $data, $authorize, function () use ($request, $contribution, $data) {
            $current = Contribution::whereKey($contribution->id)->lockForUpdate()->firstOrFail();
            if ($current->version !== (int) $data['version']) {
                throw new ApiProblem('version_conflict', 409, ['current' => $current->toArray()]);
            }
            $next = match ($data['action']) {
                'confirm' => $current->status === 'offered' ? 'confirmed' : null,
                'fulfill' => $current->status === 'confirmed' ? 'fulfilled' : null,
                'cancel' => in_array($current->status, ['offered', 'confirmed'], true) ? 'cancelled' : null,
                'complete' => $current->provenance === 'self_reported' && $current->status === 'offered' ? 'fulfilled' : null,
            };
            if (! $next) {
                throw new ApiProblem('invalid_transition', 409, ['current' => $current->toArray()]);
            }
            if ($next === 'cancelled' && $current->need_id && $current->method === 'goods') {
                $need = Need::whereKey($current->need_id)->lockForUpdate()->firstOrFail();
                $need->quantity_committed -= $current->quantity;
                $need->version++;
                $need->save();
            }
            $before = $current->status;
            if (isset($data['actual_minutes'])) {
                if ($next !== 'fulfilled' || $current->method !== 'time') {
                    throw new ApiProblem('invalid_duration', 422);
                }
                $current->actual_minutes = $data['actual_minutes'];
            }
            $current->status = $next;
            $current->version++;
            $current->save();
            $this->event($current, $request->user()->id, $before);

            return response()->json($current);
        });
    }

    public function show(Request $request, Contribution $contribution)
    {
        if ($contribution->user_id !== $request->user()->id) {
            abort_unless($contribution->center_id, 403);
            Mutation::staff($request->user()->id, $contribution->center_id);
        }

        return ['contribution' => $contribution, 'events' => DB::table('contribution_events')->where('contribution_id', $contribution->id)->orderBy('version')->get()];
    }

    public function history(Request $request)
    {
        $request->validate(['page' => 'sometimes|integer|min:1|max:100000']);

        return Contribution::where('user_id', $request->user()->id)->orderByDesc('created_at')->orderBy('id')->paginate(25);
    }

    public function centerHistory(Request $request, Center $center)
    {
        $request->validate(['page' => 'sometimes|integer|min:1|max:100000']);
        Mutation::staff($request->user()->id, $center->id);

        return Contribution::where('center_id', $center->id)->orderByDesc('created_at')->orderBy('id')->paginate(25);
    }

    public function impact(Request $request)
    {
        // Quantities from unlike goods are not added into a misleading total.
        return ['groups' => Contribution::where('user_id', $request->user()->id)->whereIn('status', ['confirmed', 'fulfilled'])
            ->selectRaw('provenance, status, COUNT(*) as contributions, COALESCE(SUM(actual_minutes), 0) as minutes, COALESCE(SUM(minutes), 0) as planned_minutes')
            ->groupBy('provenance', 'status')->orderBy('provenance')->orderBy('status')->get()];
    }

    private function event(Contribution $contribution, int $actor, ?string $before): void
    {
        DB::table('contribution_events')->insert([
            'contribution_id' => $contribution->id, 'actor_id' => $actor, 'from_status' => $before,
            'to_status' => $contribution->status, 'version' => $contribution->version, 'created_at' => now(),
        ]);
    }
}
