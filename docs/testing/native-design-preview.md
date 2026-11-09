# Native design preview validation

Initial evidence: 2026-11-03. Source prototype: `codex/visual-design-map` at `5a5bd63`.
This is a chronological log: each section's limits apply to that particular run.
November 6 follow-ups below supersede initial claims of no preview activation or
physical installation. Preview now defaults to the connected design on both
platforms; iPhone 14 Pro/17 were inspected, iPad launch was user-confirmed and
Android was checked in an emulator. Full device/accessibility/provider acceptance
is still incomplete. Current hosting/catalog evidence is in
[release-controls.md](release-controls.md).

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

## 2026-11-06: Refresh the installed Preview app

The iPhone 16 Pro simulator still had an older compiled Preview app displaying the removed session banner, technical map footer and old navigation. Commit `1dd3498` is already an ancestor of `ffe94f8` on `codex/backend-php-mysql`; no merge or mobile source change was required. The preview API returned enabled design groups, so feature flags were not the cause of the stale UI.

- Regenerated the shared bridge and Xcode project, then built `GenerosityPreview` / `Preview` in a fresh DerivedData directory. Verified bundle `org.generosity.app.preview`, channel `preview`, API `https://api-preview.generosity.global`, and absence of the production Firebase plist.
- Installed the build on iPhone 16 Pro / iOS 18.6 and iPhone 17 / iOS 27.0 without uninstalling existing app data.
- `NativePreviewTests.testFloatingMapSearchAndToolbarActions` and `testNativeTabBarWithoutSessionBanner` passed under the Preview configuration on iOS 27: 2 tests, zero failures. These focused cases use isolated fixtures.
- Separately opened the normal app as a guest and selected **Explore design preview** on both simulators. Verified the preview API's `Preview center` on real MapKit cartography, centered wordmark, toolbar actions, floating search, native tabs and absence of the old session banner/footer. iOS 18.6 uses the material fallback; iOS 27 uses Liquid Glass. The new design remains a separate entry from the ordinary map.
- Evidence: [iOS 18.6 connected design](screenshots/preview-refresh-2026-11-06/ios18-connected.png) and [iOS 27 connected design](screenshots/preview-refresh-2026-11-06/ios27-connected.png). The iOS 27 simulator is left open in the connected design.

No physical-phone installation, Android rebuild, provider authentication, push delivery or full 34-screen tour was repeated. The iOS 27 welcome screen showed a generic request error before guest access; authenticated access was not validated in this refresh. No production deployment or backend mutation was performed.

### Physical iPhone 14 Pro follow-up

Later on 2026-11-06, rebuilt `GenerosityPreview` / `Preview` for the connected iPhone 14 Pro running iOS 27.2, using its existing development provisioning profile. Build/signing and installation of `org.generosity.app.preview` succeeded; bundle metadata still targets the preview API and excludes the production Firebase configuration. Existing app data and the separate production app were preserved.

Observed the app opening and loading `Preview center` on MapKit in dark appearance. The user confirmed opening the new design, but Device Hub continued showing the ordinary map with the preview entry overlapping its wordmark; this follow-up does not claim independent visual acceptance of the new design on the phone. No account, push, location-permission or full navigation acceptance was repeated. The development profile expires on 2026-11-13 at 02:02 UTC.

### Default design in iOS Preview

The iOS Preview channel now enters the connected design directly after welcome, including subsequent launches. Its entry button and exit-to-old-layout action are absent. Effective backend groups and account isolation still apply. Missing groups show a localized unavailable/retry state and guest sign-in, without granting fixture screens or restoring the old layout. Production iOS and Android entry behavior is unchanged.

- iOS 27 simulator: `PreviewEntryTests.testPreviewStartsInConnectedDesignAndKeepsItAfterRelaunch`, `testMissingGroupsCannotRestoreOldLayoutOrGrantSections`, and `NativePreviewTests.testConnectedDesignUsesSharedCatalogAndAccountScreens` passed (3 tests, zero failures). Direct-entry acceptance reads the preview API; the missing-group and connected-navigation harnesses remain isolated. The missing-group case passed again after adding its guest sign-in assertion.
- Result bundles: `/private/tmp/GenerosityDefaultDesign-2026-11-06.xcresult` and `/private/tmp/GenerosityDefaultDesignUnavailable-2026-11-06.xcresult`.
- Localization parity/UTF-8 checks, generated design-system verification, OpenSpec strict validation and whitespace checks passed.
- Built and signed `GenerosityPreview` / `Preview`, verified its signature and preview-only configuration, then updated `org.generosity.app.preview` on the physical iPhone 14 Pro / iOS 27.2 without uninstalling app data.
- Opened the updated app from its home-screen icon and independently observed the connected MapKit design, `Preview center`, centered wordmark, floating search, list/filter/bell/inventory controls, five native tabs, and no optional entry button. Evidence: [iPhone 14 Pro default design](screenshots/preview-refresh-2026-11-06/iphone14-default-design.png).

This check does not repeat provider authentication, push delivery, location-permission prompts, accessibility acceptance or the full 34-screen tour. No production deployment, backend mutation or Android installation occurred.

### iPhone 17 and Android Preview follow-up

Updated the physical iPhone 17 / iOS 27.2 with the same signed Preview artifact validated above, preserving its app data. Opened its home-screen icon and verified direct connected-map startup in light appearance, real MapKit cartography, the centered wordmark, floating search, native tabs and the preview API's center. Evidence: [iPhone 17](screenshots/preview-refresh-2026-11-06/iphone17-default-design.png). The existing development profile expires on 2026-11-13 at 02:02 UTC.

