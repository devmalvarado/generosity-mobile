<?php

declare(strict_types=1);

header('Content-Type: application/json; charset=utf-8');
header('Cache-Control: no-store');
$given = $_SERVER['HTTP_X_DEPLOY_TOKEN'] ?? '';
if (! is_string($given) || ! hash_equals('{{TOKEN}}', $given)) {
    http_response_code(404);
    exit;
}

try {
    set_time_limit(60);
    $private = dirname(__DIR__).'/private';
    $file = $private.'/data/{{FILENAME}}';
    if (! hash_equals('{{DIGEST}}', hash_file('sha256', $file))) {
        throw new RuntimeException('catalog_digest_mismatch');
    }
    chmod($file, 0600);
    require $private.'/vendor/autoload.php';
    $app = require $private.'/bootstrap/app.php';
    $app->make(Illuminate\Contracts\Console\Kernel::class)->bootstrap();
    $catalog = json_decode(file_get_contents($file), true, 512, JSON_THROW_ON_ERROR);
    $ids = array_column($catalog['centers'], 'id');
    if (count($ids) !== 120 || count(array_unique($ids)) !== 120) {
        throw new RuntimeException('unexpected_batch');
    }
    foreach ($catalog['centers'] as $center) {
        if ($center['verificationStatus'] !== 'imported' || $center['needs'] !== []) {
            throw new RuntimeException('directory_only_required');
        }
    }
    $result = Illuminate\Support\Facades\DB::transaction(function () use ($catalog, $ids, $private) {
        $before = App\Models\Center::orderBy('id')->lockForUpdate()->get();
        if ($before->whereIn('id', $ids)->isNotEmpty()) {
            throw new RuntimeException('batch_ids_exist_review_before_retry');
        }
        $needsBefore = App\Models\Need::count();
        $membershipsBefore = Illuminate\Support\Facades\DB::table('center_memberships')->count();
        $backup = $private.'/.deployments/catalog-expansion-'.gmdate('YmdHis').'-'.bin2hex(random_bytes(4));
        if (! mkdir($backup, 0700, true)) {
            throw new RuntimeException('backup_failed');
        }
        $snapshot = json_encode(['batchIds' => $ids, 'centersBefore' => $before->toArray(), 'needsBefore' => $needsBefore,
            'membershipsBefore' => $membershipsBefore, 'sourceSha256' => '{{DIGEST}}'], JSON_THROW_ON_ERROR | JSON_PRETTY_PRINT);
        if (file_put_contents($backup.'/before.json', $snapshot) === false || ! chmod($backup.'/before.json', 0600)) {
            throw new RuntimeException('backup_failed');
        }
        app(App\Services\CatalogImport::class)->run($catalog, 'github-catalog-expansion-{{DIGEST}}');
        $unchanged = App\Models\Center::whereNotIn('id', $ids)->orderBy('id')->get()->toJson() === $before->toJson();
        if (! $unchanged || App\Models\Center::whereIn('id', $ids)->count() !== 120
            || App\Models\Need::count() !== $needsBefore
            || Illuminate\Support\Facades\DB::table('center_memberships')->count() !== $membershipsBefore) {
            throw new RuntimeException('postcondition_failed');
        }

        return ['imported' => 120, 'totalCenters' => App\Models\Center::count(), 'previousCentersUnchanged' => $before->count(),
            'needsUnchanged' => $needsBefore, 'membershipsUnchanged' => $membershipsBefore, 'sourceSha256' => '{{DIGEST}}'];
    });
    echo json_encode($result, JSON_THROW_ON_ERROR);
} catch (Throwable $error) {
    http_response_code(500);
    // Do not expose database/configuration details through the temporary endpoint.
    echo json_encode(['error' => 'catalog_publication_failed_review_before_retry']);
} finally {
    @unlink(__FILE__);
}
