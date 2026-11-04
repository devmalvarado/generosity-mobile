<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    private const FLAGS = [
        'events.publish', 'events.enroll', 'events.validate', 'campaigns.publish',
        'meals.plan', 'meals.commit', 'assistance.record', 'outcomes.publish', 'follows.update',
    ];

    public function up(): void
    {
        Schema::create('campaigns', function (Blueprint $table) {
            $table->string('id', 100)->primary();
            $table->foreignId('organizer_user_id')->constrained('users');
            $table->string('type', 30);
            $table->string('status', 20)->default('draft')->index();
            $table->json('title');
            $table->json('purpose');
            $table->string('area', 200);
            $table->string('center_id', 100)->nullable();
            $table->foreign('center_id')->references('id')->on('centers');
            $table->string('partnership_status', 20)->default('none');
            $table->json('private_plan')->nullable();
            $table->unsignedInteger('version')->default(1);
            $table->timestamps();
        });
        Schema::create('event_occurrences', function (Blueprint $table) {
            $table->string('id', 100)->primary();
            $table->string('owner_type', 20);
            $table->string('center_id', 100)->nullable();
            $table->foreign('center_id')->references('id')->on('centers');
            $table->string('campaign_id', 100)->nullable();
            $table->foreign('campaign_id')->references('id')->on('campaigns');
            $table->string('status', 20)->default('draft')->index();
            $table->json('title');
            $table->json('description');
            $table->string('activity', 120);
            $table->timestamp('starts_at');
            $table->timestamp('ends_at');
            $table->string('timezone', 64);
            $table->string('venue', 200);
            $table->unsignedInteger('capacity');
            $table->unsignedInteger('reserved')->default(0);
            $table->string('cancel_reason', 500)->nullable();
            $table->unsignedInteger('version')->default(1);
            $table->timestamps();
            $table->index(['status', 'starts_at']);
        });
        Schema::create('assisted_subjects', function (Blueprint $table) {
            $table->uuid('id')->primary();
            $table->string('center_id', 100);
            $table->foreign('center_id')->references('id')->on('centers');
            $table->foreignId('coordinator_user_id')->constrained('users');
            $table->string('consent_method', 20);
            $table->timestamp('consent_at');
            $table->string('label', 80)->nullable();
            $table->text('private_contact')->nullable();
            $table->string('status', 20)->default('active');
            $table->foreignId('claimed_user_id')->nullable()->constrained('users')->nullOnDelete();
            $table->unsignedInteger('version')->default(1);
            $table->timestamps();
        });
        Schema::create('event_enrollments', function (Blueprint $table) {
            $table->uuid('id')->primary();
            $table->string('occurrence_id', 100);
            $table->foreign('occurrence_id')->references('id')->on('event_occurrences');
            $table->foreignId('user_id')->nullable()->constrained('users')->nullOnDelete();
            $table->foreignUuid('assisted_subject_id')->nullable()->constrained('assisted_subjects')->nullOnDelete();
            $table->foreignId('actor_user_id')->constrained('users');
            $table->string('status', 20);
            $table->uuid('contribution_id')->nullable();
            $table->unsignedInteger('version')->default(1);
            $table->timestamps();
            $table->unique(['occurrence_id', 'user_id']);
            $table->unique(['occurrence_id', 'assisted_subject_id']);
        });
        Schema::create('meal_plans', function (Blueprint $table) {
            $table->uuid('id')->primary();
            $table->string('center_id', 100);
            $table->foreign('center_id')->references('id')->on('centers');
            $table->foreignId('coordinator_user_id')->constrained('users');
            $table->string('status', 20)->default('private');
            $table->string('approval_method', 30)->nullable();
            $table->timestamp('approved_at')->nullable();
            $table->string('portion_unit', 60);
            $table->json('public_title');
            $table->json('private_details');
            $table->unsignedInteger('version')->default(1);
            $table->timestamps();
        });
        Schema::create('meal_slots', function (Blueprint $table) {
            $table->uuid('id')->primary();
            $table->foreignUuid('plan_id')->constrained('meal_plans')->cascadeOnDelete();
            $table->date('local_date');
            $table->string('window_start', 8);
            $table->string('window_end', 8);
            $table->string('timezone', 64);
            $table->unsignedInteger('quantity_required');
            $table->unsignedInteger('quantity_committed')->default(0);
            $table->string('status', 20)->default('open');
            $table->timestamp('cutoff_at');
            $table->unsignedInteger('version')->default(1);
            $table->timestamps();
        });
        Schema::create('meal_commitments', function (Blueprint $table) {
            $table->uuid('id')->primary();
            $table->foreignUuid('slot_id')->constrained('meal_slots');
            $table->foreignId('user_id')->constrained('users');
            $table->foreignId('actor_user_id')->constrained('users');
            $table->foreignUuid('assisted_subject_id')->nullable()->constrained('assisted_subjects')->nullOnDelete();
            $table->unsignedInteger('quantity');
            $table->string('status', 20);
            $table->unsignedInteger('version')->default(1);
            $table->timestamps();
            $table->unique(['slot_id', 'user_id']);
        });
        Schema::create('assisted_claims', function (Blueprint $table) {
            $table->uuid('id')->primary();
            $table->foreignUuid('subject_id')->constrained('assisted_subjects');
            $table->foreignId('claimant_user_id')->constrained('users');
            $table->string('status', 20)->default('pending');
            $table->unsignedInteger('version')->default(1);
            $table->timestamps();
            $table->unique(['subject_id', 'claimant_user_id']);
        });
        Schema::create('outcome_updates', function (Blueprint $table) {
            $table->uuid('id')->primary();
            $table->string('center_id', 100)->nullable();
            $table->foreign('center_id')->references('id')->on('centers');
            $table->string('campaign_id', 100)->nullable();
            $table->foreign('campaign_id')->references('id')->on('campaigns');
            $table->foreignId('author_user_id')->constrained('users');
            $table->string('status', 20)->default('draft');
            $table->json('body')->nullable();
            $table->boolean('identifies_beneficiary')->default(false);
            $table->boolean('publication_permission')->default(false);
            $table->unsignedInteger('requested')->nullable();
            $table->unsignedInteger('committed')->nullable();
            $table->unsignedInteger('reported')->nullable();
            $table->unsignedInteger('validated')->nullable();
            $table->string('unit', 60)->nullable();
            $table->date('period_start')->nullable();
            $table->date('period_end')->nullable();
            $table->uuid('supersedes_id')->nullable();
            $table->unsignedInteger('version')->default(1);
            $table->timestamps();
        });
        Schema::create('follows', function (Blueprint $table) {
            $table->uuid('id')->primary();
            $table->foreignId('user_id')->constrained()->cascadeOnDelete();
            $table->string('target_type', 20);
            $table->string('target_id', 100);
            $table->string('channel', 20);
            $table->timestamp('consented_at');
            $table->timestamp('unsubscribed_at')->nullable();
            $table->timestamps();
            $table->unique(['user_id', 'target_type', 'target_id', 'channel']);
        });
        $now = now();
        foreach (self::FLAGS as $name) {
            DB::table('feature_flags')->insert([
                'name' => $name, 'mode' => 'off', 'tester_ids' => '[]', 'version' => 1,
                'created_at' => $now, 'updated_at' => $now,
            ]);
        }
    }

    public function down(): void
    {
        DB::table('feature_flags')->whereIn('name', self::FLAGS)->delete();
        foreach ([
            'follows', 'outcome_updates', 'assisted_claims', 'meal_commitments', 'meal_slots', 'meal_plans',
            'event_enrollments', 'assisted_subjects', 'event_occurrences', 'campaigns',
        ] as $table) {
            Schema::dropIfExists($table);
        }
    }
};
