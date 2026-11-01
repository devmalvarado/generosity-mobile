const { test, expect } = require('@playwright/test');
const api = 'https://api.generosity.global';
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
