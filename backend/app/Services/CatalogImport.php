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
            'schemaVersion' => 'required|integer|in:1', 'centers' => 'present|array|max:10000',
            'centers.*' => 'array:id,name,description,address,categories,coordinates,contact,googleMapsUrl,verificationStatus,importedAtIsoDate,needs,fundingOrigin,administrationType,directoryProfile,expectedVersion',
            'centers.*.id' => 'required|string|max:100|regex:/^[a-z0-9][a-z0-9-]*$/|distinct:strict',
            'reviewBatch' => 'sometimes|array:sourceName,sourceSha256,rows',
            'reviewBatch.sourceName' => 'required_with:reviewBatch|string|max:255',
            'reviewBatch.sourceSha256' => 'required_with:reviewBatch|regex:/^[a-f0-9]{64}$/',
            'reviewBatch.rows' => 'required_with:reviewBatch|array|max:10000',
            'reviewBatch.rows.*' => 'array:sheet,rowNumber,centerId,status,sourceData,reviewNotes,sourceUrls',
            'reviewBatch.rows.*.sheet' => 'required|string|max:80',
            'reviewBatch.rows.*.rowNumber' => 'required|integer|min:1',
            'reviewBatch.rows.*.centerId' => 'nullable|string|max:100',
            'reviewBatch.rows.*.status' => 'required|in:pending,applied,matched,duplicate',
            'reviewBatch.rows.*.sourceData' => 'present|array|max:50',
            'reviewBatch.rows.*.sourceData.*' => 'nullable|string|max:6000',
            'reviewBatch.rows.*.reviewNotes' => 'required|array|max:30',
            'reviewBatch.rows.*.reviewNotes.*' => 'required|string|max:2000',
            'reviewBatch.rows.*.sourceUrls' => 'present|array|max:30',
            'reviewBatch.rows.*.sourceUrls.*' => 'required|url:http,https|max:2048',
        ])->validate();
        $needIds = [];
        $centers = [];
        foreach ($catalog['centers'] as $center) {
            $validated = Validator::make($center, CatalogRules::directory() + [
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
            $evidenceFields = array_merge([], ...array_column($validated['directoryProfile']['evidence'] ?? [], 'fields'));
            foreach (['fundingOrigin', 'administrationType'] as $field) {
                if (($validated[$field] ?? 'not_informed') !== 'not_informed' && ! in_array($field, $evidenceFields, true)) {
                    throw ValidationException::withMessages([$field => 'A public source and review date are required.']);
                }
            }
            foreach (Arr::except($validated['directoryProfile'] ?? [], ['evidence']) as $field => $value) {
                if ($value !== null && $value !== [] && ! in_array($field, $evidenceFields, true)) {
                    throw ValidationException::withMessages(['directoryProfile.'.$field => 'A public source and review date are required.']);
                }
            }
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
        $review = $catalog['reviewBatch'] ?? null;
        $rowKeys = [];
        foreach ($review['rows'] ?? [] as $row) {
            $key = $row['sheet'].':'.$row['rowNumber'];
            if (isset($rowKeys[$key]) || ($row['status'] !== 'pending' && empty($row['centerId']))) {
                throw ValidationException::withMessages(['reviewBatch' => 'Rows must be unique; resolved rows require a center.']);
            }
            $rowKeys[$key] = true;
        }
        DB::transaction(function () use ($centers, $operator, $review) {
            foreach ($centers as $centerData) {
                $center = Center::whereKey($centerData['id'])->lockForUpdate()->first();
                if (isset($centerData['expectedVersion']) && $centerData['expectedVersion'] !== ($center?->version ?? 0)) {
                    throw new ApiProblem('catalog_version_conflict', 409);
                }
                $details = Arr::except($centerData, ['id', 'needs', 'fundingOrigin', 'administrationType', 'expectedVersion']);
                // Legacy imports omit new sections; omission must not erase reviewed enrichment.
                if ($center && ! array_key_exists('directoryProfile', $details) && isset($center->details['directoryProfile'])) {
                    $details['directoryProfile'] = $center->details['directoryProfile'];
                }
                $attributes = ['details' => $details];
                foreach (['fundingOrigin' => 'funding_origin', 'administrationType' => 'administration_type'] as $field => $column) {
                    if (array_key_exists($field, $centerData)) {
                        $attributes[$column] = $centerData[$field];
                    }
                }
                if ($center) {
                    $center->update($attributes + ['version' => $center->version + 1]);
                } else {
                    $center = Center::create(['id' => $centerData['id']] + $attributes);
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
            foreach ($review['rows'] ?? [] as $row) {
                if (! empty($row['centerId']) && ! Center::whereKey($row['centerId'])->exists()) {
                    throw ValidationException::withMessages(['reviewBatch' => 'Referenced center does not exist.']);
                }
                $identity = [
                    'source_sha256' => $review['sourceSha256'], 'sheet' => $row['sheet'], 'row_number' => $row['rowNumber'],
                ];
                $existingRow = DB::table('catalog_import_rows')->where($identity)->lockForUpdate()->first();
                DB::table('catalog_import_rows')->updateOrInsert($identity, [
                    'source_name' => $review['sourceName'], 'center_id' => $row['centerId'] ?? null, 'status' => $row['status'],
                    'source_data' => json_encode($row['sourceData'], JSON_THROW_ON_ERROR),
                    'review_notes' => json_encode($row['reviewNotes'], JSON_THROW_ON_ERROR),
                    'source_urls' => json_encode($row['sourceUrls'], JSON_THROW_ON_ERROR),
                    'created_at' => $existingRow?->created_at ?? now(), 'updated_at' => now(),
                ]);
            }
            DB::table('operator_audits')->insert([
                'operator' => $operator, 'action' => 'catalog_import',
                'details' => json_encode(['centers' => array_column($centers, 'id'), 'uid' => getmyuid(), 'sourceSha256' => $review['sourceSha256'] ?? null, 'reviewRows' => count($review['rows'] ?? [])], JSON_THROW_ON_ERROR),
                'created_at' => now(),
            ]);
        }, 5);
    }
}
