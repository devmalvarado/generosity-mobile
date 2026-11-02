Reopened on 2026-10-25 after a source audit of `d489614`. Original task IDs
are retained. Unchecked original tasks were previously marked complete but
only partially implemented; see [implementation-status.md](implementation-status.md).
The user subsequently requested the welcome screen and expanded backend scope
with center-authorized contribution transitions. The corresponding new work
is unchecked. Artifact completion is not implementation completion. Original
IDs are stable references, not the execution order; follow the dependency
sequence in [spec-index.md](spec-index.md).


2026-10-31 integration update: catalog/account snapshots, protected native
cache/outbox, entered contribution forms, owned lifecycle actions, durable visits,
local preferences, safe contact actions, welcome routing and an authenticated
center workspace are implemented. The API sync increment is published (`1adf57a`);
the center workspace is not on the live landing until its portal files reach
`main`. See [verification and remaining acceptance](../../../docs/testing/backend-integration.md).
Tasks stay unchecked unless every acceptance condition is evidenced. No change
is ready to archive.

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
- [ ] 4.6 Add the equivalent iOS settings through shared state; verify device changes, guest access, restart and no language flash on welcome.
- [ ] 4.7 Localize contribution errors and notification text through app resources; test live language propagation to search/sort, dates and schedules without losing entered form data.

Language implementation update (2026-10-30): native settings now persist
system/en/es per installation, resolving ordered device languages in shared
Kotlin before the first screen. Android handles locale configuration/resume
without resetting its search; iOS now links SharedBridge for language state
and owns one cancellable observer. Android storage/recreation/search tests and
shared fallback/account-isolation/stale-query tests pass. Task 4.6 remains open
until its welcome-screen acceptance can run: welcome itself is not implemented.
Task 4.7 remains open for errors, dates and scheduled/remote notifications;
search/sort propagation is only part of that task. This language slice did not
replace iOS sample discovery; the later task 5.4 update records that integration.

## 5. Discovery Experience

- [x] 5.1 Implement shared filtering and sorting use cases for category, location, and search criteria.
- [ ] 5.2 Replace Android's placeholder with Google Maps behind a replaceable renderer; verify the SDK boundary, pan/zoom, marker selection, filtered map/list parity and map-provider failure with list fallback on a Google-enabled device with a restricted key.
- [x] 5.3 Build the iOS discovery map and list screens using SwiftUI.
- [x] 5.4 Connect iOS discovery/details to SharedBridge with bounded subscriptions; test equivalent inputs on both platforms and repeated navigation without duplicate actions.
- [x] 5.5 Verify anonymous users can browse, filter, and open center details without registration.
- [ ] 5.6 Feed permission-gated current location into Android discovery and test browsing after denial or revocation.
- [ ] 5.7 Feed permission-gated current location into iOS discovery and test browsing after denial or revocation.
- [ ] 5.8 Add Android manual place search without GPS; test selected-origin distance labels, empty/error results and preserved filters.
- [ ] 5.9 Add iOS manual place search without GPS; test selected-origin distance labels, empty/error results and preserved filters.
- [ ] 5.10 Show Android phone/email/web actions; test missing fields, unsupported schemes, unavailable handlers and anonymous access.
- [ ] 5.11 Show iOS phone/email/web actions; test missing fields, unsupported schemes, unavailable handlers and anonymous access.

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
- [ ] 7.8 Document the selected identity provider and account/session contract, including registration, recovery, required verification and backend validation; review against user-authentication scenarios.
- [ ] 7.9 Integrate Android real registration/sign-in/renewal/sign-out/recovery; test cancellation, expiry, protected credential storage and absence of demo identity fallback.
- [ ] 7.10 Integrate the equivalent iOS identity flow; test cancellation, expiry, protected credential storage and shared account state.
- [ ] 7.11 Add Android durable private stores/outbox and public cache; test restart, migration, save failure, sign-out suspension and account isolation.
- [ ] 7.12 Add iOS durable private stores/outbox and public cache; test restart, migration, save failure, sign-out suspension and account isolation.
- [ ] 7.13 Replace Android fixed offer inputs with forms; test quantity/activity/availability/duration/notes, all rejection codes and draft preservation through authentication/conflict.
- [ ] 7.14 Add equivalent iOS need-offer forms; test shared validation, all rejection codes and draft preservation through authentication/conflict.
- [x] 7.15 Implement shared registered-user offer validation and confirmed/fulfilled-only impact aggregation with focused tests.
- [x] 7.16 Extend shared records with units, planned/actual duration, provenance, sync state and versioned event history; test incompatible units, missing metrics and cancelled/pending exclusion.
- [x] 7.17 Implement and locally verify the account-email UX corrections: queued Spanish/English mail, localized browser success/invalid/expired-link screens and visible validation errors without retained passwords; preserve JSON API contracts and native PKCE context. Serialized queue, HTTP and Chromium/WebKit evidence is in [account-email journeys](../../../docs/testing/account-email-journeys.md).
- [ ] 7.18 Publish the tested account-email release, repeat actual Hostinger email links and complete registration/recovery on physical Android/iOS and the live portal; record delivery and native-return acceptance separately from the local task 7.17 evidence.

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
- [ ] 12.5 Add durable backend notification events and provider delivery worker; test opt-in/category/expiry checks, transient retry, per-installation deduplication and invalid-token retirement.
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

