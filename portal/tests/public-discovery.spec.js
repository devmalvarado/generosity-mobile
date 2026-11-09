const { test, expect } = require('./browser');
const { readFileSync } = require('fs');

const api = 'https://api.generosity.global';
const centerId = 'comedor-centro';
const needId = '11111111-1111-4111-8111-111111111111';
const offerId = '22222222-2222-4222-8222-222222222222';
const secret = 'HOUSEHOLD-SECRET-991';
const otherSecret = 'OTHER-USER-SECRET-772';
const coordinate = '19.432601';

function pageOf(rows, last) {
  return { data: rows, current_page: 1, last_page: last || 1, per_page: 25, total: rows.length };
}

function center() {
  return {
    id: centerId,
    name: { es: 'Centro Norte <img src=x onerror=alert(1)>', en: 'North Center' },
    description: { es: 'Descripción pública del centro', en: 'Public center description' },
    address: { es: 'Calle publicada 12', en: 'Published street 12' },
    categories: ['alimentos', '<script>bad</script>'],
    contact: { phone: '+52 55 1200 4400', email: 'centro@example.org', website: 'https://example.org/centro', map: 'javascript:alert(1)' },
    coordinates: { latitude: 19.432601, longitude: -99.133201 },
    verificationStatus: 'government-certified',
    household: secret,
    roster: ['SECRET-ROSTER'],
    recipient: 'SECRET-RECIPIENT',
    fundingOrigin: 'private-grant',
    updatedAt: '2026-11-01T15:04:05Z'
  };
}

function need(methods, expires) {
  return {
    id: needId,
    centerId: centerId,
    status: 'published',
    title: { es: 'Mantas de invierno', en: 'Winter blankets' },
    description: { es: 'Veinte mantas publicadas', en: 'Twenty published blankets' },
    helpMethods: methods,
    urgent: true,
    unit: 'mantas',
    quantityRequired: 20,
    quantityCommitted: 4,
    quantityReceived: 1,
    expiresOnIsoDate: expires || '2099-12-31',
    updatedAt: '2026-11-02T08:00:00Z',
    household: secret,
    verificationStatus: 'government-certified',
    roster: ['SECRET-ROSTER']
  };
}

function user(id) {
  return { id: id, name: 'Ana Ruiz', email_verified_at: '2026-11-01T00:00:00Z' };
}

function receipt(status) {
  return {
    id: offerId, user_id: 7, need_id: needId, method: 'goods', quantity: 2,
    status: status, version: 1, title: { es: 'Mantas de invierno', en: 'Winter blankets' }
  };
}

