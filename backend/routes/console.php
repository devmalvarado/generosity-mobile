<?php

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Artisan;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schedule;
use Laravel\Sanctum\Http\Middleware\EnsureFrontendRequestsAreStateful;

Artisan::command('auth:check-portal', function () {
    $parts = parse_url(config('identity.portal_url', ''));
    if (! isset($parts['scheme'], $parts['host']) || ! in_array($parts['scheme'], ['http', 'https'], true)) {
        $this->error('GENEROSITY_PORTAL_URL must be an HTTP(S) URL.');

        return 1;
    }
    $origin = $parts['scheme'].'://'.$parts['host'].(isset($parts['port']) ? ':'.$parts['port'] : '');
    $request = Request::create('/api/v1/me', 'GET', server: ['HTTP_ORIGIN' => $origin]);
    if (! EnsureFrontendRequestsAreStateful::fromFrontend($request)
        || ! in_array($origin, config('cors.allowed_origins', []), true)
        || ! config('cors.supports_credentials')) {
        $this->error('Portal sessions unavailable: configure SANCTUM_STATEFUL_DOMAINS and credentialed CORS for '.$origin);

        return 1;
    }
    $this->info('Portal session origin is configured: '.$origin);

    return 0;
})->purpose('Check portal session configuration without reading credentials or changing data');

Schedule::command('sanctum:prune-expired --hours=24')->daily();
Schedule::command('auth:clear-resets')->daily();

Schedule::call(function () {
    DB::table('auth_attempts')->where('expires_at', '<', now())->delete();
    DB::table('auth_handoffs')->where('expires_at', '<', now())->delete();
})->hourly()->name('auth:prune-handoffs');
