const { defineConfig } = require('@playwright/test');

// These checks submit invalid forms only to the local Docker API; no real email is sent.
module.exports = defineConfig({
  testDir: './tests-auth',
  workers: 1,
  fullyParallel: false,
  timeout: 30000,
  reporter: 'list',
  use: { baseURL: 'http://127.0.0.1:8088', trace: 'off' },
  projects: [
    { name: 'chromium', use: { browserName: 'chromium' } },
    { name: 'webkit', use: { browserName: 'webkit' } }
  ]
});
