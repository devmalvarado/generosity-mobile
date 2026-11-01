<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->timestamp('deactivated_at')->nullable()->index();
            $table->string('policy_version', 32)->nullable();
            $table->timestamp('policy_accepted_at')->nullable();
            $table->timestamp('adult_confirmed_at')->nullable();
        });
    }

    public function down(): void
    {
        Schema::table('users', fn (Blueprint $table) => $table->dropColumn([
            'deactivated_at', 'policy_version', 'policy_accepted_at', 'adult_confirmed_at',
        ]));
    }
};
