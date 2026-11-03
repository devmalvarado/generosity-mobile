<?php

return [
    'enabled' => (bool) env('FCM_ENABLED', false),
    'project_id' => env('FCM_PROJECT_ID', 'generosity-v01'),
    'credentials' => env('FCM_CREDENTIALS'),
    'ios_enabled' => (bool) env('FCM_IOS_ENABLED', false),
];
