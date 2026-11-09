const { test, expect } = require('./browser');
const { readFileSync } = require('fs');
const path = require('path');
const { createHash } = require('crypto');

test('public deployment inventory includes route dependencies and current tour asset versions', () => {
  const root = path.join(__dirname, '..');
  const files = readFileSync(path.join(root, 'public-files.txt'), 'utf8').trim().split('\n');
  expect(new Set(files).size).toBe(files.length);
  for (const file of files) {
    expect(file).not.toMatch(/(^\/|\.\.|node_modules|screenshots|tests\/|\.env)/);
    const bytes = readFileSync(path.join(root, file));
    if (!file.endsWith('.html') && !/^app\/.*\.(mjs|js)$/.test(file)) continue;
    for (const match of bytes.toString().matchAll(/\b(?:src|href)="([^"]+)"|\bfrom\s+'([^']+)'|\bfetch\('([^']+)'\)/g)) {
      const reference = match[1] || match[2] || match[3];
      if (/^(https?:|mailto:|#)/.test(reference) || reference.includes('${')) continue;
      const url = new URL(reference, 'https://portal.test/' + file);
      const dependency = url.pathname.slice(1) + (url.pathname.endsWith('/') ? 'index.html' : '');
      expect(files, file + ' → ' + dependency).toContain(dependency);
      if (url.searchParams.has('v')) {
        const version = createHash('sha256').update(readFileSync(path.join(root, dependency))).digest('hex').slice(0, 12);
        expect(url.searchParams.get('v'), file + ' → ' + dependency).toBe(version);
      }
    }
  }
});

test.beforeEach(async ({ page }) => {
  await page.route('https://api.generosity.global/**', route => {
    if (route.request().url().endsWith('/auth/session')) return route.fulfill({ json: { user: null, csrf_token: 'route-test' } });
    return route.fulfill({ status: 503, json: {} });
  });
});

test('landing has no work forms or session requests and links to each independent task', async ({ page }) => {
  const requests = [];
  page.on('request', request => { if (request.url().startsWith('https://api.generosity.global')) requests.push(request.url()); });
  await page.goto('/');
  await page.locator('#idioma').selectOption('es');
  await expect(page.locator('form, #acceso, #center-workspace')).toHaveCount(0);
  for (const route of ['ayudar', 'registrar-sede', 'kindness-center', 'app']) {
    expect(await page.locator(`a[href="/${route}/"]`).count()).toBeGreaterThan(0);
  }
  expect(requests).toEqual([]);
});

test('legacy links retain the intended record and query, including navigation from an open landing', async ({ page }) => {
  for (const [hash, route, target] of [
    ['#registrar-sede', 'registrar-sede', '#site-registration'],
    ['#acceso?destino=publicar', 'kindness-center', '#account-sign-in'],
    ['#center-workspace', 'kindness-center', '#account-sign-in'],
    ['#centro/centro-norte', 'ayudar', '#discovery-status'],
    ['#necesidad/need-123', 'ayudar', '#discovery-status'],
    ['#campana/44444444-4444-4444-8444-444444444444', 'ayudar', '#discovery-status'],
    ['#evento/55555555-5555-4555-8555-555555555555', 'ayudar', '#discovery-status'],
    ['#comida/66666666-6666-4666-8666-666666666666', 'ayudar', '#discovery-status'],
    ['#descubrir', 'ayudar', '#discovery-search']
  ]) {
    await page.goto('/?source=invitation' + hash);
    await expect(page).toHaveURL(new RegExp('/' + route + '/\\?source=invitation' + hash.replace('?', '\\?') + '$'));
    await expect(page.locator(target)).toBeVisible();
    await page.reload();
    await expect(page.locator(target)).toBeVisible();
  }
  await page.goto('/');
  await page.evaluate(() => { location.hash = '#registrar-sede'; });
  await expect(page).toHaveURL(/\/registrar-sede\/#registrar-sede$/);
});

test('center entry grants no publishing access to an unassigned account and has no misspelled alias', async ({ page }) => {
  await page.route('https://api.generosity.global/auth/session', route => route.fulfill({ json: { user: { id: 42, name: 'María' }, csrf_token: 'route-test' } }));
  await page.route('https://api.generosity.global/api/v1/staff/centers', route => route.fulfill({ json: { centers: [] } }));
  await page.goto('/kindness-center');
  await page.locator('#idioma').selectOption('es');
  await expect(page).toHaveURL(/\/kindness-center\/$/);
  await expect(page.locator('#staff-status')).toContainText('no tiene centros asignados');
  await expect(page.locator('#staff-need-form')).toBeHidden();
  const response = await page.goto('/kidness-center/');
  expect(response.status()).toBe(404);
});

test('dedicated routes work at mobile widths with enlarged text and versioned assets', async ({ page, browserName }) => {
  const errors = [];
  page.on('pageerror', error => errors.push(error.message));
  for (const route of ['registrar-sede', 'kindness-center', 'ayudar']) {
    await page.setViewportSize({ width: 320, height: 800 });
    await page.goto('/' + route + '/');
    await expect(page.locator('h1')).toBeVisible();
    for (const locale of ['es', 'en']) {
      await page.locator('#idioma').selectOption(locale);
      await page.addStyleTag({ content: 'html { font-size: 200%; }' });
      expect(await page.evaluate(() => document.documentElement.scrollWidth - innerWidth)).toBeLessThanOrEqual(1);
    }
    for (const [file, selector, attr] of [['styles.css', 'link[href^="/styles.css"]', 'href'], ['portal.js', 'script[src^="/portal.js"]', 'src']]) {
      const version = createHash('sha256').update(readFileSync(path.join(__dirname, '..', file))).digest('hex').slice(0, 12);
      await expect(page.locator(selector)).toHaveAttribute(attr, '/' + file + '?v=' + version);
    }
    if (browserName === 'chromium') {
      await page.setViewportSize({ width: 390, height: 844 });
      await page.reload();
      await page.locator('#idioma').selectOption('es');
      await page.screenshot({ path: path.join(__dirname, '../screenshots/route-' + route + '-390.png'), fullPage: true });
    }
  }
  expect(errors).toEqual([]);
});

test('app tour exposes every screen in both languages and remains isolated from the API', async ({ page, browserName }) => {
  const errors = [], external = [], mutations = [];
  page.on('pageerror', error => errors.push(error.message));
  page.on('request', request => {
    if (!request.url().startsWith('http://127.0.0.1:47321/')) external.push(request.url());
    if (request.method() !== 'GET') mutations.push(request.url());
  });
  await page.setViewportSize({ width: 1440, height: 1000 });
  await page.goto('/app/');
  await expect(page.locator('#inventory button')).toHaveCount(34);
  const routes = await page.locator('#inventory button').evaluateAll(buttons => buttons.map(button => button.dataset.route));
  for (let locale = 0; locale < 2; locale++) {
    for (const route of routes) {
      await page.locator(`#inventory [data-route="${route}"]`).click();
      await expect(page.locator('#app')).not.toBeEmpty();
      await expect(page.locator('#app')).not.toContainText(/undefined|NaN|OpenSpec/);
      await expect(page.locator('#inventory button.active')).toHaveAttribute('data-route', route);
    }
    await page.locator('#language').click();
  }
  await expect(page.locator('a[href*="openspec"], a[href*="tasks.md"], a[href*="visual-roadmap"]')).toHaveCount(0);
  await page.locator('#inventory [data-route="map"]').click();
  if (await page.locator('html').getAttribute('lang') !== 'es') await page.locator('#language').click();
  for (const width of [1440, 390, 320]) {
    await page.setViewportSize({ width, height: 844 });
    expect(await page.evaluate(() => document.documentElement.scrollWidth - innerWidth)).toBeLessThanOrEqual(1);
    if (width < 940) {
      await page.locator('#screens-button').click();
      await expect(page.locator('#sheet [data-route]')).toHaveCount(34);
      await page.locator('#sheet [data-route="events"]').click();
      await expect(page.locator('#sheet')).not.toBeVisible();
      await page.locator('#navigation [data-route="map"]').click();
    }
    if (browserName === 'chromium' && width !== 320) await page.screenshot({ path: path.join(__dirname, '../screenshots/app-tour-' + width + '.png') });
  }
  await page.locator('#theme').click();
  await expect(page.locator('.map-toolbar .wordmark, .map-top .wordmark, #app .wordmark').first()).toHaveAttribute('src', '/assets/wordmark-dark.svg');
  await page.locator('#navigation .help-action').click();
  await expect(page.locator('#sheet')).toBeVisible();
  await page.keyboard.press('Escape');
  expect(external).toEqual([]);
  expect(mutations).toEqual([]);
  expect(errors).toEqual([]);
  await page.locator('.tour-brand a').click();
  await expect(page).toHaveURL('http://127.0.0.1:47321/');
  await expect(page.locator('html')).toHaveAttribute('lang', 'es');
});
