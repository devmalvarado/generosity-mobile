# Generosity web portal

Static HTML/CSS/JavaScript with a PHP-backed account and authorized-center workspace.
The public cards explain causes and ways to help. Registration and center-access
links lead to the location intake and authorized workspace; fictional centers,
figures and disconnected search/donation/publication forms have been removed.
The staff workspace reads assigned centers and manages API needs/contributions.
Source assets are documented in [PROVENANCE.md](PROVENANCE.md).

Spanish/English content identifies the institute, its RFC, fiscal address and
staff responsibility for support, legal and privacy requests. The working temporary
contact is `notificaciones@generosity.global`; the three separate mailboxes remain
deferred. Intake/footer links open `/legal/privacy` and `/legal/terms` on the API
in the selected language. The full notices live in `backend/resources/legal/`.

`#registrar-sede` contains the initial organization/location application in Spanish
and English. Visitors can prepare the form; submission requires a verified
Generosity account. The PHP API stores a private pending request and returns a
reference. The applicant can list their own requests. This does not publish a
center or grant staff membership. One application represents one location.

## Local preview and checks

Run `npm ci`, then `npm start` for `http://127.0.0.1:47321/`.
Run `npm test` for Chromium/WebKit checks, including the staff workspace.
`tests/portal.spec.js` checks public navigation, institute details, removal of
placeholder interactions, language changes, keyboard use and responsive layout.
`tests/center-applications.spec.js` covers the intake, pending retry, account
isolation, validation and mobile layout. Server integration tests are in
`backend/tests/Feature/CenterApplicationTest.php`.
API calls target `https://api.generosity.global`; browser tests intercept them with
fixtures. A local static preview is not a local API deployment or proof of live
credentials. Tests cover 320–1280 CSS pixels and enlarged text; physical-device
keyboard and screen-reader acceptance remain open.

## Publication

The [2026-11-01 release candidate](../docs/releases/account-portal-2026-11-01.md)
stages portal/API source separately. Its workflow waits for the intake route and
corrected account UI before uploading. Legal/operator inputs and production
acceptance remain required; this preparation publishes nothing by itself.

The existing main-branch deployment publishes only `index.html`, `styles.css`,
`portal.js` and `assets/` when those paths change. It does not deploy the backend,
upload the repository or delete the rest of the site. No deployment is performed
by these changes. Keep dependencies, tests, screenshots and server secrets out of
the web root. Production privacy/terms destinations still need approved content.

The new intake additionally needs its PHP routes/controller and
`2026_11_01_000003_create_center_applications.php` migration released before the
landing form is published. These are not part of the static deployment workflow.
No production application or migration has been created by this local work.

See [center portal documentation](../docs/center-portal.md) and
[integration status](../docs/testing/backend-integration.md).
See [location intake checks and remaining work](../docs/testing/center-applications.md).
