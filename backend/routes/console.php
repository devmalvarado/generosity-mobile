<?php

use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schedule;

Schedule::command('sanctum:prune-expired --hours=24')->daily();
Schedule::command('auth:clear-resets')->daily();

Schedule::call(function () {
    DB::table('auth_attempts')->where('expires_at', '<', now())->delete();
    DB::table('auth_handoffs')->where('expires_at', '<', now())->delete();
})->hourly()->name('auth:prune-handoffs');
