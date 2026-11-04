const { test, expect } = require('@playwright/test');
const api = 'https://api.generosity.global';
test.beforeEach(async ({page}) => { await page.route(/\/api\/v1\/(me\/)?features$/, r => r.fulfill({json:{channel:'production',features:{'contributions.create':true,'needs.publish':true,'needs.progress':true}}})); });
const user = { id: 42, name: 'María Peña', email: 'maria@example.test', email_verified_at: '2026-11-01T00:00:00Z' };
const receipt = { id: 'application-test-1', organization_name: 'Asociación de Apoyo', site_name: 'Sede Niñez', status: 'pending' };

async function fixture(page, initialUser = user) {
  const state = { user: initialUser, items: [], posts: [] };
  await page.route(api + '/**', async route => {
    const path = new URL(route.request().url()).pathname;
    if (path === '/auth/session') return route.fulfill({ json: { user: state.user, csrf_token: 'csrf-test' } });
    if (path === '/api/v1/staff/centers') return route.fulfill({ json: { centers: [] } });
    if (path === '/api/v1/me/center-applications') return route.fulfill({ json: { data: state.items, current_page: 1, last_page: 1, per_page: 10 } });
    if (path === '/api/v1/center-applications') {
      state.posts.push(route.request()); state.items = [receipt];
      return route.fulfill({ status: 201, json: { application: receipt } });
    }
    return route.fulfill({ status: 404 });
  });
  await page.setViewportSize({ width: 390, height: 850 });
  await page.goto('/#registrar-sede');
  await page.locator('#idioma').selectOption('es');
  return state;
}
async function fill(page) {
  for (const [name, value] of Object.entries({ organization_name: 'Asociación de Apoyo', site_name: 'Sede Niñez', activities: 'Comedor y capacitación', country: 'México', city: 'Monterrey', contact_name: 'María Peña', contact_role: 'Coordinadora' })) {
    await page.locator('#application-' + name).fill(value);
  }
  await page.locator('[name=representation_confirmed]').check();
  await page.locator('[name=contact_consent]').check();
}

test('guest draft survives sign-in and language changes; unverified accounts cannot submit', async ({ page }) => {
  const state = await fixture(page, null);
  await fill(page);
  await expect(page.locator('#registration-submit')).toBeDisabled();
  await expect(page.locator('#registration-create')).toHaveAttribute('href', api + '/auth/sign-in?client=web&locale=es&screen=register');
  await expect(page.locator('#registration-create')).toHaveAttribute('target', '_blank');
  await page.locator('#idioma').selectOption('en');
  await expect(page.locator('#registration-create')).toHaveAttribute('href', api + '/auth/sign-in?client=web&locale=en&screen=register');
  await expect(page.locator('#application-site_name')).toHaveValue('Sede Niñez');
  state.user = { ...user, email_verified_at: null };
  await page.locator('#account-retry').click();
  await expect(page.locator('#registration-session')).toContainText('Verify');
  await expect(page.locator('#registration-submit')).toBeDisabled();
  state.user = user;
  await page.locator('#account-retry').click();
  await expect(page.locator('#registration-submit')).toBeEnabled();
  expect(state.posts).toHaveLength(0);
  await expect(page.locator('#application-contact_name')).toHaveValue('María Peña');
});

test('valid submission sends consent and CSRF and only acknowledges a private pending receipt', async ({ page }) => {
  const state = await fixture(page);
  await page.locator('#registration-submit').click();
  await expect(page.locator('#registration-errors')).toContainText('Revisa');
  await expect(page.locator('#application-organization_name')).toBeFocused();
  expect(state.posts).toHaveLength(0);
  await fill(page);
  await page.locator('#registration-submit').click();
  await expect(page.locator('#registration-result')).toContainText('Pendiente de revisión');
  await expect(page.locator('#registration-result')).toContainText(receipt.id);
  await expect(page.locator('#registration-list')).toContainText('Sede Niñez');
  expect(state.posts).toHaveLength(1);
  expect(state.posts[0].headers()['x-csrf-token']).toBe('csrf-test');
  expect(state.posts[0].headers()['idempotency-key']).toMatch(/^[0-9a-f-]{36}$/);
  expect(state.posts[0].postDataJSON()).toMatchObject({ representation_confirmed: true, contact_consent: true, city: 'Monterrey' });
  expect(state.posts[0].postDataJSON()).not.toHaveProperty('user_id');
  expect(await page.evaluate(() => sessionStorage.getItem('generosity.application.pending.42'))).toBeNull();
  await expect(page.locator('#application-site_name')).toHaveValue('');
});

