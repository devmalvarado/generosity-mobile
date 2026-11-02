<?php

namespace Tests\Feature;

use App\Models\Center;
use App\Services\CatalogImport;
use Illuminate\Foundation\Testing\DatabaseMigrations;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Arr;
use Tests\TestCase;

class BloodDonationCategoryTest extends TestCase
{
    use DatabaseMigrations;

    public function test_reviewed_blood_services_are_corrected_without_touching_other_fields_and_old_clients_keep_working(): void
    {
        $this->assertSame('generosity_test', DB::connection()->getDatabaseName());
        $directory = is_dir('/catalog') ? '/catalog' : base_path('../data');
        $expected = [];
        foreach (['initial-catalog', 'catalog-expansion-2026-11-01'] as $batch) {
            $document = json_decode(file_get_contents($directory.'/'.$batch.'/catalog.json'), true, 512, JSON_THROW_ON_ERROR);
            $research = collect(json_decode(file_get_contents($directory.'/'.$batch.'/research.json'), true, 512, JSON_THROW_ON_ERROR)['records'])->keyBy('id');
            foreach ($document['centers'] as &$center) {
                $blood = $research[$center['id']]['researchCategory'] === 'blood_bank';
                $this->assertSame($blood, in_array('blood_donation', $center['categories'], true));
                if ($blood) {
                    $expected[] = $center['id'];
                    $center['categories'] = ['health']; // Reproduce the originally published rows.
                }
            }
            unset($center);
            app(CatalogImport::class)->run($document, 'test-blood-correction');
        }
        $this->assertCount(39, $expected);
        $before = Center::all()->keyBy('id');
        $migration = require database_path('migrations/2026_11_02_000001_classify_reviewed_blood_donation_centers.php');
        $migration->up();
        $once = Center::orderBy('id')->get()->toArray();
        $migration->up();
        $this->assertSame($once, Center::orderBy('id')->get()->toArray());
        foreach (Center::all() as $center) {
            if (in_array($center->id, $expected, true)) {
                $this->assertSame(['blood_donation', 'health'], $center->details['categories']);
                $this->assertSame(Arr::except($before[$center->id]->details, ['categories']), Arr::except($center->details, ['categories']));
                $this->assertSame($before[$center->id]->version + 1, $center->version);
            } else {
                $this->assertEquals($before[$center->id]->toArray(), $center->toArray());
            }
        }
        $this->assertDatabaseCount('centers', 157);
        $this->assertDatabaseCount('needs', 0);
        $this->assertDatabaseCount('center_memberships', 0);
        $this->assertSame(1, DB::table('operator_audits')->where('action', 'blood_category_added')->count());
        $legacy = $this->getJson('/api/v1/sync/catalog')->assertOk()->json('catalog.centers');
        $modern = $this->getJson('/api/v1/sync/catalog?category_version=2')->assertOk()->json('catalog.centers');
        $this->assertCount(157, $legacy);
        $this->assertCount(157, $modern);
        $this->assertCount(0, array_filter($legacy, fn ($center) => in_array('blood_donation', $center['categories'])));
        $this->assertEqualsCanonicalizing($expected, array_column(array_filter($modern, fn ($center) => in_array('blood_donation', $center['categories'])), 'id'));
        foreach ($modern as $center) {
            $this->assertArrayNotHasKey('googleMapsUrl', $center);
            $this->assertSame([], $center['needs']);
        }
        $this->getJson('/api/v1/sync/catalog?category_version=99')->assertStatus(422);
        $this->getJson('/api/v1/centers/'.$expected[0])->assertOk()->assertJsonPath('categories', ['blood_donation', 'health']);
        $migration->down();
        foreach (Center::all() as $center) {
            $this->assertEquals($before[$center->id]->details, $center->details);
        }
    }

    public function test_new_blood_only_listing_has_a_nonempty_legacy_health_category(): void
    {
        Center::create(['id' => 'new-blood-service', 'details' => ['categories' => ['blood_donation']]]);
        $this->getJson('/api/v1/sync/catalog')->assertOk()->assertJsonPath('catalog.centers.0.categories', ['health']);
        $this->getJson('/api/v1/sync/catalog?category_version=2')->assertOk()->assertJsonPath('catalog.centers.0.categories', ['blood_donation']);
    }
}
