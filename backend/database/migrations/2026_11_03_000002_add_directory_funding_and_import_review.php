<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::table('centers', function (Blueprint $table) {
            $table->enum('funding_origin', ['public', 'private', 'mixed', 'not_informed'])->default('not_informed')->index();
            $table->enum('administration_type', ['public', 'private', 'mixed', 'not_informed'])->default('not_informed');
        });
        // Source contacts and unresolved claims must never enter public center details.
        Schema::create('catalog_import_rows', function (Blueprint $table) {
            $table->id();
            $table->string('source_sha256', 64);
            $table->string('source_name');
            $table->string('sheet', 80);
            $table->unsignedInteger('row_number');
            $table->string('center_id', 100)->nullable();
            $table->foreign('center_id')->references('id')->on('centers');
            $table->enum('status', ['pending', 'applied', 'matched', 'duplicate'])->default('pending')->index();
            $table->json('source_data');
            $table->json('review_notes');
            $table->json('source_urls');
            $table->timestamps();
            $table->unique(['source_sha256', 'sheet', 'row_number'], 'catalog_source_row_unique');
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('catalog_import_rows');
        Schema::table('centers', function (Blueprint $table) {
            $table->dropColumn(['funding_origin', 'administration_type']);
        });
    }
};
