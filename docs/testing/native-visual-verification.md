# Native visual verification — 2026-10-31

Scope: foundation section 17, styling the existing native destinations.
Compare with [reference](../generosity-design-reference.png) and
[implemented roles/assets](../native-visual-style.md). The welcome screen and
new product functions are separate work.

## Brand asset follow-up — 2026-10-31

The user's subsequent PNG logos are now traced into SVG paths, including the
lettering, and used by both native headers and launcher icons. Earlier captures
in this report predate that asset replacement. See the
[brand kit and export checks](../../assets/brand/README.md).

- Android debug app/test APK compiled. Five `GenerosityUiTest` navigation tests
  passed. A repeat exposed a keyboard-dismissal timing failure in the search/
  language test; it now waits for the settings tab to reappear in merged
  semantics before tapping it, and all five tests passed on the final APK.
  The final vector optimization was rebuilt and visually checked in dark mode.
  Final lint: 0 errors, 55 warnings; remaining asset warnings concern
  the optional standalone symbol, compatibility PNG icons and API-qualified
  adaptive/monochrome resources. This is not a clean lint baseline.
- iOS compiled with the SVG asset catalog and opaque RGB app icons.
  `testAnonymousDiscoverySupportsLanguageSwitchingAndCenterDetails` passed,
  including the header's new accessible image identifier.
- Source-to-vector silhouette overlap: symbol 99.07%, wordmark 98.97%.
  All eight SVG masters have path lettering and no embedded raster. ICO entries
  are 16/24/32/48/64/128/256px; the iOS marketing icon has no alpha channel.
- Visually checked both native headers in light and dark appearance, then
  restored both devices to their initial light appearance. These targeted
  checks do not close the remaining accessibility/full-matrix acceptance work.

Updated captures: [Android light](visual/2026-10-31/brand-android-light.png),
[Android dark](visual/2026-10-31/brand-android-dark.png),
[iOS light](visual/2026-10-31/brand-ios-light.png),
[iOS dark](visual/2026-10-31/brand-ios-dark.png).

## What changed

Both apps now use the supplied mark, a lowercase wordmark, accessible orange
actions, neutral surfaces, category symbols and bottom navigation labels.
Discovery has compact search/filters, a viewport-relative map and a selected
center preview linking to the existing detail. Details put identity and
description before current needs, priorities, supported actions and verification.
Profile has guest identity, unavailable points, labeled center/need counts and
history provenance. Settings uses the same native type/spacing/color roles.

There are no invented center photos, reviews, balances, duration totals or
achievements. Android keeps its existing demo offer/sign-in behavior; iOS
renders shared read-only needs/profile state. Catalog and date remain samples.

## Devices and fixtures

| Platform | Device / logical size | Appearance / text / locale |
| --- | --- | --- |
| Android 16, API 36 | Generosity_API_36 AOSP; 1080×2400 px at 420dpi, approximately 411×914dp | Light, font scale 1.0, English |
| Android 16, API 36 | Same emulator overridden to 720×1280 px at 360dpi, 320×569dp | Dark, font scale 2.0, Spanish |
| iOS 18.6 | iPhone 16 Pro Max; 440×956pt | Light, system content size `large`, English and Spanish |
| iOS 18.6 | iPhone SE (3rd generation); 375×667pt | Dark, `accessibility-extra-extra-extra-large`, English and Spanish |

Android's `VisualAlignmentTest` supplies an explicitly test-only long center
name, all four priorities, a rejected anonymous offer and self-reported local
history. iOS tours use the existing shared sample catalog, including urgent
needs and the guest/empty profile. Neither fixture certifies backend authority.
The map-unavailable Android capture is intentional: no key/Google Play Services
is configured. MapKit captures show the native map with sample center markers.

## Checks and corrections

- Native builds, localization-key coverage, Android lint and the map-provider
  boundary guard pass. Shared business logic was not changed in this slice.
- Existing Android/iOS navigation, filter/selection, shared remaining-quantity,
  language and guest-profile checks were repeated after styling.
- Android visual tour exercises all priorities, long text, rejected offers,
  local history, unavailable metrics, language selection, keyboard and empty
  search. At 200% text the navigation uses two rows and categories wrap.
- Small-screen keyboard inspection found an Android resize problem. The app
  now uses edge-to-edge safe insets, resizes for the IME and hides bottom
  navigation while typing. Search remains visible; submitting restores tabs.
- iOS tour captures map/list, selected preview, detail/needs, profile/history,
  Settings, keyboard and empty search in both languages. Map scrolling uses
  the outer gutter to avoid panning the map. Captures wait for destination
  navigation titles before auditing to avoid measuring a transition frame.
- iOS Settings headers now use the measured secondary color after the first
  audit found insufficient contrast in the platform-default header gray.
  Automated audits cover contrast, element detection and descriptions on the
  visible Settings content. Findings on partially offscreen section headers
  are logged for manual review, not treated as visible-control contrast errors.