async function install(page, options) {
  const state = Object.assign({ user: null, centers: pageOf([center()], 3), needs: pageOf([need(['goods', 'time'])]), campaigns: pageOf([]), occurrences: pageOf([]), meals: pageOf([]), centerStatus: 200, needStatus: 200, posts: [], gets: [], participationGets: [] }, options || {});
  await page.route(api + '/**', route => {
    const url = new URL(route.request().url());
    if (/\/api\/v1\/(me\/)?features$/.test(url.pathname)) return route.fulfill({json:{channel:'production',features:state.features || {'contributions.create':true,'needs.publish':true,'needs.progress':true}}});
    const path = url.pathname;
    if (path === '/auth/session') return route.fulfill({ json: { user: state.user, csrf_token: 'csrf-test' } });
    if (route.request().method() === 'GET' && (path.startsWith('/api/v1/centers') || path.startsWith('/api/v1/needs') || path === '/api/v1/me/contributions')) state.gets.push(route.request().method() + ' ' + path + url.search);
    if (path === '/api/v1/sync/catalog') {
      state.gets.push('GET ' + path + url.search);
      return state.centerStatus === 200 && state.needStatus === 200 ? route.fulfill({ json: { catalog: { schemaVersion: 1, centers: state.centers.data.map(c => ({ ...c, needs: state.needs.data.filter(n => n.centerId === c.id).map(n => ({ ...n, status: 'active' })) })) } } }) : route.fulfill({ status: 503 });
    }
    if (path === '/api/v1/centers') return state.centerStatus === 200 ? route.fulfill({ json: state.centers }) : route.fulfill({ status: state.centerStatus, body: 'unavailable' });
    if (path === '/api/v1/needs') return state.needStatus === 200 ? route.fulfill({ json: state.needs }) : route.fulfill({ status: state.needStatus, body: 'unavailable' });
    if (path === '/api/v1/centers/' + centerId) return route.fulfill({ json: center() });
    if (path === '/api/v1/needs/' + needId) return state.needStatus === 200 ? route.fulfill({ json: state.needs.data[0] || need(['goods', 'time']) }) : route.fulfill({ status: state.needStatus, body: 'missing' });
    if (path === '/api/v1/me/contributions') return route.fulfill({ json: state.history || pageOf([]) });
    if (path === '/api/v1/campaigns' || path === '/api/v1/occurrences' || path === '/api/v1/meals') {
      state.participationGets.push('GET ' + path + url.search);
      const rows = path.endsWith('/campaigns') ? state.campaigns : path.endsWith('/occurrences') ? state.occurrences : state.meals;
      return route.fulfill({ json: rows || pageOf([]) });
    }
    if (path.startsWith('/api/v1/campaigns/') || path.startsWith('/api/v1/occurrences/') || path.startsWith('/api/v1/meals/')) {
      state.participationGets.push('GET ' + path);
      const record = path.startsWith('/api/v1/campaigns/') ? state.campaign : path.startsWith('/api/v1/occurrences/') ? state.occurrence : state.meal;
      const status = path.startsWith('/api/v1/campaigns/') ? state.campaignStatus : path.startsWith('/api/v1/occurrences/') ? state.occurrenceStatus : state.mealStatus;
      return record && status !== 404 && path.endsWith('/' + record.id) ? route.fulfill({ json: record }) : route.fulfill({ status: status || 404, body: 'missing' });
    }
    if (/^\/api\/v1\/needs\/[^/]+\/progress$/.test(path)) {
      state.participationGets.push('GET ' + path);
      if (state.progressStatus) return route.fulfill({ status: state.progressStatus, body: 'unavailable' });
      return route.fulfill({ json: state.progress || { needId: path.split('/')[4], requested: null, requestedKnown: false, committed: null, committedKnown: false, reported: null, reportedKnown: false, validated: null, validatedKnown: false, unit: null, updatedAt: '2026-11-04T15:00:00Z', fulfilled: false } });
    }
    if (path === '/api/v1/me/notifications' && route.request().method() === 'GET') {
      state.participationGets.push('GET ' + path);
      return route.fulfill({ json: state.inbox || { items: [], unread: 0, hasMore: false } });
    }
    if (path === '/api/v1/me/notification-preferences' && route.request().method() === 'GET') {
      state.participationGets.push('GET ' + path);
      return route.fulfill({ json: state.preferences || { saved: false } });
    }
    if (path === '/api/v1/contributions' && route.request().method() === 'POST') {
      state.posts.push({ key: route.request().headers()['idempotency-key'], csrf: route.request().headers()['x-csrf-token'], body: route.request().postDataJSON() });
      return state.offer(route);
    }
    if (path === '/api/v1/contributions/' + offerId + '/transition' && route.request().method() === 'POST') {
      state.posts.push({ key: route.request().headers()['idempotency-key'], csrf: route.request().headers()['x-csrf-token'], body: route.request().postDataJSON() });
      return state.cancel(route);
    }
    return route.abort();
  });
  return state;
}

async function openDiscovery(page) {
  await page.goto('/');
  await page.locator('#idioma').selectOption('es');
  const help = page.locator('a[href="/ayudar/"]:visible', { hasText: 'Cómo ayudar' });
  if (await help.count() === 0) await page.locator('.menu').click();
  await page.locator('a[href="/ayudar/"]:visible', { hasText: 'Cómo ayudar' }).first().click();
  await expect(page).toHaveURL(/\/ayudar\/$/);
  await expect(page.locator('#center-workspace')).toBeHidden();
}

test('help opens the published catalog and does not submit or open the staff path', async ({ page }) => {
  const state = await install(page);
  await page.setViewportSize({ width: 390, height: 800 });
  await openDiscovery(page);
  await expect(page.locator('#necesidades')).toHaveCount(0);
  await expect(page.locator('#discovery-results .need, #discovery-results .illustration-label')).toHaveCount(0);
  await expect(page.locator('#discovery-results')).toContainText('Centro Norte');
  await expect(page.locator('#discovery-results img, #discovery-results script')).toHaveCount(0);
  await expect(page.locator('#discovery-results')).not.toContainText(secret);
  await expect(page.locator('#discovery-results')).not.toContainText('government-certified');
  await expect(page.locator('#discovery-results')).not.toContainText(coordinate);
  await expect(page.locator('#staff-need-form')).toHaveCount(0);
  await expect(page.locator('#center-workspace')).toBeHidden();
  expect(state.posts).toEqual([]);
  expect(state.gets.some(item => item.indexOf('page=2') !== -1 || item.indexOf('page=3') !== -1)).toBe(false);
  const before = state.gets.length;
  await page.locator('#discovery-query').fill('zzzz');
  await expect(page.locator('#discovery-results')).toContainText('No encontramos coincidencias');
  expect(state.gets.length).toBe(before);
  await page.locator('#idioma').selectOption('en');
  await expect(page.locator('#discovery-title')).toHaveText('Find a place to help');
});

test('home does not fetch the catalog', async ({ page }) => {
  const state = await install(page);
  await page.goto('/');
  await page.locator('#idioma').selectOption('es');
  await expect(page.locator('#account-status')).toHaveCount(0);
  expect(state.gets).toEqual([]);
});

