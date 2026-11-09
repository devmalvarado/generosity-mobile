# Main / preview integration — 2026-11-08

## Scope and preservation

The owner approved saving existing work, integrating main, validating the full
helping lifecycle, then publishing the API before the portal and native builds.
Checkpoint `9c6c578` preserves all previous local work. Integration branch:
`codex/integrate-production-2026-11-08`, incorporating `origin/main` at `651c1bf`.

Production and preview retain separate databases, credentials, channels, app IDs,
auth callbacks, storage and provider configuration. No preview database is copied
into production. Unfinished participation/payment features remain disabled.

The native design is the default after entry. Production exposes only connected
routes, including discovery, needs, offers/history, impact, account/settings and
inbox. Mock reviews, events, awards, verification and payments cannot be opened
in production, even if their group is granted. Preview retains the full prototype.
Missing or revoked groups retain unavailable/retry behavior.

The merge also preserves main's compact institute footer and standalone legal
pages while keeping the newer dedicated routes, map and public app tour. Legal
links resolve from nested routes; ES/EN notices are generated from the canonical
backend sources and remain readable without JavaScript. Landing margins, wordmark
and card actions are aligned. The portal deploy waits for the feature API.

## Validation before deployment

- Backend: 119 tests / 6,085 assertions passed on isolated `generosity_test`.
- Shared JVM: 94 tests passed, including production-only navigation restrictions.
- Local HTTP smoke: registration/mail, browser session/CSRF, staff isolation,
  publication, partial offer/confirmation/receipt/history, capacity/cancellation,
  visits, actual duration, restart, idempotent replay and isolated backup restore.
- Hosted preview HTTPS: a temporary 10-box need received a 1-box offer (10%).
  Donor confirmation was denied; center confirmation/receipt, synchronized
  history, duplicate replay and cancellation passed. The need was closed, test
  accounts deactivated and their tokens revoked. Existing preview records remain.
  These accounts were provisioned for API acceptance, not a social-provider test.
- Portal: 148 tests passed initially; one asynchronous assertion failed before
  its request arrived. Added an explicit wait and both Chromium/WebKit rechecks
  passed (149 passing cases total; one existing skip).
- 18 strict OpenSpec items passed; design generation and environment-isolation
  checks passed; API packaging/privacy tests passed.

## Production recovery point

Private server backup: `integration-20261108-022859` under the API's
`private/.deployments/`. Includes `database.sql`, `application.tar.gz` and a
private table/hash snapshot. Neither the backup nor credentials enter Git/public
files. Baseline: 2 users, 171 centers, 3 needs, 0 contributions, 2 memberships and
1 social identity. The private environment file is fingerprinted for comparison.

## Published API and portal

- Main was integrated at `7494a62`. API follow-ups `d18e4fc` and `3836020`
  clear stale Laravel configuration before bootstrap and validate the complete
  auth asset inventory (including generated CSS) before remote mutation.
- Initial API jobs exposed those two packaging issues. Both were fixed, with
  publisher self-checks, and the final [API workflow succeeded](https://github.com/devmalvarado/generosity-mobile/actions/runs/42551773970).
- The [portal workflow succeeded](https://github.com/devmalvarado/generosity-mobile/actions/runs/42551480606)
  after the API/feature prerequisite gate. The site is [generosity.global](https://generosity.global/).
  Design-system CI also passed after aligning the check with all five generated outputs.
- Post-deployment comparison matched exact row fingerprints for all baseline
  business tables above and the private `.env` fingerprint. No account, catalog,
  need, contribution, membership or social identity was replaced by test data.
- Audited activation enabled only `preview.discovery`, `preview.contributions`,
  `preview.impact`, `preview.communication` and `preview.account`. The other five
  design groups and all nine participation controls remain off. The three
  foundation controls remain enabled. The feature administrator allowlist is
  empty: no production identity received new privileges during this operation.
- Live read-only Chromium verification passed at 390 px for `/`, `/registrar-sede/`,
  `/kindness-center/` and `/ayudar/`, with no horizontal overflow or JavaScript
  errors. The catalog rendered cards and markers; the second card selected its
  map popup. ES/EN legal pages contain the institute identity; `/app/` exposes
  its 34-screen tour; hosted account entry shows registration and loads its CSS.
  Production feature JSON reports the expected channel and restricted groups.
- Web evidence: [published map](../testing/screenshots/integration-2026-11-08/web-production-map.png).

## Native acceptance and artifacts

- Android API 34: four isolated native design tests and two connected-design
  cases passed. The latter cover actual need IDs, entered partial offers, durable
  retry/history and unavailable/revoked groups. These six cases do not mutate production.
- Android normal production and Preview entry tests both passed against their
  configured live APIs, including activity recreation. Production inventory
  excludes saved centers, reviews, awards, events and monetary donations.
  The initial emulator attempt failed because DNS could not resolve the API;
  a cold restart using the Mac's DNS restored connectivity without an app-code workaround.
- iOS production: 19 unit/bridge/localization/map/push-signal tests and four native
  design UI tests passed, including the complete isolated 34-screen tour. An
  additional normal-entry test passed against production through two launches,
  with unavailable fixture routes absent. This is guest/read-only acceptance,
  not a fresh real-provider login or center transaction from a phone.
- iOS Preview: both normal-entry/relaunch and missing-group fail-closed tests
  passed with the Preview configuration. It retains its separate bundle/API,
  without production Firebase configuration.
- [Android production map](../testing/screenshots/integration-2026-11-08/android-production-map.png)
  was inspected with real Google tiles, published center pins, detail card and
  Material 3 navigation. No permission or map-key changes were needed.
- [iOS production map](../testing/screenshots/integration-2026-11-08/ios-production-map.png)
  shows live MapKit/catalog and native bars. The simulator had no recent GPS fix;
  it correctly offered retry/address search. This is not real-location acceptance.
- Local Android debug artifacts are in ignored `build/releases/2026-11-08/`:
  `Generosity.apk` (production) and `Generosity-Pruebas.apk` (preview), with
  `checksums.json`. Both use the existing development signer, with separate
  packages, API origins, storage and provider configuration. They are not store releases.

## Remaining release acceptance

Physical devices were unavailable during this run; their earlier Preview installs
have not received this integration. Store distribution, production feature-admin
assignment, real-center/browser write acceptance, Apple/APNs credentials, preview
identity providers and full assistive-technology acceptance remain separate.
No whole OpenSpec change is archived solely on these builds/tests/deployments.
