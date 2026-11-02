<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::table('center_applications', function (Blueprint $table) {
            $table->unsignedInteger('version')->default(1)->after('status');
        });
        // Decision rows are the audit record. No retention period is selected, so nothing here expires or purges them.
        Schema::create('center_application_decisions', function (Blueprint $table) {
            $table->uuid('id')->primary();
            $table->foreignUuid('application_id')->constrained('center_applications');
            $table->unsignedInteger('position');
            $table->foreignId('actor_id')->constrained('users');
            $table->string('action', 32);
            $table->string('rationale', 2000)->nullable();
            $table->string('center_id', 100)->nullable();
            $table->foreignId('account_id')->nullable()->constrained('users');
            $table->json('roles')->nullable();
            $table->timestamp('created_at');
            $table->unique(['application_id', 'position']);
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('center_application_decisions');
        Schema::table('center_applications', fn (Blueprint $table) => $table->dropColumn('version'));
    }
};
