<?php

namespace App\Providers;

use App\Services\DeploymentEnvironment;
use Illuminate\Cache\RateLimiting\Limit;
use Illuminate\Support\Facades\RateLimiter;
use Illuminate\Support\ServiceProvider;

class AppServiceProvider extends ServiceProvider
{
    /**
     * Register any application services.
     */
    public function register(): void
    {
        //
    }

    /**
     * Bootstrap any application services.
     */
    public function boot(): void
    {
        DeploymentEnvironment::validate();
        RateLimiter::for('identity', fn ($request) => Limit::perMinute(20)->by('identity:'.$request->ip()));
        RateLimiter::for('api', fn ($request) => Limit::perMinute(120)->by($request->ip()));
        RateLimiter::for('center-applications', fn ($request) => Limit::perMinute(5)->by('center-applications:'.$request->user()->id));
        RateLimiter::for('center-application-reviews', fn ($request) => Limit::perMinute(10)->by('center-application-reviews:'.$request->user()->id));
        RateLimiter::for('center-application-cases', fn ($request) => Limit::perMinute(60)->by('center-application-cases:'.$request->user()->id));
        RateLimiter::for('participation-reviews', fn ($request) => Limit::perMinute(10)->by('participation-reviews:'.$request->user()->id));
        RateLimiter::for('accounts', function ($request) {
            $email = $request->input('email');
            $account = is_string($email) ? strtolower($email) : (string) $request->user()?->id;

            return [
                Limit::perMinute(10)->by('ip:'.$request->ip()),
                Limit::perMinute(5)->by('account:'.hash('sha256', $account)),
            ];
        });
    }
}
