<?php

return [
    'paths' => ['api/*', 'auth/session', 'auth/logout'],
    'allowed_methods' => ['GET', 'POST', 'PUT', 'OPTIONS'],
    'allowed_origins' => array_values(array_filter(array_map('trim', explode(',', env('CORS_ALLOWED_ORIGINS', ''))))),
    'allowed_headers' => ['Content-Type', 'Accept', 'Authorization', 'Idempotency-Key', 'X-CSRF-TOKEN', 'X-XSRF-TOKEN'],
    'exposed_headers' => ['Retry-After'],
    'max_age' => 600,
    'supports_credentials' => true,
];
