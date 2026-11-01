<?php

namespace App\Http\Controllers;

use App\Services\Mutation;
use Illuminate\Http\Request;
use Illuminate\Support\Carbon;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;

class VisitController extends Controller
{
    public function store(Request $request)
    {
        $data = $request->validate([
            'center_id' => 'required|string|max:100|exists:centers,id',
            'occurred_at' => 'required|string|max:64|date|before_or_equal:'.now()->addMinutes(5)->toISOString(),
            'explicit_action' => 'required|accepted',
            'user_id' => 'prohibited', 'verification_status' => 'prohibited',
        ]);

        return Mutation::run($request, $data, fn () => null, function () use ($request, $data) {
            $record = ['id' => (string) Str::uuid(), 'user_id' => $request->user()->id,
                'center_id' => $data['center_id'], 'occurred_at' => Carbon::parse($data['occurred_at'])->utc(), 'received_at' => now()];
            DB::table('visit_signals')->insert($record);

            return response()->json($record, 201);
        });
    }
}
