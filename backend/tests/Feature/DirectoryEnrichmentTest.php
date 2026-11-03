<?php

namespace Tests\Feature;

use App\Exceptions\ApiProblem;
use App\Models\Center;
use App\Services\CatalogImport;
use Illuminate\Foundation\Testing\DatabaseMigrations;
use Illuminate\Support\Facades\DB;
use Illuminate\Validation\ValidationException;
use Tests\TestCase;

class DirectoryEnrichmentTest extends TestCase
{
    use DatabaseMigrations;

    private function center(): array
    {
        return [
            'id' => 'synthetic-directory', 'name' => ['es' => 'Centro sintético'],
            'description' => ['es' => 'Apoyo alimentario.'], 'address' => ['es' => 'Dirección de prueba'],
            'categories' => ['community_kitchen'], 'coordinates' => ['latitude' => 21.88, 'longitude' => -102.29],
            'contact' => ['phone' => null, 'email' => 'contact@example.org', 'website' => null],
            'verificationStatus' => 'imported', 'importedAtIsoDate' => '2026-11-02', 'needs' => [],
            'fundingOrigin' => 'mixed', 'administrationType' => 'private',
            'directoryProfile' => [
                'fundingSources' => ['donations', 'public_support'], 'legalName' => 'Centro Sintético, A.C.',
                'evidence' => [[
                    'fields' => ['fundingOrigin', 'administrationType', 'fundingSources', 'legalName'],
                    'url' => 'https://example.org/about', 'reviewedOn' => now()->toDateString(),
                    'statement' => 'Declared public and private support; not an audited financial statement.',
                ]],
            ],
        ];
    }

    private function review(): array
    {
        return ['sourceName' => 'Synthetic.xlsx', 'sourceSha256' => str_repeat('a', 64), 'rows' => [[
            'sheet' => 'Organizations', 'rowNumber' => 2, 'status' => 'pending', 'centerId' => null,
            'sourceData' => ['Name' => 'Pending center', 'Private contact' => 'private@example.org'],
            'reviewNotes' => ['Location is unconfirmed.'], 'sourceUrls' => [],
        ]]];
    }

    public function test_enrichment_persists_without_leaking_private_rows_or_breaking_mobile(): void
    {
        $this->assertSame('generosity_test', DB::connection()->getDatabaseName());
        $center = $this->center();
        app(CatalogImport::class)->run(['schemaVersion' => 1, 'centers' => [$center], 'reviewBatch' => $this->review()], 'test');
        $this->assertDatabaseHas('centers', ['id' => $center['id'], 'funding_origin' => 'mixed', 'administration_type' => 'private']);
        $this->assertDatabaseCount('centers', 1);
        $this->assertDatabaseCount('catalog_import_rows', 1);
        $this->getJson('/api/v1/centers/'.$center['id'])->assertOk()
            ->assertJsonPath('fundingOrigin', 'mixed')->assertJsonPath('administrationType', 'private')
            ->assertJsonPath('directoryProfile.legalName', 'Centro Sintético, A.C.')
            ->assertDontSee('private@example.org');
        $snapshot = $this->getJson('/api/v1/sync/catalog?category_version=2')->assertOk();
        $this->assertArrayNotHasKey('directoryProfile', $snapshot->json('catalog.centers.0'));
        $this->assertArrayNotHasKey('fundingOrigin', $snapshot->json('catalog.centers.0'));
        $snapshot->assertDontSee('private@example.org');

        unset($center['fundingOrigin'], $center['administrationType'], $center['directoryProfile']);
        app(CatalogImport::class)->run(['schemaVersion' => 1, 'centers' => [$center], 'reviewBatch' => $this->review()], 'legacy');
        $this->assertSame('mixed', Center::findOrFail($center['id'])->funding_origin);
        $this->assertSame('Centro Sintético, A.C.', Center::findOrFail($center['id'])->details['directoryProfile']['legalName']);
        $this->assertDatabaseCount('catalog_import_rows', 1);
    }

    public function test_invalid_values_and_private_profile_keys_reject_whole_batch(): void
    {
        foreach (['invalid_origin', 'no_evidence', 'private_contact', 'invalid_url', 'bad_reference', 'duplicate_row'] as $case) {
            $center = $this->center();
            $review = $this->review();
            match ($case) {
                'invalid_origin' => $center['fundingOrigin'] = 'donataria',
                'no_evidence' => $center['directoryProfile']['evidence'] = [],
                'private_contact' => $center['directoryProfile']['responsiblePhone'] = 'private',
                'invalid_url' => $center['directoryProfile']['socialLinks'] = ['javascript:alert(1)'],
                'bad_reference' => $review['rows'][0]['centerId'] = 'missing',
                'duplicate_row' => $review['rows'][] = $review['rows'][0],
            };
            try {
                app(CatalogImport::class)->run(['schemaVersion' => 1, 'centers' => [$center], 'reviewBatch' => $review], 'test');
                $this->fail('Accepted invalid case: '.$case);
            } catch (ValidationException) {
                $this->assertDatabaseCount('centers', 0);
                $this->assertDatabaseCount('catalog_import_rows', 0);
            }
        }
    }

    public function test_pending_only_batches_and_conflicts_preserve_staff_edits(): void
    {
        app(CatalogImport::class)->run(['schemaVersion' => 1, 'centers' => [], 'reviewBatch' => $this->review()], 'test');
        $this->assertDatabaseCount('centers', 0);
        $center = $this->center();
        app(CatalogImport::class)->run(['schemaVersion' => 1, 'centers' => [$center]], 'test');
        $before = Center::findOrFail($center['id']);
        $before->update(['version' => 8]);
        $center['expectedVersion'] = 1;
        try {
            app(CatalogImport::class)->run(['schemaVersion' => 1, 'centers' => [$center]], 'stale');
            $this->fail('Stale import overwrote a newer center.');
        } catch (ApiProblem) {
            $this->assertSame(8, Center::findOrFail($center['id'])->version);
        }
    }

    public function test_reviewed_aguascalientes_batch_imports_only_resolved_places(): void
    {
        $root = is_dir('/catalog') ? '/catalog' : base_path('../data');
        $batch = json_decode(file_get_contents($root.'/aguascalientes-directory-2026-11-02/catalog.json'), true, 512, JSON_THROW_ON_ERROR);
        $report = json_decode(file_get_contents($root.'/aguascalientes-directory-2026-11-02/reconciliation.json'), true, 512, JSON_THROW_ON_ERROR);
        $this->assertCount(131, $report['rows']);
        $this->assertCount(17, $batch['centers']);
        foreach ($batch['centers'] as $center) {
            $this->assertSame('imported', $center['verificationStatus']);
            $this->assertSame([], $center['needs']);
            if ($center['expectedVersion'] > 0) {
                Center::create(['id' => $center['id'], 'details' => [], 'version' => $center['expectedVersion']]);
            } else {
                preg_match('/!3d(-?[\d.]+)!4d(-?[\d.]+)/', $center['googleMapsUrl'], $pin);
                $this->assertSame((float) $pin[1], $center['coordinates']['latitude']);
                $this->assertSame((float) $pin[2], $center['coordinates']['longitude']);
            }
        }
        app(CatalogImport::class)->run($batch, 'reviewed-fixture');
        $this->assertDatabaseCount('centers', 17);
        $this->assertDatabaseCount('needs', 0);
        $pending = array_filter($report['rows'], fn ($row) => $row['status'] === 'pending');
        $this->assertCount(112, $pending);
        foreach ($pending as $row) {
            $this->assertNull($row['centerId']);
        }
    }
}
