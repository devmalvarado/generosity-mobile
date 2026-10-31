<?php

namespace App\Console\Commands;

use App\Exceptions\ApiProblem;
use App\Services\CatalogImport;
use Illuminate\Console\Command;
use Illuminate\Validation\ValidationException;

class ImportCatalog extends Command
{
    protected $signature = 'catalog:import {file} {--operator= : Required operator label for the audit log}';

    protected $description = 'Atomically upsert a reviewed schemaVersion 1 catalog; never delete missing records';

    public function handle(CatalogImport $import): int
    {
        $file = $this->argument('file');
        if (! $this->option('operator') || strlen($this->option('operator')) > 100 || ! is_file($file) || filesize($file) > 10 * 1024 * 1024) {
            $this->error('Supply an operator label and a catalog file smaller than 10 MB.');

            return self::FAILURE;
        }
        try {
            $import->run(json_decode(file_get_contents($file), true, 512, JSON_THROW_ON_ERROR), $this->option('operator'));
        } catch (\JsonException|ValidationException|ApiProblem|\TypeError $e) {
            $this->error('Import rejected: '.$e->getMessage());

            return self::FAILURE;
        }
        $this->info('Catalog committed. Existing reservations and unpublished states were preserved.');

        return self::SUCCESS;
    }
}
