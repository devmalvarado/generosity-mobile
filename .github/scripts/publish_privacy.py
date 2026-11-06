"""Publish map notices and repair the portal CORS preflight without deploying the API."""
import io
import os
import secrets
from pathlib import Path
from urllib.error import HTTPError
from urllib.request import Request, urlopen

from publish_api import connect, require_env, delete_if_present

NOTICES = ('privacy.es.md', 'privacy.en.md')
ROOT = '/private/resources/legal'


def read(ftp, path):
    body = io.BytesIO()
    ftp.retrbinary('RETR ' + path, body.write)
    return body.getvalue()


def replace(ftp, path, body):
    staged = path + '.new'
    ftp.storbinary('STOR ' + staged, io.BytesIO(body))
    if read(ftp, staged) != body:
        raise RuntimeError('Privacy upload verification failed before replacement')
    ftp.rename(staged, path)


def publish(ftp, release):
    if not release or any(c not in '0123456789-' for c in release):
        raise ValueError('Expected a numeric workflow run and attempt')
    desired = {name: (Path('backend/resources/legal') / name).read_bytes() for name in NOTICES}
    if not all(b'OpenStreetMap' in body for body in desired.values()):
        raise ValueError('Both privacy notices must disclose the map provider')
    previous = {name: read(ftp, ROOT + '/' + name) for name in NOTICES}
    changed = [name for name in NOTICES if previous[name] != desired[name]]
    if not changed:
        print('Privacy notices already current')
        return
    backup = '/private/.deployments/portal-privacy-' + release
    ftp.mkd(backup)
    ftp.sendcmd('SITE CHMOD 700 ' + backup)
    for name in changed:
        target = backup + '/' + name
        ftp.storbinary('STOR ' + target, io.BytesIO(previous[name]))
        if read(ftp, target) != previous[name]:
            raise RuntimeError('Privacy backup verification failed; originals unchanged')
    completed = []
    try:
        for name in changed:
            replace(ftp, ROOT + '/' + name, desired[name])
            completed.append(name)
    except Exception:
        for name in completed:
            replace(ftp, ROOT + '/' + name, previous[name])
        raise
    print('Published privacy notices; backup: ' + backup)


def refresh_cors(ftp, release):
    request = Request('https://api.generosity.global/auth/session', method='OPTIONS', headers={
        'Origin': 'https://generosity.global', 'Access-Control-Request-Method': 'GET',
        'Access-Control-Request-Headers': 'x-generosity-channel',
    })
    with urlopen(request, timeout=30) as response:
        if 'x-generosity-channel' in response.headers.get('Access-Control-Allow-Headers', '').lower():
            print('Portal channel preflight already supported')
            return
    target = '/private/config/cors.php'
    previous = read(ftp, target)
    backup = '/private/.deployments/portal-cors-' + release
    ftp.mkd(backup)
    ftp.sendcmd('SITE CHMOD 700 ' + backup)
    ftp.storbinary('STOR ' + backup + '/cors.php', io.BytesIO(previous))
    if read(ftp, backup + '/cors.php') != previous:
        raise RuntimeError('CORS backup failed; configuration unchanged')
    token = secrets.token_hex(32)
    helper = '_portal_' + secrets.token_hex(12) + '.php'
    script = '''<?php
declare(strict_types=1);
header('Cache-Control: no-store');
if ($_SERVER['REQUEST_METHOD'] !== 'POST' || !hash_equals('__DEPLOY_VALUE__', $_SERVER['HTTP_X_DEPLOY_TOKEN'] ?? '')) { http_response_code(404); exit; }
$private = dirname(__DIR__) . '/private';
$cache = $private . '/bootstrap/cache/config.php';
$backup = $private . '/.deployments/portal-cors-RELEASE/config.php';
$step = 'backup';
try {
    if (is_file($cache) && (!copy($cache, $backup) || !chmod($backup, 0600))) { throw new RuntimeException(); }
    $step = 'config-cache';
    $command = 'cd ' . escapeshellarg($private) . ' && /opt/alt/php84/usr/bin/php artisan config:cache 2>&1';
    exec($command, $output, $code);
    if ($code !== 0 || !chmod($cache, 0600)) { throw new RuntimeException(); }
    $step = 'verify';
    if (function_exists('opcache_invalidate')) { opcache_invalidate($cache, true); opcache_invalidate($private . '/config/cors.php', true); }
    $configuration = require $cache;
    if (!in_array('X-Generosity-Channel', $configuration['cors']['allowed_headers'], true)) { throw new RuntimeException(); }
    echo 'portal_cors_ready';
} catch (Throwable $error) {
    if (is_file($backup)) { copy($backup, $cache); chmod($cache, 0600); }
    http_response_code(500);
    echo 'portal_cors_failed:' . $step . ':' . get_class($error);
} finally { unlink(__FILE__); }
'''.replace('__DEPLOY_VALUE__', token).replace('RELEASE', release)
    try:
        replace(ftp, target, Path('backend/config/cors.php').read_bytes())
        ftp.storbinary('STOR /public_html/' + helper, io.BytesIO(script.encode()))
        request = Request('https://api.generosity.global/' + helper, method='POST', data=b'', headers={'X-Deploy-Token': token})
        try:
            with urlopen(request, timeout=60) as response:
                if response.read() != b'portal_cors_ready':
                    raise RuntimeError('CORS cache refresh did not confirm success')
        except HTTPError as error:
            body = error.read(200).decode('utf-8', errors='replace')
            # Only the helper's fixed stage/class diagnostic is safe for action logs.
            if body.startswith('portal_cors_failed:') and all(c.isalnum() or c in ':_\\' for c in body):
                print(body)
            raise
    finally:
        delete_if_present(ftp, '/public_html/' + helper)
    print('Updated portal CORS configuration; backup: ' + backup)


if __name__ == '__main__':
    with connect(require_env('API_FTP_SERVER'), require_env('API_FTP_USERNAME'), require_env('API_FTP_PASSWORD')) as ftp:
        release = os.environ['GITHUB_RUN_ID'] + '-' + os.environ['GITHUB_RUN_ATTEMPT']
        publish(ftp, release)
        refresh_cors(ftp, release)
