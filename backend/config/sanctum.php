<?php

return [
    // Only explicitly configured first-party origins receive CSRF-protected web sessions.
    'stateful' => array_values(array_filter(explode(',', env('SANCTUM_STATEFUL_DOMAINS', '')))),
    'guard' => ['web'],
    'expiration' => 10080,
    'token_prefix' => '',
    'middleware' => [],
];
