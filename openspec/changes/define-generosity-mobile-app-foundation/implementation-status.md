# Foundation Implementation Audit

**2026-10-31 Facebook/mobile-contact planning update:** The user added Facebook
entry and private mobile numbers for selected features to the requirements.
Specs now cover missing/unverified Facebook email, explicit linking, progressive
phone collection, possession verification and server-enforced action gates.
Tasks 18.7–18.10 and 19.1–19.7 remain unchecked: no Facebook adapter, phone storage,
verification delivery or phone-gated action is implemented by this update.
The action matrix, delivery provider/countries/limits and retention policy still
require decisions. Existing Google/Apple test evidence below does not cover them.

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
traceability](../../../docs/social-sign-in.md). Live providers, private-relay mail,
physical-device secure storage/browser returns and API hosting are still pending.
The existing main-branch selective landing deployment was inspected and preserved;
this increment performs no push, merge or production deployment. Section 18 tracks
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
external identity adapter. No production deployment or native/portal integration
is claimed.

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
separately. Its welcome screen is now requested; ratings, rewards, events
and payments remain outside the expanded plan. Backend alert delivery is
planned, not evidence that real-time data or notifications already work.
