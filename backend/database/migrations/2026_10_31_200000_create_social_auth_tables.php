<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('social_identities', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained()->cascadeOnDelete();
            $table->string('provider', 16);
            $table->string('subject', 255)->collation('utf8mb4_bin');
            $table->unique(['provider', 'subject']);
            $table->unique(['user_id', 'provider']);
            $table->timestamps();
        });
        Schema::create('auth_attempts', function (Blueprint $table) {
            $table->char('id', 64)->primary();
            $table->string('provider', 16);
            $table->char('binding_hash', 64);
            $table->string('nonce', 64);
            $table->text('context');
            $table->foreignId('link_user_id')->nullable()->constrained('users')->cascadeOnDelete();
            $table->timestamp('expires_at')->index();
        });
        Schema::create('auth_handoffs', function (Blueprint $table) {
            $table->char('id', 64)->primary();
            $table->foreignId('user_id')->constrained()->cascadeOnDelete();
            $table->string('client', 16);
            $table->char('challenge', 43);
            $table->timestamp('expires_at')->index();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('auth_handoffs');
        Schema::dropIfExists('auth_attempts');
        Schema::dropIfExists('social_identities');
    }
};