test('catalog failure keeps the search and does not present illustration cards as current needs', async ({ page }) => {
  const state = await install(page);
  await openDiscovery(page);
  await page.locator('#discovery-needs-tab').click();
  await expect(page.locator('#discovery-results')).toContainText('Mantas de invierno');
  await page.locator('#discovery-query').fill('Norte');
  state.centerStatus = 503;
  await page.locator('#discovery-refresh').click();
  await expect(page.locator('#discovery-status')).toContainText('No pudimos cargar el directorio');
  await expect(page.locator('#discovery-query')).toHaveValue('Norte');
  await expect(page.locator('#discovery-results')).toHaveText('');
  await expect(page.locator('#discovery-results .need, #discovery-results .illustration-label')).toHaveCount(0);
  await expect(page.locator('#necesidades')).toHaveCount(0);
  expect(state.gets.filter(item => item.startsWith('GET /api/v1/centers?page=3'))).toEqual([]);
});

test('a direct missing record does not open another record', async ({ page }) => {
  const state = await install(page, { needStatus: 404 });
  await page.goto('/#necesidad/' + needId);
  await page.locator('#idioma').selectOption('es');
  await expect(page.locator('#discovery-status')).toContainText('No se abrió otro');
  await expect(page.locator('#discovery-detail')).toBeHidden();
  await expect(page.locator('#discovery-results')).toHaveText('');
  await expect(page.locator('body')).not.toContainText('Centro Norte');
  expect(state.gets.some(item => item.indexOf('/api/v1/centers') !== -1)).toBe(false);
  await page.goto('/#centro/NOT-VALID');
  await expect(page.locator('#discovery-status')).toContainText('No se abrió otro');
  expect(state.gets.filter(item => item.indexOf('NOT-VALID') !== -1)).toEqual([]);
});

test('a guest can prepare an offer but cannot submit it', async ({ page }) => {
  const state = await install(page);
  await page.goto('/#necesidad/' + needId);
  await page.locator('#idioma').selectOption('es');
  await expect(page.locator('#discovery-quantity')).toBeVisible();
  await page.locator('#discovery-quantity').fill('2');
  await page.locator('#discovery-submit').click();
  await expect(page.locator('#discovery-offer-status')).toContainText('La oferta no se envió');
  await expect(page.locator('#discovery-sign-in')).toBeVisible();
  expect(state.posts).toEqual([]);
  await expect(page.locator('#descubrir input[autocomplete=cc-number], #descubrir input[name=card]')).toHaveCount(0);
});

test('money stays unavailable', async ({ page }) => {
  await install(page, { needs: pageOf([need(['money'])]) });
  await page.goto('/#necesidad/' + needId);
  await page.locator('#idioma').selectOption('es');
  await expect(page.locator('#discovery-offer-limit')).toContainText('El dinero no se puede aportar');
  await expect(page.locator('#discovery-submit')).toBeDisabled();
  await expect(page.locator('#descubrir input[autocomplete=cc-number]')).toHaveCount(0);
});

test('an expired need cannot be offered from this page', async ({ page }) => {
  const state = await install(page, { user: user(7), needs: pageOf([need(['goods'], '2000-01-01')]) });
  await page.goto('/#necesidad/' + needId);
  await page.locator('#idioma').selectOption('es');
  await expect(page.locator('#discovery-offer-limit')).toContainText('ya pasó');
  await expect(page.locator('#discovery-submit')).toBeDisabled();
  expect(state.posts).toEqual([]);
});

test('verified goods offer sends CSRF and the same key after a lost response', async ({ page }) => {
  let lost = true;
  const state = await install(page, { user: user(7) });
  state.offer = route => lost ? route.abort() : route.fulfill({ status: 201, json: receipt('offered') });
  await page.goto('/#necesidad/' + needId);
  await page.locator('#idioma').selectOption('es');
  await page.locator('#discovery-quantity').fill('2');
  await page.locator('#discovery-notes').fill('Dejar en recepción');
  await page.locator('#discovery-submit').click();
  await expect(page.locator('#discovery-retry')).toBeVisible();
  await expect(page.locator('#discovery-quantity')).toBeDisabled();
  const stored = await page.evaluate(id => sessionStorage.getItem('generosity.offer.pending.' + id), 7);
  expect(JSON.parse(stored).body.quantity).toBe(2);
  await page.locator('#discovery-quantity').evaluate(el => { el.disabled = false; el.value = '9'; });
  lost = false;
  await page.locator('#discovery-retry').click();
  await expect(page.locator('#discovery-offer-status')).toContainText(offerId);
  await expect(page.locator('#discovery-offer-status')).toContainText('no confirma la recepción');
  expect(state.posts).toHaveLength(2);
  expect(state.posts[0].key).toMatch(/^[0-9a-f-]{36}$/i);
  expect(state.posts[1].key).toBe(state.posts[0].key);
  expect(state.posts[0].csrf).toBe('csrf-test');
  expect(state.posts[1].body).toMatchObject({ need_id: needId, method: 'goods', quantity: 2, notes: 'Dejar en recepción' });
  expect(state.posts[1].body.quantity).not.toBe(9);
});

