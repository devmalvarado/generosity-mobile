<?php

return [
    // Bearer-only API: ambient browser cookies never authenticate a write.
    'stateful' => [],
    'guard' => [],
    'expiration' => 10080,
    'token_prefix' => '',
    'middleware' => [],
];
