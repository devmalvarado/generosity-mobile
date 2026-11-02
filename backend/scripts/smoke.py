#!/usr/bin/env python3
"""Exercise only the local Compose API, mail sink, restart and isolated restore."""
import html
import http.cookiejar
import json
import os
import re
import secrets
import subprocess
import tempfile
import time
import urllib.error
import urllib.request
import urllib.parse
import uuid
from datetime import datetime, timezone

os.chdir(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
BASE = 'http://localhost:8088'
MAIL = 'http://localhost:8028'


def compose(*args, data=None):
    return subprocess.run(['docker', 'compose', *args], input=data, stdout=subprocess.PIPE,
                          stderr=subprocess.PIPE, check=True).stdout


def request(path, body=None, token=None, key=None, base=BASE, expected=None):
    headers = {'Accept': 'application/json'}
    if token:
        headers['Authorization'] = 'Bearer ' + token
    if key:
        headers['Idempotency-Key'] = key
    if body is not None:
        headers['Content-Type'] = 'application/json'
    req = urllib.request.Request(base + path, data=json.dumps(body).encode() if body is not None else None, headers=headers)
    try:
        response = urllib.request.urlopen(req, timeout=15)
    except urllib.error.HTTPError as error:
        if expected != error.code:
            raise
        response = error
    with response:
        if expected is not None:
            assert response.status == expected
        return json.load(response)


def delivered_link(email, path):
    compose('exec', '-T', 'app', 'php', 'artisan', 'queue:work', '--stop-when-empty', '--max-time=30', '--tries=3')
    messages = request('/api/v1/messages', base=MAIL)['messages']
    for message in messages:
        if any(recipient['Address'] == email for recipient in message['To']):
            body = request('/api/v1/message/' + message['ID'], base=MAIL)
            links = re.findall(r'href=["\']([^"\']+)["\']', body.get('HTML', ''))
            for link in links:
                link = html.unescape(link)
                if link.startswith(BASE + path):
                    return link
    raise AssertionError('Expected account message did not arrive in local Mailpit')


def register(email, password):
    request('/api/v1/auth/register', {'adult_confirmed': True, 'terms_accepted': True, 'privacy_acknowledged': True,
        'policy_version': '2026-11-01', 'name': 'Local smoke check', 'email': email, 'password': password})
    link = delivered_link(email, '/verify-email/')
    with urllib.request.urlopen(link, timeout=15) as response:
        assert response.status == 200 and b'<html' in response.read()
    return request('/api/v1/auth/login', {'email': email, 'password': password, 'device_name': 'local-smoke'})['token']


class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        return None


cookies = http.cookiejar.CookieJar()
browser = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(cookies), NoRedirect())
portal_origin = 'http://localhost:47321'


def web(path, body=None, headers=None, expected=200):
    outgoing = {'Accept': 'application/json', 'Origin': portal_origin, **(headers or {})}
    data = urllib.parse.urlencode(body).encode() if body is not None else None
    req = urllib.request.Request(BASE + path, data=data, headers=outgoing)
    try:
        response = browser.open(req, timeout=15)
    except urllib.error.HTTPError as error:
        response = error
    with response:
        assert response.status == expected, (path.split('?')[0], response.status, expected)
        content = response.read()
        return response.headers, json.loads(content) if 'application/json' in response.headers.get('Content-Type', '') else content


