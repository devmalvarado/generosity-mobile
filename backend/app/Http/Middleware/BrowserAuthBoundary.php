<?php

namespace App\Http\Middleware;

use App\Exceptions\ApiProblem;
use Closure;
use Illuminate\Http\Request;

class BrowserAuthBoundary
{
    public function handle(Request $request, Closure $next)
    {
        if (! $request->is('auth/*', 'reset-password', 'reset-password/*', 'verify-email/*', 'legal/*', 'admin/*')) {
            return $next($request);
        }
        $locale = $request->input('locale');
        app()->setLocale(in_array($locale, ['es', 'en'], true) ? $locale : $request->getPreferredLanguage(['en', 'es']));
        $channel = $request->header('X-Generosity-Channel');
        if ($channel !== null && $channel !== config('generosity.channel')) {
            throw new ApiProblem('environment_mismatch', 409);
        }
        abort_if((app()->isProduction() || config('generosity.channel') === 'preview') && ! $request->isSecure(), 400);
        abort_if((int) $request->header('Content-Length', 0) > 65536 || strlen($request->getContent()) > 65536, 413);
        $response = $next($request);
        $response->headers->set('Cache-Control', 'no-store');
        $response->headers->set('Referrer-Policy', 'no-referrer');
        $response->headers->set('X-Content-Type-Options', 'nosniff');
        // Chrome also checks form-action on the POST redirect to the identity provider.
        $response->headers->set('Content-Security-Policy', "default-src 'none'; img-src 'self'; font-src 'self'; style-src 'self' 'unsafe-inline'; form-action 'self' https://accounts.google.com https://appleid.apple.com; frame-ancestors 'none'; base-uri 'none'");

        return $response;
    }
}
