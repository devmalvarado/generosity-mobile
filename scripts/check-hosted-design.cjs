const { chromium, webkit } = require('../portal/node_modules/playwright');
const assert = require('node:assert/strict');
(async () => {
  for (const engine of [chromium, webkit]) {
    const browser = await engine.launch();
    const page = await browser.newPage({ viewport: { width: 320, height: 800 } });
    const violations = [];
    page.on('console', message => { if (/Content Security Policy|violates.*directive|Refused to load.*style/i.test(message.text())) violations.push(message.text()); });
    for (const locale of ['es', 'en']) {
      const response = await page.goto('http://127.0.0.1:8088/auth/sign-in?locale=' + locale);
      assert.equal(response.status(), 200);
      assert.match(response.headers()['content-security-policy'], /style-src 'self'/);
      await page.waitForFunction(() => getComputedStyle(document.documentElement).getPropertyValue('--gen-accent').trim() === '#C6401D');
      assert.equal(await page.locator('.account-modes a[aria-current=page]').evaluate(el => getComputedStyle(el).backgroundColor), 'rgb(198, 64, 29)');
      assert.equal(await page.locator('.provider-google').evaluate(el => getComputedStyle(el).backgroundColor), 'rgb(255, 255, 255)');
      await page.addStyleTag({ content: 'html { font-size: 200%; }' });
      assert.ok(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth + 1), 'Account page overflows with 200% text: ' + locale);
      if (engine === chromium && locale === 'es') await page.screenshot({ path: '/tmp/generosity-design-account-es.png', fullPage: true });
      await page.goto('http://127.0.0.1:8088/legal/privacy?locale=' + locale);
      await page.waitForFunction(() => getComputedStyle(document.documentElement).getPropertyValue('--gen-accent').trim() === '#C6401D');
    }
    assert.deepEqual(violations, []);
    await browser.close();
  }
  console.log('Account and legal CSS/CSP verified in Chromium and WebKit, ES/EN, 320px and 200% text. Provider branding unchanged.');
})().catch(error => { console.error(error.message); process.exit(1); });