The first unrestricted iOS 18.6 text-clipping audit returned “Text clipped”
without identifying an element. This remains an unresolved manual acceptance
item; the subsequent automated suite does not claim text-clipping certification.
See the final execution results below and retained captures in `visual/2026-10-31/`.

## Reference comparison

The retained screens match the concept's orange identity, neutral page/white
card hierarchy, category tiles, compact discovery header, selected-center card,
aligned priorities and prominent impact panel. Native fonts, current destination
labels and detail navigation remain platform-specific. The prototype has no
approved photo data or working rewards, so those areas use category symbols or
explicit unavailable states. The images intentionally show the actual app data.

Representative Android captures: [list](visual/2026-10-31/android-large-en-light/list.png),
[detail/needs](visual/2026-10-31/android-large-en-light/detail.png),
[profile](visual/2026-10-31/android-large-en-light/guest-profile.png),
[settings](visual/2026-10-31/android-large-en-light/settings.png),
[200% keyboard](visual/2026-10-31/android-small-es-dark-200/keyboard-search.png),
[200% rejection/navigation](visual/2026-10-31/android-small-es-dark-200/rejected-offer.png).
iOS accessibility-size captures: [Settings](visual/2026-10-31/ios-small-es-dark-ax5/es-settings.png),
[keyboard](visual/2026-10-31/ios-small-es-dark-ax5/es-keyboard.png),
[need](visual/2026-10-31/ios-small-es-dark-ax5/es-need.png).
Default-size iOS captures: [selected map](visual/2026-10-31/ios-large-es-light/es-selected-map.png),
[detail](visual/2026-10-31/ios-large-es-light/es-detail.png),
[profile](visual/2026-10-31/ios-large-es-light/es-profile.png),
[Settings](visual/2026-10-31/ios-large-es-light/es-settings.png).

## Execution results

- Android: 12/12 connected tests pass (11 existing plus the visual tour).
  The dark/200% Spanish tour also passes. Six map/visual tests pass again after
  making the unavailable-map message scrollable. Lint: 0 errors, 47 warnings.
- iPhone SE: both English/Spanish visual tours pass with maximum accessibility
  text and dark appearance, including visible Settings contrast/description
  checks. Result: `Test-Generosity-2026.10.31_00-24-04--0600.xcresult`.
- iPhone 16 Pro Max: 11/11 UI tests pass (nine existing plus both visual tours).
  Result: `Test-Generosity-2026.10.31_00-26-58--0600.xcresult`.
- Both native builds, localization coverage, map-provider isolation and strict
  foundation OpenSpec validation pass. The foundation remains active at 45/127
  tasks complete: eight visual implementation tasks closed; 17.4 and 17.10 open.

## Remaining acceptance work

Task 17.10 stays open. Complete manual VoiceOver/TalkBack order and interaction,
resolve the unattributed iOS clipping report, complete the full locale/text-size/
appearance cross-product, and exercise more iOS long-content/all-priority/
populated-history fixtures. Screenshots and semantic assertions are not a
screen-reader audit. Google attribution/gestures on live Android tiles require
a restricted key and Google-enabled device, as tracked in task 17.4 and
[native maps](../native-maps.md).

The existing visit action remains reachable; there is still no durable visit
delivery or complete contact/form workflow. No visual checkbox certifies those
separate foundation features. The foundation remains active, not archived.

## Reproduce

Build the apps and test APK using the README toolchain. With an Android emulator:

```bash
./gradlew :androidApp:assembleDebug :androidApp:assembleDebugAndroidTest :androidApp:lintDebug :androidApp:verifyMapProviderBoundary
adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk
adb install -r androidApp/build/outputs/apk/androidTest/debug/androidApp-debug-androidTest.apk
adb shell am instrument -w -e class org.generosity.app.VisualAlignmentTest \
  -e visualLocale es -e visualLabel small-es-dark-200 \
  org.generosity.app.test/androidx.test.runner.AndroidJUnitRunner
adb exec-out run-as org.generosity.app tar -cf - files/visual > /tmp/generosity-visuals.tar
```

Set and record `wm size`, `wm density`, system `font_scale` and `cmd uimode night`
before the test; restore their previous values afterward. Captures are generated
under the test app's private `files/visual/` directory. Export before uninstall;
Gradle's connected-test runner removes its installation after completion.

```bash
xcodebuild -project iosApp/Generosity.xcodeproj -scheme Generosity \
  -destination 'platform=iOS Simulator,name=iPhone 16 Pro Max,OS=18.6' \
  -derivedDataPath iosApp/build/DerivedData -parallel-testing-enabled NO \
  -collect-test-diagnostics never CODE_SIGNING_ALLOWED=NO test \
  -only-testing:GenerosityUITests/GenerosityVisualTests
```

iOS keeps screenshots as XCTest attachments in the resulting `.xcresult`.
Use `simctl ui <device> appearance` and `content_size` to record/change test
conditions, then restore them. Do not interact with a device while tests run.
