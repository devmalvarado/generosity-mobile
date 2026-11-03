<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('notification_preferences', function (Blueprint $table) {
            $table->foreignId('user_id')->primary()->constrained()->cascadeOnDelete();
            $table->boolean('urgent')->default(false);
            $table->boolean('all_categories')->default(false);
            $table->json('categories');
            $table->timestamps();
        });
        Schema::create('push_installations', function (Blueprint $table) {
            $table->uuid('id')->primary();
            $table->foreignId('user_id')->constrained()->cascadeOnDelete();
            $table->foreignId('access_token_id')->constrained('personal_access_tokens')->cascadeOnDelete();
            $table->string('secret_hash', 64);
            $table->text('token');
            $table->string('token_hash', 64)->unique();
            $table->string('platform', 10);
            $table->string('locale', 5);
            $table->boolean('authorized')->default(false);
            $table->timestamps();
        });
        Schema::create('inbox_notices', function (Blueprint $table) {
            $table->uuid('id')->primary();
            $table->uuid('event_id');
            $table->foreignId('user_id')->constrained()->cascadeOnDelete();
            $table->string('need_id', 100);
            $table->foreign('need_id')->references('id')->on('needs')->cascadeOnDelete();
            $table->string('center_id', 100);
            $table->json('title');
            $table->timestamp('read_at')->nullable();
            $table->timestamp('created_at');
            $table->unique(['user_id', 'event_id']);
            $table->index(['user_id', 'created_at']);
        });
        Schema::create('push_deliveries', function (Blueprint $table) {
            $table->id();
            $table->foreignUuid('notice_id')->constrained('inbox_notices')->cascadeOnDelete();
            $table->foreignUuid('installation_id')->constrained('push_installations')->cascadeOnDelete();
            $table->string('token_hash', 64);
            $table->string('status', 20)->default('pending');
            $table->timestamp('accepted_at')->nullable();
            $table->timestamp('created_at');
            $table->unique(['notice_id', 'installation_id']);
        });
    }

    public function down(): void
    {
        foreach (['push_deliveries', 'inbox_notices', 'push_installations', 'notification_preferences'] as $table) {
            Schema::dropIfExists($table);
        }
    }
};