test('lost response survives reload and retries the identical operation only after a click', async ({ page }) => {
  await fixture(page);
  const requests = [];
  await page.route(api + '/api/v1/center-applications', route => {
    requests.push(route.request());
    return requests.length === 1 ? route.abort('failed') : route.fulfill({ json: { application: receipt } });
  });
  await fill(page); await page.locator('#registration-submit').click();
  await expect(page.locator('#registration-result')).toContainText('No pudimos confirmar');
  await page.reload();
  await expect(page.locator('#registration-submit')).toHaveText('Reintentar el mismo envío');
  await expect(page.locator('#application-site_name')).toHaveValue('Sede Niñez');
  await expect(page.locator('#application-site_name')).toBeDisabled();
  expect(requests).toHaveLength(1);
  await page.locator('#registration-submit').click();
  await expect(page.locator('#registration-result')).toContainText('Solicitud recibida');
  expect(requests).toHaveLength(2);
  expect(requests[0].postData()).toBe(requests[1].postData());
  expect(requests[0].headers()['idempotency-key']).toBe(requests[1].headers()['idempotency-key']);
});

test('location fields are visible, preserve precision and require a valid coordinate pair', async ({ page }) => {
  const state = await fixture(page);
  await fill(page);
  await expect(page.locator('#application-address')).toBeVisible();
  await expect(page.locator('#application-google_maps_url')).toBeVisible();
  await page.locator('#application-address').fill('Ignacio Allende 177, Del Carmen, Coyoacán');
  await page.locator('#application-google_maps_url').fill('https://maps.app.goo.gl/example');
  await page.locator('#application-latitude').fill('19.3562351');
  await page.locator('#registration-submit').click();
  await expect(page.locator('#application-longitude')).toBeFocused();
  expect(state.posts).toHaveLength(0);
  await page.locator('#application-longitude').fill('-181');
  await page.locator('#registration-submit').click();
  expect(state.posts).toHaveLength(0);
  await page.locator('#application-longitude').fill('-99.1627481');
  await page.locator('#idioma').selectOption('en');
  await expect(page.getByLabel('Google Maps link', { exact: true })).toHaveValue('https://maps.app.goo.gl/example');
  await expect(page.locator('#application-location-hint')).toContainText('protected');
  await page.locator('#registration-submit').click();
  await expect(page.locator('#registration-result')).toContainText(receipt.id);
  expect(state.posts[0].postDataJSON()).toMatchObject({ latitude: '19.3562351', longitude: '-99.1627481', google_maps_url: 'https://maps.app.goo.gl/example' });
});

test('server validation keeps values and focuses the invalid optional field', async ({ page }) => {
  await fixture(page);
  await page.route(api + '/api/v1/center-applications', route => route.fulfill({ status: 422, json: { error: { code: 'validation_failed', details: { phone: ['Invalid phone'] } } } }));
  await fill(page); await page.locator('#registration-submit').click();
  await expect(page.locator('#registration-errors')).toContainText('Revisa');
  await expect(page.locator('#application-phone')).toBeFocused();
  await expect(page.locator('#application-phone')).toHaveAttribute('aria-invalid', 'true');
  await expect(page.locator('#application-site_name')).toHaveValue('Sede Niñez');
  await expect(page.locator('#application-site_name')).toBeEnabled();
});

test('account switch clears private fields and ignores the previous account response', async ({ page }) => {
  const state = await fixture(page);
  let release, started;
  const requestStarted = new Promise(resolve => { started = resolve; });
  await page.route(api + '/api/v1/center-applications', async route => {
    started(); await new Promise(resolve => { release = resolve; });
    await route.fulfill({ json: { application: receipt } });
  });
  await fill(page); await page.locator('#registration-submit').click(); await requestStarted;
  state.user = { ...user, id: 99, name: 'Otra cuenta' };
  await page.locator('#account-retry').click();
  await expect(page.locator('#application-site_name')).toHaveValue('');
  release();
  await expect(page.locator('#registration-result')).not.toContainText(receipt.id);
  expect(await page.evaluate(() => sessionStorage.getItem('generosity.application.pending.42'))).toBeNull();
  await expect(page.locator('#registration-list')).toBeEmpty();
});

