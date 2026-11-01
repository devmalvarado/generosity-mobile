# Native/backend/center-workspace integration

Date: 2026-10-31 (America/Monterrey). Branch: `codex/backend-php-mysql`.
The API catalog/account sync increment is published with `1adf57a`. Native and
portal checks below are local evidence, not a live three-client release.
The center workspace is not on the live landing until its files reach `main`.

## Implemented boundaries

| Surface | Behavior | Main evidence |
| --- | --- | --- |
| PHP snapshots | Public allowlisted catalog; owned account history/visits; assigned-center discovery | `backend/tests/Feature/ApiTest.php`, `backend/openapi.json` |
| Shared state | Account-isolated catalog/history/outbox, save-before-send, stable operation keys, safe replay, visible rejection, private-session expiry independent of public refresh | `shared/src/commonTest/kotlin/org/generosity/BackendStoreTest.kt` |
| Android storage/transport | API-scoped atomic encrypted files in no-backup storage, Keystore key, credentials bound to account, HTTPS and no redirects | `AndroidConnection.kt`, `BackendStorageTest.kt` |
| iOS storage/transport | API-scoped atomic protected files excluded from backup, Keychain identity/token, account-bound HTTPS and no redirects | `IOSConnection.swift`, `AccountSessionTests.swift` |
| Native forms/history | Entered goods/time/personal-help fields; server statuses; owner cancellation and self-reported completion; no invented actual duration | `ContributionFlowTest.kt`, `BackendBridgeTests.swift`, PHP lifecycle tests |
| Center workspace | Assigned-center selection, paginated needs/contributions, draft/publish/edit/close, staff confirmation/fulfillment, explicit actual minutes | `portal/tests/center-workspace.spec.js`, PHP authorization/concurrency tests |
| Visits/preferences | Guest visits stay local; account visits queue and synchronize; notification preferences persist per account/device; no official-verification promotion | Shared/backend visit tests and native protected-store tests |
| Hosted account entry | Registration, recovery and verification resend preserve native PKCE/client/locale context | `backend/tests/Feature/SocialAuthTest.php` |
| Welcome/contact | Guest/sign-in/create-account routing; localized brand assets; only validated phone/email/http(s) contact actions | Native welcome tests, `ContactLinkTest.kt` |

Public example forms/cards remain labeled samples. Only the authenticated center
workspace writes to the API. Release native roots use the API; deterministic
catalogs are limited to debug/test entry points. The portal tests stub API responses;
backend tests and the HTTP smoke separately exercise real Laravel/MariaDB behavior.
These are complementary checks, not proof of a live three-client journey.

## Executable verification

Run from the repository root unless a working directory is stated:

```sh
# backend/; isolated generosity_test database
docker compose exec -T app composer test

# backend/; LOCAL development database, Mailpit, restart and temporary restore DB
python3 scripts/smoke.py

./gradlew :shared:jvmTest :androidApp:connectedDebugAndroidTest --offline
./gradlew :shared:prepareSharedBridge --offline
xcodegen generate --spec iosApp/project.yml
xcodebuild -project iosApp/Generosity.xcodeproj -scheme Generosity \
  -destination 'platform=iOS Simulator,name=iPhone 16 Pro' CODE_SIGNING_ALLOWED=NO test

# portal/
npm test

python3 scripts/check-localization.py
openspec validate --all --strict --no-interactive
git diff --check
```

Android requires the configured Android Studio JDK/SDK and a booted emulator.
Xcode needs the generated Shared framework. Test reports are under
`shared/build/test-results/jvmTest/`, `androidApp/build/reports/androidTests/`,
Xcode's `.xcresult`, and `portal/test-results/`. Browser screenshots are generated
under `portal/screenshots/`. Do not commit tokens, test databases or email messages.

Verified in this increment:

- PHP: 35 tests / 393 assertions, real MariaDB in the isolated test database.
- Local HTTP smoke: registration, queued email to Mailpit, login, assigned centers,
  catalog/account snapshots, actual duration, visits, restart and exact operation
  replay passed. A temporary SQL restore matched business tables, including visits;
  only that temporary database was removed. Labeled fixtures remain locally.
- Shared JVM: 69 tests passed, including malformed/deep data, cross-account snapshot
  rejection, queue durability, lost responses, storage failure and expired sessions.
- Android API 36 emulator: all 15 existing regression/integration tests passed.
  After the final isolation/copy changes, protected-storage, contribution/retry and
  the new welcome test passed (3 targeted tests). Welcome checks English/Spanish
  actions at 200% font scale. This is 16 distinct tests, not one 16-test final run.
