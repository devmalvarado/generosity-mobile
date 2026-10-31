#!/usr/bin/env python3
"""Exercise only the local Compose API, mail sink, restart and isolated restore."""
import html
import json
import os
import re
import secrets
import subprocess
import tempfile
import time
import urllib.error
import urllib.request
import uuid

os.chdir(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
BASE = 'http://localhost:8088'
MAIL = 'http://localhost:8028'


def compose(*args, data=None):
    return subprocess.run(['docker', 'compose', *args], input=data, stdout=subprocess.PIPE,
                          stderr=subprocess.PIPE, check=True).stdout


def request(path, body=None, token=None, key=None, base=BASE):
    headers = {'Accept': 'application/json'}
    if token:
        headers['Authorization'] = 'Bearer ' + token
    if key:
        headers['Idempotency-Key'] = key
    if body is not None:
        headers['Content-Type'] = 'application/json'
    req = urllib.request.Request(base + path, data=json.dumps(body).encode() if body is not None else None, headers=headers)
    with urllib.request.urlopen(req, timeout=15) as response:
        return json.load(response)


suffix = uuid.uuid4().hex[:12]
email = 'smoke-' + suffix + '@example.test'
password = secrets.token_urlsafe(24) + 'Aa1'
assert request('/api/v1/health')['status'] == 'ready'
request('/api/v1/auth/register', {'name': 'Local smoke check', 'email': email, 'password': password})
compose('exec', '-T', 'app', 'php', 'artisan', 'queue:work', '--stop-when-empty', '--max-time=30', '--tries=3')
messages = request('/api/v1/messages', base=MAIL)['messages']
message = next(item for item in messages if any(recipient['Address'] == email for recipient in item['To']))
body = request('/api/v1/message/' + message['ID'], base=MAIL)
link = re.search(r'http://localhost:8088/api/v1/auth/verify/[^\s<>"\)]+', html.unescape(body.get('Text', '') + body.get('HTML', ''))).group()
assert request(link.removeprefix(BASE))['status'] == 'email_verified'
token = request('/api/v1/auth/login', {'email': email, 'password': password, 'device_name': 'local-smoke'})['token']
compose('exec', '-T', 'app', 'php', 'artisan', 'center:staff', 'kc-children-001', email, '--operator=local-smoke')
need = request('/api/v1/staff/centers/kc-children-001/needs', {
    'title': {'en': 'Local backup check'}, 'description': {'en': 'Development fixture'},
    'helpMethods': ['goods'], 'urgent': False, 'quantityRequired': 2, 'unit': 'kit', 'status': 'published',
}, token, str(uuid.uuid4()))
request('/api/v1/contributions', {'need_id': need['id'], 'method': 'goods', 'quantity': 1}, token, str(uuid.uuid4()))
key = str(uuid.uuid4())
activity = {'method': 'time', 'minutes': 30, 'activity': 'Local recovery check'}
offer = request('/api/v1/contributions', activity, token, key)
assert offer['provenance'] == 'self_reported'
request('/api/v1/contributions/' + offer['id'] + '/transition', {'action': 'complete', 'version': 1}, token, str(uuid.uuid4()))
compose('restart', 'app')
for attempt in range(30):
    try:
        request('/api/v1/health')
        break
    except (urllib.error.URLError, ConnectionError):
        time.sleep(0.5)
else:
    raise RuntimeError('API did not recover after restart')
assert request('/api/v1/contributions', activity, token, key) == offer
assert request('/api/v1/contributions/' + offer['id'], token=token)['contribution']['status'] == 'fulfilled'
assert request('/api/v1/needs/' + need['id'])['quantityCommitted'] == 1

# Compare restored business tables byte-for-byte, including hashed identities and operations.
database = 'generosity_restore_' + suffix
dump_args = ('--single-transaction', '--skip-comments', '--skip-extended-insert', '--compact', '--order-by-primary', '--skip-add-locks')
tables = ('users', 'centers', 'needs', 'center_memberships', 'contributions', 'contribution_events', 'operations', 'operator_audits', 'personal_access_tokens')
dump = compose('exec', '-T', 'db', 'mariadb-dump', '-uroot', '-plocal-test-root-only', *dump_args, 'generosity', *tables)
compose('exec', '-T', 'db', 'mariadb', '-uroot', '-plocal-test-root-only', '-e', 'CREATE DATABASE ' + database)
try:
    with tempfile.TemporaryFile() as backup:
        backup.write(dump)
        backup.seek(0)
        compose('exec', '-T', 'db', 'mariadb', '-uroot', '-plocal-test-root-only', database, data=backup.read())
    restored = compose('exec', '-T', 'db', 'mariadb-dump', '-uroot', '-plocal-test-root-only', *dump_args, database, *tables)
    assert dump == restored, 'Restored identities, reservations, history or operation outcomes differ'
finally:
    compose('exec', '-T', 'db', 'mariadb', '-uroot', '-plocal-test-root-only', '-e', 'DROP DATABASE ' + database)
print('PASS: HTTP registration, local email verification, login, contribution, restart, idempotent replay and isolated database restore.')