test('storage failure never submits and private history is escaped', async ({ page }) => {
  const state = await fixture(page);
  state.items = [{ ...receipt, site_name: '<img src=x onerror=alert(1)>' }];
  await page.locator('#registration-refresh').click();
  await expect(page.locator('#registration-list')).toContainText('<img');
  await expect(page.locator('#registration-list img')).toHaveCount(0);
  await fill(page);
  await page.evaluate(() => { Storage.prototype.setItem = function () { throw new Error('Unavailable'); }; });
  await page.locator('#registration-submit').click();
  await expect(page.locator('#registration-result')).toContainText('No se envió nada');
  expect(state.posts).toHaveLength(0);
  await expect(page.locator('#application-site_name')).toHaveValue('Sede Niñez');
});

test('optional center reference is omitted when empty and is not treated as recorded unless echoed', async ({ page }) => {
  const state = await fixture(page);
  let mode = 'default';
  await page.route(api + '/api/v1/center-applications', route => {
    state.posts.push(route.request());
    if (mode === 'silent') return route.fulfill({ status: 201, json: { application: { ...receipt, id: 'application-test-2', status: 'pending', version: 1 } } });
    if (mode === 'echo') return route.fulfill({ status: 201, json: { application: { ...receipt, id: 'application-test-3', status: 'pending', version: 1, claimed_center_id: 'comedor-centro' } } });
    if (mode === 'missing') return route.fulfill({ status: 409, json: { error: { code: 'center_unavailable' } } });
    return route.fulfill({ status: 201, json: { application: receipt } });
  });
  await fill(page);
  await page.locator('#application-claimed_center_id').fill('Comedor Centro');
  await page.locator('#registration-submit').click();
  await expect(page.locator('#application-claimed_center_id')).toBeFocused();
  expect(state.posts).toHaveLength(0);
  await page.locator('#application-claimed_center_id').fill('');
  await page.locator('#registration-submit').click();
  expect(state.posts[0].postDataJSON()).not.toHaveProperty('claimed_center_id');
  expect(state.posts[0].postDataJSON()).not.toHaveProperty('roles');
  await fill(page);
  await page.locator('#application-claimed_center_id').fill('comedor-centro');
  await page.locator('#idioma').selectOption('en');
  await expect(page.locator('#application-claim-hint')).toContainText('does not change');
  await expect(page.locator('#application-claim-hint')).toContainText('grant access');
  mode = 'silent';
  await page.locator('#registration-submit').click();
  await expect(page.locator('#registration-result')).toContainText('was not recorded');
  await expect(page.locator('#registration-result')).toContainText('application-test-2');
  await expect(page.locator('#registration-result')).not.toContainText('reference was recorded');
  await page.locator('#idioma').selectOption('es');
  await fill(page);
  await page.locator('#application-claimed_center_id').fill('ausente');
  mode = 'missing';
  await page.locator('#registration-submit').click();
  await expect(page.locator('#registration-result')).toContainText('No se encontró');
  await expect(page.locator('#registration-result')).not.toContainText('Folio:');
  await expect(page.locator('#application-claimed_center_id')).toHaveValue('ausente');
  await page.locator('#application-claimed_center_id').fill('comedor-centro');
  mode = 'echo';
  await page.locator('#registration-submit').click();
  await expect(page.locator('#registration-result')).toContainText('quedó registrada');
  await expect(page.locator('#registration-result')).toContainText('no se concedió acceso');
  expect(state.posts[state.posts.length - 1].postDataJSON()).toMatchObject({ claimed_center_id: 'comedor-centro' });
  expect(state.posts[state.posts.length - 1].postDataJSON()).not.toHaveProperty('roles');
  await expect(page.locator('#registrar-sede input[type=file]')).toHaveCount(0);
  await expect(page.locator('#registrar-sede [name=decision]')).toHaveCount(0);
});