Android Preview now also enters the connected design directly after welcome and activity recreation. Its inventory has no exit to the old layout. Missing or revoked design groups show unavailable/retry and guest sign-in; production Android retains its existing entry behavior.

- Both Android production and preview debug APKs compile.
- Five focused Preview tests pass on Google APIs ARM64 / Android 14 (API 34): `PreviewEntryTest`, both `ConnectedDesignTest` cases, `NativePreviewTest.nativeNavigationSelectsEachTabWithoutSessionBanner` and `floatingMapSearchAndToolbarActionsRespectFlags`. They cover direct entry/recreation, missing grants, guest sign-in, entered partial offers, durable offline retry/history, native navigation and toolbar/group boundaries. Only direct-entry acceptance reads the preview API; other probes use isolated data.
- Four production regression tests pass: `CurrentLocationTest`, `AccountEntryTest` and `MapDiscoveryTest.everyCardSelectsItsPinAndOpensItsOwnDetail`. No production API records were created.
- With explicit user approval, created **Generosity Android Preview Maps** in `generosity-v01`, restricted to **Maps SDK for Android**, package `org.generosity.app.preview` and development SHA-1 `71:E3:02:05:BB:33:A7:9C:39:D6:5D:01:E2:D2:97:C0:7E:65:F2:1B`. Stored its value only as `MAPS_PREVIEW_API_KEY` in ignored `local.properties`. Production Maps/Firebase credentials and billing were not changed.
- Rebuilt and installed the final Preview APK with that key, then manually verified real Google tiles and the center pin, direct guest entry, center detail and current needs with progress from the preview API. The app is left open on the map. Evidence: [Android map](screenshots/preview-refresh-2026-11-06/android-default-design.png), [center detail](screenshots/preview-refresh-2026-11-06/android-connected-detail.png), [needs](screenshots/preview-refresh-2026-11-06/android-connected-needs.png).
- Localization, generated design resources, strict OpenSpec validation and whitespace checks pass.

Android was tested in the emulator, not a physical phone. This follow-up does not repeat real GPS/permission acceptance, provider login, push delivery, dark/large-text accessibility or the full 34-screen tour. Maps is configured for this development certificate; another signing certificate needs its own authorized restriction. Preview Firebase/push configuration remains separate and pending.

### Physical iPad installation — 2026-11-06

Installed **Generosity Pruebas 0.1.0 (3)** on the user's **iPad (6th generation), model A1954, iPadOS 17.7.5**. The user completed computer trust, enabled Developer Mode, and explicitly authorized registering the device in the existing Personal Team and updating the development provisioning profile.

- `GenerosityPreview` / `Preview` built successfully after Xcode completed the device's first development preparation. Verified universal device families `[1, 2]`, bundle `org.generosity.app.preview`, API `https://api-preview.generosity.global`, absence of production Firebase configuration and inclusion of the iPad in the profile.
- The incremental build updated `Assets.car` and the provisioning profile without refreshing the existing resource seal. Re-signed the compiled bundle with the existing development identity and Xcode-generated Preview entitlements. `codesign --verify --deep --strict` passed before installation.
- `devicectl device install app` confirmed installation. The new profile expires **2026-11-14 03:34:37 UTC** (**2026-11-13 21:34:37 America/Mexico_City**); this installation must be renewed afterward.
- The user initially reported an opening error, then confirmed the app works after accepting the developer trust prompt on the iPad. This is user-confirmed launch acceptance, not independent visual acceptance.

Device Hub reports screen sharing requires iOS 27 or later, and QuickTime's UI inspection timed out. No iPad screenshots, portrait/landscape layout acceptance, GPS, account or push checks are claimed. The existing native material fallback applies on iPadOS 17; no product source changes or production deployment were needed.


## 2026-11-08: Main integration and production default

The integrated production apps now enter the connected design after welcome,
without the optional exploration button. Production inventory/direct navigation
are restricted to service-connected routes even if every preview group is granted.
Preview retains the 34-screen prototype and its separate identity/API/storage.

- Shared: 94 JVM cases passed, including production route and revocation checks.
- Android API 34: four isolated design and two connected-design UI cases passed.
  The latter verify entered partial offers, real need IDs, durable offline retry,
  history and group revocation. Live production and Preview entry/recreation cases
  also passed (two additional tests). An initial live failure was emulator DNS;
  cold boot with the Mac's DNS resolved it without changing application behavior.
- iOS 27: 19 unit/bridge/localization/map/push-signal cases and four design UI
  cases passed, including all 34 isolated routes. Normal production entry/relaunch
  passed against the published API with mock destinations absent. Preview entry/
  relaunch and missing-grant behavior also passed (three additional entry cases).
- Both Android APKs and both iOS simulator configurations built. The live maps
  were inspected with the published catalog: [Android](screenshots/integration-2026-11-08/android-production-map.png)
  and [iOS](screenshots/integration-2026-11-08/ios-production-map.png). iOS had no
  recent simulator GPS fix and displayed retry/address-search guidance.
- Backend and portal were published with only the five connected production
  groups enabled. See [integration evidence](../releases/integration-2026-11-08.md)
  for backups, preserved records, lifecycle checks and APK artifacts.

The iPhone 14 Pro, iPhone 17 and iPad were unavailable to `devicectl` during this
run. They were not updated. No new physical GPS/provider/push or assistive-technology
acceptance is claimed; store builds and remaining provider configuration stay open.
