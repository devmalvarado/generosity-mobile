#!/usr/bin/env python3
"""Explicit, insert-only publication of the reviewed 2026-11-01 expansion."""

import hashlib
import io
import json
import os
import secrets
from pathlib import Path

from publish_api import ORIGIN, connect, delete_if_present, http_status, require_env

BATCH = Path('data/catalog-expansion-2026-11-01')


def reviewed_payload():
    payload = (BATCH / 'catalog.json').read_bytes()
    document = json.loads(payload)
    records = json.loads((BATCH / 'research.json').read_text())['records']
    eligible = {r['id']: r for r in records if r['loadStatus'] == 'ready_for_import'}
    centers = document.get('centers', [])
    ids = [c['id'] for c in centers]
    if document.get('schemaVersion') != 1 or len(ids) != 120 or len(set(ids)) != 120 or set(ids) != set(eligible):
        raise ValueError('Expected the 120 reviewed, distinct center IDs')
    for c in centers:
        r = eligible[c['id']]
        if (c['verificationStatus'] != 'imported' or c['needs'] != []
                or c['coordinates'] != {'latitude': r['latitude'], 'longitude': r['longitude']}
                or c['googleMapsUrl'] != r['googleMapsUrl'] or c['contact'] != r['contact']
                or c['address']['es'] != r['address']):
            raise ValueError('Catalog differs from reviewed source')
    return payload


def release_script(digest, token, filename):
    text = Path('.github/scripts/import_catalog.php').read_text()
    for key, value in [('DIGEST', digest), ('TOKEN', token), ('FILENAME', filename)]:
        text = text.replace('{{' + key + '}}', value)
    return text.encode()


def main():
    if os.environ.get('CONFIRM_CATALOG_EXPANSION') != 'true':
        raise SystemExit('Explicit workflow confirmation is required')
    payload = reviewed_payload()
    digest = hashlib.sha256(payload).hexdigest()
    token = secrets.token_hex(32)
    filename = 'catalog-expansion-' + digest + '.json'
    endpoint = '_catalog_' + secrets.token_hex(12) + '.php'
    credentials = [require_env(k) for k in ('API_FTP_SERVER', 'API_FTP_USERNAME', 'API_FTP_PASSWORD')]
    ftp = connect(*credentials)
    try:
        # Existing API release layout: /private is a sibling of /public_html.
        ftp.cwd('/private/data')
        ftp.cwd('/')
        ftp.storbinary('STOR /private/data/' + filename, io.BytesIO(payload))
        if ftp.size('/private/data/' + filename) != len(payload):
            raise RuntimeError('Dataset upload size mismatch')
        ftp.storbinary('STOR /public_html/' + endpoint, io.BytesIO(release_script(digest, token, filename)))
        status, body = http_status(ORIGIN + '/' + endpoint, headers={'X-Deploy-Token': token}, timeout=90)
        print('release_status=' + str(status), flush=True)
        result = json.loads(body)
        if status != 200 or result.get('imported') != 120 or result.get('sourceSha256') != digest:
            raise RuntimeError('Import not confirmed; inspect before retrying')
        print(json.dumps(result), flush=True)
    finally:
        delete_if_present(ftp, '/public_html/' + endpoint)
        ftp.quit()
    status, _ = http_status(ORIGIN + '/' + endpoint)
    if status != 404:
        raise RuntimeError('Temporary release endpoint remains reachable')
    for path in ('/private/data/' + filename, '/data/' + filename):
        status, _ = http_status(ORIGIN + path)
        if status not in (403, 404):
            raise RuntimeError('Dataset unexpectedly accessible through web root')
    print('temporary_endpoint_removed; dataset_private', flush=True)


if __name__ == '__main__':
    main()
