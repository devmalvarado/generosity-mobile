const { test, expect } = require('@playwright/test');
const api = 'https://api.generosity.global';

test('guest entry uses the hosted page and keeps the local draft in this tab', async ({ page }) => {
  await page.route(api + '/auth/session', route => route.fulfill({ json: { user: null, csrf_token: 'csrf-test' } }));
  await page.setViewportSize({ width: 320, height: 800 });
  await page.goto('/#acceso');
  await page.locator('#idioma').selectOption('es');
  await page.locator('#titulo').fill('Medicinas para el centro');
  await expect(page.locator('#account-status')).toContainText('invitado');
  await expect(page.locator('#account-sign-in')).toHaveAttribute('href', api + '/auth/sign-in?client=web&locale=es');
  await expect(page.locator('#account-sign-in')).toHaveAttribute('target', '_blank');
  await expect(page.locator('#account-sign-in')).toHaveAttribute('rel', 'noopener');
  await expect(page.locator('#account-sign-out')).toBeHidden();
  await page.locator('#idioma').selectOption('en');
  await expect(page.locator('#account-sign-in')).toHaveAttribute('href', api + '/auth/sign-in?client=web&locale=en');
  await expect(page.locator('#titulo')).toHaveValue('Medicinas para el centro');
});

test('session identity is escaped and logout sends CSRF with credentials', async ({ page }) => {
  let user = { id: 42, name: '<img src=x onerror=alert(1)>' };
  let csrf;
  await page.route(api + '/auth/session', route => route.fulfill({ json: { user, csrf_token: 'csrf-test' } }));
  await page.route(api + '/auth/logout', route => {
    csrf = route.request().headers()['x-csrf-token'];
    user = null;
    return route.fulfill({ status: 204 });
  });
  await page.goto('/#acceso');
  await expect(page.locator('#account-status')).toContainText('<img');
  await expect(page.locator('#account-status img')).toHaveCount(0);
  await page.locator('#account-sign-out').click();
  await expect(page.locator('#account-sign-out')).toBeHidden();
  expect(csrf).toBe('csrf-test');
  const stored = await page.evaluate(() => JSON.stringify(localStorage) + JSON.stringify(sessionStorage));
  expect(stored).not.toContain('csrf-test');
  expect(stored).not.toContain('token');
});

test('unavailable backend remains guest-capable and retry restores account entry', async ({ page }) => {
  let available = false;
  await page.route(api + '/auth/session', route => available
    ? route.fulfill({ json: { user: null, csrf_token: 'csrf-test' } })
    : route.fulfill({ status: 503 }));
  await page.goto('/#acceso');
  await page.locator('#idioma').selectOption('es');
  await expect(page.locator('#account-status')).toContainText('No se pudo conectar');
  await expect(page.locator('#account-sign-in')).toBeHidden();
  await page.locator('#titulo').fill('Mantas');
  available = true;
  await page.locator('#account-retry').click();
  await expect(page.locator('#account-sign-in')).toBeVisible();
  await expect(page.locator('#titulo')).toHaveValue('Mantas');
});

test('failed logout does not pretend the server session was revoked', async ({ page }) => {
  await page.route(api + '/auth/session', route => route.fulfill({ json: { user: { id: 42, name: 'María Peña' }, csrf_token: 'csrf-test' } }));
  await page.route(api + '/auth/logout', route => route.fulfill({ status: 503 }));
  await page.goto('/#acceso');
  await page.locator('#idioma').selectOption('es');
  await page.locator('#account-sign-out').click();
  await expect(page.locator('#account-status')).toContainText('No se pudo cerrar');
  await expect(page.locator('#account-sign-out')).toBeVisible();
  await page.route(api + '/auth/session', route => route.fulfill({ status: 503 }));
  await page.locator('#account-retry').click();
  await expect(page.locator('#account-status')).toContainText('No se pudo conectar');
  await expect(page.locator('#account-sign-out')).toBeDisabled();
});
