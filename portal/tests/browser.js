const base = require('@playwright/test');

// Browser tests never fetch public OSM tiles; real cartography is reviewed manually.
const test = base.test.extend({
  context: async ({ context }, use) => {
    await context.route('https://tile.openstreetmap.org/**', route => route.fulfill({
      contentType: 'image/png',
      body: Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQIHWP4z8DwHwAFgAI/ScLttAAAAABJRU5ErkJggg==', 'base64')
    }));
    await use(context);
  }
});
module.exports = { test, expect: base.expect };