test('capacity failure does not record the offer', async ({ page }) => {
  const state = await install(page, { user: user(7) });
  state.offer = route => route.fulfill({ status: 409, json: { error: { code: 'capacity_exceeded', details: { available: 3 } } } });
  await page.goto('/#necesidad/' + needId);
  await page.locator('#idioma').selectOption('es');
  await page.locator('#discovery-quantity').fill('8');
  await page.locator('#discovery-submit').click();
  await expect(page.locator('#discovery-offer-status')).toContainText('Disponible según el servidor: 3');
  await expect(page.locator('#discovery-retry')).toBeHidden();
  await expect(page.locator('#discovery-offer-status')).not.toContainText(offerId);
  expect(state.posts).toHaveLength(1);
});

test('signing in restores the draft and does not submit it', async ({ page }) => {
  const state = await install(page, { user: user(7) });
  state.offer = route => { throw new Error('offer was submitted automatically'); };
  await page.addInitScript(id => sessionStorage.setItem('generosity.discovery.intent', JSON.stringify({
    needId: id, method: 'goods', quantity: '2', minutes: '', activity: '', availability: '', notes: 'Borrador privado'
  })), needId);
  await page.goto('/ayudar/');
  await page.locator('#idioma').selectOption('es');
  await expect(page).toHaveURL(new RegExp('#necesidad/' + needId + '$'));
  await expect(page.locator('#discovery-quantity')).toHaveValue('2');
  await expect(page.locator('#discovery-notes')).toHaveValue('Borrador privado');
  await expect(page.locator('#discovery-offer-status')).toHaveText('');
  expect(state.posts).toEqual([]);
});

test('owner can cancel an offered contribution and an account switch clears it', async ({ page }) => {
  let current = user(7);
  const own = { id: offerId, user_id: 7, need_id: needId, method: 'goods', quantity: 2, status: 'offered', version: 4, notes: 'NOTA-CUENTA-7', title: { es: 'Mantas de invierno', en: 'Winter blankets' } };
  const other = { id: '33333333-3333-4333-8333-333333333333', user_id: 99, method: 'time', minutes: 30, status: 'offered', version: 1, notes: otherSecret, title: { es: 'Ajena', en: 'Other' } };
  const state = await install(page, { user: current, history: pageOf([own, other]) });
  state.cancel = route => {
    own.status = 'cancelled';
    own.version = 5;
    state.history = pageOf([own, other]);
    return route.fulfill({ json: own });
  };
  await page.goto('/#necesidad/' + needId);
  await page.locator('#idioma').selectOption('es');
  await expect(page.locator('#discovery-history-list')).toContainText('NOTA-CUENTA-7');
  await expect(page.locator('#discovery-history-list')).not.toContainText(otherSecret);
  await page.locator('#discovery-history-list button').click();
  await expect(page.locator('#discovery-history-list')).toContainText('Cancelada');
  await expect(page.locator('#discovery-history-list button')).toHaveCount(0);
  expect(state.posts[0].body).toEqual({ action: 'cancel', version: 4 });
  expect(state.posts[0].csrf).toBe('csrf-test');
  expect(state.posts[0].key).toMatch(/^[0-9a-f-]{36}$/i);
  await page.locator('#discovery-notes').fill('NOTA-CUENTA-7');
  current = user(8);
  state.user = current;
  state.history = pageOf([]);
  await page.locator('#account-retry').click();
  await expect(page.locator('#discovery-notes')).toHaveValue('');
  await expect(page.locator('#discovery-history-list')).not.toContainText('NOTA-CUENTA-7');
  const stored = await page.evaluate(() => JSON.stringify(sessionStorage));
  expect(stored).not.toContain('NOTA-CUENTA-7');
  expect(stored).not.toContain('csrf-test');
});

