<?php

namespace Tests\Feature;

use App\Models\Center;
use App\Services\CatalogImport;
use Illuminate\Foundation\Testing\DatabaseMigrations;
use Illuminate\Support\Facades\DB;
use Tests\TestCase;

class CatalogExpansionTest extends TestCase
{
    use DatabaseMigrations;

    private function dataset(string $path): array
    {
        $directory = is_dir('/catalog') ? '/catalog' : base_path('../data');

        return json_decode(file_get_contents($directory.'/'.$path.'.json'), true, 512, JSON_THROW_ON_ERROR);
    }

    public function test_expansion_preserves_source_pins_review_exclusions_and_search_coverage(): void
    {
        $batch = 'catalog-expansion-2026-11-01/';
        $records = collect($this->dataset($batch.'research')['records']);
        $catalog = $this->dataset($batch.'catalog')['centers'];
        $coverage = $this->dataset($batch.'coverage')['areas'];
        $searches = $this->dataset($batch.'searches')['searches'];
        $this->assertCount(132, $records);
        $this->assertCount(132, $records->unique('id'));
        $this->assertCount(132, $records->unique('googleFeatureId'));
        $this->assertCount(120, $catalog);
        $this->assertCount(12, $records->where('loadStatus', 'held_for_review'));
        $this->assertCount(132, $coverage);
        $this->assertCount(33, collect($coverage)->unique('area'));
        $this->assertCount(132, collect($coverage)->unique(fn ($item) => $item['area'].'/'.$item['category']));
        $this->assertCount(156, $searches);

        foreach ($records as $source) {
            preg_match('/!3d(-?[\d.]+)!4d(-?[\d.]+)/', urldecode($source['sourceGoogleMapsUrl']), $pin);
            $this->assertCount(3, $pin, $source['name']);
            $this->assertSame([$source['latitude'], $source['longitude']], [(float) $pin[1], (float) $pin[2]]);
            $this->assertContains($source['sourceGoogleMapsUrl'], $source['sources']);
            $this->assertSame('not_informed', $source['fundingOrigin']);
            $this->assertNull($source['donationAcceptance']);
            $this->assertNotSame('pending_review', $source['loadStatus']);
            if ($source['searchArea'] === 'vaticano') {
                $this->assertSame('IT', $source['country']);
                $this->assertSame('nearby_or_outside', $source['locationRelationToSearch']);
                $this->assertStringContainsString('Italia', $source['address']);
            }
            if ($source['loadStatus'] === 'held_for_review') {
                $this->assertNotEmpty($source['reviewNotes']);
            }
        }
        foreach ($catalog as $center) {
            $source = $records->firstWhere('id', $center['id']);
            $this->assertNotNull($source);
            $this->assertSame('ready_for_import', $source['loadStatus']);
            $this->assertSame('imported', $center['verificationStatus']);
            $this->assertSame([], $center['needs']);
            $this->assertSame($source['address'], $center['address']['es']);
            $this->assertSame($source['contact'], $center['contact']);
            $this->assertSame($source['latitude'], $center['coordinates']['latitude']);
            $this->assertSame($source['longitude'], $center['coordinates']['longitude']);
            $this->assertSame($source['googleMapsUrl'], $center['googleMapsUrl']);
            if ($source['googlePlaceId'] !== null) {
                parse_str(parse_url($center['googleMapsUrl'], PHP_URL_QUERY), $query);
                $this->assertSame($source['googlePlaceId'], $query['query_place_id']);
                $this->assertSame([$source['latitude'], $source['longitude']], array_map('floatval', explode(',', $query['query'])));
            }
            $this->assertStringNotContainsString('Cerrado permanentemente', $source['searchSummary']);
        }
        foreach ($coverage as $item) {
            $this->assertGreaterThan(0, $item['queryCount']);
            $this->assertSame($item['queryCount'], collect($searches)->where('area', $item['area'])->where('category', $item['category'])->count());
            foreach (array_merge($item['readyIds'], $item['heldIds'], $item['relatedIds']) as $id) {
                $this->assertNotNull($records->firstWhere('id', $id));
            }
        }
    }

    public function test_repeated_import_keeps_previous_batch_and_mobile_contract_intact(): void
    {
        $this->assertSame('generosity_test', DB::connection()->getDatabaseName());
        $old = $this->dataset('initial-catalog/catalog');
        $new = $this->dataset('catalog-expansion-2026-11-01/catalog');
        app(CatalogImport::class)->run($old, 'test-initial-catalog');
        $before = Center::all()->mapWithKeys(fn ($center) => [$center->id => $center->details])->all();
        app(CatalogImport::class)->run($new, 'test-expanded-catalog');
        app(CatalogImport::class)->run($new, 'test-expanded-catalog-repeat');
        $this->assertDatabaseCount('centers', 157);
        $this->assertDatabaseCount('needs', 0);
        $this->assertDatabaseCount('center_memberships', 0);
        foreach ($before as $id => $details) {
            $this->assertSame($details, Center::findOrFail($id)->details);
        }
        foreach ($this->dataset('catalog-expansion-2026-11-01/research')['records'] as $source) {
            if ($source['loadStatus'] === 'held_for_review') {
                $this->assertDatabaseMissing('centers', ['id' => $source['id']]);
            }
        }
        $snapshot = $this->getJson('/api/v1/sync/catalog')->assertOk()->assertJsonCount(157, 'catalog.centers');
        foreach ($snapshot->json('catalog.centers') as $center) {
            $this->assertArrayNotHasKey('googleMapsUrl', $center);
            $this->assertArrayNotHasKey('fundingOrigin', $center);
        }
        $example = $new['centers'][0];
        $this->getJson('/api/v1/centers/'.$example['id'])->assertOk()
            ->assertJsonPath('googleMapsUrl', $example['googleMapsUrl']);
    }
}
