const { test, expect } = require('@playwright/test');
const tokens = require('../../design/tokens.json');

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
