<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Support\Facades\DB;

return new class extends Migration
{
    private const NAMES = ['preview.discovery', 'preview.contributions', 'preview.impact', 'preview.community', 'preview.assistance', 'preview.organizations', 'preview.communication', 'preview.operations', 'preview.account', 'preview.payments'];

    public function up(): void
    {
        foreach (self::NAMES as $name) {
            DB::table('feature_flags')->insert(['name' => $name, 'mode' => 'off', 'tester_ids' => '[]', 'version' => 1, 'created_at' => now(), 'updated_at' => now()]);
        }
    }

    public function down(): void
    {
        DB::table('feature_flags')->whereIn('name', self::NAMES)->delete();
    }
};
