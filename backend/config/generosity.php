<?php

return [
    // Verified account IDs allowed to decide center applications. Empty keeps
    // review unavailable; do not invent IDs, a turnaround or a retention period.
    'application_reviewer_ids' => array_values(array_filter(
        array_map('trim', explode(',', (string) env('CENTER_APPLICATION_REVIEWER_IDS', ''))),
        fn (string $id) => $id !== '' && ctype_digit($id)
    )),
];