- iOS 18.6 iPhone 16 Pro simulator: 11 existing UI tests passed; 6 unit/bridge tests
  passed after the integration updates. The new welcome UI test then passed in
  both languages, including guest entry and saved entry after restart. Provider
  browser returns and physical-device acceptance remain separate gates.
  Screenshot inspection also corrected the primary button's foreground contrast;
  welcome/profile then passed a final two-test run. Captures:
  [Spanish](screenshots/integration/ios-welcome-es.png) and
  [English](screenshots/integration/ios-welcome-en.png).
- Portal: 53 passed, 1 existing WebKit screenshot skip, Chromium and WebKit.
  Seven workspace cases per engine include lost response/reload, exact-key retry,
  conflict draft preservation, account switching, storage-deletion failure, actual
  duration retained across session refresh and narrow-screen text enlargement.
- Localization: 108 resource keys, English/Spanish parity, plurals, permissions,
  UTF-8/NFC and placeholders passed. Strict OpenSpec: 16 items passed.

## Queue and cache semantics

Each native account has separate history, preferences and queue keys, within an
API-specific protected namespace. Sign-out clears private UI immediately and
suspends that account's pending operations; signing into another account cannot
submit or display them. Guest visits are never silently attributed/uploaded after
registration. Lost/failed writes cannot report local success.

Unknown network outcomes retain the original operation key/body and cannot be
discarded. Known 400/403/404/409/422 rejections remain visible, are not automatically
retried and may be explicitly discarded. Reauthentication is required after 401;
429/server/network failures keep pending work. A malformed snapshot never replaces
the accepted cache. Backend authorization is rechecked before idempotent replay.

The portal saves one uncertain submission per account in session storage before
sending. Reload can retry its exact body/key. Account switching clears private
views/drafts; failure to remove browser storage never prevents that UI clearing.
An unknown submission must be reconciled before starting another one. New need
drafts that have not been submitted are not durable across a tab reload.

This first sync is a bounded full snapshot: 2,000 centers, 10,000 published needs,
5,000 contributions/visits per account and 100 native queued operations. The server
rejects an oversized snapshot rather than silently truncating it. Cursor paging,
tombstones, store migrations, background sync and larger-scale performance remain
open. Device preferences are not yet server-synchronized. Invalid/unavailable
cached data reports an error; the app does not invent replacement contributions.

## Deployment and remaining acceptance

Production received both migrations in the `1adf57a` publish:

- `2026_11_01_000001_add_contribution_context`: title/unit/actual-duration snapshots.
- `2026_11_01_000002_create_visit_signals`: owned explicit visit records.

`GET /api/v1/sync/catalog` returned 200 with `catalog`, `revision`, and `updatedAt`,
and zero centers. That is not a fabricated demo catalog and not proof of a staffed
center. Portal workspace tests intercept the API; they are not live CORS or
production-center acceptance. The selective landing workflow does not deploy PHP.
Rolling application code back must not drop newly recorded visits/history.

On 2026-11-01, the user authorized Hostinger SMTP activation separately. The
production queue cron delivered an approved Gmail test email, Hostinger recorded
it as delivered, and the recipient confirmed Gmail Inbox placement on the same
date. The Laravel scheduler ran automatically. Queue/failed-job
counts were zero and the API health/private-file checks passed. See
[SMTP activation evidence](../hostinger-backend.md#smtp-activation-evidence--2026-11-01).
The subsequent [live account-email review](account-email-journeys.md) exercised
registration, resend, verification and password reset using actual queued mail.
Tampered/expired links, reset replay, password replacement and session revocation
passed. The review found English-only email/reset content, raw JSON completion
pages and invisible reset validation errors; human-facing acceptance remains open.
The expanded backend regression suite passed 38 tests / 425 assertions.

Still open:

- The real portal → Android/iOS → center-confirmation → history journey with
  production HTTPS/CORS, account switching and two devices; native offline/conflict
  UX for every rejection, migration/recovery case, unit-aware impact presentation
  and editable rejected drafts.
- Actual Google/Apple credentials and device acceptance; Facebook, phone verification,
  provider unlink/deletion and full registration/recovery UX, including the three
  findings in the account-email review and provider quota/failure acceptance.
  Phone verification is required for group/event creation; the SMS provider remains
  unselected. No subscription or SMS delivery was enabled by this work.
- Full need preview/inline-error UX, audit/urgent-event integration and live browser
  session acceptance. Physical mobile virtual keyboards and screen readers remain
  part of the release matrix, not covered by browser emulation alone.
- GPS/manual origin, Google Maps key acceptance, actual notification/reminder
  delivery, preference/device endpoints, legal welcome links/illustration and full
  visual/accessibility acceptance. Controls explicitly state delivery is unavailable.
- Food rescue and expanded categories now have their own active planning change;
  they are not implemented by the directory documentation update.

Broad foundation tasks remain unchecked where any of their acceptance conditions
are missing. Do not archive a change based only on structural validation.
