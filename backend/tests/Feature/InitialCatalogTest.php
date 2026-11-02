<?php

namespace Tests\Feature;

use App\Models\Center;
use App\Services\CatalogImport;
use Illuminate\Foundation\Testing\DatabaseMigrations;
use Illuminate\Support\Facades\DB;
use Illuminate\Validation\ValidationException;
use Tests\TestCase;

class InitialCatalogTest extends TestCase
{
    use DatabaseMigrations;

    private function dataset(string $file): array
    {
        $directory = is_dir('/catalog/initial-catalog') ? '/catalog/initial-catalog' : base_path('../data/initial-catalog');

        return json_decode(file_get_contents($directory.'/'.$file.'.json'), true, 512, JSON_THROW_ON_ERROR);
    }

    public function test_initial_load_preserves_pin_precision_sources_and_legacy_mobile_contract(): void
    {
        $this->assertSame('generosity_test', DB::connection()->getDatabaseName());
        $catalog = $this->dataset('catalog');
        $records = collect($this->dataset('research')['records']);
        $this->assertCount(40, $records);
        $this->assertCount(40, $records->unique('googlePlaceId'));
        $this->assertCount(37, $catalog['centers']);
        foreach ($catalog['centers'] as $center) {
            $source = $records->firstWhere('id', $center['id']);
            $this->assertSame('ready_imported', $source['loadStatus']);
            $this->assertSame('imported', $center['verificationStatus']);
            $this->assertSame([], $center['needs']);
            $this->assertSame($source['latitude'], $center['coordinates']['latitude']);
            $this->assertSame($source['longitude'], $center['coordinates']['longitude']);
            $this->assertSame($source['googleMapsUrl'], $center['googleMapsUrl']);
            parse_str(parse_url($center['googleMapsUrl'], PHP_URL_QUERY), $query);
            $this->assertSame($source['googlePlaceId'], $query['query_place_id']);
            $this->assertSame([$source['latitude'], $source['longitude']], array_map('floatval', explode(',', $query['query'])));
        }
        app(CatalogImport::class)->run($catalog, 'test-initial-catalog');
        app(CatalogImport::class)->run($catalog, 'test-repeat');
        $this->assertDatabaseCount('centers', 37);
        $this->assertDatabaseCount('needs', 0);
        $this->assertDatabaseCount('center_memberships', 0);
        $this->assertSame($catalog['centers'][0]['googleMapsUrl'], Center::findOrFail($catalog['centers'][0]['id'])->details['googleMapsUrl']);
        $this->getJson('/api/v1/centers/'.$catalog['centers'][0]['id'])->assertOk()->assertJsonPath('googleMapsUrl', $catalog['centers'][0]['googleMapsUrl']);
        // Mobile version 1 rejects unknown keys; source metadata stays on the directory API.
        $snapshot = $this->getJson('/api/v1/sync/catalog')->assertOk()->assertJsonCount(37, 'catalog.centers');
        foreach ($snapshot->json('catalog.centers') as $center) {
            $this->assertArrayNotHasKey('googleMapsUrl', $center);
        }
        foreach ($records->where('loadStatus', 'held_for_review') as $record) {
            $this->assertDatabaseMissing('centers', ['id' => $record['id']]);
        }
    }

    public function test_missing_contact_exception_cannot_create_needs_or_claim_verification(): void
    {
        $catalog = $this->dataset('catalog');
        $center = collect($catalog['centers'])->first(fn ($center) => ! array_filter($center['contact']));
        foreach ([['googleMapsUrl' => null], ['googleMapsUrl' => 'https://evil.test/maps/'],
            ['verificationStatus' => 'officially_verified'], ['verificationStatus' => 'visit_signaled'],
            ['needs' => [['id' => 'invented-need']]]] as $invalid) {
            try {
                app(CatalogImport::class)->run(['schemaVersion' => 1, 'centers' => [array_replace($center, $invalid)]], 'test');
                $this->fail('Invalid directory-only listing accepted');
            } catch (ValidationException $error) {
                $this->assertNotEmpty($error->errors());
            }
        }
        $this->assertDatabaseCount('centers', 0);
    }
}
