<?php

namespace App\Http\Controllers;

use App\Services\CatalogRules;
use App\Services\CenterApplicationReview;
use App\Services\Mutation;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;

class CenterApplicationController extends Controller
{
    public function index(Request $request)
    {
        return DB::table('center_applications')->where('user_id', $request->user()->id)
            ->orderByDesc('created_at')->orderBy('id')->paginate(10)
            ->through(fn ($row) => CenterApplicationReview::summary($row));
    }

    public function store(Request $request)
    {
        $data = $request->validate([
            'organization_name' => 'required|string|max:200',
            'legal_name' => 'nullable|string|max:200',
            'site_name' => 'required|string|max:200',
            'activities' => 'required|string|max:2000',
            'country' => 'required|string|max:100',
            'region' => 'nullable|string|max:100',
            'city' => 'required|string|max:100',
            'postal_code' => 'nullable|string|max:20',
            'address' => 'nullable|string|max:500',
            'google_maps_url' => CatalogRules::googleMapsUrl(),
            'latitude' => 'nullable|required_with:longitude|numeric|between:-90,90',
            'longitude' => 'nullable|required_with:latitude|numeric|between:-180,180',
            'contact_name' => 'required|string|max:160',
            'contact_role' => 'required|string|max:160',
            'phone' => 'nullable|string|max:40|regex:/^\+?[0-9 ()\-.]{6,40}$/',
            'website' => 'nullable|url:http,https|max:2048',
            'representation_confirmed' => 'required|accepted',
            'contact_consent' => 'required|accepted',
            'claimed_center_id' => 'nullable|string|max:100|regex:/^[a-z0-9][a-z0-9-]*$/|exists:centers,id',
            'user_id' => 'prohibited', 'status' => 'prohibited', 'roles' => 'prohibited',
            'account_id' => 'prohibited', 'membership' => 'prohibited',
            'verified' => 'prohibited', 'center_id' => 'prohibited', 'contact_email' => 'prohibited',
        ]);
        foreach (['legal_name', 'region', 'postal_code', 'address', 'phone', 'website'] as $optional) {
            $data[$optional] ??= null;
        }
        // Preserve fingerprints and pending retries created before location fields existed.
        foreach (['google_maps_url', 'latitude', 'longitude', 'claimed_center_id'] as $optional) {
            if (($data[$optional] ?? null) === null) {
                unset($data[$optional]);
            }
        }
        foreach (['latitude', 'longitude'] as $coordinate) {
            if (isset($data[$coordinate])) {
                $data[$coordinate] = (float) $data[$coordinate];
            }
        }
        $data['representation_confirmed'] = $data['contact_consent'] = true;
        ksort($data);

        return Mutation::run($request, $data, fn () => null, function () use ($request, $data) {
            $fingerprint = hash('sha256', json_encode($data, JSON_THROW_ON_ERROR));
            $existing = DB::table('center_applications')->where('user_id', $request->user()->id)
                ->where('fingerprint', $fingerprint)->first();
            if ($existing) {
                return response()->json(['application' => CenterApplicationReview::summary($existing)], 200);
            }
            if (isset($data['claimed_center_id'])) {
                CenterApplicationReview::lockClaimedCenter($data['claimed_center_id']);
            }
            $record = [
                'id' => (string) Str::uuid(), 'user_id' => $request->user()->id,
                'contact_email' => $request->user()->email,
                'details' => json_encode($data, JSON_THROW_ON_ERROR), 'fingerprint' => $fingerprint,
                'status' => 'pending', 'version' => 1, 'consent_version' => 'site-application-v1',
                'created_at' => now(), 'updated_at' => now(),
            ];
            DB::table('center_applications')->insert($record);

            return response()->json(['application' => CenterApplicationReview::summary((object) $record)], 201);
        });
    }

    public function show(Request $request, string $application)
    {
        return CenterApplicationReview::show($request, $application);
    }

    public function decide(Request $request, string $application)
    {
        return CenterApplicationReview::decide($request, $application);
    }

    public function acceptMembership(Request $request, string $application)
    {
        return CenterApplicationReview::accept($request, $application);
    }

    public function withdraw(Request $request, string $application)
    {
        return CenterApplicationReview::withdraw($request, $application);
    }
}