test('closing the share sheet records nothing and the text invitation is not a scannable image', async ({ page }) => {
  await page.addInitScript(() => {
    const share = () => Promise.reject(new DOMException('closed', 'AbortError'));
    try { Object.defineProperty(navigator, 'share', { configurable: true, writable: true, value: share }); }
    catch (_) { navigator.share = share; }
  });
  await install(page);
  await page.goto('/#centro/' + centerId);
  await page.locator('#idioma').selectOption('es');
  await expect(page.locator('#discovery-link')).toHaveValue(new RegExp('#centro/' + centerId + '$'));
  const link = await page.locator('#discovery-link').inputValue();
  expect(link.startsWith('http://127.0.0.1:47321/')).toBe(true);
  expect(link).not.toContain('utm_');
  await page.locator('#discovery-share-button').click();
  await expect(page.locator('#discovery-share-status')).toContainText('No se registró un envío');
  const downloadPromise = page.waitForEvent('download');
  await page.locator('#discovery-download').click();
  const download = await downloadPromise;
  expect(download.suggestedFilename()).toBe('generosity-invitation.txt');
  const text = readFileSync(await download.path(), 'utf8');
  expect(text).toContain('Centro Norte');
  expect(text).toContain(link);
  expect(text).toContain('No es una imagen para escanear');
  expect(text).toContain('No indica lugares disponibles');
  expect(text).not.toContain(secret);
  expect(text).not.toContain(coordinate);
  expect(text).not.toMatch(/data:image/);
});

test('discovery does not overflow at 320 and keeps controls tappable', async ({ page }) => {
  await install(page);
  await page.setViewportSize({ width: 320, height: 800 });
  await page.goto('/#descubrir');
  await page.locator('#idioma').selectOption('es');
  await expect(page.locator('#discovery-results')).toContainText('Centro Norte');
  const overflow = await page.evaluate(() => Math.max(document.documentElement.scrollWidth, document.body.scrollWidth) - document.documentElement.clientWidth);
  expect(overflow).toBeLessThanOrEqual(1);
  await expect(page.locator('#discovery-offer')).toBeHidden();
  const box = await page.locator('#discovery-locate').boundingBox();
  expect(box.height).toBeGreaterThanOrEqual(44);
  expect(box.width).toBeGreaterThanOrEqual(44);
});


test('a remote pause blocks new offers but preserves cancellation and recovers on refresh', async ({page}) => {
  const state = await install(page, {user:user(7), history:pageOf([receipt('offered')]), features:{'contributions.create':false}});
  state.cancel = route => { state.history = pageOf([receipt('cancelled')]); return route.fulfill({json:receipt('cancelled')}); };
  await page.goto('/#necesidad/' + needId);
  await page.locator('#idioma').selectOption('es');
  await expect(page.locator('#discovery-offer-limit')).toContainText('no está disponible');
  await expect(page.locator('#discovery-submit')).toBeDisabled();
  await page.locator('#discovery-history-list button').click();
  await expect(page.locator('#discovery-history-list')).toContainText('Cancelada');
  state.features = {'contributions.create':true};
  await page.locator('#account-retry').click();
  await expect(page.locator('#discovery-submit')).toBeEnabled();
  await page.route(/\/api\/v1\/me\/features$/, route => route.abort());
  await page.locator('#account-retry').click();
  await expect(page.locator('#discovery-submit')).toBeDisabled();
  await expect(page.locator('#discovery-history-list')).toContainText('Cancelada');
  expect(state.posts).toHaveLength(1);
  expect(state.posts[0].body.action).toBe('cancel');
});

test('location is opt-in, sorts the complete catalog and keeps coordinates out of storage and API requests', async ({ page }) => {
  await page.addInitScript(() => {
    window.locationRequests = 0;
    Object.defineProperty(navigator, 'geolocation', { value: { getCurrentPosition(success) {
      window.locationRequests++;
      success({ coords: { latitude: 21.88, longitude: -102.29 } });
    } } });
  });
  const near = { ...center(), id: 'aguascalientes', name: { es: 'Centro Águilas', en: 'Eagles center' }, address: { es: 'Colonia Jardín, Aguascalientes' }, coordinates: { latitude: 21.881, longitude: -102.291 } };
  const missing = { ...center(), id: 'sin-ubicacion', name: { es: 'Sin coordenadas' }, coordinates: null };
  const invalid = { ...center(), id: 'invalid-location', coordinates: { latitude: 99, longitude: -99 } };
  const rows = Array.from({ length: 24 }, (_, i) => ({ ...center(), id: 'centro-' + i }));
  const state = await install(page, { centers: pageOf([...rows, near, missing, invalid]) });
  await page.goto('/ayudar/');
  await page.locator('#idioma').selectOption('es');
  await expect(page.locator('#discovery-count')).toHaveText('27 resultados');
  expect(await page.evaluate(() => window.locationRequests)).toBe(0);
  await expect(page.locator('.center-pin')).toHaveCount(25);
  await expect(page.locator('#discovery-offer')).toBeHidden();
  await expect(page.locator('#discovery-sign-in')).toBeHidden();
  await page.locator('.center-pin[title="Centro Águilas"]').click();
  await expect(page.locator('#discovery-results article[aria-current=true]')).toContainText('Centro Águilas');
  await page.locator('#discovery-locate').click();
  await expect(page.locator('#discovery-results article').first()).toContainText('Centro Águilas');
  await expect(page.locator('#discovery-results article').first()).toContainText('km');
  await expect(page.locator('#discovery-location-status')).toContainText('ordenados por cercanía');
  expect(await page.evaluate(() => window.locationRequests)).toBe(1);
  await page.locator('#discovery-query').fill('jardin');
  await expect(page.locator('#discovery-count')).toHaveText('1 resultado');
  await expect(page.locator('.center-pin')).toHaveCount(1);
  expect(state.gets.filter(p => p.includes('sync/catalog'))).toHaveLength(1);
  expect(state.gets.join(' ')).not.toMatch(/21\.88|102\.29/);
  expect(await page.evaluate(() => JSON.stringify(localStorage) + JSON.stringify(sessionStorage))).not.toMatch(/21\.88|102\.29/);
  await page.locator('#discovery-results button').click();
  await expect(page.locator('.leaflet-popup')).toHaveCount(1);
  await expect(page.locator('.leaflet-popup')).toContainText('Centro Águilas');
  await expect(page.locator('#discovery-results article')).toHaveAttribute('aria-current', 'true');
  await page.locator('#discovery-location-clear').click();
  await expect(page.locator('#discovery-results')).not.toContainText('km');
});

