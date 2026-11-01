import assert from 'node:assert/strict';
import { createHash, randomBytes, randomUUID } from 'node:crypto';
import { createServer } from 'node:http';
import test from 'node:test';
import { contractOperations, createApp, implementedOperations } from '../src/app.js';
import { loadConfig } from '../src/config.js';
import { createMemoryStore } from '../src/store.js';

function app() {
  const config = loadConfig({ NODE_ENV: 'test', APP_URL: 'http://127.0.0.1:8088', APP_KEY: 'test-key' });
  config.limits = { api: 1000, accountIp: 1000, account: 1000, identity: 1000, applications: 1000 };
  const store = createMemoryStore();
  const mail = [];
  return { ...createApp({ config, store, mail }), config, mail };
}

async function listen(application) {
  const server = createServer((req, res) => application.handle(req, res));
  await new Promise((resolve) => server.listen(0, '127.0.0.1', resolve));
  const { port } = server.address();
  return {
    server,
    url: `http://127.0.0.1:${port}`,
    close: () => new Promise((resolve) => server.close(resolve)),
  };
}

async function call(base, method, path, { body, token, headers = {} } = {}) {
  const response = await fetch(`${base}${path}`, {
    method,
    headers: {
      ...(body === undefined ? {} : { 'content-type': 'application/json' }),
      ...(token ? { authorization: `Bearer ${token}` } : {}),
      ...headers,
    },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  const text = await response.text();
  return { status: response.status, body: text ? JSON.parse(text) : null, headers: response.headers };
}

const registerBody = (email) => ({
  name: 'Ada Lopez',
  email,
  password: 'Correct-horse-12',
  adult_confirmed: true,
  terms_accepted: true,
  privacy_acknowledged: true,
  policy_version: '2026-11-01',
});

const applicationBody = {
  organization_name: 'Comedor Norte',
  site_name: 'Sede uno',
  activities: 'Comidas comunitarias',
  country: 'Mexico',
  city: 'Monterrey',
  contact_name: 'Ada Lopez',
  contact_role: 'Coordinacion',
  representation_confirmed: true,
  contact_consent: true,
};

test('implements every OpenAPI operation and no extra route', () => {
  const { spec } = loadConfig({ NODE_ENV: 'test' }).contract;
  assert.deepEqual(implementedOperations().sort(), contractOperations(spec).sort());
});

test('health keeps payments disabled', async () => {
  const application = app();
  const server = await listen(application);
  try {
    const health = await call(server.url, 'GET', '/api/v1/health');
    assert.equal(health.status, 200);
    assert.deepEqual(health.body, { status: 'ready', apiVersion: 1, payments: 'disabled' });
    assert.equal(health.headers.get('cache-control'), 'no-store');
  } finally {
    await server.close();
  }
});

test('account, catalog and private application follow the contract', async () => {
  const application = app();
  const server = await listen(application);
  try {
    const email = 'ada@example.test';
    const created = await call(server.url, 'POST', '/api/v1/auth/register', { body: registerBody(email) });
    assert.equal(created.status, 202);
    assert.equal(application.mail.length, 1);
    const repeat = await call(server.url, 'POST', '/api/v1/auth/register', { body: registerBody(email) });
    assert.equal(repeat.status, 202);
    assert.equal(application.mail.length, 1);

    const denied = await call(server.url, 'POST', '/api/v1/auth/login', { body: { email, password: 'Correct-horse-12', device_name: 'test' } });
    assert.equal(denied.status, 403);
    assert.equal(denied.body.error.code, 'email_not_verified');

    const link = new URL(application.mail[0].url);
    const verified = await call(server.url, 'GET', `${link.pathname}${link.search}`);
    assert.equal(verified.status, 200);
    const broken = await call(server.url, 'GET', `${link.pathname}${link.search}x`);
    assert.equal(broken.status, 403);

    const login = await call(server.url, 'POST', '/api/v1/auth/login', { body: { email, password: 'Correct-horse-12', device_name: 'test' } });
    assert.equal(login.status, 200);
    assert.equal(login.body.token_type, 'Bearer');
    const token = login.body.token;

    const guest = await call(server.url, 'GET', '/api/v1/me');
    assert.equal(guest.status, 401);
    assert.deepEqual(guest.body.error.details, {});

    const snapshot = await call(server.url, 'GET', '/api/v1/sync/catalog');
    assert.equal(snapshot.status, 200);
    assert.equal(snapshot.body.catalog.centers.length, 0);
    assert.equal(snapshot.body.revision, createHash('sha256').update(JSON.stringify({ schemaVersion: 1, centers: [] }).replace(/\//g, '\\/')).digest('hex'));

    const key = randomUUID();
    const submitted = await call(server.url, 'POST', '/api/v1/center-applications', {
      body: { ...applicationBody, status: 'approved', center_id: 'invented' },
      token,
      headers: { 'idempotency-key': key },
    });
    assert.equal(submitted.status, 422);
    const accepted = await call(server.url, 'POST', '/api/v1/center-applications', {
      body: applicationBody,
      token,
      headers: { 'idempotency-key': key },
    });
    assert.equal(accepted.status, 201);
    assert.equal(accepted.body.application.status, 'pending');
    const replay = await call(server.url, 'POST', '/api/v1/center-applications', {
      body: applicationBody,
      token,
      headers: { 'idempotency-key': key },
    });
    assert.equal(replay.status, 201);
    assert.equal(replay.body.application.id, accepted.body.application.id);
    const conflict = await call(server.url, 'POST', '/api/v1/center-applications', {
      body: { ...applicationBody, site_name: 'Otra sede' },
      token,
      headers: { 'idempotency-key': key },
    });
    assert.equal(conflict.status, 409);
    assert.equal(conflict.body.error.code, 'operation_reused');
    const catalog = await call(server.url, 'GET', '/api/v1/centers');
    assert.equal(catalog.body.total, 0);
    const staff = await call(server.url, 'GET', '/api/v1/staff/centers', { token });
    assert.deepEqual(staff.body.centers, []);

    const logout = await call(server.url, 'POST', '/api/v1/auth/logout', { token });
    assert.equal(logout.status, 204);
    const after = await call(server.url, 'GET', '/api/v1/me', { token });
    assert.equal(after.status, 401);
  } finally {
    await server.close();
  }
});

test('assigned staff can publish a need and a visit does not verify the center', async () => {
  const application = app();
  const now = application.config.now();
  await application.store.insertCenter({
    id: 'center-1',
    details: {
      name: 'Comedor Norte',
      description: 'Comidas',
      categories: ['meals'],
      coordinates: null,
      address: 'Public street',
      contact: { email: 'sede@example.test' },
      verificationStatus: 'imported',
      importedAtIsoDate: '2026-11-01',
    },
    version: 1,
    created_at: now.toISOString(),
    updated_at: now.toISOString(),
  });
  const server = await listen(application);
  try {
    const email = 'staff@example.test';
    await call(server.url, 'POST', '/api/v1/auth/register', { body: registerBody(email) });
    const link = new URL(application.mail[0].url);
    await call(server.url, 'GET', `${link.pathname}${link.search}`);
    const login = await call(server.url, 'POST', '/api/v1/auth/login', { body: { email, password: 'Correct-horse-12', device_name: 'test' } });
    const token = login.body.token;
    await application.store.addMembership(login.body.user.id, 'center-1');

    const need = await call(server.url, 'POST', '/api/v1/staff/centers/center-1/needs', {
      token,
      headers: { 'idempotency-key': randomUUID() },
      body: {
        title: { es: 'Arroz', en: 'Rice' },
        description: { es: 'Un kilo', en: 'One kilo' },
        helpMethods: ['goods'],
        urgent: false,
        quantityRequired: 2,
        unit: 'kg',
        status: 'published',
      },
    });
    assert.equal(need.status, 201);
    const sync = await call(server.url, 'GET', '/api/v1/sync/catalog');
    assert.equal(sync.body.catalog.centers[0].needs[0].status, 'active');
    assert.equal(sync.body.catalog.centers[0].verificationStatus, 'imported');

    const helper = await call(server.url, 'POST', '/api/v1/auth/register', { body: registerBody('helper@example.test') });
    assert.equal(helper.status, 202);
    const helperLink = new URL(application.mail.at(-1).url);
    await call(server.url, 'GET', `${helperLink.pathname}${helperLink.search}`);
    const helperLogin = await call(server.url, 'POST', '/api/v1/auth/login', { body: { email: 'helper@example.test', password: 'Correct-horse-12', device_name: 'phone' } });
    const offer = await call(server.url, 'POST', '/api/v1/contributions', {
      token: helperLogin.body.token,
      headers: { 'idempotency-key': randomUUID() },
      body: { need_id: need.body.id, method: 'goods', quantity: 1 },
    });
    assert.equal(offer.status, 201);
    const selfConfirm = await call(server.url, 'POST', `/api/v1/contributions/${offer.body.id}/transition`, {
      token: helperLogin.body.token,
      headers: { 'idempotency-key': randomUUID() },
      body: { action: 'confirm', version: 1 },
    });
    assert.equal(selfConfirm.status, 403);
    const confirmed = await call(server.url, 'POST', `/api/v1/contributions/${offer.body.id}/transition`, {
      token,
      headers: { 'idempotency-key': randomUUID() },
      body: { action: 'confirm', version: 1 },
    });
    assert.equal(confirmed.status, 200);
    assert.equal(confirmed.body.status, 'confirmed');

    const visit = await call(server.url, 'POST', '/api/v1/visits', {
      token: helperLogin.body.token,
      headers: { 'idempotency-key': randomUUID() },
      body: { center_id: 'center-1', occurred_at: now.toISOString(), explicit_action: true, verification_status: 'verified' },
    });
    assert.equal(visit.status, 422);
    const recorded = await call(server.url, 'POST', '/api/v1/visits', {
      token: helperLogin.body.token,
      headers: { 'idempotency-key': randomUUID() },
      body: { center_id: 'center-1', occurred_at: now.toISOString(), explicit_action: true },
    });
    assert.equal(recorded.status, 201);
    const detail = await call(server.url, 'GET', '/api/v1/centers/center-1');
    assert.equal(detail.body.verificationStatus, 'imported');

    const money = await call(server.url, 'POST', '/api/v1/contributions', {
      token: helperLogin.body.token,
      headers: { 'idempotency-key': randomUUID() },
      body: { method: 'money' },
    });
    assert.equal(money.status, 422);
    const exchange = await call(server.url, 'POST', '/api/v1/auth/exchange', {
      body: { code: randomBytes(32).toString('hex').slice(0, 64), code_verifier: 'a'.repeat(43), client: 'ios' },
    });
    assert.equal(exchange.status, 401);
    assert.equal(exchange.body.error.code, 'invalid_handoff');
  } finally {
    await server.close();
  }
});
