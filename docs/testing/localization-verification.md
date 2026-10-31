# Localization verification — 2026-10-31

Scope: mobile Spanish/English copy, Unicode, native resources and future language
registration. See [implementation and adding a language](../localization.md).

## Results

| Check | Result |
| --- | --- |
| Shared JVM tests | 37 passed |
| Shared Android host tests | 37 passed |
| Shared ARM64 iOS simulator tests | 40 passed |
| Android native/UI checks | 12 passed: navigation, language persistence, map/list behavior, Unicode resources and native plurals |
| Android Spanish visual tour | 1 passed; exported settings/detail captures visually reviewed |
| iOS native localization tests | 2 passed: plurals, regional locale resolution and accented resource text |
| iOS language/navigation UI tests | 9 passed |
| iOS Spanish visual tour | 1 passed, including the existing scoped accessibility audit; settings/detail captures visually reviewed |
| Android APK, test APK and debug App Bundle | Built successfully; language splits disabled |
| iOS simulator app and shared XCFramework | Built successfully |
| Resource validation | 69 strings, 2 plural keys and permission resources for both languages; key/placeholder parity, UTF-8/NFC and validator negative self-checks passed |
| Android lint | 0 errors, 19 warnings; no missing-plural or dynamic-language-bundle warning remains |
| Active foundation OpenSpec | Strict validation passed |
| Git whitespace check | Passed |

Shared tests cover precomposed and decomposed ñ, searches with/without accents,
preserved displayed content, ordered device preferences and manual override.
Native resource tests verify zero/one/many quantities and singular/plural goods
offers in the selected language. UI tests exercise the updated literal copy,
language changes without losing search, and persistence across restarts.

Devices: Android API 36 emulator (`Generosity_API_36`); iPhone 16 Pro Max,
iOS 18.6 simulator. The Android visual tour uses a long-content fixture and
the existing unavailable-map fallback. The iOS tour uses sample centers.

## Visual evidence

- [Android settings](visual/2026-10-31/localization/android-es-settings.png)
- [Android center detail](visual/2026-10-31/localization/android-es-detail.png)
- [iOS settings](visual/2026-10-31/localization/ios-es-settings.png)
- [iOS center detail](visual/2026-10-31/localization/ios-es-detail.png)

These show Español, ubicación, niños, Educación, Histórico, México, Útiles,
lápices and the complete phrase “Faltan 15 unidades” rendering correctly.
Prior brand/visual screenshots are historical; their unaccented text does not
represent the current app.

## Reproduce

```bash
sh scripts/check-localization.sh --self-test
./gradlew :shared:jvmTest :shared:testAndroidHostTest :shared:iosSimulatorArm64Test \
  :shared:prepareSharedBridge :androidApp:assembleDebug :androidApp:assembleDebugAndroidTest \
  :androidApp:bundleDebug :androidApp:lintDebug
xcodegen generate --spec iosApp/project.yml
```

Use the README SDK/JDK configuration. Install the Android app/test APKs and run
`GenerosityUiTest`, `AndroidLanguageSettingsTest`, `LocalizationResourcesTest`
and `MapDiscoveryTest` through instrumentation. `VisualAlignmentTest` accepts
`visualLocale=es` and `visualLabel=localization-es` for screenshot export.

```bash
xcodebuild -project iosApp/Generosity.xcodeproj -scheme Generosity \
  -destination 'platform=iOS Simulator,name=iPhone 16 Pro Max,OS=18.6' \
  -derivedDataPath iosApp/build/DerivedData -parallel-testing-enabled NO \
  -collect-test-diagnostics never CODE_SIGNING_ALLOWED=NO test \
  -only-testing:GenerosityLocalizationTests \
  -only-testing:GenerosityUITests/GenerosityUiChecklistTests \
  -only-testing:GenerosityUITests/GenerosityVisualTests/testSpanishVisualTour
```

Additional languages are not enabled or translated by this change. Each needs
reviewed resources, plural rules, content and a native layout/keyboard/accessibility
review. No RTL-language release audit, full device/text-size matrix or manual
VoiceOver/TalkBack audit was performed here. The separately developed portal was
not changed. Existing functional gaps remain tracked in the active foundation.
