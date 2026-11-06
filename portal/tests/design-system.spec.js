const { test, expect } = require('./browser');
const tokens = require('../../design/tokens.json');
const { readFileSync } = require('fs');
const path = require('path');
const { createHash } = require('crypto');

test('every public route keeps the published favicon instead of the app launcher export', async ({ page }) => {
  await page.route('https://api.generosity.global/**', route => route.abort());
  const favicon = readFileSync(path.join(__dirname, '../assets/favicon.png'));
  expect(favicon).toEqual(readFileSync(path.join(__dirname, '../../assets/brand/source/published-favicon.png')));
  expect([favicon.readUInt32BE(16), favicon.readUInt32BE(20)]).toEqual([192, 192]);
  expect(readFileSync(path.join(__dirname, '../assets/apple-touch-icon.png')))
    .toEqual(readFileSync(path.join(__dirname, '../../assets/brand/source/published-apple-touch-icon.png')));
  const version = createHash('sha256').update(favicon).digest('hex').slice(0, 12);
  for (const route of ['/', '/ayudar/', '/registrar-sede/', '/kindness-center/', '/app/']) {
    await page.goto(route);
    await expect(page.locator('link[rel=icon]')).toHaveAttribute('href', '/assets/favicon.png?v=' + version);
    await expect(page.locator('link[rel=icon]')).toHaveAttribute('sizes', '192x192');
  }
});

test('shared action, font and radius variables drive the rendered components', async ({ page }) => {
  await page.route('https://api.generosity.global/**', route => route.abort());
  await page.goto('/');
  const button = page.locator('.hero-actions .solid');
  const rgb = hex => `rgb(${hex.match(/\w\w/g).map(x => parseInt(x, 16)).join(', ')})`;
  await expect(button).toHaveCSS('background-color', rgb(tokens.colors.accent[0]));
  await page.addStyleTag({ content: ':root { --gen-accent: #184c70; --gen-font-body: Georgia, serif; --gen-radius-pill: 9px; }' });
  await expect(button).toHaveCSS('background-color', 'rgb(24, 76, 112)');
  await expect(button).toHaveCSS('font-family', 'Georgia, serif');
  await expect(button).toHaveCSS('border-radius', '9px');
  expect(await page.locator('.brand img').first().evaluate(img => img.complete && img.naturalWidth > 0)).toBe(true);
});
