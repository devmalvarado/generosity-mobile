# OpenSpec reconciliation — 2026-11-04

Branch: `codex/backend-php-mysql`. Scope: verify pending local work, correct
implementation status, close evidenced tasks and commit the resulting source.
No production deployment, provider configuration, feature activation, physical
phone installation or catalog mutation is part of this reconciliation.
The tested implementation is committed as `1dd3498` (gated native designs and
web participation foundations); the following documentation commit records this
task reconciliation. Neither commit implies publication to the hosting service.

## Task decisions

The starting inventory was 11 active changes, 528 checkbox tasks, 133 checked
and 395 open. Broad tasks retain every original criterion; implementation is not
split into artificial completions to conceal missing acceptance.
Six existing tasks are now closed: 4.6, 7.8, 16.1, 16.2, 16.3 and 16.12.
The resulting inventory is 139 checked and 389 open; the foundation has 93
checked and 84 open. All 11 changes remain active, with no archive claimed.

| Foundation task | Evidence for closure |
| --- | --- |
| 4.6 | `GenerosityStore` resolves shared locale before the first render. Fresh iOS simulator tests cover ordered/fallback device languages, override/restart/device changes and bilingual welcome with guest restart. |
| 7.8 | `docs/social-sign-in.md` and `docs/testing/account-email-journeys.md` document Laravel account authority, Google/Apple, verification, registration/recovery, PKCE, protected storage, expiry/logout and backend validation with scenario/test mapping. Live-provider acceptance remains in 7.9/7.10/7.18b/18.5. |
| 16.1 | Canonical `portal/`, preserved source PoC, updated `portal/PROVENANCE.md`, README commands, scoped static workflow and separate API/session deployment. |
| 16.2 | `portal.spec.js`, `center-applications.spec.js` and `public-discovery.spec.js` verify honest public/failed submission states, account entry, retained input and idempotent retries. |
| 16.3 | Chromium/WebKit tests verify narrow defaults, 320/360/390/768/1280 reflow, long text, menu/filter state, 44px controls and 200% text. Real keyboard and assistive-technology acceptance remain in 16.11. |
| 16.12 | New `ApiTest` cases exercise actual database-backed cookies through production CSRF middleware: invalid/missing CSRF, foreign origin, revoked cookies and fresh anonymous CSRF all deny staff mutations without changing domain data. Existing API/private-draft and browser escaped-content cases cover the other criteria. |

Other implementation/acceptance decisions are recorded in the foundation's
[current status](../../openspec/changes/define-generosity-mobile-app-foundation/implementation-status.md).
In particular, cache/outbox is implemented but migrations/failures/full sync are
not fully accepted; native forms lack the complete rejected-draft correction
flow; center authoring still needs preview, field-specific errors and need audits.
The new design binds supported services, but does not implement all roadmap
features represented by its 34 routes. No change is ready for archive.

## Checks performed on this working tree

- Shared JVM: 93 tests, zero failures. Android host: 93 tests, zero failures.
- Android production and preview debug APKs built; production instrumentation
  compiled. On the Android 14 Google APIs emulator, `ConnectedDesignTest` and
  `NotificationInboxTest` passed four cases, covering real-state binding through
  controlled responses, entered offer/outbox/history, offline retry, feature
  revocation, inbox destination and sign-out isolation.
- iOS 27 iPhone 17 simulator: all 19 unit/bridge/localization tests and four
  selected UI cases passed (ordered languages, unsupported fallback, saved
  override/restart/device changes, connected native route inventory).
  The separate bilingual welcome/guest-restart UI test also passed, bringing
  this run to 24 iOS tests with no failures.
- Portal: 111 Chromium/WebKit tests passed; one pre-existing duplicate WebKit
  screenshot test is intentionally skipped. API responses are intercepted
  fixtures, not proof of production cross-client acceptance.
- Baseline backend: 112 tests / 5,865 assertions passed on isolated
  `generosity_test` before the additional closeout regressions.
- Final backend: 119 tests / 6,077 assertions passed, including participation
  authorization/overlap regressions and the new staff-cookie tests. The CORS
  assertion matches the framework's single trusted-origin response header;
  foreign callers remain unauthorized independently of browser enforcement.
- Design outputs, environment-isolation self-test, preview-package self-test,
  localization validator self-test and all 149 ES/EN keys passed. Design-system
  tests passed four cases; one optional asset-rendering case was skipped because
  this interpreter lacks authoring dependencies.
- PHP formatting passed for the eight reviewed implementation/test files;
  strict OpenSpec validation passed all 18 items, and `git diff --check` passed.

## Safety corrections in the local participation increment

Review identified current-authority checks needed before assisted cancellation
and idempotent replay, a publication toggle bypass on existing outcome drafts,
claim/enrollment overlaps that could consume two seats, and missing occurrence
versions in public reads. These fixes and their focused regression results are
covered by the final backend run above. They do not complete the broader web,
community or assisted-participation specs.

## Remaining release gates

Feature API/migrations, administrator/tester selection, preview infrastructure
and updated client distribution remain open. Google has live web/iOS evidence;
remaining Android/linking/expiry cases, Apple credentials/APNs and phone-provider
configuration remain separate. Reminders/proximity, full physical push, permanent
account erasure, payments and independent legal/accessibility acceptance are not
certified by this run. Main/delta specs must be reconciled only when the relevant
change is actually ready to archive.
