const { defineConfig } = require("@playwright/test");

module.exports = defineConfig({
  testDir: "./tests",
  timeout: 30000,
  expect: { timeout: 5000 },
  fullyParallel: true,
  reporter: "list",
  use: {
    baseURL: "http://127.0.0.1:47321",
    trace: "off"
  },
  webServer: {
    command: "python3 -m http.server 47321 --bind 127.0.0.1",
    url: "http://127.0.0.1:47321",
    reuseExistingServer: !process.env.CI
  },
  projects: [
    { name: "chromium", use: { browserName: "chromium" } },
    { name: "webkit", use: { browserName: "webkit" } }
  ]
});