test('denied location and unavailable map leave public search and hosted login usable', async ({ page }) => {
  await page.addInitScript(() => {
    Object.defineProperty(navigator, 'geolocation', { value: { getCurrentPosition(_, fail) { fail({ code: 1 }); } } });
  });
  await install(page);
  await page.route(api + '/auth/session', route => route.fulfill({ status: 503 }));
  await page.route('**/assets/vendor/leaflet/leaflet.js', route => route.abort());
  await page.goto('/ayudar/');
  await page.locator('#idioma').selectOption('es');
  await page.locator('#discovery-locate').click();
  await expect(page.locator('#discovery-location-status')).toContainText('No se permitió');
  await expect(page.locator('#discovery-map-status')).toContainText('seguir usando la lista');
  await expect(page.locator('#account-sign-in')).toBeVisible();
  await expect(page.locator('#account-sign-in')).toHaveAttribute('target', '_blank');
  await expect(page.locator('#discovery-results')).toContainText('Centro Norte');
  await page.locator('#discovery-query').fill('Calle publicada');
  await expect(page.locator('#discovery-results')).toContainText('Centro Norte');
});

test('a location timeout can be retried and never invents a position', async ({ page }) => {
  await page.addInitScript(() => {
    window.locationRequests = 0;
    Object.defineProperty(navigator, 'geolocation', { value: { getCurrentPosition(_, fail, options) {
      window.locationRequests++; window.locationTimeout = options.timeout; fail({ code: 3 });
    } } });
  });
  await install(page);
  await page.goto('/ayudar/');
  await page.locator('#idioma').selectOption('es');
  await page.locator('#discovery-locate').click();
  await expect(page.locator('#discovery-location-status')).toContainText('No pudimos obtener');
  await expect(page.locator('#discovery-location-clear')).toBeHidden();
  await expect(page.locator('#discovery-results')).not.toContainText('km');
  await page.locator('#discovery-locate').click();
  expect(await page.evaluate(() => [window.locationRequests, window.locationTimeout])).toEqual([2, 10000]);
});

test('a center opens its published needs, with the offer hidden until a need is selected', async ({ page }) => {
  const state = await install(page);
  await page.goto('/ayudar/#centro/' + centerId);
  await page.locator('#idioma').selectOption('es');
  await expect(page.locator('#discovery-offer')).toBeHidden();
  await page.locator('.related-need', { hasText: 'Mantas de invierno' }).click();
  await expect(page.locator('#discovery-offer')).toBeVisible();
  await page.locator('#discovery-quantity').fill('2');
  await expect(page.locator('#discovery-sign-in')).toHaveAttribute('target', '_blank');
  expect(state.posts).toEqual([]);
});

test('local live preview reads only the public relay and never checks or proxies a private session', async ({ page }) => {
  const privateRequests = [];
  page.on('request', request => { if (request.url().startsWith(api)) privateRequests.push(request.url()); });
  const html = readFileSync(require('path').join(__dirname, '../ayudar/index.html'), 'utf8')
    .replace('<head>', '<head><meta name="generosity-public-api-origin" content="/__public">');
  await page.route('**/ayudar/', route => route.fulfill({ contentType: 'text/html', body: html }));
  await page.route('**/__public/api/v1/sync/catalog?**', route => route.fulfill({ json: { catalog: { schemaVersion: 1, centers: [] } } }));
  await page.route('**/__public/api/v1/campaigns?**', route => route.fulfill({ json: pageOf([]) }));
  await page.route('**/__public/api/v1/occurrences?**', route => route.fulfill({ json: pageOf([]) }));
  await page.route('**/__public/api/v1/meals?**', route => route.fulfill({ json: pageOf([]) }));
  await page.goto('/ayudar/');
  await page.locator('#idioma').selectOption('es');
  await expect(page.locator('#account-status')).toContainText('sitio publicado');
  await expect(page.locator('#account-sign-in')).toBeVisible();
  await expect(page.locator('#account-retry')).toBeHidden();
  await expect(page.locator('#discovery-count')).toHaveText('0 resultados');
  expect(privateRequests).toEqual([]);
});

