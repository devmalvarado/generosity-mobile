# Native design preview validation

Date: 2026-11-03. Source prototype: `codex/visual-design-map` at `5a5bd63`.

## Automated cases

- Shared `PreviewSessionTest`: all 34 routes in ES/EN, unique control identities, guarded links, unknown/missing/revoked access, session clearing, blood-category filtering and selection/back/favorites, positive quantities and valid dates, pending/confirmed/completed sample offers, independent sessions, consent, blocked payments/tax, reversible participation/ranking and registration-coordinate validation.
- Shared `FeatureAccessTest`: effective preview flags recognize only known keys; existing account/offline checks apply.
- Backend `FeatureFlagsTest`: ten preview groups start off; independent verified-tester targeting; existing admin authorization/CSRF/audit/version and real-operation regression tests.
- Android `NativePreviewTest`: native pin/detail/favorite flow, refreshed grant removal, and all-route inventory tour.
- iOS `NativePreviewTests`: native pin/detail flow and all-route inventory tour using a DEBUG-only isolated fixture.

## Reproduction

Use the normal local Java/Android environment and the existing Docker test database:

```sh
./gradlew :shared:jvmTest :shared:testAndroidHostTest :shared:prepareSharedBridge
./gradlew :androidApp:assembleProductionDebug :androidApp:assemblePreviewDebug
./gradlew :androidApp:connectedProductionDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.generosity.app.NativePreviewTest
cd backend
docker compose exec -T app composer test
```

Generate the Xcode project with `xcodegen generate --spec iosApp/project.yml`, then run scheme `Generosity` on a simulator, selecting `GenerosityLocalizationTests` and `GenerosityUITests/NativePreviewTests`. iOS tests generate screenshots as XCTest attachments. Android screenshot export is separate from the functional tour to avoid emulator PixelCopy timeouts interrupting route assertions.

## Results

- 90 shared tests passed on JVM and 90 on the Android host target (the same cross-platform suite).
- Backend: 101 tests, 5,605 assertions passed on isolated MariaDB/PHP 8.4.
- Android production and preview APKs built. The final functional run passed both native UI cases on the API 34 ARM64 emulator, including all 34 routes and access revocation (2 tests, zero failures; Gradle completed in 10m 23s on the slow emulator).
- iOS: 19 existing bridge/localization/location tests passed; the strengthened 34-route native UI tour passed on iPhone 16 Pro / iOS 18.6 with no failures. The tour asserts each destination title and waits for the sheet dismissal before screenshots.
- OpenSpec strict validation and centralized design-resource checks passed.
- Captures are under `screenshots/native-design-preview/`. Reviewed iOS welcome, impact, disabled-payment and map layouts, including final light/dark map captures. Android map clipping and pin placement were corrected and the final map capture was inspected.
- Android screenshot export encountered an emulator PixelCopy timeout. That run was not counted as passed, and screenshot export was removed from the functional test. Only the successfully exported Android map image is retained; no full Android screenshot inventory is claimed.

## Limits

No production flags or data are changed. No physical-device, VoiceOver/TalkBack or full large-text acceptance is claimed. Fictional forms do not validate unimplemented provider/service contracts. This evidence does not close the underlying real-feature roadmap specs.

## 2026-11-04: System navigation bars

Removed the persistent fictional-session banner and technical map footer. iOS now uses system `TabView` and `NavigationStack` toolbars; Android uses Material 3 `TopAppBar`, `NavigationBar` and `NavigationBarItem`. The inventory remains in the top toolbar. Runtime flags and the isolated data model are preserved.

- Shared: 91 JVM tests and 91 Android-host tests passed, including primary tab selection, nested destinations, partial grants and groups without a primary tab.
- iOS: `testNativeTabBarWithoutSessionBanner` passed on iPhone 17 / iOS 27.0. It checks the actual system tab bar, all five destinations, selected state, banner absence, center detail and return to the map.
- Android: `nativeNavigationSelectsEachTabWithoutSessionBanner` and `nativeMapSelectionDetailsAndFlagRevocation` passed on API 34 ARM64 (2 tests, zero failures). Production and preview APKs compiled.
- Visual review: iOS light/dark system bars and Android native selected-tab appearance. Screenshots: [iOS light](screenshots/native-system-bars-2026-11-04/ios-light.png), [iOS dark](screenshots/native-system-bars-2026-11-04/ios-dark.png), [Android](screenshots/native-system-bars-2026-11-04/android-profile.png).
- This revision used focused navigation regression tests; the full native 34-route tour above was last run on 2026-11-03. No physical-device or assistive-technology acceptance was added.

## 2026-11-04: Floating map search and brand header

The native preview map now places the shared wordmark in the center, list/filter buttons on the left, and notifications/screen inventory on the right. The repeated map action row and close X are removed. The inventory retains the actual exit action; the standalone iOS test fixture has no no-op exit. Search floats above the map with a magnifier and simulated-location button, using iOS Liquid Glass/material and Android Material 3. iOS placeholder and icon colors use the shared adaptive palette for contrast.

