Reopened on 2026-10-25 after a source audit of `d489614`. Original task IDs
are retained. Unchecked original tasks were previously marked complete but
only partially implemented; see [implementation-status.md](implementation-status.md).
The user subsequently requested the welcome screen and expanded backend scope
with center-authorized contribution transitions. The corresponding new work
is unchecked. Artifact completion is not implementation completion. Original
IDs are stable references, not the execution order; follow the dependency
sequence in [spec-index.md](spec-index.md).


2026-11-04 reconciliation: both native roots use the PHP catalog/account APIs,
protected cache/outbox, entered forms and server history. The center workspace,
Google web/iOS entry and Android urgent FCM have recorded published checkpoints.
The new connected design, release controls and partial-goods progress have local
evidence but retain their deployment/device gates. See
[the current implementation summary](implementation-status.md) and
[closeout evidence](../../../docs/testing/openspec-closeout-2026-11-04.md).
Tasks remain open when any required behavior or acceptance is missing; local
fixtures and builds do not establish live multi-client acceptance. No foundation
archive is appropriate.

## 1. Project Foundation

- [x] 1.1 Scaffold the Kotlin Multiplatform project structure for shared logic, iOS, and Android targets.
- [x] 1.2 Configure the shared module build so iOS and Android can consume common domain code.
- [x] 1.3 Add baseline app entry points for the iOS SwiftUI app and Android Compose app.
- [x] 1.4 Add project documentation that names the product "Generosity", documents Spanish and English as initial supported languages, and keeps backend, backoffice, verification, and payment processing out of this mobile scope.

## 2. Shared Domain Model

- [x] 2.1 Define shared models for KindnessCenter, CenterCategory, CenterNeed, HelpMethod, UserProfile, Contribution, Reminder, NotificationPreference, VisitSignal, LocalePreference, and LocalizedText.
- [x] 2.2 Define verification status values that distinguish imported, visit-signaled, and officially verified centers without allowing mobile visit signals to become official verification.
- [x] 2.3 Define repository interfaces for Kindness Center catalog access, needs access, user profile data, contribution data, reminders, notification preferences, and visit signals.
- [x] 2.4 Add shared validation and mapping tests for the core domain models, localized text fallback, and stable domain label codes.

## 3. Imported Catalog Data

- [x] 3.1 Choose and document the initial imported catalog format, including how localized Spanish and English text fields are represented.
- [x] 3.2 Implement an imported catalog data source behind the Kindness Center repository interface.
- [x] 3.3 Add sample imported Kindness Center data covering all required categories.
- [x] 3.4 Handle missing or invalid imported catalog data with a non-crashing empty state.
- [x] 3.5 Load and validate versioned bundled JSON through the shared repository; test malformed data, missing files, duplicate IDs and localized fallback (3.2/3.4 currently cover only memory).

## 4. Localization

- [x] 4.1 Configure Spanish and English localization resources for the iOS app.
- [x] 4.2 Configure Spanish and English localization resources for the Android app.
- [x] 4.3 Add Android user-settings choices system/en/es and persist installation mode; verify first launch, regional/ordered device languages, explicit override, restart and return to system mode.
- [x] 4.4 Map shared domain label codes for categories, help methods, verification states, notification types, and empty states to localized platform strings.
- [x] 4.5 Add localization coverage checks or tests to prevent missing Spanish or English strings for app-controlled text.
- [x] 4.6 Add the equivalent iOS settings through shared state; verify device changes, guest access, restart and no language flash on welcome.
- [ ] 4.7 Localize contribution errors and notification text through app resources; test live language propagation to search/sort, dates and schedules without losing entered form data.

Language implementation update (2026-10-30): native settings now persist
system/en/es per installation, resolving ordered device languages in shared
Kotlin before the first screen. Android handles locale configuration/resume
without resetting its search; iOS now links SharedBridge for language state
and owns one cancellable observer. Android storage/recreation/search tests and
shared fallback/account-isolation/stale-query tests pass. Welcome and iOS language
settings are now implemented; the 2026-11-04 reconciliation records their current
acceptance separately from this historical checkpoint.
Task 4.7 remains open for errors, dates and scheduled/remote notifications;
search/sort propagation is only part of that task. This language slice did not
replace iOS sample discovery; the later task 5.4 update records that integration.

4.6 acceptance (2026-11-04): synchronous shared language resolution, ordered
device languages, fallback, persisted override/restart/device changes and
bilingual welcome/guest restart passed on the iOS 27 simulator. See
[closeout evidence](../../../docs/testing/openspec-closeout-2026-11-04.md).

## 5. Discovery Experience

- [x] 5.1 Implement shared filtering and sorting use cases for category, location, and search criteria.
- [ ] 5.2 Replace Android's placeholder with Google Maps behind a replaceable renderer; verify the SDK boundary, pan/zoom, marker selection, filtered map/list parity and map-provider failure with list fallback on a Google-enabled device with a restricted key.
- [x] 5.3 Build the iOS discovery map and list screens using SwiftUI.
- [x] 5.4 Connect iOS discovery/details to SharedBridge with bounded subscriptions; test equivalent inputs on both platforms and repeated navigation without duplicate actions.
- [x] 5.5 Verify anonymous users can browse, filter, and open center details without registration.
- [x] 5.6 Feed permission-gated current location into Android discovery and test browsing after denial or revocation.
- [x] 5.7 Feed permission-gated current location into iOS discovery and test browsing after denial or revocation.
- [x] 5.8 Add Android manual place search without GPS; test selected-origin distance labels, empty/error results and preserved filters.
- [ ] 5.9 Add iOS manual place search without GPS; test selected-origin distance labels, empty/error results and preserved filters.
- [ ] 5.10 Show Android phone/email/web actions; test missing fields, unsupported schemes, unavailable handlers and anonymous access.
- [ ] 5.11 Show iOS phone/email/web actions; test missing fields, unsupported schemes, unavailable handlers and anonymous access.
- [x] 5.12 Give every Android/iOS result direct detail access; open the exact selected detail, synchronize card/pin selection and frame catalog coordinates or the selected center. Validate non-first cards, list/map transitions and far-away/empty results; track remaining live Google-provider acceptance separately in 5.2.

