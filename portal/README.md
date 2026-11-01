# Generosity web portal

Static HTML/CSS/JavaScript with a PHP-backed account and authorized-center workspace.
The public cards and donation form remain labeled samples; the staff workspace
reads assigned centers and manages real API needs/contributions when the API is
configured. Source assets are documented in [PROVENANCE.md](PROVENANCE.md).

## Local preview and checks

Run `npm ci`, then `npm start` for `http://127.0.0.1:47321/`.
Run `npm test` for Chromium/WebKit checks, including the staff workspace.
API calls target `https://api.generosity.global`; browser tests intercept them with
fixtures. A local static preview is not a local API deployment or proof of live
credentials. Tests cover 320–1280 CSS pixels and enlarged text; physical-device
keyboard and screen-reader acceptance remain open.

## Publication

The existing main-branch deployment publishes only `index.html`, `styles.css`,
`portal.js` and `assets/` when those paths change. It does not deploy the backend,
upload the repository or delete the rest of the site. No deployment is performed
by these changes. Keep dependencies, tests, screenshots and server secrets out of
the web root. Production privacy/terms destinations still need approved content.

See [center portal documentation](../docs/center-portal.md) and
[integration status](../docs/testing/backend-integration.md).