- iOS: `testFloatingMapSearchAndToolbarActions` and `testNativeTabBarWithoutSessionBanner` passed on iPhone 17 / iOS 27.0. The map case covers list selection, resource filters, search removing an excluded pin, simulated location, inbox/back and inventory, as well as the wordmark and absence of the old title/exit button.
- Android: `floatingMapSearchAndToolbarActionsRespectFlags` and `nativeNavigationSelectsEachTabWithoutSessionBanner` passed on API 34 ARM64 (2 tests, zero failures). The map case additionally revokes the communication group and verifies the bell disappears. Production and preview APKs compiled. The map case also passed in dark appearance.
- The search test exposed stale Android pins: the child composable received an unchanged mutable session and could skip rendering. It now receives the current center list, selection, language and origin state, so filtering and pin selection redraw consistently.
- Visual evidence: [iOS light](screenshots/native-map-header-2026-11-04/ios-light.png), [iOS dark](screenshots/native-map-header-2026-11-04/ios-dark.png), [Android light](screenshots/native-map-header-2026-11-04/android-light.png), [Android dark](screenshots/native-map-header-2026-11-04/android-dark.png). Reviewed logo centering, control spacing, floating search and both appearance modes.
- OpenSpec strict validation, generated design resources and whitespace checks passed. No physical-device, full large-text/assistive-technology or older-iOS runtime acceptance was added. No backend deployment or preview-flag activation occurred.

### Manual Android exploration

The debug-only `NativePreviewActivity` keeps the same isolated design open for manual browsing without a running instrumentation test. Built and installed `previewDebug`, opened the map in Spanish on `Generosity_Google_API_34`, and verified the rendered screen and resumed activity. The first launch took 22 seconds; Android subsequently reported the activity displayed. The merged `previewRelease` manifest was generated and checked to exclude this activity, while `previewDebug` includes it. No account or backend connection is constructed by this entry point.

## Connected mobile destinations — 2026-11-04

This evidence covers the new bindings to the existing mobile integration, not completion of all 34 backend features.

- Shared JVM: 93 tests, zero failures. `ConnectedDesignTest` checks current/covered/expired needs from visible centers, empty filtered results, language updates and group boundaries.
- Android API 34: `ConnectedDesignTest.connectedOfferUsesServerIdAndSharedOutboxHistory` passes. It opens an API-supplied center through the connected list, retains cached needs offline, submits quantity 3 with the actual need ID, verifies durable retry/idempotency and authoritative history, searches a manual area with the typed query, clears the incompatible catalog text filter, and observes contribution/preview revocation.
- Android: `NotificationInboxTest.connectedInboxOpensRealCenterAndClearsOnSignOut` passes with an isolated connection. The notice opens the real center ID without needing push permission; sign-out clears private notices.
- Android isolated prototype regression: `NativePreviewTest.floatingMapSearchAndToolbarActionsRespectFlags` passes. Mock fixtures remain usable independently of the connected renderer.
- iOS 27 simulator: `NativePreviewTests.testConnectedDesignUsesSharedCatalogAndAccountScreens` passes through list/detail and needs/history/impact/inbox/profile/settings/welcome/map. Its offline harness uses the shared bundled catalog, not `PreviewSession` center fixtures. The test caught a parent accessibility identifier overriding child controls; the root identifier was removed.
- iOS isolated prototype header regression and `BackendBridgeTests.testEnteredPersonalHelpCrossesBridgeAndPrivateHistoryClearsOnSignOut` pass. The latter checks the entered quantity/notes, bridge payload, server history and private-state cleanup.
- Final production/preview debug APKs and the iOS Debug simulator app compile. Android instrumentation probes compile with the provider-neutral overlay-padding argument. The production Release merged manifest excludes both debug design activities.
- OpenSpec strict validation and `git diff --check` pass.

Manual public-catalog inspection uses **production debug builds** with the connected DEBUG launch entry: real MapKit/Google Maps cartography and published center IDs/names are displayed. No production contribution, visit, account registration, staff action or payment was submitted for these tests. iOS uses Spanish and Android follows its English device setting. Map overlays retain provider attribution; Google Maps receives measured overlay padding and iOS reserves its attribution footer.

Screenshots are in `screenshots/connected-native-design-2026-11-04/`. API read probes returned 200 for health/centers/needs and 404 for features/campaigns/occurrences/meals. Normal feature activation and distribution remain pending; DEBUG launch visibility does not grant API permissions. Physical phones, real provider login, real push delivery and the full 34-screen tour were not repeated for this binding change.

Final manual appearance checks: light/dark native controls are readable on both simulators; iOS MapKit also changes its cartography. The existing Android Google Maps adapter still uses daytime base tiles in dark appearance. Both simulators are left open in connected mode, with light appearance restored.