- [x] 5.13 Keep map/search/category controls fixed above independently scrolling cards; open details separately and preserve selection/list position on Back. Validate native UI tests and manual geocoding; retain task 5.2 for real Google-provider acceptance.
- [x] 5.14 Open the selected center in Google Maps and native/device maps from iOS/Android details; validate exact coordinates, localization, invalid destinations, unavailable handlers and permission-independent access.

Task 5.6 update (2026-11-02): native Android single-fix location now drives the
shared 10 km filter, nearest ordering, map origin and distance labels. Automatic
entry uses existing foreground permission; explicit requests accept approximate
access. Denial, disabled services and timeout retain manual browsing; returning
from Settings retries, and manual choices cancel/ignore late fixes. Eight unit
tests and two emulator UI cases passed, together with the eight map-discovery
regressions. Native approximate-permission acquisition, automatic entry and
quick restart with the recent OS fix were verified using emulator GPS and live
Google tiles. Older task-5.6 pending notes below are superseded by this update.
Physical Android GPS validation remains a release check; see
[location implementation](../../../docs/native-maps.md#android-current-location--2026-11-02).

Task 5.14 update (2026-11-01): iOS uses native Apple map items and Google HTTPS
URLs; Android uses geo intents with an HTTPS fallback plus an explicit Google
action. Two iOS destination tests, two iOS UI tests (including the denied-GPS
explanation), and four Android instrumented tests passed. Android UI tests capture
real detail-button intents for first/non-first centers in both languages. SDK
boundary and localization checks pass. See [external map actions](../../../docs/native-maps.md#external-map-actions--2026-11-01).

Physical follow-up: both iPhones received the signed update; Google Maps on the
14 Pro displayed the chosen center's coordinate marker. Apple's first-run setup
still prevents final destination acceptance. Automatic GPS refresh now preserves
an open detail, including an expired/moved origin, while catalog removals and
explicit area changes remain authoritative. Two shared regressions passed as
part of 75 JVM tests. See [physical QA and limitations](../../../docs/testing/native-visual-verification.md#physical-ios-follow-up--2026-11-01).

Tasks 5.8–5.9 and 5.13 update (2026-11-01): both native apps now use a fixed
map/header, separately scrolling cards and full-screen details with Back.
Search area uses native geocoding, explicit suggestion selection, a shared
10 km radius, distance ordering and unchanged name/category filters. Android
contract/UI tests cover successful, empty and failed lookup without GPS, plus
clearing the origin. iOS live Coyoacán lookup, distance labels, category filtering
and card/pin/detail navigation were verified in the simulator. Task 5.9 remains
open specifically for deterministic iOS lookup-error/timeout acceptance; its
implementation is present. Android GPS and real Google tiles remain separate.
See [behavior and evidence](../../../docs/native-maps.md).

Task 5.7 update (2026-11-01): iOS now obtains a foreground, permission-gated
Core Location fix and uses the shared 10 km discovery filter and distance order.
Map/list show Use my location, an origin marker, localized distances and
approximate/denied/unavailable states. Manual search cancels pending acquisition;
coordinates remain session-only. Seven location tests, three map tests and three
simulator UI tests passed, including grant/denial dialogs, revocation, stale fixes,
timeout, manual override, language switching and existing card/pin/detail flows.
The signed update is installed and launched on both connected iPhones. Physical
iPhone 14 Pro acceptance confirmed nearby distances and matching map/list results
after location permission was enabled. The iPhone 17's GPS scope, nearby distances,
map/list and second-center detail were subsequently observed through QuickTime
in light/English appearance. Live Coyoacán lookup and return to the device origin
also passed on the dark/Spanish iPhone 14 Pro. Android task 5.6 and iOS
lookup-error task 5.9 remain open.

Task 5.2 live-provider update (2026-11-01): the locally configured development
key is restricted to Maps SDK for Android and the matching package/debug SHA-1.
APK build, signature and provider-boundary checks pass. Android 16 Google APIs
remains affected by service/keyboard ANRs; Android 14 Google APIs responds.
Its legacy-renderer crash was fixed with the optional Apache HTTP manifest
library. Live tiles/attribution, pan/zoom, card/pin synchronization, non-first
detail and Back preserving selection/list position passed visually on Android
14. Live provider/network failure and recovery, filtered map/list parity and
remaining device/inset acceptance keep 5.2/17.4 open. Android GPS remains in 5.6;
see [setup evidence](../../../docs/native-maps.md#android-development-configuration-verified--2026-11-01).

Task 5.2 implementation update (2026-10-26): the Google adapter, neutral renderer,
configuration, boundary guard and automated contract/fallback tests now exist.
The task remains unchecked: live Google tiles/gestures and provider-error behavior
still require a restricted key and Google-enabled device. See
[native map acceptance](../../../docs/native-maps.md). MapKit remains unchanged;
this did not complete GPS, manual place search or iOS shared-state integration.

Task 5.4 implementation update (2026-10-30): SwiftUI map/list discovery and
details now render shared models/state through the root store. Search,
categories, selection, localized content fallback and current-needs filtering
use Kotlin; the duplicate Swift catalog/models/rules were removed. MapKit
retains only native presentation state. Controller tests compare equivalent
inputs with the state consumed by Android and cover repeated subscriptions,
navigation and a single offer without duplicate recording. All 36 shared ARM64
iOS tests, 11 Android connected tests and 9 iOS UI tests pass. This does not
complete the separate forms/history, real clock, persistence or backend tasks.

## 6. Needs and Help Methods

- [x] 6.1 Implement shared needs models and read-only needs retrieval.
- [x] 6.2 Display current needs on center detail screens when available.
- [x] 6.3 Display urgent needs distinctly when urgency metadata is available.
- [x] 6.4 Hide expired needs from current needs lists when expiration metadata is available.
- [x] 6.5 Enforce the money donation boundary so the app does not process payments or collect bank/payment details.
- [x] 6.6 Replace demo dates with real clocks and explicit instants/zones; test UTC date-only expiry, resume across expiry, device skew, reminder DST gaps/folds and travel.
- [x] 6.7 Implement shared need status, priority ordering, bounded goods commitments and latest-snapshot lookup with focused tests.

## 7. User Profile and Contributions

- [x] 7.1 Implement anonymous and registered user state handling in shared logic.
- [ ] 7.2 Complete Android history/impact with lifecycle and sync state, unit-aware totals and personal-help provenance; test empty/loading/offline/error states and no invented points or money.
- [x] 7.3 Ensure generosity points are presented as non-monetary and non-redeemable.
- [ ] 7.4 Add Android personal-help forms with entered activity, duration/quantity and notes; verify self-reported labels and field validation in UI tests.
- [ ] 7.5 Verify registered-user empty history states on Android and iOS; current UI tests cover anonymous users only.
- [ ] 7.6 Add iOS shared history/impact with units and provenance; test the same loading/empty/offline/error and no-fabricated-totals cases as Android.
- [ ] 7.7 Add iOS personal-help forms; verify entered values, self-reported labels and field validation in UI tests.
- [x] 7.8 Document the selected identity provider and account/session contract, including registration, recovery, required verification and backend validation; review against user-authentication scenarios.
- [ ] 7.9 Integrate Android real registration/sign-in/renewal/sign-out/recovery; test cancellation, expiry, protected credential storage and absence of demo identity fallback.
- [ ] 7.10 Integrate the equivalent iOS identity flow; test cancellation, expiry, protected credential storage and shared account state.
- [ ] 7.11 Add Android durable private stores/outbox and public cache; test restart, migration, save failure, sign-out suspension and account isolation.
- [ ] 7.12 Add iOS durable private stores/outbox and public cache; test restart, migration, save failure, sign-out suspension and account isolation.
- [ ] 7.13 Replace Android fixed offer inputs with forms; test quantity/activity/availability/duration/notes, all rejection codes and draft preservation through authentication/conflict.
- [ ] 7.14 Add equivalent iOS need-offer forms; test shared validation, all rejection codes and draft preservation through authentication/conflict.
- [x] 7.15 Implement shared registered-user offer validation and confirmed/fulfilled-only impact aggregation with focused tests.
- [x] 7.16 Extend shared records with units, planned/actual duration, provenance, sync state and versioned event history; test incompatible units, missing metrics and cancelled/pending exclusion.
- [x] 7.17 Implement and locally verify the account-email UX corrections: queued Spanish/English mail, localized browser success/invalid/expired-link screens and visible validation errors without retained passwords; preserve JSON API contracts and native PKCE context. Serialized queue, HTTP and Chromium/WebKit evidence is in [account-email journeys](../../../docs/testing/account-email-journeys.md).
- [x] 7.18a Publish the tested account-email release and repeat actual Hostinger verification/recovery links, localized validation/success, token single-use and session revocation through live hosted forms. Completed 2026-11-02; evidence in account-email journeys.
- [ ] 7.18b Complete mail-app-to-native registration/recovery on physical Android/iOS and record native return separately from the live HTTP/form acceptance.

## 8. Notifications and Reminders

- [x] 8.1 Implement shared notification preference models for nearby centers, urgent needs, and reminders.
- [x] 8.2 Add platform permission flows for notifications and location access.
- [ ] 8.3 Add Android reminder create/edit/disable/delete and durable native scheduling; test restart, changed time, duplicate prevention and denied permissions.
- [ ] 8.4 Connect Android switches to actual permission state and durable preferences; test schedule cancellation and visible offline backend-update status.
- [ ] 8.5 Connect Android nearby alerts to actual permission results and shared eligibility; test denied and revoked permissions.
- [ ] 8.6 Connect Android localized content and alert destinations; test language changes, deleted targets and private destinations under the wrong account.
- [ ] 8.7 Add iOS reminder create/edit/disable/delete and durable native scheduling; test restart, changed time, duplicate prevention and denied permissions.
- [ ] 8.8 Connect iOS switches to actual permission state and durable preferences; test schedule cancellation and visible offline backend-update status.
- [ ] 8.9 Connect iOS nearby alerts to actual location and notification permissions; test denied and revoked permissions.
- [ ] 8.10 Connect iOS localized content and alert destinations; test language changes, deleted targets and private destinations under the wrong account.
- [x] 8.11 Implement configurable 500 m nearby range and 24 h per-center cooldown plus explicit category/All interests; test boundaries, stale data and unsupported background-state messaging.
- [ ] 8.12 Deliver eligible nearby and urgent alerts on Android within platform limits, with opt-out tests.
- [ ] 8.13 Deliver eligible nearby and urgent alerts on iOS within platform limits, with opt-out tests.
- [ ] 8.14 Configure institutional Firebase messaging for Android and iOS (FCM → APNs), private HTTP v1 server credentials and signed iOS push entitlements; verify actual foreground/background/locked-device delivery, token rotation and revoked permissions. Maps/OAuth credentials are not messaging credentials. Follow the 2026-11-02 delivery design; keep unavailable providers visibly inactive.
- [x] 8.15 Implement the urgent-need FCM slice: institutional Firebase app registrations, least-privilege PHP service identity, private key/configuration, Android-specific Firebase API key, opt-in token registration and owned foreground reconciliation. Actual Android emulator foreground/background receipt passed on 2026-11-02; see [evidence](../../../docs/releases/push-delivery-2026-11-02.md).
- [ ] 8.16 Complete signed iOS APNs activation and physical Android/iOS locked/terminated/tap acceptance. iOS adapter and simulator tests exist, but Apple membership/key/profile are unavailable; `FCM_IOS_ENABLED=false` and personal-team push entitlement stays disabled. 8.12–8.14 remain open for these gates and proximity delivery.

## 9. Visit Signals and Verification Boundary

- [ ] 9.1 Persist/queue Android explicit visits before showing success; test saved/pending/received/failure states and no automatic upload of guest records after sign-in.
- [x] 9.2 Prevent passive visit-signal submission based only on proximity.
- [x] 9.3 Ensure submitting a visit signal never changes a center to officially verified in mobile state.
- [x] 9.4 Add localized UI copy and state handling that makes official center verification a future backend/backoffice workflow.
- [ ] 9.5 Persist/queue iOS explicit visits through shared state; test saved/pending/received/failure states and no automatic upload of guest records after sign-in.
- [ ] 9.6 Test visit storage failure, minimum evidence, permission denial, restart recovery and no promotion to official verification.

- [x] 9.7 Replace public import/visit status labels with an accessible checkmark only for verified centers in Android/iOS cards, map previews and details; preserve internal provenance. Validated 2026-11-01 by Android `onlyVerifiedCentersShowAnAccessibleCheckmark` and iOS `testOnlyVerifiedCentersShowAnAccessibleCheckmark` against all three states in English/Spanish; localization and strict OpenSpec checks pass.

## 10. Verification

- [x] 10.1 Add unit tests for shared discovery, filtering, needs expiration, notification preference, contribution, localization fallback, and visit-signal logic.
- [ ] 10.2 Extend Android UI tests to exercise search/category filtering, registered history, offers, permissions and reminders; retain the two existing smoke tests.
- [ ] 10.3 Extend iOS UI tests to exercise search/category filtering, registered history, offers, permissions and reminders; retain the two existing smoke tests.
- [x] 10.4 Run OpenSpec validation for the change and fix any proposal, design, spec, or task issues.
- [ ] 10.5 Verify every normative scenario on its relevant native, browser and backend surfaces, reconcile delta specs with main specs, and rerun strict OpenSpec validation before archiving this change again.

## 11. Backend Foundation

2026-10-31 PHP migration: see [decision](../../../docs/backend-php-mysql.md),
[API contract](../../../backend/openapi.json) and implementation-status.md.
11.3 has a server schema/route check but still lacks native/portal contract tests.
11.5 is reopened: transactional upsert/reservation preservation is implemented;
source revision conflicts, authoritative removals and tombstones are not.
The Kotlin-specific evidence remains available on the preserved mockup branch.

- [x] 11.1 Record backend framework/database/hosting and local-runtime selections in an architecture decision; verify they support the API, transaction, migration and worker contracts without a portal dependency.
- [x] 11.2 Add backend service, database migrations, isolated development fixtures and local startup instructions; test clean startup, readiness and restart durability without production credentials.
- [ ] 11.3 Publish the machine-readable API schema with errors, pagination, versions and idempotency; run contract tests against native and center portal request/response examples.
- [x] 11.4 Integrate provider validation, account mapping and controlled center-role provisioning; test anonymous/public reads, invalid credentials, cross-user access and cross-center denial, and document the operator procedure.
- [ ] 11.5 Add atomic catalog file import and versioned public reads; test invalid revision rollback, duplicate imports, removed records and preservation of accepted commitments.
- [x] 11.5a Research an initial four-category catalog in Coapa, central Coyoacán, UNAM, San Miguel Chapultepec/surroundings and Aguascalientes capital; preserve source URLs and exact Maps place-pin coordinates, hold discrepancies, and load imported-only records locally without fabricated needs. 40 researched, 37 imported locally and published with explicit owner approval, 3 held; Google Maps metadata stays outside strict mobile-v1 snapshots. See [batch and evidence](../../../data/initial-catalog/README.md). Production import completed on 2026-11-01; remaining reconciliation work in 11.5 is not completed by this subtask.
- [x] 11.5b Extend public-source research to the 33 requested Mexico/US/Europe zones and all four categories. Preserve place-pin precision, actual postal country, nearby alternatives, office/meal-service distinctions and unknown funding origin. 132 researched, 120 loaded locally, 12 held; the 40 prior development centers, six needs and three memberships remain unchanged. See [batch, coverage, source evidence and tests](../../../data/catalog-expansion-2026-11-01/README.md). This does not complete full directory fields or reconciliation.
- [x] 11.5c Publish the explicitly approved 120-center expansion using insert-only checks, private backup and an operator audit; verify every public record, unchanged prior records, strict mobile compatibility and removal of the temporary publication endpoint. Completed 2026-11-01: 157 production centers, prior 37 unchanged, 12 held excluded, no new needs or memberships. See [release evidence](../../../docs/releases/catalog-expansion-2026-11-01.md).
- [ ] 11.6 Add owned profile/preferences/reminder/visit endpoints and device registration; test ownership, payload validation, visit idempotency, token rotation and sign-out deactivation.
- [x] 11.7 Add structured redacted diagnostics, rate limits and deployment secret configuration; test that failures do not expose secrets/private records or partially write domain data.
- [x] 11.8 Document database backup/restore and upgrade/rollback; demonstrate an isolated restore preserving identities, reservations, histories and acknowledged operation IDs.

## 12. Synchronization and Delivery

- [ ] 12.1 Implement backend change cursors/versions/tombstones with full-resync fallback; test overlapping pages, interrupted sync and expired cursors without losing unsent client work.
- [ ] 12.2 Implement shared durable operation dispatch and reconciliation; test lost responses, replays with changed payloads, rejection rollback and account-switch suspension.
- [ ] 12.3 Connect Android to catalog/account sync; test offline bundle/cache, stale indicators, full resync and preserved history for removed catalog items.
- [ ] 12.4 Connect iOS to the same sync contracts; test equivalent offline/reconnect outcomes and cross-device history updates.
- [x] 12.5 Add transactional domain outbox and idempotent owned-inbox/FCM delivery jobs using the bounded Hostinger database queue; test opt-in/category/expiry checks, publication vs quantity-only edits, transient retry, per-installation deduplication, invalid-token retirement and provider acceptance distinct from user receipt. Implemented for urgent-need publication/transition; broader reference inbox categories remain under reference-product task 9.1.
- [ ] 12.6 Document sync/alert conflict and retry semantics; verify the documented offline preference behavior using an interrupted-network integration test.

## 13. Contribution Lifecycle

- [x] 13.1 Add atomic backend offer creation and capacity reservation; test competing offers for the final units, real-time expiry and idempotent retry without overbooking.
- [x] 13.2 Add owner cancellation and center-only confirmation/fulfillment with version checks and event history; test forbidden transitions, cancellation/fulfillment races, exactly-once release and terminal states.
- [x] 13.3 Add backend personal-help recording/completion with self-reported provenance; test owner authorization and refusal to convert personal records into verified center contributions.
- [ ] 13.4 Expose permitted lifecycle actions on Android and show pending/conflict outcomes; UI-test cancellation followed by server-confirmed history and updated impact.
- [ ] 13.5 Expose the equivalent lifecycle/history behavior on iOS; UI-test center transitions arriving through sync without duplicate impact.
- [x] 13.6 Document and exercise center staff actions through authenticated API tooling independently of the browser UI; verify staff can confirm/fulfill only their assigned center without granting official verification.

13.6 evidence: `ApiTest::test_scoped_staff_transitions_owner_isolation_and_terminal_state` denies the owner and another center, then lets assigned staff confirm and fulfill. `test_revoked_staff_cannot_replay_a_previously_accepted_operation` stops a replay after revocation and leaves verification `imported`. Browser workspace tests do not satisfy this task.

## 14. Welcome Experience

- [ ] 14.1 Obtain approved logo/illustration assets and configured Terms/Privacy destinations; verify resource provenance and that legal links resolve before release acceptance.
- [ ] 14.2 Build Android welcome with guest/sign-in/registration routing and saved entry state; test first launch, returning guest/session, cancellation and sign-out routing.
- [ ] 14.3 Build equivalent iOS welcome routing; test first launch, returning guest/session, cancellation and sign-out routing.
- [ ] 14.4 Verify both welcome layouts against the supplied reference in English/Spanish, on small/large phones with enlarged text and screen readers; record screenshots and confirm no clipping or promotional-frame reproduction.

## 15. Cross-System Acceptance

- [ ] 15.1 Run portal need publication -> native discovery -> offer -> portal confirmation -> fulfillment -> history across Android, iOS and the backend; verify identical authoritative IDs, states, units and provenance.
- [ ] 15.2 Run restart/offline/reconnect, account switching and concurrent capacity scenarios across both apps; verify isolation, no duplicates and correct cancellation accounting.
- [ ] 15.3 Run opted-in reminder/urgent/nearby delivery and denial/revocation scenarios on both platforms; record actual platform limitations without claiming unsupported background guarantees.

15.1–15.2 partial evidence (2026-11-02): the local HTTP smoke passed with
separate donor/staff identities, real cookies/CSRF, publication, offer replay,
confirmation/fulfillment and matching history, capacity/cancellation, restart and
isolated database restore. The native pilot and device offline/account-switching
matrix remain open; no real pilot center/need has been supplied.

15.1 publication subset (2026-11-02, later owner authorization): created a clearly
labeled fictional DEMO center on the published backend and granted the existing
owner account staff access only to it. Live portal draft → publish → edit 10 to
20 kits / urgent → close → republish retained one authoritative need ID. The
physical iPhone 14 Pro showed 10 after Update, 20 / Urgent after foreground resume,
and no published needs after closing and refreshing. All 157 original centers
were unchanged. The DEMO remains published for owner use. This does not close
15.1 (native offers/fulfillment/history and Android remain), 15.2, or 15.3:
at that checkpoint, publishing a need sent no push notification (superseded by
the urgent-need delivery slice below). See the
[published demo evidence](../../../docs/releases/critical-foundation-2026-11-02.md#owner-authorized-published-demo--2026-11-02).

15.1 Android publication follow-up (2026-11-02): `NeedCatalogSyncTest` passed
3/3 on the Android 14 Google APIs emulator. Spanish/English controlled responses
cover publish/quantity/urgency/close, manual Refresh, resume updating open detail
and failed-refresh cache preservation. The opt-in live test used Android HTTPS
and encrypted storage to display the same published DEMO need and restore it
from cache. Build 2's APK is unchanged. This completes the emulator publication
check only; physical Android, native offers/fulfillment/history and push delivery
remain open. See [Android evidence](../../../docs/releases/critical-foundation-2026-11-02.md#android-needs-acceptance--2026-11-02).

Push follow-up (2026-11-02): build 3 and the published backend passed real FCM
foreground/background delivery on the Android emulator, with two isolated DEMO
needs closed afterwards. Shared owned-inbox/banner/read tests, Android bilingual
inbox UI tests, and iOS parser/build tests passed. Physical Android delivery and
all real APNs delivery remain pending; this does not close pilot task 15.1 or
the complete notification acceptance matrix. See [push evidence](../../../docs/releases/push-delivery-2026-11-02.md).

## 16. Mobile-First Center Web Portal

- [x] 16.1 Review and consolidate the provided landing source/assets into canonical `portal/` without deleting the PoC; document asset provenance, local preview/test commands and browser deployment/session choices without adding an unnecessary UI framework.
- [x] 16.2 Replace false submission/review/payment claims and fabricated public metrics with real data, explicit samples or unavailable states; test that disconnected forms preserve input and never report acceptance, and public center entry routes to sign-in.
- [x] 16.3 Rework landing/navigation/form CSS from narrow-screen defaults with wider enhancements; test shrinkable inputs, long labels, wrapping, hidden menu/filter items and 44-pixel primary touch targets.
- [ ] 16.4 Connect provider-backed staff sign-in, assigned-center selection and sign-out; test no-assignment, role revocation, private-view clearing and session renewal without leaking or automatically submitting another account's work.
- [ ] 16.5 Implement center-scoped backend draft/create/publish/edit/close endpoints and audit events; test payload/date validation, private drafts, restart durability, idempotent publication, version conflicts and closing without cancelling accepted contributions.
- [ ] 16.6 Integrate staff writes with public catalog revisions and urgent events; test atomic capacity checks, forbidden unit/method changes, import source ownership and stale-import rejection without losing center-managed needs.
- [ ] 16.7 Implement the phone-first need editor, preview and explicit draft/publish actions; test goods and time inputs, inline errors, language fallback, lost responses and retry without duplicate needs.
- [ ] 16.8 Add the center's needs list with draft/published status, edit and close actions; test empty/loading/error states, outstanding-offer confirmation and conflicts that preserve unsaved edits.
- [ ] 16.9 Connect paginated contribution list/detail and permitted confirmation/fulfillment actions; test donor-cancellation races, minimal donor disclosure and matching native history/impact.
- [ ] 16.10 Add Spanish/English web resources and browser-local system/en/es settings; test ordered language fallback, persisted override, localized errors/dates, keyboard navigation, screen-reader labels and focus restoration.
- [ ] 16.11 Add browser acceptance tests and screenshots at 320/360/390/768/1280 CSS pixels in Chromium and WebKit; assert no horizontal page overflow, no false success and functional menu/forms, then record 200-percent text and real Safari/Chrome virtual-keyboard checks.
- [x] 16.12 Test direct API cross-center denial, public draft/private-field exclusion, script-content rendering, CSRF/origin protection and revoked sessions; document unresolved failures as release blockers rather than treating browser-only validation as authorization.
- [x] 16.13a Implement and locally verify `/kindness-center/` as the dedicated center entry; help opens `/ayudar/`, unassigned accounts have no publishing controls and `kidness-center` is not an alias.
- [x] 16.13b Publish the dedicated portal routes and verify direct entry, legacy links and HTTPS guest-session access. See [2026-11-05 release](../../../docs/releases/portal-map-2026-11-05.md).
- [ ] 16.13c Complete real authenticated production workspace acceptance after deploying its missing feature-flag endpoints (still 404 on 2026-11-08). Preview has the feature API and HTTP lifecycle evidence; this does not close production browser write acceptance.
- [x] 16.14 Move location applications to `/registrar-sede/` and discovery/offers/history to `/ayudar/`; remove operational forms from the landing, preserve legacy fragments, locale and hosted-account form/retry boundaries, and test responsive pages.
- [x] 16.15 Adapt all 34 reference screens into the isolated public `/app/` tour with ES/EN, light/dark appearance, canonical brand tokens, mobile navigation and no specification/task links; verify tour actions do not call the API or send data.
- [x] 16.16 Update the explicit static-file deployment inventory, cache versions and isolated-preview packaging for every route; validate generated assets and the public package without deploying it.
- [x] 16.17 Repair the published tour's module MIME compatibility, check every module response during deployment, and verify rendered navigation and screen changes on the domain. Release `portal-release-2026-11-05.3` passes the three module MIME checks; all 34 published screens rendered, plus filters, ES/EN and appearance switching. See [release follow-up](../../../docs/releases/portal-map-2026-11-05.md#interactive-tour-hosting-fix).

16.13a–16.16 local evidence (2026-11-05): existing portal regression checks pass
in Chromium/WebKit after moving fixtures to their dedicated routes. The 12
additional route/tour checks pass, including all 34 screens in both languages,
legacy redirects, account without membership, 320-pixel enlarged text and public
asset versions. No new live account writes or hosting changes were performed.
Screenshots: `portal/screenshots/route-*-390.png` and `app-tour-{390,1440}.png`.
The explicitly inventoried isolated preview package has preview API/channel metadata on all four
operational pages. Foundation-wide physical-device and production acceptance
remain separate; this does not implement pending service-backed tour features.

16.1–16.3 and 16.12 acceptance (2026-11-04): canonical-source/provenance review,
111 passing browser cases, and the final 119-test PHP suite. New real-cookie
tests run production CSRF middleware, reject missing/invalid tokens, deny foreign
origin and revoked-session staff writes, and verify domain data is unchanged.
Existing tests cover cross-center denial, public/private separation and escaped
rendering. See [closeout evidence](../../../docs/testing/openspec-closeout-2026-11-04.md).

- [x] 16.18 Align landing header/content/footer margins and need-card actions; keep institute attribution to a compact footer credit; provide standalone ES/EN legal pages from canonical notices and localize their links. Validate narrow screens, enlarged text, keyboard use, no-JavaScript legal access and an explicit public deployment allowlist. Do not represent placeholder counters or invented centers as operational data.

## 17. Native Visual Alignment

- [x] 17.1 Finalize the reference-aligned native color/type/spacing/icon roles and approved brand assets; record contrast checks and deliberate omissions without adding new destinations, domain fields or a separate design-system framework.
- [x] 17.2 Apply the native visual roles to Android theme, existing navigation and reusable controls; verify Map/List/Profile/Settings and supported appearance modes remain usable with unchanged actions.
- [x] 17.3 Apply the equivalent roles to SwiftUI theme, existing tabs and reusable controls; verify the same destinations and platform-appropriate appearance/accessibility behavior.
- [ ] 17.4 Align Android discovery search/categories/map selection and center preview; test existing filter/query retention, list fallback, safe insets and map attribution without treating an unavailable-map state or test renderer as working cartography.
- [x] 17.5 Align iOS discovery composition and selection preview; verify the same behavior on small/large phones with an adaptive map viewport and independently scrolling cards.
- [x] 17.6 Restyle Android center details/current-need rows and supported help actions; test long content, absent photos, all priorities, rejected offers and unchanged contact/visit access.
- [x] 17.7 Restyle the equivalent SwiftUI detail/need surfaces; test identical hierarchy, state handling and readable non-color urgency cues without introducing a new needs feed.
- [x] 17.8 Restyle Android Profile/Impact, Settings and existing form states; verify labeled units/provenance, guest/empty/unavailable states and no invented points, duration, balances or achievement panels.
- [x] 17.9 Restyle equivalent SwiftUI Profile/Impact, Settings and form states; verify the same data semantics and that visual changes do not restore sample-only business logic.
- [ ] 17.10 Record Android/iOS screenshot baselines and reference comparison using the matrix in docs/testing/mobile-ui-checks.md; test Spanish/English, small/large phones, enlarged text, keyboard/screen-reader access, supported appearance modes and failure states, and rerun navigation/flow regressions before marking visual alignment complete.

- [x] 17.11 Consolidate native/web/account colors, font configuration and common dimensions in one source; migrate existing components without changing navigation or business rules.
- [x] 17.12 Establish editable vector logo masters, shared asset export and stale-output checks in native builds, web tests and publication workflows.
- [x] 17.13 Verify generator edits/contrast/stale outputs, Android/iOS compilation and UI regression checks, portal rendering and hosted account CSS/CSP; record physical-device limitations separately. Evidence: `docs/testing/design-system.md`.

Physical visual follow-up (2026-11-01): dark/Spanish iPhone 14 Pro and
light/English iPhone 17 map/list/detail were inspected. The area-search button's
dark-mode foreground was corrected using the existing design-system role and
verified on the 14 Pro. This adds targeted real-device evidence to 17.13; full
screen-reader, text-size/locale and failure-state acceptance in 17.10 stays open.

## 18. Generosity-Only Social Sign-In

- [x] 18.1 Specify Google and Apple for Generosity web/Android/iOS, explicit account linking, missing-configuration behavior and exclusion of institute-wide SSO.
- [x] 18.2 Implement server authorization-code exchange, signed ID-token validation, browser binding, one-use attempts and subject-based identities; test replay, invalid claims, email collisions, linking and private-relay email.
- [x] 18.3 Implement HttpOnly browser sessions, exact-origin CORS, CSRF and mobile-first account entry in the existing portal; test guest, signed-in, unavailable and failed-logout states in Chromium/WebKit.
- [x] 18.4 Implement native PKCE handoff and secure session storage adapters; remove Android's fixed sample login and gate remote accounts from demo contribution writes; compile both clients and test PKCE/callback/shared-state boundaries.
- [ ] 18.5 Configure Google Cloud OAuth consent/client credentials and Apple Developer Services ID/key/domain; complete live authorization, cancellation, account linking, private relay, restart, expiry and logout on web and physical Android/iOS devices.

18.5 partial activation (2026-11-02): confirmed institutional account access to
`generosity-v01`; saved approved OAuth branding, legal links and basic identity
scopes; created the web client with the exact backend callback; installed Google
credentials only on the private server and verified API health and enabled hosted
entry. Corrected and published the exact-provider CSP form redirect allowance;
75 backend tests / 5,187 assertions passed. Real Google consent, web sign-in,
logout and repeat sign-in passed; one verified account, one Google identity and
no center roles were confirmed. On a physical iPhone 14 Pro, Google return,
session restoration after process restart, logout/revocation and cancelling the
iOS sign-in permission dialog passed. External / Testing remains selected.
Repeated return, process restart, logout and system-dialog cancellation on the
physical iPhone 17 with iOS 27.2 passed. Registration and recovery screens are
reachable; actual mail-app-to-native completion remains separate in 7.18b.
Google consent denial/linking, token expiry, Android acceptance and Apple
configuration remain pending. See the
[activation checkpoint](../../../docs/social-sign-in.md#google-activation-checkpoint--2026-11-02).

- [x] 18.6 Deploy the API to api.generosity.global with HTTPS, secure host-only sessions, exact Generosity CORS/stateful origins, real database/SMTP and scheduled auth cleanup; verify the existing main-only landing workflow remains separate.

18.6 evidence, rechecked 2026-11-01: health is 200, `/.env` is 403, and the cron scripts are not web-reachable. The session cookie has no `Domain`, and is `Secure`, `HttpOnly` and `SameSite=Lax`. CORS allows only `https://generosity.global` with credentials. `routes/console.php` schedules hourly handoff cleanup plus daily Sanctum and reset pruning; hPanel runs that scheduler every minute. Hostinger SMTP delivered one approved test message to Gmail Inbox. The subsequent [account-email review](../../../docs/testing/account-email-journeys.md) passed live backend/security journeys; task 7.17 closed the user-facing gaps; 7.18a now records the repeated live email/form acceptance and 7.18b retains physical-device completion.
18.6 follow-up (2026-11-02): fixed the empty production Sanctum stateful list.
Real Google web login now reads private application history and assigned-center
state correctly; no memberships were granted. Added a regression-tested
`auth:check-portal` deployment guard. Published the protected application review
routes, additive migration and landing tracker; reviewer IDs remain unconfigured
and the real center pilot requires owner-supplied details. See
[release evidence](../../../docs/releases/critical-foundation-2026-11-02.md).

- [ ] 18.7 Add Facebook server configuration and authorization adapter using the existing hosted entry/session boundaries; test app-bound identity validation, denied permission, invalid state, wrong browser, expiry, replay and unavailable configuration without adding native provider SDKs.
- [ ] 18.8 Add pending email completion for social identities without trusted verified email; test absent/unverified email, existing-email collisions, explicit linking without provider email and no account merge or premature protected access.
- [ ] 18.9 Expose Facebook through hosted web/native entry with Spanish/English states; test cancellation, existing-account resolution, linking, PKCE handoff and unchanged guest access across web, Android and iOS.
- [ ] 18.10 Configure the Meta app and server-only credentials, verify applicable publication/privacy/data-deletion requirements, and record real web/Android/iOS acceptance before enabling Facebook; do not treat fixture tests as live approval.

## 19. Progressive Mobile Contact and Verification

- [ ] 19.1 Complete the action/purpose/recipient matrix: group/event creation requires verified mobile contact (approved 2026-10-31); document delivery provider/channel, countries, challenge lifetime/retry limits, send/cost caps and retention/removal policy while preserving no-phone beneficiary and assisted-participation routes.
- [ ] 19.2 Add private account mobile contact storage, E.164 normalization and verification metadata; test international/invalid input, account isolation, scoped disclosure, removal and exclusion from public payloads/logs/analytics.
- [ ] 19.3 Implement account/number/purpose-bound one-use verification with the selected delivery provider; test invalid/expired/reused/superseded codes, resend and attempt limits, provider outage, concurrent verification and recent-reauthenticated replacement/removal without takeover or automatic account merging.
- [ ] 19.4 Enforce the approved phone requirements in backend action authorization and expose verification state/errors to shared KMP contracts; test direct API bypass, stale client state, revoked proof and preservation of unrelated account operations.
- [ ] 19.5 Add Android account settings and progressive phone prompts using shared server state; test Spanish/English labels, international input, accessibility, failed verification, cancellation, account switching and unsent-draft preservation without automatic resubmission.
- [ ] 19.6 Add equivalent iOS settings and progressive prompts through the shared bridge; verify the same behavior and VoiceOver/keyboard access without a separate Swift-only verification authority.
- [ ] 19.7 Add mobile-first web contact settings/prompts with session/CSRF protection; test missing/unverified/verified states, explicit sharing, safe draft preservation and cross-client state consistency, then record live delivery acceptance for supported countries before activation.

## 20. Account lifecycle

- [ ] 20.1 Approve the per-category retention/deletion policy, then implement idempotent recently reauthenticated account deletion with an atomic permanent authorization block; test existing tokens/sessions, reset/verification links, handoffs, social callbacks, role checks and concurrent mutations cannot restore or access the deleted account.
- [ ] 20.2 Implement provider unlink and revocation callbacks with provider proof validation, explicit recent reauthentication, last-working-sign-in-method protection and audit events; test replay, cross-account requests and preservation of contribution provenance.
- [ ] 20.3 Verify provider-specific data-deletion instructions, user-visible deletion progress and retained-record explanations before store review; do not mark deletion complete while an active identity/session remains.
- [ ] 20.4 Implement Android settings deletion, explicit confirmation, acknowledgement/retry and private-cache/outbox removal; test Spanish/English, accessibility, offline/response-loss and another device reconnecting after deletion.
- [ ] 20.5 Implement equivalent iOS settings deletion and shared-state cleanup; test stale Keychain/session recovery, deep links, cancelled reauthentication, VoiceOver and parity with Android.
- [ ] 20.6 Implement web account deletion and a public ownership-verified request/status resource usable without installed apps; test CSRF, reauthentication, enumeration protection, scoped status access and no dependency on support or organizational ownership transfer, then configure the store deletion URL.
- [ ] 20.7 Implement resumable personal-data purge/anonymization, restricted retention expiry, provider token revocation, delivery suppression and backup deletion replay; test failed/retried jobs, retained financial provenance, public identity removal and no resurrection after restore. Record that offline device copies clear on next contact, not instantly.
- [ ] 20.8 Obtain independent legal review of the published ES/EN terms/privacy and approve remaining category-specific retention, backup expiry and financial policies before enabling their associated functions. Owner-approved adult policy, current documents and versioned registration acceptance are completed separately in 20.9; this task is not closed by editorial publication.

- [x] 20.9 Publish owner-approved ES/EN terms and privacy notice at stable public URLs with the institute identity, adult-only account rule and temporary working notificaciones@generosity.global contact; link landing and hosted registration, enforce current-version acceptance for new password/social accounts, and test missing/declined/stale receipts.
- [x] 20.10 Implement an operator-confirmed permanent access-block flag with atomic credential/session/reset/handoff revocation and audit; test replayed registration, delayed provider subjects and queued account mail. This bounded task does not complete 20.1 or 20.3–20.7.
- [x] 20.11 Publish the account/legal/portal release after local validation and verify live legal resources, health, guarded intake and landing links. Record real-device journeys and independent legal/store-deletion review as pending rather than archiving the foundation.

20.9–20.11 evidence: release `bd5b82e` published to API and portal on 2026-11-01;
both GitHub workflows succeeded. Four public legal pages, policy fields, protected
intake and exact landing file hashes were checked live. Local regression and scope
limits are recorded in [account-policy tests](../../../docs/testing/account-policy.md)
and the [release record](../../../docs/releases/account-portal-2026-11-01.md).
Full erasure, provider revocation, native deletion, independent review and physical
device journeys remain unchecked. No foundation archive is appropriate.

## 21. Partial Goods Contributions and Progress

- [x] 21.1 Expose center-fulfilled goods totals separately from commitments; negotiate the native sync field and retain the legacy payload. Test one box of ten, staff fulfillment, wrong units, cancellation, retries and capacity overflow.
- [x] 21.2 Add optional validated receipt data and shared progress calculations; preserve cached unknown receipts and keep fully committed needs visible until expiry/closure, with offers disabled.
- [x] 21.3 Render localized progress, units, remaining capacity and receipt totals in Android, iOS and the center portal; explain package versus loose-item quantities in contribution/publication forms.
- [x] 21.4 Complete backend/shared/native builds, portal accessibility/layout checks and iOS simulator acceptance; record results in docs/testing/need-progress.md. Automated labels/layout passed; manual screen-reader and physical-device acceptance remain pending.
- [ ] 21.5 Complete production API/portal rollout and acknowledged native offer -> center fulfillment -> client refresh acceptance. Preview API/portal and updated native builds were distributed on 2026-11-06; preview HTTP partial-offer/receipt/history and Android emulator progress display passed. Full native-to-center acceptance and production rollout remain open. No payment activation is included.
