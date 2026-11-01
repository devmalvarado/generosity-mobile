<?php

namespace App\Http\Controllers;

use App\Exceptions\ApiProblem;
use App\Models\Center;
use App\Models\Need;
use App\Services\CatalogRules;
use App\Services\Mutation;
use Illuminate\Http\Request;
use Illuminate\Support\Arr;
use Illuminate\Support\Str;

class CatalogController extends Controller
{
    public function centers(Request $request)
    {
        $request->validate(['page' => 'sometimes|integer|min:1|max:100000']);

        return Center::orderBy('id')->paginate(25)->through(fn ($center) => $this->center($center));
    }

    public function center(Center $center): array
    {
        return array_merge($center->details, ['id' => $center->id, 'version' => $center->version, 'updatedAt' => $center->updated_at->toISOString()]);
    }

    public function needs(Request $request)
    {
        $data = $request->validate(['center_id' => 'sometimes|string|max:100', 'page' => 'sometimes|integer|min:1|max:100000']);

        return Need::where('status', 'published')->when(isset($data['center_id']), fn ($query) => $query->where('center_id', $data['center_id']))
            ->orderBy('id')->paginate(25)->through(fn ($need) => $need->publicData());
    }

    public function need(Need $need): array
    {
        abort_unless($need->status === 'published', 404);

        return $need->publicData();
    }

    public function staffNeeds(Request $request, Center $center)
    {
        $request->validate(['page' => 'sometimes|integer|min:1|max:100000']);
        Mutation::staff($request->user()->id, $center->id);

        return Need::where('center_id', $center->id)->orderBy('id')->paginate(25)->through(fn ($need) => $need->publicData());
    }

    public function createNeed(Request $request, Center $center)
    {
        $data = $request->validate(CatalogRules::need() + ['status' => 'required|in:draft,published,closed']);

        return Mutation::run($request, $data, fn () => Mutation::staff($request->user()->id, $center->id), function () use ($center, $data) {
            $need = Need::create(['id' => (string) Str::uuid(), 'center_id' => $center->id] + $this->attributes($data));

            return response()->json($need->refresh()->publicData(), 201);
        });
    }

    public function updateNeed(Request $request, Center $center, Need $need)
    {
        abort_unless($need->center_id === $center->id, 404);
        $data = $request->validate(CatalogRules::need() + ['status' => 'required|in:draft,published,closed', 'version' => 'required|integer|min:1']);

        return Mutation::run($request, $data, fn () => Mutation::staff($request->user()->id, $center->id), function () use ($need, $data) {
            $current = Need::whereKey($need->id)->lockForUpdate()->firstOrFail();
            if ($current->version !== (int) $data['version']) {
                throw new ApiProblem('version_conflict', 409, ['current' => $current->publicData()]);
            }
            if (($data['quantityRequired'] ?? null) !== null && $data['quantityRequired'] < $current->quantity_committed) {
                throw new ApiProblem('capacity_below_commitments');
            }
            if ($current->quantity_committed > 0 && ($data['unit'] ?? null) !== ($current->details['unit'] ?? null)) {
                throw new ApiProblem('reserved_unit_immutable');
            }
            $current->fill($this->attributes($data));
            $current->version++;
            $current->save();

            return response()->json($current->publicData());
        });
    }

    private function attributes(array $data): array
    {
        $data['urgent'] = (bool) $data['urgent'];

        return [
            'details' => Arr::only($data, ['title', 'description', 'helpMethods', 'urgent', 'unit']),
            'quantity_required' => $data['quantityRequired'] ?? null,
            'expires_on' => $data['expiresOnIsoDate'] ?? null,
            'status' => $data['status'],
        ];
    }
}