const campaignId = '44444444-4444-4444-8444-444444444444';
const eventId = '55555555-5555-4555-8555-555555555555';
const mealId = '66666666-6666-4666-8666-666666666666';
const noticeId = '77777777-7777-4777-8777-777777777777';

function campaign() {
  return {
    id: campaignId, status: 'published', type: 'drive',
    title: { es: 'Campaña de invierno', en: 'Winter campaign' },
    purpose: { es: 'Mantas publicadas', en: 'Published blankets' },
    area: 'Centro publicado', partnershipAccepted: false, centerId: 'comedor-secreto',
    alternatives: [], updatedAt: '2026-11-04T15:00:00Z',
    private_plan: 'PLAN-SECRETO-441', household: secret
  };
}

function occurrence(status) {
  return {
    id: eventId, status: status || 'published', version: 3,
    title: { es: status === 'cancelled' ? 'Jornada cancelada' : 'Jornada del sábado', en: 'Saturday session' },
    description: { es: 'Apoyo publicado', en: 'Published support' },
    activity: 'acomodo', startsAt: '2099-12-31T15:00:00Z', endsAt: '2099-12-31T18:00:00Z',
    timezone: 'America/Mexico_City', venue: 'Salón publicado',
    capacity: 10, reserved: 10, available: 0, ownerType: 'center', centerId: centerId,
    alternatives: [], updatedAt: '2026-11-04T15:00:00Z', roster: ['SECRET-ROSTER']
  };
}

function meal() {
  return {
    id: mealId, status: 'open',
    title: { es: 'Comida del jueves', en: 'Thursday meal' },
    localDate: '2099-12-31', windowStart: '12:00:00', windowEnd: '14:00:00',
    timezone: 'America/Mexico_City', portionUnit: 'raciones',
    quantityRequired: 20, quantityCommitted: 4, available: 16,
    alternatives: [], updatedAt: '2026-11-04T15:00:00Z',
    private_details: 'DETALLE-PRIVADO-882', recipient: 'SECRET-RECIPIENT'
  };
}

test('published campaigns, events and meals show public text and do not prefetch the next page', async ({ page }) => {
  const state = await install(page, { campaigns: pageOf([campaign()], 2), occurrences: pageOf([occurrence()]), meals: pageOf([meal()]) });
  await page.goto('/ayudar/');
  await page.locator('#idioma').selectOption('es');
  await page.locator('#discovery-campaigns-tab').click();
  await expect(page.locator('#discovery-results')).toContainText('Campaña de invierno');
  await expect(page.locator('#discovery-results')).not.toContainText('PLAN-SECRETO-441');
  await expect(page.locator('#discovery-results')).not.toContainText('comedor-secreto');
  expect(state.participationGets.some(item => item.includes('page=2'))).toBe(false);
  await expect(page.locator('#discovery-campaigns-next')).toBeVisible();
  await page.locator('#discovery-campaigns-next').click();
  await expect.poll(() => state.participationGets.filter(item => item.includes('/campaigns?page=2')).length).toBe(1);
  expect(state.participationGets.some(item => item.includes('page=3'))).toBe(false);
  await page.locator('#discovery-occurrences-tab').click();
  await expect(page.locator('#discovery-results')).toContainText('Jornada del sábado');
  await expect(page.locator('#discovery-results')).not.toContainText('SECRET-ROSTER');
  await page.locator('#discovery-meals-tab').click();
  await expect(page.locator('#discovery-results')).toContainText('Comida del jueves');
  await expect(page.locator('#discovery-results')).toContainText('Disponible según el servidor: 16');
  await expect(page.locator('#discovery-results')).not.toContainText('DETALLE-PRIVADO-882');
  await expect(page.locator('#discovery-results')).not.toContainText('SECRET-RECIPIENT');
  expect(state.posts).toEqual([]);
});

test('a missing or invalid campaign does not open another record', async ({ page }) => {
  const missing = '88888888-8888-4888-8888-888888888888';
  const state = await install(page, { campaigns: pageOf([campaign()]), campaignStatus: 404 });
  await page.goto('/ayudar/#campana/' + missing);
  await page.locator('#idioma').selectOption('es');
  await expect(page.locator('#discovery-status')).toContainText('No se abrió otro');
  await expect(page.locator('#discovery-detail')).toBeHidden();
  await expect(page.locator('body')).not.toContainText('Campaña de invierno');
  expect(state.participationGets.filter(item => item.includes(campaignId))).toEqual([]);
  expect(state.gets.some(item => item.includes('/api/v1/centers'))).toBe(false);
  await page.goto('/ayudar/#campana/NOT_VALID');
  await expect(page.locator('#discovery-status')).toContainText('No se abrió otro');
  expect(state.participationGets.filter(item => item.includes('NOT_VALID'))).toEqual([]);
  expect(state.posts).toEqual([]);
});

