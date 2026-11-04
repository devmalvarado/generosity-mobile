<?php

$preview = env('GENEROSITY_CHANNEL', 'production') === 'preview';

return [
    'portal_url' => env('GENEROSITY_PORTAL_URL', 'https://generosity.global/#acceso'),
    'clients' => $preview ? ['android-preview' => 'org.generosity.app.preview.android:/oauth/callback', 'ios-preview' => 'org.generosity.app.preview.ios:/oauth/callback'] : [
        'android' => 'org.generosity.app.android:/oauth/callback',
        'ios' => 'org.generosity.app.ios:/oauth/callback',
    ],
    'google' => [
        'client_id' => env('GOOGLE_CLIENT_ID'),
        'client_secret' => env('GOOGLE_CLIENT_SECRET'),
    ],
    'apple' => [
        'client_id' => env('APPLE_SERVICE_ID'),
        'team_id' => env('APPLE_TEAM_ID'),
        'key_id' => env('APPLE_KEY_ID'),
        'private_key_path' => env('APPLE_PRIVATE_KEY_PATH'),
    ],
];
