<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('feature_flags', function (Blueprint $table) {
            $table->string('name', 80)->primary();
            $table->string('mode', 12)->default('off');
            $table->json('tester_ids');
            $table->unsignedInteger('version')->default(1);
            $table->timestamps();
        });
        foreach (['contributions.create', 'needs.publish', 'needs.progress'] as $name) {
            DB::table('feature_flags')->insert(['name' => $name, 'mode' => 'all', 'tester_ids' => '[]', 'version' => 1, 'created_at' => now(), 'updated_at' => now()]);
        }
    }

    public function down(): void
    {
        Schema::dropIfExists('feature_flags');
    }
};
