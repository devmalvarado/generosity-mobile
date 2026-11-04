<?php

return [
    'channel' => env('GENEROSITY_CHANNEL', 'production'),
    'feature_admin_ids' => array_values(array_filter(array_map('trim', explode(',', (string) env('FEATURE_ADMIN_IDS', ''))), fn (string $id) => ctype_digit($id))),
    // Verified account IDs allowed to decide center applications. Empty keeps
    // review unavailable; do not invent IDs, a turnaround or a retention period.
    'application_reviewer_ids' => array_values(array_filter(
        array_map('trim', explode(',', (string) env('CENTER_APPLICATION_REVIEWER_IDS', ''))),
        fn (string $id) => $id !== '' && ctype_digit($id)
    )),
    // Empty or absent keeps the action unavailable. Do not invent a threshold,
    // withdrawal channel, reviewer, operator, consent version or retention period.
    'campaign_organizer_min_points' => ($points = env('CAMPAIGN_ORGANIZER_MIN_POINTS')) === null || $points === '' ? null : (int) $points,
    'meal_withdrawal_channel' => trim((string) env('MEAL_WITHDRAWAL_CHANNEL', '')),
    'assisted_claim_reviewer_ids' => array_values(array_filter(
        array_map('trim', explode(',', (string) env('ASSISTED_CLAIM_REVIEWER_IDS', ''))),
        fn (string $id) => $id !== '' && ctype_digit($id)
    )),
    'funnel_operator_ids' => array_values(array_filter(
        array_map('trim', explode(',', (string) env('FUNNEL_OPERATOR_IDS', ''))),
        fn (string $id) => $id !== '' && ctype_digit($id)
    )),
    'follow_email_consent_version' => trim((string) env('FOLLOW_EMAIL_CONSENT_VERSION', '')),
    'attribution_retention_days' => ($days = env('ATTRIBUTION_RETENTION_DAYS')) === null || $days === '' ? null : (int) $days,
];
