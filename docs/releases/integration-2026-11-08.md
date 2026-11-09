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

## Remaining release acceptance

API publication, post-deployment preservation checks, portal verification and
native acceptance results are recorded below as they complete. Physical devices
were unavailable during this run. Store distribution, Apple/APNs credentials,
preview identity providers and full assistive-technology acceptance remain separate.