test('a cancelled event stays cancelled and closed actions are not submitted', async ({ page }) => {
  const cancelled = occurrence('cancelled');
  cancelled.alternatives = [{ id: 'otro', title: 'OTRA-OPCION-SECRETA' }];
  const state = await install(page, { user: user(7), campaign: campaign(), occurrence: cancelled, meal: meal() });
  await page.goto('/ayudar/#evento/' + eventId);
  await page.locator('#idioma').selectOption('es');
  await expect(page.locator('#discovery-detail')).toContainText('Jornada cancelada');
  await expect(page.locator('#discovery-detail')).toContainText('cancelado');
  await expect(page.locator('#discovery-detail')).toContainText('No se abrió otro');
  await expect(page.locator('body')).not.toContainText('OTRA-OPCION-SECRETA');
  await expect(page.locator('#discovery-detail button, #discovery-detail input, #discovery-detail textarea')).toHaveCount(0);
  for (const [hash, text] of [['#campana/' + campaignId, 'Campaña de invierno'], ['#comida/' + mealId, 'Comida del jueves']]) {
    await page.goto('/ayudar/' + hash);
    await page.locator('#idioma').selectOption('es');
    await expect(page.locator('#discovery-detail')).toContainText(text);
    await expect(page.locator('#discovery-detail')).toContainText('no se envía desde esta página');
    await expect(page.locator('#discovery-detail')).toContainText('no se activan desde esta página');
    await expect(page.locator('#discovery-detail')).toContainText('El dinero no se puede aportar');
  }
  await expect(page.locator('#descubrir input[type=email], #descubrir input[autocomplete=cc-number], #descubrir input[name=card]')).toHaveCount(0);
  expect(state.posts).toEqual([]);
});

test('need progress does not claim fulfillment', async ({ page }) => {
  const state = await install(page, { progress: {
    needId: needId, requested: 20, requestedKnown: true, committed: 4, committedKnown: true,
    reported: 909091, reportedKnown: false, validated: 1, validatedKnown: true, unit: 'mantas',
    updatedAt: '2026-11-04T15:00:00Z', fulfilled: true, claim: 'CUBIERTA-INVENTADA'
  } });
  await page.goto('/ayudar/#necesidad/' + needId);
  await page.locator('#idioma').selectOption('es');
  await expect(page.locator('#discovery-detail')).toContainText('Comprometido según el servidor: 4');
  await expect(page.locator('#discovery-detail')).toContainText('Validado según el servidor: 1');
  await expect(page.locator('#discovery-detail')).toContainText('no está confirmado');
  await expect(page.locator('#discovery-detail')).toContainText('no indica que la necesidad esté cubierta');
  await expect(page.locator('#discovery-detail')).not.toContainText('CUBIERTA-INVENTADA');
  await expect(page.locator('#discovery-detail')).not.toContainText('909091');
  expect(state.posts).toEqual([]);
});

test('inbox and preferences are read without a write and clear when the account changes', async ({ page }) => {
  let current = user(7);
  const state = await install(page, {
    user: current,
    inbox: { items: [{ id: noticeId, title: { es: 'Aviso público', en: 'Public notice' }, read: false, household: secret, body: 'CUERPO-PRIVADO-773' }], unread: 1, hasMore: false },
    preferences: { saved: false, email: 'secret@example.org' }
  });
  await page.goto('/ayudar/');
  await page.locator('#idioma').selectOption('es');
  await expect(page.locator('#discovery-inbox')).toContainText('Aviso público');
  await expect(page.locator('#discovery-inbox')).toContainText('Sin leer según el servidor: 1');
  await expect(page.locator('#discovery-inbox')).toContainText('No hay preferencias guardadas');
  await expect(page.locator('#discovery-inbox')).not.toContainText('CUERPO-PRIVADO-773');
  await expect(page.locator('#discovery-inbox')).not.toContainText(secret);
  await expect(page.locator('#discovery-inbox')).not.toContainText('secret@example.org');
  await expect(page.locator('#discovery-inbox input, #discovery-inbox button, #discovery-inbox textarea')).toHaveCount(0);
  expect(state.posts).toEqual([]);
  current = user(8);
  state.user = current;
  state.inbox = { items: [], unread: 0, hasMore: false };
  await page.locator('#account-retry').click();
  await expect(page.locator('#discovery-inbox')).not.toContainText('Aviso público');
  expect(state.posts).toEqual([]);
});
