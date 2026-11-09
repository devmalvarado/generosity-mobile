# Partial need contributions and progress

Initial local verification: 2026-11-02 (America/Monterrey), on
`codex/backend-php-mysql`. The local results below are retained with their original
scope; subsequent preview deployment/distribution is recorded near the end.
No production progress rollout is certified by this record.

## Behavior and authority

- A need for 10 boxes of 10 items uses target `10` and unit `boxes of 10 items`.
  One accepted box offer commits 10%, leaving 9 boxes available. If individual
  items are accepted, publish target `100` with unit `items` instead. Units are
  not converted implicitly; the API rejects a conflicting supplied unit.
- The bar explicitly measures **committed** goods, including fulfilled offers.
  A separate **received by the center** count sums only center-fulfilled goods
  contributions. Confirmation of an offer alone does not mean delivery.
- Owner cancellation releases its reserved quantity exactly once. Fulfilled
  contributions cannot be cancelled by the donor. Existing transactional capacity
  checks and idempotency prevent overcommitment and duplicate receipt counting.
- Fully committed, published needs remain visible in center details until expiry
  or closure; their offer actions are disabled. Cancelling an unfulfilled offer
  restores available capacity. A published need with no numeric target has no bar.
- Android, iOS and the authenticated center workspace show labels, units, totals
  and percentages alongside the native progress control; color is not the only
  indication. Spanish and English use the existing design tokens and resources.
- Native cache/outbox rules are unchanged: unsent or uncertain operations do not
  advance server progress. Accepted catalog refreshes persist receipt data through
  restart. Legacy snapshots display unknown receipt data rather than inventing zero.

## API compatibility

`Need::withProgress()` calculates `quantityReceived` from existing contributions;
there is no new writable counter or database migration. List/sync queries aggregate
receipts together, avoiding one lookup per need. Public data contains totals only,
not donor identities or notes.

Updated native clients request
`/api/v1/sync/catalog?category_version=2&progress_version=1`.
Only the negotiated response includes `quantityReceived` for needs with a target.
Legacy strict readers keep their original payload. The snapshot revision includes
the negotiated receipt data. Unsupported progress versions return 422. Public and
staff need endpoints expose the additive receipt field without negotiation.

## Executed checks

| Surface | Result |
| --- | --- |
| Laravel/PHP 8.4, isolated `generosity_test` database | 91 tests, 5,523 assertions passed. The partial-box case covers wrong units, capacity, center authority, confirmation versus fulfillment, exact retry, cancellation, legacy sync and rejection of money offers. Existing concurrent mutation checks remain passing. |
| Shared Kotlin JVM and Android host tests | 81 tests passed on each target, including 1/10, 29/100 integer percentages, missing targets, invalid receipts, completed-need visibility and cache restart. |
| Android | Debug APK built; instrumentation sources compiled. Device instrumentation was not executed for this increment. |
| iOS unit/bridge/localization | 19 tests passed on the iPhone 16 Pro iOS 18.6 simulator. |
| iOS progress UI | One test passed through both English and Spanish center-detail journeys. Progress labels, unknown-receipt copy and screenshots were checked. |
| Portal | 81 Chromium/WebKit tests passed, with one existing WebKit screenshot skip. Includes partial/full/no-target progress, staff operations, narrow layouts at 320–1280 px and 200% text. API responses are intercepted fixtures. |
| Resources and specification | 148 ES/EN keys and localization self-check passed. Both affected OpenSpec changes passed strict validation; generated design resources and whitespace checks passed. |

Screenshots: [Spanish](screenshots/need-progress/ios-es.png),
[English](screenshots/need-progress/ios-en.png).

Reproduce from the repository root (JDK/Android SDK configured):

```sh
./gradlew :shared:jvmTest :shared:testAndroidHostTest \
  :androidApp:assembleDebug :androidApp:compileDebugAndroidTestKotlin
./gradlew :shared:prepareSharedBridge
xcodebuild -project iosApp/Generosity.xcodeproj -scheme Generosity \
  -destination 'platform=iOS Simulator,name=iPhone 16 Pro' \
  -only-testing:GenerosityUITests/GenerosityUiChecklistTests/testPartialNeedProgressInEnglishAndSpanish \
  CODE_SIGNING_ALLOWED=NO test
npm --prefix portal test -- --workers=2
sh scripts/check-localization.sh --self-test
openspec validate define-generosity-mobile-app-foundation --strict --no-interactive
openspec validate add-reference-product-features --strict --no-interactive
```

Run `docker compose exec -T app composer test` from `backend/` against the isolated
test database. Reports live under `shared/build/test-results/`, Xcode's `.xcresult`
and `portal/test-results/`; the debug artifact is
`androidApp/build/outputs/apk/debug/androidApp-debug.apk`.

## Remaining boundaries

Preview API/portal deployment on November 6 included partial progress. Its HTTP
journey verified offer, center confirmation/receipt and history; Android Preview
subsequently displayed needs/progress from that API. Updated native builds reached
the iPhone 14 Pro/17, iPad and Android emulator. The public catalog merge preserved
the test need's committed/received two of ten kits. See
[preview evidence](release-controls.md) and [native distribution](native-design-preview.md).

Production publication and the full native offer → center receipt → refreshed
native history/progress journey remain open in foundation task 21.5. Emulator
component/connected-offer checks do not close physical Android, native
enlarged-text/dark-mode or manual screen-reader acceptance for this component.

Money is specified separately in external-donations task 10.12 and is not enabled:
MXN 10,000 against a gross MXN 200,000 goal is 5%, but only confirmed allocated
provider payments can count. Fees, net versus gross targets, refunds, chargebacks,
duplicate callbacks and overfunding must follow that contract before activation.
No invented monetary receipts or client-side payment authority were introduced.
