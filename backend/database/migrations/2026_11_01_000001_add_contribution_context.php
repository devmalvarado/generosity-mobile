<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::table('contributions', function (Blueprint $table) {
            $table->json('title')->nullable();
            $table->string('quantity_unit', 60)->nullable();
            $table->unsignedInteger('actual_minutes')->nullable();
        });
    }

    public function down(): void
    {
        Schema::table('contributions', fn (Blueprint $table) => $table->dropColumn(['title', 'quantity_unit', 'actual_minutes']));
    }
};
