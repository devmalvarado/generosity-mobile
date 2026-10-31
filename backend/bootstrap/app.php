<?php

use App\Exceptions\ApiProblem;
use App\Http\Middleware\ApiBoundary;
use Illuminate\Foundation\Application;
use Illuminate\Foundation\Configuration\Exceptions;
use Illuminate\Foundation\Configuration\Middleware;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Log;
use Symfony\Component\HttpFoundation\Response;

return Application::configure(basePath: dirname(__DIR__))
    ->withRouting(
        web: __DIR__.'/../routes/web.php',
        api: __DIR__.'/../routes/api.php',
        commands: __DIR__.'/../routes/console.php',
        health: '/up',
    )
    ->withMiddleware(function (Middleware $middleware): void {
        $middleware->append(ApiBoundary::class);
        $middleware->api(append: ['throttle:api']);
    })
    ->withExceptions(function (Exceptions $exceptions): void {
        $exceptions->report(function (Throwable $e) {
            // SQL and mail exception messages can contain private payloads or credentials.
            Log::error('backend_failure', ['type' => class_basename($e)]);
        })->stop();
        $exceptions->shouldRenderJsonWhen(
            fn (Request $request) => $request->is('api/*') || $request->expectsJson(),
        );
        $exceptions->dontReport([ApiProblem::class]);
        $exceptions->render(function (ApiProblem $e) {
            return response()->json(['error' => ['code' => $e->errorCode, 'details' => $e->details]], $e->status);
        });
        $exceptions->respond(function (Response $response) {
            if (! request()->is('api/*')) {
                return $response;
            }
            if ($response->getStatusCode() >= 400) {
                $body = json_decode($response->getContent(), true);
                if (! isset($body['error'])) {
                    $code = match ($response->getStatusCode()) {
                        401 => 'unauthenticated', 403 => 'forbidden', 404 => 'not_found',
                        405 => 'method_not_allowed', 422 => 'validation_failed',
                        429 => 'rate_limited', default => 'service_unavailable',
                    };
                    $response = response()->json(['error' => ['code' => $code,
                        'details' => $response->getStatusCode() === 422 ? ($body['errors'] ?? []) : new stdClass,
                    ]], $response->getStatusCode(), array_intersect_key($response->headers->all(), array_flip(['retry-after'])));
                }
            }
            $response->headers->set('Cache-Control', 'no-store');
            $response->headers->set('X-Content-Type-Options', 'nosniff');

            return $response;
        });
    })->create();
