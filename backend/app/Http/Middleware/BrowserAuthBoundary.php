<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;

class BrowserAuthBoundary
{
    public function handle(Request $request, Closure $next)
    {
        if (! $request->is('auth/*', 'reset-password', 'reset-password/*', 'verify-email/*', 'legal/*')) {
            return $next($request);
        }
        $locale = $request->input('locale');
        app()->setLocale(in_array($locale, ['es', 'en'], true) ? $locale : $request->getPreferredLanguage(['en', 'es']));
        abort_if(app()->isProduction() && ! $request->isSecure(), 400);
        abort_if((int) $request->header('Content-Length', 0) > 65536 || strlen($request->getContent()) > 65536, 413);
        $response = $next($request);
        $response->headers->set('Cache-Control', 'no-store');
        $response->headers->set('Referrer-Policy', 'no-referrer');
        $response->headers->set('X-Content-Type-Options', 'nosniff');
        $response->headers->set('Content-Security-Policy', "default-src 'none'; img-src 'self'; font-src 'self'; style-src 'self' 'unsafe-inline'; form-action 'self'; frame-ancestors 'none'; base-uri 'none'");

        return $response;
    }
}
