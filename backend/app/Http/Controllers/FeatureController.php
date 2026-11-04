<?php

namespace App\Http\Controllers;

use App\Services\FeatureFlags;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class FeatureController extends Controller
{
    public function publicFeatures(): array
    {
        return FeatureFlags::snapshot(null);
    }

    public function accountFeatures(Request $request): array
    {
        return FeatureFlags::snapshot($request->user()->id);
    }

    public function index(Request $request)
    {
        FeatureFlags::authorizeAdmin($request);

        return view('features', ['flags' => DB::table('feature_flags')->whereIn('name', FeatureFlags::NAMES)->orderBy('name')->get()]);
    }

    public function update(Request $request, string $name)
    {
        FeatureFlags::authorizeAdmin($request);
        $ids = $request->input('tester_ids', '');
        if (is_string($ids)) {
            $request->merge(['tester_ids' => $ids === '' ? [] : array_map('trim', explode(',', $ids))]);
        }
        $data = $request->validate([
            'mode' => 'required|in:off,testers,all', 'version' => 'required|integer|min:1',
            'tester_ids' => 'present|array|max:100', 'tester_ids.*' => 'required|integer|min:1|distinct',
            'reason' => 'required|string|max:500',
        ]);
        FeatureFlags::change($request, $name, $data);

        return redirect('/admin/features?locale='.app()->getLocale())->with('saved', true);
    }
}