## 16. Mobile-First Center Web Portal

- [ ] 16.1 Review and consolidate the provided landing source/assets into canonical `portal/` without deleting the PoC; document asset provenance, local preview/test commands and browser deployment/session choices without adding an unnecessary UI framework.
- [ ] 16.2 Replace false submission/review/payment claims and fabricated public metrics with real data, explicit samples or unavailable states; test that disconnected forms preserve input and never report acceptance, and public center entry routes to sign-in.
- [ ] 16.3 Rework landing/navigation/form CSS from narrow-screen defaults with wider enhancements; test shrinkable inputs, long labels, wrapping, hidden menu/filter items and 44-pixel primary touch targets.
- [ ] 16.4 Connect provider-backed staff sign-in, assigned-center selection and sign-out; test no-assignment, role revocation, private-view clearing and session renewal without leaking or automatically submitting another account's work.
- [ ] 16.5 Implement center-scoped backend draft/create/publish/edit/close endpoints and audit events; test payload/date validation, private drafts, restart durability, idempotent publication, version conflicts and closing without cancelling accepted contributions.
- [ ] 16.6 Integrate staff writes with public catalog revisions and urgent events; test atomic capacity checks, forbidden unit/method changes, import source ownership and stale-import rejection without losing center-managed needs.
- [ ] 16.7 Implement the phone-first need editor, preview and explicit draft/publish actions; test goods and time inputs, inline errors, language fallback, lost responses and retry without duplicate needs.
- [ ] 16.8 Add the center's needs list with draft/published status, edit and close actions; test empty/loading/error states, outstanding-offer confirmation and conflicts that preserve unsaved edits.
- [ ] 16.9 Connect paginated contribution list/detail and permitted confirmation/fulfillment actions; test donor-cancellation races, minimal donor disclosure and matching native history/impact.
- [ ] 16.10 Add Spanish/English web resources and browser-local system/en/es settings; test ordered language fallback, persisted override, localized errors/dates, keyboard navigation, screen-reader labels and focus restoration.
- [ ] 16.11 Add browser acceptance tests and screenshots at 320/360/390/768/1280 CSS pixels in Chromium and WebKit; assert no horizontal page overflow, no false success and functional menu/forms, then record 200-percent text and real Safari/Chrome virtual-keyboard checks.
- [ ] 16.12 Test direct API cross-center denial, public draft/private-field exclusion, script-content rendering, CSRF/origin protection and revoked sessions; document unresolved failures as release blockers rather than treating browser-only validation as authorization.
- [ ] 16.13 Serve the landing's center entry at `/kindness-center`; verify help entry does not open it, an unassigned account sees no publishing controls, and `kidness-center` is not an alias.

## 17. Native Visual Alignment

