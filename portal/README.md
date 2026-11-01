# Generosity web portal

Static HTML/CSS/JavaScript with a PHP-backed account and authorized-center workspace.
The public cards show concrete types of support: food parcels, school supplies and companionship. Registration and center-access
links lead to the location intake and authorized workspace; fictional centers,
figures and disconnected search/donation/publication forms have been removed.
The staff workspace reads assigned centers and manages API needs/contributions.
Source assets are documented in [PROVENANCE.md](PROVENANCE.md).

The landing identifies the institute only in a short footer credit. Separate
Spanish/English legal pages contain its RFC, address and staff responsibility. The working temporary
contact is `notificaciones@generosity.global`; the three separate mailboxes remain
deferred. Intake/footer links open standalone pages on the main website:
`aviso-de-privacidad.html`, `terminos-y-condiciones.html`, `privacy-notice.html`
and `terms-and-conditions.html`. They work without JavaScript or API requests.
The canonical notices remain in `backend/resources/legal/`; with the existing
backend Docker service running, run `python3 scripts/build-portal-legal.py` from
the repository root after changing them. `--check` detects stale generated pages.
The API legal URLs remain available for the hosted authentication flow.

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

The [2026-11-01 account/portal release](../docs/releases/account-portal-2026-11-01.md)
published the API and intake migration before the landing. The current portal
workflow stages an explicit eleven-file allowlist: index, CSS, JS, four legal
pages and four brand assets. It does not upload the repository, private config,
tests or screenshots and does not delete the rest of the site. API publication
remains separate. Update generated legal pages in the same commit as their source.
A real applicant/center pilot and physical-device acceptance remain pending.

See [center portal documentation](../docs/center-portal.md) and
[integration status](../docs/testing/backend-integration.md).
See [location intake checks and remaining work](../docs/testing/center-applications.md).