test('tracker names every returned state and only offers the account its own allowed action', async ({ page }) => {
  const state = await fixture(page);
  state.items = [{ id: 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa', organization_name: 'Sin versión', site_name: 'Cambios antiguos', status: 'changes_requested', decision: { action: 'request_changes', rationale: 'Falta el nombre legal', recorded_at: '2026-11-02T00:00:00Z' } }];
  await page.locator('#registration-refresh').click();
  await expect(page.locator('#registration-list')).toContainText('Se solicitaron cambios');
  await expect(page.locator('#registration-list')).toContainText('Falta el nombre legal');
  await expect(page.locator('#registration-list summary')).toHaveCount(0);
  const ids = ['11111111-1111-4111-8111-111111111111', '22222222-2222-4222-8222-222222222222', '33333333-3333-4333-8333-333333333333', '44444444-4444-4444-8444-444444444444', '55555555-5555-4555-8555-555555555555', '66666666-6666-4666-8666-666666666666', '77777777-7777-4777-8777-777777777777'];
  state.items = [
    { id: ids[0], organization_name: 'Uno', site_name: 'Pendiente', status: 'pending', version: 1, claimed_center_id: 'comedor-centro' },
    { id: ids[1], organization_name: 'Dos', site_name: 'Cambios', status: 'changes_requested', version: 2, decision: { action: 'request_changes', rationale: '<img src=x onerror=alert(1)>', recorded_at: '2026-11-02T00:00:00Z' } },
    { id: ids[2], organization_name: 'Tres', site_name: 'Aprobada', status: 'approved', version: 3, acceptance_required: true, account_id: 42, roles: ['staff'], center_id: 'comedor-centro', decision: { action: 'approve', rationale: 'Coincide con la ficha', recorded_at: '2026-11-02T00:00:00Z' } },
    { id: ids[3], organization_name: 'Cuatro', site_name: 'Otra cuenta', status: 'approved', version: 1, acceptance_required: true, account_id: 7, roles: ['staff'], center_id: 'otro-centro' },
    { id: ids[4], organization_name: 'Cinco', site_name: 'Rechazada', status: 'rejected', version: 2, decision: { action: 'reject', rationale: 'No representa a la sede', recorded_at: '2026-11-02T00:00:00Z' } },
    { id: ids[5], organization_name: 'Seis', site_name: 'Retirada', status: 'withdrawn', version: 2 },
    { id: ids[6], organization_name: 'Siete', site_name: 'Aceptada', status: 'approved', version: 4, acceptance_required: false, accepted_at: '2026-11-02T01:00:00Z', account_id: 42, roles: ['staff'], center_id: 'comedor-centro' }
  ];
  const actions = [];
  await page.route(/\/api\/v1\/center-applications\/[^/]+\/acceptance$/, route => {
    actions.push(route.request());
    state.items[2] = { ...state.items[2], acceptance_required: false, accepted_at: '2026-11-02T02:00:00Z', version: 4 };
    return route.fulfill({ json: { application: state.items[2] } });
  });
  await page.route(/\/api\/v1\/center-applications\/[^/]+\/withdrawal$/, route => {
    actions.push(route.request());
    state.items[0] = { ...state.items[0], status: 'withdrawn', version: 2 };
    return route.fulfill({ json: { application: state.items[0] } });
  });
  await page.locator('#registration-refresh').click();
  await expect(page.locator('#registration-history [data-i18n="application.historyNote"]')).toContainText('No hay un plazo');
  await expect(page.locator('#registration-history [data-i18n="application.historyNote"]')).toContainText('no hay un plazo de conservación');
  await expect(page.locator('#registration-list')).toContainText('Centro referido: comedor-centro');
  await expect(page.locator('#registration-list')).toContainText('<img');
  await expect(page.locator('#registration-list img')).toHaveCount(0);
  await expect(page.locator('#registration-list summary')).toHaveCount(4);
  await expect(page.getByRole('button', { name: 'Aceptar acceso de personal' })).toHaveCount(1);
  await expect(page.locator('#registration-list')).toContainText('Solo esa cuenta puede aceptar');
  await expect(page.locator('#registration-list')).toContainText('no verifica la organización');
  await page.locator('#idioma').selectOption('en');
  await expect(page.locator('#registration-list')).toContainText('Changes requested');
  await expect(page.locator('#registration-list')).toContainText('Withdrawn');
  await expect(page.locator('#registration-list')).toContainText('Not accepted');
  await expect(page.locator('#registration-list')).toContainText('Staff access accepted. This does not verify');
  await expect(page.locator('#registration-history [data-i18n="application.historyNote"]')).toContainText('No response time');
  await expect(page.locator('#registration-history [data-i18n="application.historyNote"]')).toContainText('no retention period');
  await expect(page.getByRole('button', { name: 'Accept staff access' })).toHaveCount(1);
  await page.locator('#idioma').selectOption('es');
  await page.getByRole('button', { name: 'Aceptar acceso de personal' }).click();
  await expect(page.locator('#registration-action-status')).toContainText('no verifica la organización');
  expect(actions).toHaveLength(1);
  expect(actions[0].url()).toContain('/' + ids[2] + '/acceptance');
  expect(actions[0].headers()['x-csrf-token']).toBe('csrf-test');
  expect(actions[0].headers()['idempotency-key']).toMatch(/^[0-9a-f-]{36}$/);
  expect(actions[0].postDataJSON()).toEqual({ version: 3 });
  await expect(page.locator('#registration-action-status')).toContainText('no verifica la organización');
  await expect(page.getByRole('button', { name: 'Aceptar acceso de personal' })).toHaveCount(0);
  const own = page.locator('#registration-list li').filter({ hasText: 'Uno' });
  await own.locator('summary').click();
  await own.locator('textarea').fill('Ya no represento a la sede');
  await own.getByRole('button', { name: 'Confirmar retiro' }).click();
  await expect(page.locator('#registration-action-status')).toContainText('se conservan');
  expect(actions).toHaveLength(2);
  expect(actions[1].url()).toContain('/' + ids[0] + '/withdrawal');
  expect(actions[1].postDataJSON()).toEqual({ version: 1, rationale: 'Ya no represento a la sede' });
  expect(actions[1].postDataJSON()).not.toHaveProperty('roles');
  await expect(page.locator('#registration-action-status')).toContainText('se conservan');
  await expect(own).toContainText('Retirada');
  await expect(own.locator('summary')).toHaveCount(0);
});

test('lost withdrawal is retried only after a click and keeps the same key', async ({ page }) => {
  const state = await fixture(page);
  state.items = [{ id: '11111111-1111-4111-8111-111111111111', organization_name: 'Uno', site_name: 'Pendiente', status: 'pending', version: 1 }];
  const requests = [];
  await page.route(/\/api\/v1\/center-applications\/[^/]+\/withdrawal$/, route => {
    requests.push(route.request());
    if (requests.length === 1) return route.abort('failed');
    state.items = [{ ...state.items[0], status: 'withdrawn', version: 2 }];
    return route.fulfill({ json: { application: state.items[0] } });
  });
  await page.locator('#registration-refresh').click();
  await page.locator('#registration-list summary').click();
  await page.getByRole('button', { name: 'Confirmar retiro' }).click();
  await expect(page.locator('#registration-action-status')).toContainText('No pudimos confirmar');
  await page.reload();
  await expect(page.locator('#registration-action-retry')).toBeVisible();
  expect(requests).toHaveLength(1);
  await page.locator('#registration-action-retry').click();
  await expect(page.locator('#registration-action-status')).toContainText('se conservan');
  expect(requests).toHaveLength(2);
  expect(requests[0].headers()['idempotency-key']).toBe(requests[1].headers()['idempotency-key']);
  expect(requests[0].postData()).toBe(requests[1].postData());
});

test('mobile first layout, labels and controls work in both languages with enlarged text', async ({ page }) => {
  await fixture(page);
  for (const width of [320, 390, 768, 1280]) {
    await page.setViewportSize({ width, height: 900 });
    for (const lang of ['es', 'en']) {
      await page.locator('#idioma').selectOption(lang);
      await page.evaluate(() => document.documentElement.style.fontSize = '200%');
      const overflow = await page.evaluate(() => Array.from(document.querySelectorAll('body *')).filter(el => el.getBoundingClientRect().right > innerWidth + 1 && el.getBoundingClientRect().width > 0).map(el => el.id || el.tagName + '.' + el.className));
      expect(await page.evaluate(() => document.documentElement.scrollWidth - innerWidth), JSON.stringify({ width, lang, overflow })).toBeLessThanOrEqual(1);
      await expect(page.locator('#registration-submit')).toBeVisible();
      const box = await page.locator('#registration-submit').boundingBox();
      expect(box.height).toBeGreaterThanOrEqual(44);
      await page.evaluate(() => document.documentElement.style.fontSize = '');
    }
  }
});
