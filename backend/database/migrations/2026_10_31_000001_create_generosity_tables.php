<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('personal_access_tokens', function (Blueprint $table) {
            $table->id();
            $table->morphs('tokenable');
            $table->text('name');
            $table->string('token', 64)->unique();
            $table->text('abilities')->nullable();
            $table->timestamp('last_used_at')->nullable();
            $table->timestamp('expires_at')->nullable()->index();
            $table->timestamps();
        });
        Schema::create('centers', function (Blueprint $table) {
            $table->string('id', 100)->primary();
            $table->json('details');
            $table->unsignedInteger('version')->default(1);
            $table->timestamps();
        });
        Schema::create('center_memberships', function (Blueprint $table) {
            $table->foreignId('user_id')->constrained();
            $table->string('center_id', 100);
            $table->foreign('center_id')->references('id')->on('centers');
            $table->primary(['user_id', 'center_id']);
        });
        Schema::create('needs', function (Blueprint $table) {
            $table->string('id', 100)->primary();
            $table->string('center_id', 100);
            $table->foreign('center_id')->references('id')->on('centers');
            $table->json('details');
            $table->string('status', 20)->default('draft')->index();
            $table->unsignedInteger('quantity_required')->nullable();
            $table->unsignedInteger('quantity_committed')->default(0);
            $table->date('expires_on')->nullable();
            $table->unsignedInteger('version')->default(1);
            $table->timestamps();
        });
        Schema::create('contributions', function (Blueprint $table) {
            $table->uuid('id')->primary();
            $table->foreignId('user_id')->constrained();
            $table->string('center_id', 100)->nullable();
            $table->foreign('center_id')->references('id')->on('centers');
            $table->string('need_id', 100)->nullable();
            $table->foreign('need_id')->references('id')->on('needs');
            $table->string('method', 20);
            $table->unsignedInteger('quantity')->nullable();
            $table->unsignedInteger('minutes')->nullable();
            $table->string('activity', 500)->nullable();
            $table->string('availability', 500)->nullable();
            $table->text('notes')->nullable();
            $table->string('provenance', 30);
            $table->string('status', 20);
            $table->unsignedInteger('version')->default(1);
            $table->timestamps();
            $table->index(['user_id', 'created_at']);
            $table->index(['center_id', 'created_at']);
        });
        Schema::create('contribution_events', function (Blueprint $table) {
            $table->id();
            $table->foreignUuid('contribution_id')->constrained();
            $table->foreignId('actor_id')->constrained('users');
            $table->string('from_status', 20)->nullable();
            $table->string('to_status', 20);
            $table->unsignedInteger('version');
            $table->timestamp('created_at');
            $table->unique(['contribution_id', 'version']);
        });
        Schema::create('operations', function (Blueprint $table) {
            $table->foreignId('user_id')->constrained();
            $table->uuid('key');
            $table->string('request_hash', 64);
            $table->json('response');
            $table->unsignedSmallInteger('status');
            $table->timestamp('created_at');
            $table->primary(['user_id', 'key']);
        });
        Schema::create('operator_audits', function (Blueprint $table) {
            $table->id();
            $table->string('operator');
            $table->string('action');
            $table->json('details');
            $table->timestamp('created_at');
        });
    }

    public function down(): void
    {
        foreach (['operator_audits', 'operations', 'contribution_events', 'contributions', 'needs', 'center_memberships', 'centers', 'personal_access_tokens'] as $table) {
            Schema::dropIfExists($table);
        }
    }
};
