# Generosity web portal

Static HTML/CSS/JavaScript with a PHP-backed account and authorized-center workspace.
The public cards explain causes and ways to help. Registration and center-access
links lead to the location intake and authorized workspace; fictional centers,
figures and disconnected search/donation/publication forms have been removed.
The staff workspace reads assigned centers and manages API needs/contributions.
Source assets are documented in [PROVENANCE.md](PROVENANCE.md).

`#como-funciona` illustrates the publication → iPhone notification → confirmed
contribution journey in Spanish and English. Its three HTML/CSS phones use shared
design tokens and generated brand assets; they are not interactive forms or live
records. Captions explain partial contributions and center-confirmed receipt.
The illustration does not request notification permission or send notifications;
real iOS delivery still requires the pending APNs configuration.

Spanish/English content identifies the institute, its RFC, fiscal address and
staff responsibility for support, legal and privacy requests. The working temporary
contact is `notificaciones@generosity.global`; the three separate mailboxes remain
deferred. Intake/footer links open `/legal/privacy` and `/legal/terms` on the API
in the selected language. The full notices live in `backend/resources/legal/`.

`/registrar-sede/` contains the initial organization/location application in Spanish
and English. Visitors can prepare the form; submission requires a verified
Generosity account. The PHP API stores a private pending request and returns a
reference. The applicant can list their own requests. This does not publish a
center or grant staff membership. One application represents one location.

## Independent routes

| Path | Purpose |
| --- | --- |
| `/` | Public presentation, illustrative helping journey and links; no forms or session requests. |
| `/registrar-sede/` | One location application, account access and private application tracker. |
| `/kindness-center/` | Account access and the authorized-center needs/contributions workspace. |
| `/ayudar/` | Real map and public directory; optional location, center/need details, offers and contribution history. |
| `/app/` | Interactive product tour with all 34 design-preview screens, ES/EN and light/dark appearances. |

Directory indexes support direct opening, reloading and trailing-slash redirects
without a client-side router. Old landing fragments (`#registrar-sede`, `#acceso`,
`#center-workspace`, `#descubrir`, `#centro/...`, `#necesidad/...`) redirect to their
new page, preserving the query and fragment. Shared account/forms code initializes
only components present on that page. Sign-in remains hosted by the API in a new
tab, preserving the originating form and refreshing its session on focus.

The `/app/` tour is adapted from `codex/visual-design-map`'s `design/preview/`.
It does not expose specifications or task links. It presents current and planned
capabilities and keeps actions in memory; it never calls the API, sends data,
requests GPS or processes payments. Public-tour colors/radii and logos come from
the same generated design sources as the apps. This is a presentation route, not
an implementation or acceptance of the pending service-backed capabilities.

## Local preview and checks

Run `npm ci`, then `npm start` for `http://127.0.0.1:47321/`.
Run `npm test` for Chromium/WebKit checks, including the staff workspace.
`tests/portal.spec.js` checks public navigation, institute details, removal of
placeholder interactions, language changes, keyboard use and responsive layout.
`tests/center-applications.spec.js` covers the intake, pending retry, account
isolation, validation and mobile layout. Server integration tests are in
`backend/tests/Feature/CenterApplicationTest.php`.
`npm start` uses `scripts/serve-portal.py --live-catalog`, bound only to loopback.
It relays allowlisted **public GETs** to `https://api.generosity.global` because
production CORS permits `https://generosity.global`, not localhost. It forwards no
cookies, tokens or browser headers; private routes, mutations and redirects are
rejected. `--self-test` checks that boundary. Use `--port 47323` if the browser-test
server occupies 47321. The local page opens hosted sign-in but deliberately does
not claim a local authenticated session; real cookie acceptance requires the
published HTTPS site. The relay is excluded from the deployment inventory.

Public discovery reads the existing bounded `/api/v1/sync/catalog` snapshot with
category v2 and progress v1. The same relay also allowlists published
campaign, occurrence and meal list and detail GETs, plus need progress. It does
not relay inbox, preferences, sessions or writes. Search covers names, addresses and descriptions
across the snapshot, with accent-insensitive matching; it is **not a geocoder**.
Optional geolocation sorts by straight-line distance and fits nearby markers,
including results beyond the first 12-card page. Coordinates stay in memory.
Leaflet/OSM provides the web basemap; native Google Maps/MapKit are unchanged.
No map or permission failure blocks the list. The offer form appears only for an
opened need and refetches its authoritative record before preparing an offer.

Browser tests intercept API responses and OSM tiles; they do not run against the
live preview relay or fetch public tile infrastructure. A local preview is not a
local API deployment or proof of live credentials. Tests cover 320–1280 CSS pixels and enlarged text; physical-device
keyboard and screen-reader acceptance remain open.

## Publication

The [2026-11-02 release](../docs/releases/critical-foundation-2026-11-02.md)
published the account workspace and application tracker after their API routes
and additive migrations. Real Google web sign-in reads the account's own
applications and assigned-center state. Review decisions remain unavailable
until an operator configures the reviewer allowlist. A real center pilot still
requires the center, responsible account and actual need.

The main-branch deployment stages only the files explicitly listed in
`public-files.txt`, including the four directory routes and their public assets. It does not deploy the backend,
upload the repository or delete the rest of the site. Keep dependencies, tests,
screenshots and server secrets out of the web root. Published legal destinations
are hosted by the API.

An explicit `portal-release-*` Git tag also publishes that exact revision without
merging unrelated mobile/backend work into `main`. The workflow updates only the
two hosted privacy notices before the map, verifies and privately backs up their
previous contents, and repairs an outdated portal CORS configuration/cache when
needed. It does not publish API controllers, credentials or data. It then
uploads the public inventory. Old legal-page URLs redirect to the canonical API
documents. Do not move or reuse a published release tag.

The new intake additionally needs its PHP routes/controller and
`2026_11_01_000003_create_center_applications.php` migration released before the
landing form is published. These are not part of the static deployment workflow.
The review tracker additionally uses migration `2026_11_02_000002`; both migrations
are published. Requests do not publish centers or grant membership automatically.

Hostinger caches CSS/JS for a week. All operational HTML pages reference `styles.css` and
`portal.js` with `?v=` followed by the first 12 hexadecimal SHA-256 characters of
each file. The stylesheet imports `assets/design-tokens.css` the same way. Update
references from the innermost asset outward (including tour modules/tokens) whenever a file changes and run the
portal tests; a check rejects stale versions and missing design colors. This
prevents cached old scripts from failing against new HTML. Upload assets before
HTML during manual releases.

See [center portal documentation](../docs/center-portal.md) and
[integration status](../docs/testing/backend-integration.md).
See [location intake checks and remaining work](../docs/testing/center-applications.md).

The reviewed route separation and map were published as `portal-release-2026-11-05.2`.
See [release evidence and remaining write prerequisites](../docs/releases/portal-map-2026-11-05.md).
This checkpoint does not include later local changes.
`tests/routes.spec.js` verifies legacy redirects, direct entry, isolated requests,
mobile reflow, every tour screen in both languages and tour exit. The preview
packager uses the same public-file inventory and rewrites API metadata on every
operational route, retaining the isolated preview channel.
