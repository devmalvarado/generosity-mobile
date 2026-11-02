#!/usr/bin/env python3
"""Publish the API release over FTPS.

The FTP account lands in public_html. Application code is uploaded as one
archive to the domain root and extracted to /private. The production .env is
never uploaded and never deleted. Migrations are pending-only; this script
has no migrate:fresh path.
"""

import os
import json
import hashlib
import secrets
import ssl
import sys
import tempfile
import urllib.error
import urllib.request
import zipfile
from ftplib import FTP, FTP_TLS, error_perm
from pathlib import Path

BACKEND = Path("backend")
ORIGIN = "https://api.generosity.global"
SKIP_DIRS = {"tests", "public", "node_modules", ".git"}
SKIP_NAMES = {".env", ".env.example", ".env.backup", ".env.production", ".phpunit.result.cache"}
RUNTIME_PREFIXES = (
    "storage/logs/",
    "storage/framework/cache/data/",
    "storage/framework/sessions/",
    "storage/framework/views/",
    "storage/pail/",
)
CACHE_FILES = {
    "bootstrap/cache/config.php",
    "bootstrap/cache/routes-v7.php",
    "bootstrap/cache/events.php",
}


def say(message):
    print(message, flush=True)


def require_env(name):
    value = os.environ.get(name, "")
    if not value or any(ch.isspace() for ch in value):
        say(f"missing_{name}")
        sys.exit(2)
    return value


class SessionFTP(FTP_TLS):
    def ntransfercmd(self, cmd, rest=None):
        conn, size = FTP.ntransfercmd(self, cmd, rest)
        if self._prot_p:
            conn = self.context.wrap_socket(
                conn, server_hostname=self.host, session=self.sock.session
            )
        return conn, size


def connect(host, user, password):
    ctx = ssl.create_default_context()
    ctx.check_hostname = False
    ctx.verify_mode = ssl.CERT_REQUIRED
    ftp = SessionFTP(context=ctx)
    ftp.connect(host, 21, timeout=180)
    ftp.auth()
    ftp.login(user, password)
    ftp.prot_p()
    ftp.set_pasv(True)
    ftp.cwd("/")
    return ftp


def private_files():
    files = []
    for path in BACKEND.rglob("*"):
        if not path.is_file() or path.name == ".DS_Store":
            continue
        rel = path.relative_to(BACKEND).as_posix()
        parts = set(Path(rel).parts)
        if parts & SKIP_DIRS or path.name in SKIP_NAMES or path.name.startswith(".env"):
            continue
        if rel.startswith(RUNTIME_PREFIXES) and path.name != ".gitignore":
            continue
        if rel in CACHE_FILES:
            continue
        files.append((path, rel))
    return files


def build_zip(destination, import_initial_catalog=False):
    files = private_files()
    if import_initial_catalog:
        catalog = Path("data/initial-catalog/catalog.json")
        document = json.loads(catalog.read_text())
        centers = document.get("centers", [])
        if document.get("schemaVersion") != 1 or len(centers) != 37 or len({c["id"] for c in centers}) != 37 or any(c["verificationStatus"] != "imported" or c["needs"] for c in centers):
            raise SystemExit("initial catalog must contain exactly 37 distinct imported-only listings without needs")
        files.append((catalog, "data/initial-catalog.json"))
    with zipfile.ZipFile(destination, "w", compression=zipfile.ZIP_DEFLATED) as archive:
        for path, rel in files:
            name = f"private/{rel}"
            if ".." in Path(name).parts or not name.startswith("private/"):
                raise SystemExit(f"refusing archive entry {name}")
            archive.write(path, name)
    names = zipfile.ZipFile(destination).namelist()
    banned = [name for name in names if ".env" in name or name.startswith("private/tests/") or name.startswith("private/public/")]
    if banned or len(names) != len(files):
        raise SystemExit("release archive failed its safety check")
    say(f"archive_entries={len(names)} bytes={destination.stat().st_size}")
    return names


def public_index():
    text = (BACKEND / "public" / "index.php").read_text(encoding="utf-8")
    replacements = {
        "__DIR__.'/../storage/framework/maintenance.php'": "__DIR__.'/../private/storage/framework/maintenance.php'",
        "__DIR__.'/../vendor/autoload.php'": "__DIR__.'/../private/vendor/autoload.php'",
        "__DIR__.'/../bootstrap/app.php'": "__DIR__.'/../private/bootstrap/app.php'",
    }
    for old, new in replacements.items():
        if old not in text:
            raise SystemExit("index.php no longer has the expected local path")
        text = text.replace(old, new, 1)
    if "/../vendor/autoload.php" in text or "/../bootstrap/app.php" in text:
        raise SystemExit("index.php still points inside the document root")
    return text


