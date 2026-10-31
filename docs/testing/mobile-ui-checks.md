# Mobile UI Checks

## Implemented Smoke Tests

The original two tests on each platform check opening the
app, manually switching to Spanish, opening a sample center from the list,
displaying its current needs, and the anonymous profile empty state. The
initial cross-platform execution was 2026-10-25, as recorded in the repository README.
They do not prove real maps/location, authentication, persistence, contribution
forms, registered history, reminders or alert delivery.

On 2026-10-26 Android's suite grew to seven passing tests: the original two and
five in `MapDiscoveryTest`. The new tests inject a provider-free renderer to
verify shared filters/selection, empty results, map/list state retention,
localized delayed-load feedback, retry and list/details without a map. The
actual app smoke tests run without a Google key. This does not validate live
Google cartography or gestures. See [native map acceptance](../native-maps.md).
iOS code/tests were not changed or rerun in this Android slice.

On 2026-10-30 the language slice passes ten Android connected tests (four app
UI tests, five map-contract tests and one native preference-storage test) and
six iOS UI tests. Both native selectors live in Settings and support system,
Spanish and English. The checks cover persisted override, return to system
mode and retaining a localized search. iOS UI tests also cover ordered regional
device languages, unsupported-language fallback and app relaunch; Android's
storage test covers persistence through adapter recreation with regional input.
Shared tests cover language precedence, filter/selection retention, account
transitions and rejecting late discovery results from a previous locale.
English/Spanish iOS Settings screenshots are retained as XCTest attachments
and were visually inspected. These results do not certify the future welcome
screen, all localized dates/errors/notifications, full iOS domain parity or
the visual acceptance matrix below.

Later on 2026-10-30 the discovery integration expands the passing suites to
eleven Android connected tests and nine iOS UI tests. Both native suites select
the children-support category, retain the selected center through repeated
Map/Settings/List navigation and display the same sample remaining quantity.
iOS also verifies marker-to-list detail selection and empty searches removing
stale detail content. A discovery screenshot is retained as a test attachment
and was visually inspected. The 36 shared ARM64 iOS tests include controller
parity against Android's shared-state path, need status/priority and a single
recorded offer after repeated observation/navigation. These are fixture-based
integration checks, not evidence of backend authority or full history/forms
parity. The broader visual acceptance matrix remains pending.

## Target Coverage

The broader mobile foundation contract still requires these UI checks:

- Anonymous users can open discovery without registration.
- Users can switch between English and Spanish.
- Category/search-filtered centers remain visible in map and list flows.
- Users can open a Kindness Center detail view.
- Current needs, urgent markers, and money donation boundaries are visible.
- Profile empty states are visible before contribution history exists.
- Notification settings expose nearby-center, urgent-need, and reminder controls.
- Visit-signal copy states that official verification happens through a future backend/backoffice workflow.

Android checks live in `androidApp/src/androidTest/java/org/generosity/app/GenerosityUiTest.kt`.

iOS checks live in `iosApp/GenerosityAppTests/GenerosityUiChecklistTests.swift`.

## Visual Acceptance Matrix

The user requested visual alignment only, preserving existing flows. The
2026-10-31 [native visual verification](native-visual-verification.md) records
the executed subset and remaining checks. The original smoke tests alone do
not establish visual acceptance. Contracts:
[mobile-welcome](../../openspec/changes/define-generosity-mobile-app-foundation/specs/mobile-welcome/spec.md)
and [mobile-visual-alignment](../../openspec/changes/define-generosity-mobile-app-foundation/specs/mobile-visual-alignment/spec.md).
Compare against the retained [reference](../generosity-design-reference.png).

| Surface | Capture and compare | Required state variants |
| --- | --- | --- |
| Welcome | Wordmark, light background, guest/sign-in/registration hierarchy and illustration placement. | First entry, long labels, enlarged text; no promotional device frame. |
| Map and list | Compact brand/search/categories, dominant map, selected-center preview and consistent list rows. | Selection, long names, filtered empty results, denied/unavailable location, loading/error. |
| Center details | Identity/metadata, description, needs and help actions in order; no nested section cards. | Long name/description, no photo, no needs, unsupported payment action and failed offer. |
| Current needs in details | Aligned icon/title/priority/description and available quantity/unit data. | Each priority, long translated text and no eligible needs; no new global-feed controls. |
| Profile and impact | Account/guest identity, compact labeled metrics and history in the existing Profile destination. | Guest, no contributions, populated history, unavailable metric and self-reported/center-confirmed provenance. |
| Settings and existing forms | Matching type/spacing/control roles and existing actions. | Both locales, field errors, disabled/pending controls, keyboard open and supported appearance modes. |

For Android and iOS separately:

- Capture at least one small and one large supported phone in logical native units, recording device/OS, locale and text-size settings with the result.
- Cover Spanish and English at default and enlarged system text; use long content, safe-area insets and keyboard-open search/form states.
- Verify meaningful touch areas, screen-reader order/names, non-color selection/priority cues, contrast and no text/control overlap. Include manual accessibility checks, not screenshots alone.
- Use explicit deterministic test fixtures and distinguish these from production catalog/accounts. Test real data/error binding separately; fixture screenshots do not certify a backend, map or payment integration.
- Compare visual hierarchy and styling to the reference, then use approved per-platform captures for regression checks. Native typography/control differences and deliberately omitted unapproved functions are expected and documented.
- Never approve a test renderer or unavailable-map state as working cartography, relabel entry counts as hours, or fill missing metrics with the reference's sample balances to make a screenshot match.
- Rerun navigation, search/filter, needs and profile tests to demonstrate that visual changes preserved existing behavior. Do not mask application controls or content to hide screenshot differences.

Store verification evidence with task 17.10; do not mark the visual change
complete until relevant native checks and reference review have actually run.
