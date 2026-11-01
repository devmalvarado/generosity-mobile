# Foundation Implementation Audit

## Adult policy and scoped access closure — 2026-11-01

This entry supersedes the earlier contact/legal-draft status below. The owner
approved adult-only accounts, current ES/EN terms/privacy and temporary use of
notificaciones@generosity.global, handled by institute staff. Separate mailboxes
remain deferred. Current documents, registration receipts and an operator-assisted
permanent access block are implemented; automated checks are recorded in
[account-policy evidence](../../../docs/testing/account-policy.md). Publication
is tracked separately in 20.11. No full deletion/store-review task is complete.
Existing accounts have null receipts rather than fabricated consent.


## Institute identity and contact channels — 2026-11-01

The owner supplied the institute's fiscal identity/address and designated institute
personnel to handle `soporte@generosity.global`, `legales@generosity.global` and
`privacidad@generosity.global`. These details replace the corresponding legal-draft
placeholders. Mailbox creation and receipt checks remain pending: after preparing
the support creation form, the owner chose documents-only work. No new mailbox,
staff access or legal publication was completed. Task 20.8 remains open for the
privacy notice, policy decisions, review, working contacts and implementation.

## Account-email fixes and portal release preparation — 2026-11-01

Foundation task 7.17 is implemented and validated locally: Spanish/English queued
mail, verification/reset HTML outcomes, visible form errors, safe retries and
preserved iOS/Android PKCE context. API JSON responses and revocation remain
covered. Malformed email arrays now produce validation errors, not HTTP 500.
The release preparation includes the pending center-application migration, auth
assets and route/view cache refresh, plus a portal prerequisite check before any
static upload. No deployment, push or production migration occurred. See
[release instructions](../../../docs/releases/account-portal-2026-11-01.md).
Task 7.18 retains the production-mail/physical-device acceptance gate. The initial
catalog, legal publication details and intake reviewer remain separate inputs.
The institute corrected its bank to BBVA; payment enablement remains disabled.

## Account entry visibility and Android parity — 2026-11-01