def http_status(url, headers=None, timeout=30):
    request = urllib.request.Request(url, headers=headers or {})
    try:
        with urllib.request.urlopen(request, timeout=timeout) as response:
            return response.status, response.read()
    except urllib.error.HTTPError as exc:
        return exc.code, exc.read()


def delete_if_present(ftp, path):
    try:
        ftp.delete(path)
        say(f"deleted {path}")
    except error_perm:
        say(f"absent {path}")


def main():
    host = require_env("API_FTP_SERVER")
    user = require_env("API_FTP_USERNAME")
    password = require_env("API_FTP_PASSWORD")
    if not (BACKEND / "vendor" / "autoload.php").is_file():
        say("vendor_missing")
        sys.exit(2)

    with tempfile.TemporaryDirectory() as temporary:
        archive_path = Path(temporary) / "api-private.zip"
        import_initial_catalog = os.environ.get("IMPORT_INITIAL_CATALOG", "false") == "true"
        names = build_zip(archive_path, import_initial_catalog)
        catalog_step = ""
        if import_initial_catalog:
            digest = hashlib.sha256(Path("data/initial-catalog/catalog.json").read_bytes()).hexdigest()
            # Explicit workflow action only; keep a private rollback snapshot and refuse overwrites.
            catalog_step = r"""
require $private . '/vendor/autoload.php';
$app = require $private . '/bootstrap/app.php';
$app->make(Illuminate\Contracts\Console\Kernel::class)->bootstrap();
$catalogFile = $private . '/data/initial-catalog.json';
if (!hash_equals('DIGEST', hash_file('sha256', $catalogFile))) { throw new RuntimeException('catalog_digest_mismatch'); }
$catalog = json_decode(file_get_contents($catalogFile), true, 512, JSON_THROW_ON_ERROR);
Illuminate\Support\Facades\DB::transaction(function () use ($catalog, $private) {
    $ids = array_column($catalog['centers'], 'id');
    $existing = App\Models\Center::whereIn('id', $ids)->lockForUpdate()->get();
    if ($existing->isNotEmpty()) { throw new RuntimeException('initial_catalog_ids_already_exist_review_before_retry'); }
    $backup = $private . '/.deployments/initial-catalog-' . gmdate('YmdHis') . '-' . bin2hex(random_bytes(4));
    if (!mkdir($backup, 0700, true)) { throw new RuntimeException('catalog_backup_failed'); }
    $snapshot = json_encode(['batchIds' => $ids, 'existingBatchCenters' => $existing->toArray(), 'totalCentersBefore' => App\Models\Center::count(), 'totalNeedsBefore' => App\Models\Need::count(), 'sourceSha256' => 'DIGEST'], JSON_THROW_ON_ERROR | JSON_PRETTY_PRINT);
    if (file_put_contents($backup . '/before.json', $snapshot) === false || !chmod($backup . '/before.json', 0600)) { throw new RuntimeException('catalog_backup_failed'); }
    $needsBefore = App\Models\Need::count();
    app(App\Services\CatalogImport::class)->run($catalog, 'github-initial-catalog-DIGEST');
    if (App\Models\Center::whereIn('id', $ids)->count() !== 37 || App\Models\Need::count() !== $needsBefore) { throw new RuntimeException('catalog_postcondition_failed'); }
});
echo "initial_catalog_imported=37\n";
""".replace("DIGEST", digest)
        token = secrets.token_hex(32)
        script_name = "_r" + secrets.token_hex(8) + ".php"
        script = f"""<?php
declare(strict_types=1);
header('Content-Type: text/plain; charset=utf-8');
set_time_limit(180);
$given = $_SERVER['HTTP_X_DEPLOY_TOKEN'] ?? '';
if (!is_string($given) || !hash_equals('{token}', $given)) {{
    http_response_code(404);
    exit;
}}
$root = dirname(__DIR__);
$zipPath = $root . '/api-private.zip';
$zip = new ZipArchive();
if ($zip->open($zipPath) !== true) {{
    http_response_code(500);
    echo "open_failed\\n";
    exit;
}}
for ($i = 0; $i < $zip->numFiles; $i++) {{
    $name = $zip->getNameIndex($i);
    if (!is_string($name) || !str_starts_with($name, 'private/') || str_contains($name, '..') || str_ends_with($name, '/.env') || $name === 'private/.env') {{
        $zip->close();
        http_response_code(500);
        echo "bad_entry\\n";
        exit;
    }}
}}
if (!$zip->extractTo($root)) {{
    $zip->close();
    http_response_code(500);
    echo "extract_failed\\n";
    exit;
}}
$zip->close();
echo "extracted\\n";
@chmod($root . '/private/.env', 0600);
foreach (array($root . '/private/storage', $root . '/private/bootstrap/cache') as $dir) {{
    if (is_dir($dir)) {{
        @chmod($dir, 0775);
    }}
}}
@unlink($zipPath);
$bin = '/opt/alt/php84/usr/bin/php';
echo 'cli=' . (is_executable($bin) ? 'yes' : 'no') . "\\n";
if (!is_executable($bin)) {{
    @unlink(__FILE__);
    exit;
}}
$private = $root . '/private';
foreach (array(array('migrate', '--force'), array('config:cache'), array('route:cache'), array('view:cache')) as $args) {{
    $parts = array_merge(array($bin, $private . '/artisan'), $args);
    $cmd = 'cd ' . escapeshellarg($private) . ' && ' . implode(' ', array_map('escapeshellarg', $parts)) . ' 2>&1';
    $out = array();
    $code = 1;
    exec($cmd, $out, $code);
    echo $args[0] . ' exit=' . $code . "\\n";
    foreach ($out as $line) {{
        echo $line . "\\n";
    }}
    if ($code !== 0) {{
        @unlink(__FILE__);
        exit;
    }}
}}
{catalog_step}
@unlink(__FILE__);
echo "cleaned\\n";
"""
        if "migrate:fresh" in script or "DB_PASSWORD" in script:
            raise SystemExit("refusing to upload an unsafe release script")
        script_path = Path(temporary) / script_name
        script_path.write_text(script, encoding="utf-8")

        ftp = connect(host, user, password)
        with archive_path.open("rb") as handle:
            ftp.storbinary("STOR /api-private.zip", handle)
        if ftp.size("/api-private.zip") != archive_path.stat().st_size:
            delete_if_present(ftp, "/api-private.zip")
            ftp.quit()
            raise SystemExit("remote archive size mismatch")
        status, body = http_status(ORIGIN + "/api-private.zip")
        say(f"archive_url_status={status}")
        if status == 200 and body.startswith(b"PK"):
            delete_if_present(ftp, "/api-private.zip")
            ftp.quit()
            raise SystemExit("archive was web-accessible")
        with script_path.open("rb") as handle:
            ftp.storbinary(f"STOR /public_html/{script_name}", handle)
        ftp.quit()

        status, body = http_status(
            ORIGIN + "/" + script_name,
            headers={"X-Deploy-Token": token},
            timeout=150,
        )
        say(f"release_status={status}")
        say(body.decode("utf-8", "replace"))

        ftp = connect(host, user, password)
        delete_if_present(ftp, f"/public_html/{script_name}")
        delete_if_present(ftp, "/api-private.zip")
        if b"cleaned" not in body:
            ftp.quit()
            raise SystemExit("release did not finish")
        listed = []
        ftp.retrlines("LIST /private/.env", listed.append)
        if not listed or not listed[0].startswith("-rw-------"):
            ftp.quit()
            raise SystemExit("production env is missing or not private")
        say("env_private")
        index = public_index().encode()
        robots = b"User-agent: *\nDisallow: /\n"
        htaccess = (BACKEND / "public" / ".htaccess").read_bytes()
        for name, payload in (("index.php", index), (".htaccess", htaccess), ("robots.txt", robots)):
            with tempfile.NamedTemporaryFile() as handle:
                handle.write(payload)
                handle.flush()
                handle.seek(0)
                ftp.storbinary(f"STOR /public_html/{name}", handle)
        try:
            ftp.mkd("/public_html/auth-assets")
        except error_perm:
            ftp.cwd("/public_html/auth-assets")
            ftp.cwd("/")
        for asset in sorted((BACKEND / "public" / "auth-assets").iterdir()):
            if not asset.is_file() or asset.suffix not in {".png", ".woff2", ".txt", ".md"}:
                raise SystemExit("unexpected authentication asset")
            with asset.open("rb") as handle:
                ftp.storbinary(f"STOR /public_html/auth-assets/{asset.name}", handle)
        delete_if_present(ftp, "/public_html/default.php")
        public = []
        ftp.retrlines("NLST /public_html", public.append)
        say("public " + ",".join(sorted(item.split("/")[-1] for item in public)))
        ftp.quit()
        say(f"published entries={len(names)}")


if __name__ == "__main__":
    main()
