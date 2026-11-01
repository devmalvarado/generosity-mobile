<?php

namespace App\Http\Controllers;

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
            ->through(fn ($row) => $this->summary($row));
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
            'contact_name' => 'required|string|max:160',
            'contact_role' => 'required|string|max:160',
            'phone' => 'nullable|string|max:40|regex:/^\+?[0-9 ()\-.]{6,40}$/',
            'website' => 'nullable|url:http,https|max:2048',
            'representation_confirmed' => 'required|accepted',
            'contact_consent' => 'required|accepted',
            'user_id' => 'prohibited', 'status' => 'prohibited',
            'verified' => 'prohibited', 'center_id' => 'prohibited', 'contact_email' => 'prohibited',
        ]);
        foreach (['legal_name', 'region', 'postal_code', 'address', 'phone', 'website'] as $optional) {
            $data[$optional] ??= null;
        }
        $data['representation_confirmed'] = $data['contact_consent'] = true;
        ksort($data);

        return Mutation::run($request, $data, fn () => null, function () use ($request, $data) {
            $fingerprint = hash('sha256', json_encode($data, JSON_THROW_ON_ERROR));
            $existing = DB::table('center_applications')->where('user_id', $request->user()->id)
                ->where('fingerprint', $fingerprint)->first();
            if ($existing) {
                return response()->json(['application' => $this->summary($existing)], 200);
            }
            $record = [
                'id' => (string) Str::uuid(), 'user_id' => $request->user()->id,
                'contact_email' => $request->user()->email,
                'details' => json_encode($data, JSON_THROW_ON_ERROR), 'fingerprint' => $fingerprint,
                'status' => 'pending', 'consent_version' => 'site-application-v1',
                'created_at' => now(), 'updated_at' => now(),
            ];
            DB::table('center_applications')->insert($record);

            return response()->json(['application' => $this->summary((object) $record)], 201);
        });
    }

    private function summary(object $row): array
    {
        $details = json_decode($row->details, true, 512, JSON_THROW_ON_ERROR);

        return ['id' => $row->id, 'organization_name' => $details['organization_name'],
            'site_name' => $details['site_name'], 'status' => $row->status,
            'submitted_at' => (string) $row->created_at];
    }
}
