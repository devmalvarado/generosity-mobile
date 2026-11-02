<?php

use App\Models\Center;
use Illuminate\Database\Migrations\Migration;
use Illuminate\Support\Facades\DB;

return new class extends Migration
{
    // Reviewed blood-service IDs only; never infer medical services from a name.
    private const IDS = [
        'mx-hospital-angeles-acoxpa-e042ef',
        'mx-servicios-hematologicos-e6c809',
        'mx-banco-de-sangre-instituto-nacional-de-pediatria-ff6d51',
        'mx-banco-de-sangre-perinatologia-412949',
        'mx-banco-de-sangre-cruz-roja-cdmx-ca6d74',
        'mx-hemocel-64facc',
        'mx-centro-estatal-de-transfusion-sanguinea-cets-ebac62',
        'mx-banco-de-sangre-vida-sc-ce8bb4',
        'maps-mx-4f48aad0a94b33de',
        'maps-mx-e16ee4006e146bcf',
        'maps-mx-ca09e7c7c35d83a3',
        'maps-mx-8f31508f90080768',
        'maps-mx-1fb23c275461f9f9',
        'maps-mx-085b1fadcc4ddf1f',
        'maps-mx-e272140733322c5e',
        'maps-mx-0fdef35291bd3fc6',
        'maps-mx-e5f9383958e7133e',
        'maps-mx-2e6e7dbf46e1a231',
        'maps-mx-8304ec8b9be82f05',
        'maps-mx-2b417b7ec80d8804',
        'maps-mx-301b38d8cf2350a2',
        'maps-mx-445789c2f90474aa',
        'maps-mx-085b3ee43be19791',
        'maps-us-0a9b4230d532906f',
        'maps-us-320fd923c6ae27d2',
        'maps-us-7a5c50e75e4d0caa',
        'maps-us-b33d88443b813f42',
        'maps-us-68508d5230a27952',
        'maps-us-6425949efafba8e8',
        'maps-us-3633ec95564f386f',
        'maps-us-13cb05a960f1b76e',
        'maps-us-3583545ed14745e2',
        'maps-es-b4c69b1793149979',
        'maps-it-3ad5ddf96bee67ca',
        'maps-it-71883cf7a6e97f2a',
        'maps-gb-ded01c55d594e8b1',
        'maps-mx-9f51c15fbda8c134',
        'maps-mx-4df6b6bffd900ad4',
        'maps-es-929eb6a16213fee9',
    ];

    public function up(): void
    {
        $this->classify(true);
    }

    public function down(): void
    {
        $this->classify(false);
    }

    private function classify(bool $add): void
    {
        DB::transaction(function () use ($add) {
            $before = [];
            foreach (Center::whereIn('id', self::IDS)->lockForUpdate()->get() as $center) {
                $details = $center->details;
                $categories = $details['categories'];
                if (! in_array('health', $categories, true) || in_array('blood_donation', $categories, true) === $add) {
                    continue;
                }
                $before[$center->id] = $categories;
                $details['categories'] = $add ? array_merge(['blood_donation'], $categories)
                    : array_values(array_diff($categories, ['blood_donation']));
                $center->update(['details' => $details, 'version' => $center->version + 1]);
            }
            if ($before !== []) {
                DB::table('operator_audits')->insert([
                    'operator' => 'migration-blood-donation-category',
                    'action' => $add ? 'blood_category_added' : 'blood_category_removed',
                    'details' => json_encode(['previousCategories' => $before], JSON_THROW_ON_ERROR),
                    'created_at' => now(),
                ]);
            }
        });
    }
};