suffix = uuid.uuid4().hex[:12]
email = 'smoke-' + suffix + '@example.test'
password = secrets.token_urlsafe(24) + 'Aa1'
settings = json.loads(compose('exec', '-T', 'app', 'php', '-r', '''require 'vendor/autoload.php';
$app = require 'bootstrap/app.php'; $app->make(Illuminate\\Contracts\\Console\\Kernel::class)->bootstrap();
echo json_encode([app()->environment(), config('mail.default'), config('mail.mailers.smtp.host')]);'''))
assert settings == ['local', 'smtp', 'mailpit'], 'Smoke checks require local Compose and the Mailpit sink'
assert request('/api/v1/health')['status'] == 'ready'
token = register(email, password)
staff_email = 'staff-' + suffix + '@example.test'
staff_password = secrets.token_urlsafe(24) + 'Aa1'
staff_token = register(staff_email, staff_password)
compose('exec', '-T', 'app', 'php', 'artisan', 'center:staff', 'kc-children-001', staff_email, '--operator=local-smoke')
csrf = web('/auth/session')[1]['csrf_token']
web('/auth/password', {'email': staff_email, 'password': staff_password, '_token': csrf, 'client': 'web', 'locale': 'es'}, expected=302)
session = web('/auth/session')[1]
csrf = session['csrf_token']
assert session['user']['email'] == staff_email
assert web('/api/v1/staff/centers')[1]['centers'][0]['id'] == 'kc-children-001'
assert web('/api/v1/me/center-applications')[1]['total'] == 0
web('/api/v1/contributions', {'method': 'goods', 'quantity': 1}, expected=419)
need = request('/api/v1/staff/centers/kc-children-001/needs', {
    'title': {'en': 'Local backup check'}, 'description': {'en': 'Development fixture'},
    'helpMethods': ['goods'], 'urgent': False, 'quantityRequired': 2, 'unit': 'kit', 'status': 'published',
}, staff_token, str(uuid.uuid4()))
offer_key = str(uuid.uuid4())
goods_body = {'need_id': need['id'], 'method': 'goods', 'quantity': 1}
goods = request('/api/v1/contributions', goods_body, token, offer_key)
assert request('/api/v1/contributions', goods_body, token, offer_key) == goods
assert request('/api/v1/staff/centers', token=token)['centers'] == []
request('/api/v1/contributions/' + goods['id'] + '/transition', {'action': 'confirm', 'version': 1}, token, str(uuid.uuid4()), expected=403)
for action, version, status in [('confirm', 1, 'confirmed'), ('fulfill', 2, 'fulfilled')]:
    result = web('/api/v1/contributions/' + goods['id'] + '/transition', {'action': action, 'version': version},
        {'X-CSRF-TOKEN': csrf, 'Idempotency-Key': str(uuid.uuid4())})[1]
    assert result['status'] == status and result['id'] == goods['id']
    history = request('/api/v1/sync/account', token=token)['contributions']
    assert any(row['id'] == goods['id'] and row['status'] == status and row['quantity_unit'] == 'kit' for row in history)
cancelled = request('/api/v1/contributions', goods_body, token, str(uuid.uuid4()))
request('/api/v1/contributions', goods_body, token, str(uuid.uuid4()), expected=409)
cancel_key = str(uuid.uuid4())
cancel_body = {'action': 'cancel', 'version': 1}
cancel_path = '/api/v1/contributions/' + cancelled['id'] + '/transition'
cancel_result = request(cancel_path, cancel_body, token, cancel_key)
assert cancel_result['status'] == 'cancelled'
assert request(cancel_path, cancel_body, token, cancel_key) == cancel_result
assert request('/api/v1/needs/' + need['id'])['quantityCommitted'] == 1
catalog = request('/api/v1/sync/catalog')['catalog']
assert any(item['id'] == need['id'] and item['unit'] == 'kit' for center in catalog['centers'] for item in center['needs'])
visit_key = str(uuid.uuid4())
visit_body = {'center_id': 'kc-children-001', 'occurred_at': datetime.now(timezone.utc).isoformat(), 'explicit_action': True}
visit = request('/api/v1/visits', visit_body, token, visit_key)
key = str(uuid.uuid4())
activity = {'method': 'time', 'minutes': 30, 'activity': 'Local recovery check'}
offer = request('/api/v1/contributions', activity, token, key)
assert offer['provenance'] == 'self_reported'
request('/api/v1/contributions/' + offer['id'] + '/transition', {'action': 'complete', 'version': 1, 'actual_minutes': 25}, token, str(uuid.uuid4()))
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
assert request('/api/v1/visits', visit_body, token, visit_key) == visit
account = request('/api/v1/sync/account', token=token)
assert any(row['id'] == offer['id'] and row['actual_minutes'] == 25 and row['status'] == 'fulfilled' for row in account['contributions'])
assert len([row for row in account['visits'] if row['id'] == visit['id']]) == 1

# Compare restored business tables byte-for-byte, including hashed identities and operations.
database = 'generosity_restore_' + suffix
dump_args = ('--single-transaction', '--skip-comments', '--skip-extended-insert', '--compact', '--order-by-primary', '--skip-add-locks')
tables = ('users', 'centers', 'needs', 'center_memberships', 'contributions', 'contribution_events', 'operations', 'operator_audits', 'personal_access_tokens', 'visit_signals')
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
print('PASS: real local HTTP registration/mail, browser cookie + CSRF, staff isolation, need publication, offer/confirmation/fulfillment/history, capacity/cancellation, visits, actual duration, restart, idempotent replay and isolated database restore. No native UI or production pilot acceptance is implied.')