Both native guest Profiles now expose **Create account** alongside **Sign in**.
The shared hosted entry shows the selected email form directly and uses local
Google/Apple logo assets. The owner approved the hosted UI deployment, which is
live; neither social provider is configured. Android's follow-up passed five JVM
tests and four API 36 instrumented tests, including both languages at 200% text,
busy/registered states and the actual Profile-to-browser registration/login
intent with Android client, locale and PKCE context. Public HTTP checks preserve
that context when switching modes. See [test scope and reproduction](../../../docs/social-sign-in.md#account-entry-visibility--2026-11-01).
Those earlier checks did not complete account-email UX. The later local fixes
above close 7.17; tasks 7.9, 7.18 and 18.5 retain live/native acceptance.

## Hostinger SMTP activation — 2026-11-01

The user selected the included Hostinger mailbox service. Production now uses
`notificaciones@generosity.global`, implicit TLS on port 465 and a private mailbox
app password. Both the bounded queue worker and Laravel scheduler run through
hPanel every minute. The scheduled worker processed an approved Gmail test email,
and the recipient confirmed Inbox delivery on 2026-11-01;
no queued or failed jobs remained. DNS authentication and private configuration
checks passed. [Deployment evidence and recovery instructions](../../../docs/hostinger-backend.md#smtp-activation-evidence--2026-11-01)
record the exact scope. Task 18.6 is met by that deployment: HTTPS, host-only
sessions, exact portal CORS, the real database, SMTP transport and scheduled
auth cleanup, with the landing workflow still separate. A subsequent
[live account-email review](../../../docs/testing/account-email-journeys.md)
verified registration, resend, signed links, password reset and session revocation.
Those email localization, completion-screen and validation gaps were subsequently
corrected locally in task 7.17; production verification remains in 7.18. Provider quotas and cross-client acceptance remain open.
The expanded backend suite passed 38 tests / 425 assertions. No social/SMS provider
was enabled, and this review did not deploy application code.

## Current integration increment — 2026-10-31

Local changes on `codex/backend-php-mysql` now connect both native roots to the
PHP catalog/account snapshots. Release entry points no longer select the bundled
catalog; debug-only fixtures keep deterministic UI regression coverage. Native
adapters bind credentials and protected cache/outbox files to their API/account.
Operations are saved before sending, retain their idempotency key after a lost
response, and stop dispatching under another account. Public discovery refreshes
independently of private-session expiry. The API portion was published with
`1adf57a`. A production catalog read returned no centers. The center workspace
is not on the live landing until its portal files reach `main`.

Both apps now collect goods/time/personal-help inputs and show server history,
owner cancellation and self-reported completion. The center workspace selects
assigned centers, edits/publishes/closes needs and confirms/fulfills contributions.
Actual time is explicitly recorded, never inferred from planned duration. Visits
are durable locally for guests and queued to the owned API for accounts; neither
route confers official center verification. Notification preferences are durable
per account/device, while delivery remains unavailable. Welcome has guest/sign-in/
create-account entry and safe phone/email/web contact actions are wired.

Evidence and exact remaining acceptance gaps are in
[backend integration verification](../../../docs/testing/backend-integration.md).
Partial tasks remain unchecked: full delta sync/tombstones, profile/preferences
server updates, all rejection/draft flows, live cross-device/CORS acceptance,
GPS/manual origins, actual alerts/reminders, provider credentials, phone delivery,
Facebook, account deletion and the full visual/accessibility matrix are still open.
Welcome illustration/legal destinations are not release-complete. The phone rule
for group/event creation is approved; SMS provider selection is pending.

Do not archive this foundation or equate local tests with production acceptance.
The catalog/account endpoints and two migrations are on the published API.
Native distribution and the live center workspace are not. SMTP and cron were
activated afterward as recorded above.

## Earlier checkpoints (historical evidence)

**2026-10-31 publication correction:** The account sign-in increment was merged
to `main` as `2f71b73` and published. `https://api.generosity.global/api/v1/health`
reported ready with payments disabled. `/.env` stayed forbidden. Google and Apple
buttons remain disabled because no provider credentials are configured. At this
checkpoint mail still used the log driver and auth-cleanup cron was not
scheduled. That mail and cron status was superseded on 2026-11-01. Tasks
7.8–7.10, 16.4 and 18.5 stay open: documentation and hosted entry are not
device acceptance or live provider approval.

**2026-10-31 Facebook/mobile-contact planning update:** The user added Facebook
entry and private mobile numbers for selected features to the requirements.
Specs now cover missing/unverified Facebook email, explicit linking, progressive
phone collection, possession verification and server-enforced action gates.
Tasks 18.7–18.10 and 19.1–19.7 remain unchecked: no Facebook adapter, phone storage,
verification delivery or phone-gated action is implemented by this update.
Group/event creation now requires verified mobile contact. The delivery
provider/countries/limits and retention policy still require decisions. Existing Google/Apple test evidence below does not cover them.

**2026-10-31 Google/Apple identity increment:** The user selected identity only
for Generosity web/Android/iOS, excluding other institute platforms. Google and
Sign in with Apple are implemented behind server configuration on
`codex/backend-php-mysql`; neither provider has real credentials yet. Hosted
browser entry, explicit linking, subject-based identities, secure web sessions,
PKCE native handoff, Keychain/Keystore storage and native restore/logout are wired.
Android no longer signs in as the fixed sample “Ana”. Real accounts cannot write
accepted-looking demo contributions. Data synchronization and center workspace
flows remain incomplete; tasks 7.8–7.10 and 16.4 are not certified end-to-end.

Evidence: 30 backend tests / 339 assertions (including cryptographically signed
provider fixtures), 58 shared JVM tests, 5 Android unit tests, Android debug build
and instrumentation-test compilation, iOS simulator build and 4 unit tests,
39 portal tests with one existing WebKit screenshot skip, locale validation and
14 strict OpenSpec items passing. Composer audit reports no known advisories.
The pre-existing missing Android `nearby_foreground_only` resource was filled to
restore cross-platform localization parity. See [identity configuration and test
traceability](../../../docs/social-sign-in.md). Live providers, private-relay mail and
physical-device secure storage/browser returns are still pending. API hosting was
completed afterward; see the publication correction above. Section 18 tracks
this bounded implementation and its outstanding acceptance tasks. The foundation
remains active and must not be archived.

**2026-10-31 PHP backend replacement:** User-requested checkpoint `36eab89`
is preserved on `codex/mockup-backend-kotlin`; `codex/backend-php-mysql`
replaces only the standalone service with Laravel 13 / PHP 8.4 and MySQL/MariaDB.
The HTTP API includes verified accounts, revocable tokens, account recovery,
audited operator staff assignment, public catalog, staff need management,
transactional goods/time offers, versioned/idempotent transitions and separately
labeled personal help/history/impact. Native/shared Kotlin remains unchanged.
The [architecture decision](../../../docs/backend-php-mysql.md) explicitly
selects Laravel's first-party provider instead of the previously unspecified
external identity adapter. That checkpoint did not itself deploy production or
integrate the native apps; the later publication correction records what shipped.

Executable evidence is in `backend/tests/Feature/ApiTest.php` (real MariaDB,
including independent-process reservation/transition races) and
`backend/scripts/smoke.py` (actual local HTTP, queued email verification,
login, durable reservations/history after restart and isolated SQL restore).
The OpenAPI route map and core response fields are checked; full client contract
acceptance is still open. Dependency audit found no known advisories at the
verification time. See [backend verification](../../../docs/testing/backend-php-verification.md)
for the final run counts and environment.

Tasks 11.1/11.4 are now complete for the selected PHP provider/runtime. Task 11.5
is reopened because imports are atomic upserts, with no source-revision conflict
or deletion/tombstone protocol. Tasks 11.2/11.7/11.8 and 13.1–13.3 have replacement
PHP evidence. Profile/preferences updates, visits/reminders/device endpoints,
account sync, notification delivery, client authentication/history adapters,
production SMTP/hosting and all cross-system acceptance remain open. Do not
archive the foundation based on backend-only tests.

**2026-10-31 native visual addendum:** Tasks 17.1–17.3 and 17.5–17.9 are
implemented for the existing Map/List/Profile/Settings flows. Both platforms
have native orange/neutral themes, the supplied logo, responsive discovery,
selected previews, clear detail/need priorities and honest labeled profile
metrics. iOS reads shared state; no duplicate business rules were introduced.
Android's large-text navigation and keyboard resize were corrected during
device testing. Assets, contrast pairs and omissions are recorded in
[native visual style](../../../docs/native-visual-style.md).

Twelve Android tests pass, plus the visual tour on a 320dp-wide emulator at
200% text/dark appearance. English and Spanish tours pass on iPhone SE at
maximum accessibility text/dark appearance. See the final iOS regression and
retained native captures in [visual verification](../../../docs/testing/native-visual-verification.md).
Task 17.4 remains open for live Google attribution/gesture acceptance with a
restricted key/Google-enabled device. Task 17.10 remains open for the full
visual/accessibility matrix, including manual screen readers and the
unattributed iOS clipping report. These UI changes do not implement welcome,
authentication, durable contributions, authoritative impact or gamification.

**2026-10-30 discovery addendum (after the language slice):** Task 5.4 is now
complete. SwiftUI map/list/detail screens observe the root SharedBridge store
and render shared centers/current needs. The Swift sample catalog, independent
filter/sort/expiry rules and duplicate domain types were removed. Native code
only adapts coordinates and localized label resources. Selection remains shared
across tabs, and filtered-out centers no longer leave stale details visible.
Visit feedback uses shared session state and explicitly disclaims durable save
or delivery. Both apps still use the shared demonstration catalog/clock.

Verification: 36 shared ARM64 iOS tests, 11 Android connected tests and 9 iOS
UI tests pass. New controller tests compare direct shared-state inputs with
the iOS bridge, including need status/priority and repeated observation followed
by exactly one offer. Native tests cover category/selection retention, remaining
quantity, marker-to-list selection, language and empty search without stale
details. The iOS discovery attachment was visually inspected. Both apps build;
localization coverage and strict foundation validation pass. Forms, account
persistence, history/impact, native delivery and the backend remain separate
unfinished tasks; task 5.4 does not certify full cross-system parity.

**2026-10-30 language addendum:** Both apps now expose installation-specific
system/Spanish/English settings, restore them before rendering, and resolve
ordered regional device languages through shared Kotlin. Android refreshes
localized discovery without clearing query/categories and ignores late results
for an obsolete query/locale. iOS now links SharedBridge for language state;
restarting its observer replaces the previous subscription and disposal stops it.
Native iOS app copy now uses Localizable.strings instead of duplicate dictionaries.
Task 4.3 is complete. Task 4.6 retains its welcome acceptance gate, and 4.7
still requires complete errors/date/notification integration. This is not full
iOS domain parity (5.4), account-data persistence or backend implementation.
Shared tests pass on JVM/Android host (33 each) and ARM64 iOS (34, including
observer lifecycle); four Android marker unit tests and ten Android connected
tests pass. Six iOS UI tests pass on iPhone 16 Pro / iOS 18.6, including
device-language resolution, persistence across relaunch, return to system mode
and search retention. English/Spanish iOS Settings captures were inspected.
Android lint passes with zero errors and 47 existing warnings. Localization
coverage, map-provider boundaries and strict foundation validation pass.
The historical audit below is not the current language behavior.

**2026-10-26 addendum:** Android's placeholder has now been replaced by a
Google Maps Compose adapter behind `CenterMapRenderer`. Shared business code
is unchanged and iOS retains MapKit. APK compilation, four marker unit tests,
seven Android UI tests, 25 shared tests each on JVM/Android host, localization
coverage, the map-SDK boundary guard and strict foundation validation pass.
Lint has zero errors and 47 warnings. No key is configured and the existing
emulator is AOSP, so live Google tiles/gestures/provider failures remain
unverified and task 5.2 stays open. GPS, iOS shared integration and MapLibre
implementation are not included. See [native maps](../../../docs/native-maps.md).
The dated audit below remains the historical pre-implementation snapshot.

Date: 2026-10-25. Code baseline: `d489614` on `main`.
Method: source, test and documentation inspection. No application code is
changed by this audit. Earlier successful build/test results remain recorded
in [README.md](../../../README.md); they are not proof of full spec compliance.

This evidence snapshot predates the user's subsequent scope expansion on the
same date. The active [spec index](spec-index.md) now includes welcome,
device-language settings, all identified gaps, backend scope and the subsequently
requested mobile-first center web portal. The external landing's source review
is recorded in [center-portal.md](../../../docs/center-portal.md). No native,
web or backend code was implemented by these planning updates.
The later visual-only alignment request is also specified, not implemented;
its screenshot/accessibility acceptance is tracked in tasks 17.1-17.10.

## Archive Correction

The user requested reopening the prematurely archived foundation. Its
original `.openspec.yaml` creation date (2026-05-13), proposal, design and
seven delta specs are retained in this active change. Git history preserves
the earlier archive and its all-checked task list. Original task IDs are
retained, partial work is unchecked, and uncovered work has explicit tasks.

The seven main specs were already synchronized before this audit. They
remain normative contracts, not a release/compliance report. The foundation
deltas originally retained their ADDED form to preserve provenance. Shared
contribution behavior from the consolidated code was documented in both main
and delta specs. Subsequent additions and MODIFIED blocks now describe planned
behavior and intentionally differ from main until reviewed and implemented.

`openspec status` can report all artifacts complete while `tasks.md` still
contains unfinished implementation. Do not archive on artifact presence or
structural validation alone. No other active change was found to archive.

That statement reflects the initial audit. The later
[reference product follow-up](../add-reference-product-features/spec-index.md)
is now also active and entirely unimplemented; its newly scoped reviews,
events, gamification and hosted payments do not change this source snapshot.

## Capability Evidence

Paths below are repository-relative. "Partial" means the end-to-end contract
is not met on both platforms, even when individual shared rules pass tests.

| Capability | State and evidence | Open task references |
| --- | --- | --- |
| `mobile-platform-architecture` | Partial. Shared models/use cases exist and Android observes `shared/src/commonMain/kotlin/org/generosity/app/GenerosityAppState.kt`. `iosApp/SharedBridge/Sources/SharedBridge/GenerosityStore.swift` exists but `iosApp/GenerosityApp/ContentView.swift` still uses `SampleData`. | 5.4 and platform adapter tasks in sections 5, 7 and 8. |
| `kindness-center-discovery` | Partial. Shared search/category/distance rules and anonymous detail/list flows exist. `androidApp/src/main/java/org/generosity/app/ui/GenerosityApp.kt` has a placeholder `MapSurface`; iOS MapKit starts at fixed Mexico City coordinates. Neither app supplies current GPS to shared discovery or displays available contact fields. Catalog data is hard-coded; JSON is a documented contract, not a reader. | 3.5, 5.2, 5.4, 5.6-5.11. |
| `kindness-center-needs` | Partial. Shared `CurrentNeedsUseCase`, `CenterNeed` and `RecordContributionUseCase` implement status, priority, expiry, quantity limits and payment rejection. Apps still use a May 2026 demo date. iOS duplicates an older needs model. Local commitment changes are not authoritative backend inventory. | 5.4, 6.6, 7.13-7.14. |
| `user-contribution-profile` | Partial. Shared registered-user rules, in-memory history and impact qualification exist. Android signs in as fixed user `user-1` / `Ana`, sends five goods or a fixed volunteer/weekday offer, and omits most error states. iOS displays only an anonymous profile. Personal-help recording has no UI; no provider, persistence or status-transition flow exists. | 7.2, 7.4-7.14. Lifecycle authority and units are now specified but still unimplemented; see sections 11-13 of tasks. |
| `app-localization` | Partial. Both apps have English/Spanish resources and a manual selector; shared fallback/stable codes and a key-coverage script exist. Both start in English without restoring user/device choice. Shared notification strings are hard-coded rather than wired to native resources; locale changes do not refresh Android shared search/sort results. | 4.3, 4.6-4.7, 8.6, 8.10. |
| `location-notifications` | Partial. Models, eligibility and permission prompts exist. Android ignores permission callback results; iOS tracks authorization but does not schedule notifications. Switches only change memory. No reminder editor, durable schedule, actual nearby/urgent delivery or rate policy exists. | 8.3-8.13. |
| `center-verification-signals` | Partial. Verification labels and explicit buttons exist, and shared rules distinguish evidence from official verification. Android `signalVisit` only changes UI state; iOS only shows text. Neither records/queues through `VisitSignalRepository`. | 9.1, 9.5-9.6. |

## Task Corrections

Previously checked tasks 4.3, 5.2, 5.4, 7.2, 7.4, 7.5, 8.3-8.6, 9.1,
10.2 and 10.3 have been reopened. Platform-spanning tasks are split where
necessary. Additional tasks expose missing integration instead of treating
models, placeholders, toggles or a success message as completed features.

Retained checked tasks have narrower evidence:

- 3.2/3.4: an in-memory catalog adapter and exception-to-empty fallback;
  a real JSON reader and its failure tests are tracked by 3.5.
- 5.3: an interactive iOS MapKit/list surface exists, but shared data and
  current location remain open in 5.4/5.7.
- 6.4: date filtering exists; production-clock integration remains in 6.6.
- 7.1: registered/anonymous domain state exists, not authenticated identity.
- 8.2: permission prompts exist, not handling every result or delivering alerts.
- 10.1: shared unit coverage exists; this does not certify native integration.
- 10.4: structural OpenSpec validation, not acceptance of unfinished behavior.

## Verification and Archive Gate

Existing shared coverage is in `DomainLogicTest.kt`, `ContributionFlowTest.kt`
and `GenerosityAppStateTest.kt`. Each platform has two UI smoke tests, not
the full checklist in [mobile-ui-checks.md](../../../docs/testing/mobile-ui-checks.md).
The reopened task list remains the completion authority.

Before re-archiving, complete or explicitly rescope each open task with the
user, verify every scenario on the relevant platform, reconcile main/delta
requirements, and run strict validation. Do not use `--no-validate` or
`--yes` to hide unfinished work. Do not remove existing requirements merely
to make the prototype look complete.

The [visual reference](../../../docs/design-reference.md) is recorded
separately. Welcome belongs to this foundation; ratings, rewards, events
and payments have their own active changes and remain unimplemented here. Backend alert delivery is
planned, not evidence that real-time data or notifications already work.
