<?php

namespace App\Http\Middleware;

use App\Exceptions\ApiProblem;
use Closure;
use Illuminate\Http\Request;

class ApiBoundary
{
    public function handle(Request $request, Closure $next)
    {
        $channel = $request->header('X-Generosity-Channel');
        if ($channel !== null && $channel !== config('generosity.channel')) {
            throw new ApiProblem('environment_mismatch', 409);
        }
        if ($request->is('api/*')) {
            $request->headers->set('Accept', 'application/json');
        }
        if ((app()->environment('production') || config('generosity.channel') === 'preview') && ! $request->secure()) {
            throw new ApiProblem('https_required', 400);
        }
        if (strlen($request->getContent()) > 65536) {
            throw new ApiProblem('payload_too_large', 413);
        }
        $response = $next($request);
        $response->headers->set('Cache-Control', 'no-store');
        $response->headers->set('X-Content-Type-Options', 'nosniff');

        return $response;
    }
}