- [x] 17.1 Finalize the reference-aligned native color/type/spacing/icon roles and approved brand assets; record contrast checks and deliberate omissions without adding new destinations, domain fields or a separate design-system framework.
- [x] 17.2 Apply the native visual roles to Android theme, existing navigation and reusable controls; verify Map/List/Profile/Settings and supported appearance modes remain usable with unchanged actions.
- [x] 17.3 Apply the equivalent roles to SwiftUI theme, existing tabs and reusable controls; verify the same destinations and platform-appropriate appearance/accessibility behavior.
- [ ] 17.4 Align Android discovery search/categories/map selection and center preview; test existing filter/query retention, list fallback, safe insets and map attribution without treating an unavailable-map state or test renderer as working cartography.
- [x] 17.5 Align iOS discovery composition and selection preview; verify the same behavior on small/large phones without retaining a fixed-height map merely for layout convenience.
- [x] 17.6 Restyle Android center details/current-need rows and supported help actions; test long content, absent photos, all priorities, rejected offers and unchanged contact/visit access.
- [x] 17.7 Restyle the equivalent SwiftUI detail/need surfaces; test identical hierarchy, state handling and readable non-color urgency cues without introducing a new needs feed.
- [x] 17.8 Restyle Android Profile/Impact, Settings and existing form states; verify labeled units/provenance, guest/empty/unavailable states and no invented points, duration, balances or achievement panels.
- [x] 17.9 Restyle equivalent SwiftUI Profile/Impact, Settings and form states; verify the same data semantics and that visual changes do not restore sample-only business logic.
- [ ] 17.10 Record Android/iOS screenshot baselines and reference comparison using the matrix in docs/testing/mobile-ui-checks.md; test Spanish/English, small/large phones, enlarged text, keyboard/screen-reader access, supported appearance modes and failure states, and rerun navigation/flow regressions before marking visual alignment complete.

- [x] 17.11 Consolidate native/web/account colors, font configuration and common dimensions in one source; migrate existing components without changing navigation or business rules.
- [x] 17.12 Establish editable vector logo masters, shared asset export and stale-output checks in native builds, web tests and publication workflows.
- [x] 17.13 Verify generator edits/contrast/stale outputs, Android/iOS compilation and UI regression checks, portal rendering and hosted account CSS/CSP; record physical-device limitations separately. Evidence: `docs/testing/design-system.md`.

## 18. Generosity-Only Social Sign-In

- [x] 18.1 Specify Google and Apple for Generosity web/Android/iOS, explicit account linking, missing-configuration behavior and exclusion of institute-wide SSO.
- [x] 18.2 Implement server authorization-code exchange, signed ID-token validation, browser binding, one-use attempts and subject-based identities; test replay, invalid claims, email collisions, linking and private-relay email.
- [x] 18.3 Implement HttpOnly browser sessions, exact-origin CORS, CSRF and mobile-first account entry in the existing portal; test guest, signed-in, unavailable and failed-logout states in Chromium/WebKit.
- [x] 18.4 Implement native PKCE handoff and secure session storage adapters; remove Android's fixed sample login and gate remote accounts from demo contribution writes; compile both clients and test PKCE/callback/shared-state boundaries.
- [ ] 18.5 Configure Google Cloud OAuth consent/client credentials and Apple Developer Services ID/key/domain; complete live authorization, cancellation, account linking, private relay, restart, expiry and logout on web and physical Android/iOS devices.
- [x] 18.6 Deploy the API to api.generosity.global with HTTPS, secure host-only sessions, exact Generosity CORS/stateful origins, real database/SMTP and scheduled auth cleanup; verify the existing main-only landing workflow remains separate.

18.6 evidence, rechecked 2026-11-01: health is 200, `/.env` is 403, and the cron scripts are not web-reachable. The session cookie has no `Domain`, and is `Secure`, `HttpOnly` and `SameSite=Lax`. CORS allows only `https://generosity.global` with credentials. `routes/console.php` schedules hourly handoff cleanup plus daily Sanctum and reset pruning; hPanel runs that scheduler every minute. Hostinger SMTP delivered one approved test message to Gmail Inbox. The subsequent [account-email review](../../../docs/testing/account-email-journeys.md) passed live backend/security journeys; task 7.17 now closes their user-facing gaps locally; task 7.18 retains publication and production/device acceptance.
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
