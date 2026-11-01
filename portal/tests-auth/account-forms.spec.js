const { test, expect } = require('@playwright/test');

for (const locale of ['es', 'en']) {
  test(`${locale}: legal pages fit mobile and registration links expose the current policy`, async ({ page }) => {
    await page.setViewportSize({ width: 320, height: 800 });
    await page.goto(`/auth/sign-in?client=web&locale=${locale}&screen=register`);
    const form = page.locator('form[action="/auth/account/register"]');
    for (const field of ['adult_confirmed', 'terms_accepted', 'privacy_acknowledged']) {
      await expect(form.locator(`input[name=${field}]`)).not.toBeChecked();
      await expect(form.locator(`input[name=${field}]`)).toHaveAttribute('required', '');
    }
    for (const doc of ['terms', 'privacy']) {
      const link = form.locator(`a[href*="/legal/${doc}"]`);
      expect(await link.getAttribute('href')).toContain(`locale=${locale}`);
    }
    for (const doc of ['terms', 'privacy']) {
      await page.goto(`/legal/${doc}?locale=${locale}`);
      await expect(page.locator('html')).toHaveAttribute('lang', locale);
      await expect(page.locator('article')).toContainText('2026-11-01');
      await expect(page.locator('article')).toContainText('notificaciones@generosity.global');
      await page.addStyleTag({ content: 'body{font-size:200% !important}' });
      expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true);
    }
  });

  test(`${locale}: recovery shows mismatch and invalid-link guidance without keeping passwords`, async ({ page, browserName }) => {
    await page.setViewportSize({ width: 320, height: 800 });
    await page.goto(`/reset-password/invalid-browser-test-token?email=${browserName}-${locale}-browser-check%40example.invalid&locale=${locale}`);
    await expect(page.locator('html')).toHaveAttribute('lang', locale);
    await page.locator('#new-password').fill('BrowserCheckPassword123');
    await page.locator('#confirm-password').fill('DifferentPassword123');
    await page.getByRole('button', { name: locale === 'es' ? 'Restablecer contraseña' : 'Reset Password' }).click();
    await expect(page.getByRole('alert')).toContainText(locale === 'es' ? 'no coincide' : 'does not match');
    await expect(page.locator('#new-password')).toHaveValue('');
    await expect(page.locator('#confirm-password')).toHaveValue('');
    await expect(page.locator('#new-password')).toHaveAttribute('aria-invalid', 'true');
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true);
    await page.locator('#new-password').fill('BrowserCheckPassword123');
    await page.locator('#confirm-password').fill('BrowserCheckPassword123');
    await page.getByRole('button', { name: locale === 'es' ? 'Restablecer contraseña' : 'Reset Password' }).click();
    await expect(page.getByRole('heading', { level: 1 })).toHaveText(locale === 'es' ? 'Este enlace de recuperación no es válido' : 'This recovery link is not valid');
    await page.getByRole('link', { name: locale === 'es' ? 'Solicitar un nuevo enlace de recuperación' : 'Request a new recovery link' }).click();
    await expect(page.locator('details[open] input[type=email]')).toBeVisible();
  });

  if (locale === 'es') test(`${locale}: registration errors preserve name/email, expose no password and fit enlarged mobile text`, async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 });
    await page.goto(`/auth/sign-in?client=web&locale=${locale}&screen=register`);
    await page.locator('#new-name').fill('María Peña');
    await page.locator('#new-email').fill(`${locale}-browser-check@example.invalid`);
    await page.locator('#new-password').fill('lowercaseonly');
    for (const field of ['adult_confirmed', 'terms_accepted', 'privacy_acknowledged']) await page.locator(`form[action="/auth/account/register"] input[name=${field}]`).check();
    await page.locator('form[action="/auth/account/register"] button').click();
    await expect(page.getByRole('alert')).toContainText(locale === 'es' ? 'mayúsculas' : 'uppercase');
    await expect(page.locator('#new-name')).toHaveValue('María Peña');
    await expect(page.locator('#new-email')).toHaveValue(`${locale}-browser-check@example.invalid`);
    await expect(page.locator('#new-password')).toHaveValue('');
    await page.addStyleTag({ content: 'body{font-size:200% !important}' });
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true);
  });
}
