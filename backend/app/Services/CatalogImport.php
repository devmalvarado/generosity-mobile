<?php

namespace App\Services;

use App\Exceptions\ApiProblem;
use App\Models\Center;
use App\Models\Need;
use Illuminate\Support\Arr;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Validator;
use Illuminate\Validation\ValidationException;

class CatalogImport
{
    public function run(array $catalog, string $operator): void
    {
        Validator::make($catalog, [
            'schemaVersion' => 'required|integer|in:1', 'centers' => 'required|array|max:10000',
            'centers.*' => 'array:id,name,description,address,categories,coordinates,contact,googleMapsUrl,verificationStatus,importedAtIsoDate,needs',
            'centers.*.id' => 'required|string|max:100|regex:/^[a-z0-9][a-z0-9-]*$/|distinct:strict',
        ])->validate();
        $needIds = [];
        $centers = [];
        foreach ($catalog['centers'] as $center) {
            $validated = Validator::make($center, [
                'id' => 'required|string|max:100', 'name' => CatalogRules::localized(),
                'description' => CatalogRules::localized(), 'address' => CatalogRules::localized(),
                'categories' => 'required|array|min:1|max:30', 'categories.*' => 'required|in:children_support,older_adults,disability_support,blood_donation,health,addiction_recovery,community_kitchen,education,vulnerable_families,organ_tissue_donation|distinct',
                'coordinates' => 'required|array:latitude,longitude',
                'coordinates.latitude' => 'required|numeric|between:-90,90', 'coordinates.longitude' => 'required|numeric|between:-180,180',
                'contact' => 'required|array:phone,email,website', 'contact.phone' => 'nullable|string|max:100',
                'contact.email' => 'nullable|email|max:254', 'contact.website' => 'nullable|url:http,https|max:2048',
                'googleMapsUrl' => CatalogRules::googleMapsUrl(),
                'verificationStatus' => 'required|in:imported,visit_signaled,officially_verified',
                'importedAtIsoDate' => 'required|date_format:Y-m-d', 'needs' => 'present|array|max:1000',
                'needs.*' => 'array:id,centerId,title,description,helpMethods,urgent,expiresOnIsoDate,quantityRequired,quantityCommitted,unit,status,category,priority,publishedOnIsoDate',
            ])->validate();
            // Initial directory-only listings may lack contact; they cannot claim verification or solicit help.
            if (! array_filter($validated['contact']) && ! ($validated['verificationStatus'] === 'imported'
                && ! empty($validated['googleMapsUrl']) && $center['needs'] === [])) {
                throw ValidationException::withMessages(['contact' => 'At least one public contact is required.']);
            }
            $validated['coordinates'] = array_map('floatval', $validated['coordinates']);
            $validated['needs'] = [];
            foreach ($center['needs'] as $need) {
                $item = Validator::make($need, CatalogRules::need() + [
                    'id' => 'required|string|max:100|regex:/^[a-z0-9][a-z0-9-]*$/',
                    'quantityCommitted' => 'sometimes|integer|in:0',
                    'centerId' => 'sometimes|in:'.$center['id'],
                    'status' => 'sometimes|in:active,covered,expired,cancelled',
                    'category' => 'sometimes|in:'.implode(',', $validated['categories']),
                    'priority' => 'sometimes|in:normal,medium,high,urgent',
                    'publishedOnIsoDate' => 'nullable|date_format:Y-m-d',
                ])->validate();
                if (isset($item['priority']) && ($item['priority'] === 'urgent') !== (bool) $item['urgent']) {
                    throw ValidationException::withMessages(['priority' => 'Priority must agree with urgent.']);
                }
                if (isset($needIds[$item['id']])) {
                    throw ValidationException::withMessages(['needs' => 'Need identifiers must be globally unique.']);
                }
                $needIds[$item['id']] = true;
                $item['urgent'] = (bool) $item['urgent'];
                $validated['needs'][] = $item;
            }
            $centers[] = $validated;
        }
        DB::transaction(function () use ($centers, $operator) {
            foreach ($centers as $centerData) {
                $center = Center::whereKey($centerData['id'])->lockForUpdate()->first();
                $details = Arr::except($centerData, ['id', 'needs']);
                if ($center) {
                    $center->update(['details' => $details, 'version' => $center->version + 1]);
                } else {
                    $center = Center::create(['id' => $centerData['id'], 'details' => $details]);
                }
                foreach ($centerData['needs'] as $data) {
                    $need = Need::whereKey($data['id'])->lockForUpdate()->first();
                    if ($need && $need->center_id !== $center->id) {
                        throw new ApiProblem('need_center_immutable');
                    }
                    if ($need && ($data['quantityRequired'] ?? null) !== null && $data['quantityRequired'] < $need->quantity_committed) {
                        throw new ApiProblem('capacity_below_commitments');
                    }
                    if ($need?->quantity_committed > 0 && ($data['unit'] ?? null) !== ($need->details['unit'] ?? null)) {
                        throw new ApiProblem('reserved_unit_immutable');
                    }
                    $attributes = [
                        'details' => Arr::only($data, ['title', 'description', 'helpMethods', 'urgent', 'unit', 'category', 'priority', 'publishedOnIsoDate']),
                        'quantity_required' => $data['quantityRequired'] ?? null,
                        'expires_on' => $data['expiresOnIsoDate'] ?? null,
                    ];
                    if ($need) {
                        // Imports preserve both reservations and staff-controlled publication state.
                        $need->update($attributes + ['version' => $need->version + 1]);
                    } else {
                        $status = ($data['status'] ?? 'active') === 'active' ? 'published' : 'closed';
                        Need::create(['id' => $data['id'], 'center_id' => $center->id, 'status' => $status] + $attributes);
                    }
                }
            }
            DB::table('operator_audits')->insert([
                'operator' => $operator, 'action' => 'catalog_import',
                'details' => json_encode(['centers' => array_column($centers, 'id'), 'uid' => getmyuid()], JSON_THROW_ON_ERROR),
                'created_at' => now(),
            ]);
        }, 5);
    }
}
