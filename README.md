# Generosity

Generosity is a cross-platform mobile app for iOS and Android that helps people discover nearby Kindness Centers and understand how they can help through time, goods, or money.

The shared contribution flow connects a center to its needs, offers of help, and personal impact. It tracks need priority and status, remaining goods quantities, and contributions. Only confirmed or fulfilled contributions count toward impact. Android observes this state through `GenerosityAppState`.

The initial mobile scope uses imported Kindness Center data and supports Spanish and English. Backend services, Kindness Center backoffice workflows, official center verification, direct bank/payment processing, admin tooling, and data import automation are outside this repository's first mobile foundation.

## Product Boundaries

- Product name: Generosity.
- Initial languages: Spanish and English.
- Initial data source: imported Kindness Center catalog data.
- Mobile platforms: iOS and Android.
- Shared logic: Kotlin Multiplatform.
- iOS UI: SwiftUI.
- Android UI: Jetpack Compose.
- Out of scope: backend repository, center portal/backoffice, official organization verification, direct bank/payment processing, tax receipts, moderation tooling, and admin tooling.

## Repository Structure

- `shared/`: Kotlin Multiplatform shared domain, repository interfaces, imported sample catalog, use cases, and tests.
- `androidApp/`: Android Compose app foundation and Android localization resources.
- `iosApp/`: SwiftUI app foundation, localization resources, and the `SharedBridge` integration package.
- `data/`: sample imported catalog data.
- `docs/`: implementation notes and verification checklists.
- `openspec/`: OpenSpec proposals, specs, and task tracking.

## Build and Test

This is the consolidated working copy for `devmalvarado/generosity-mobile`.
Use the included Gradle wrapper from this directory. Requires JDK 17 or newer
and Android SDK platform 36. Set `sdk.dir` in the ignored `local.properties`
file or set `ANDROID_HOME` to your SDK installation.

```bash
# Shared tests on JVM and Android host; no emulator or Xcode required.
./gradlew :shared:jvmTest :shared:testAndroidHostTest

# Android APK and compilation of the Android UI tests.
./gradlew :androidApp:assembleDebug :androidApp:compileDebugAndroidTestKotlin

# Kotlin compilation for the iOS simulator, without linking an Apple binary.
./gradlew :shared:compileKotlinIosSimulatorArm64

# Includes native simulator tests; requires full Xcode and simulator runtimes.
./gradlew :shared:allTests

# Builds and stages the XCFramework consumed by iosApp/SharedBridge.
# Requires full Xcode, with its developer directory selected.
./gradlew :shared:prepareSharedBridge
```

The debug APK is at `androidApp/build/outputs/apk/debug/androidApp-debug.apk`.
Compilation of UI tests does not execute them; running them requires an
Android device or emulator.

### Run Locally on This Mac

Xcode 27.1, XcodeGen, Android Studio, Android SDK 36, and the
`Generosity_API_36` ARM64 emulator are installed. The iOS app project and
shared test scheme are included; XcodeGen is only needed after changing
`iosApp/project.yml`.

For terminal builds, use the bundled JDK and the full Xcode toolchain:

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export ANDROID_HOME="$HOME/Library/Android/sdk"
export DEVELOPER_DIR="/Applications/Xcode.app/Contents/Developer"
```

For Android, open this repository in Android Studio, select `androidApp`
and `Generosity_API_36`, then Run. Alternatively, start the emulator in one
terminal and install the app from another:

```bash
"$ANDROID_HOME/emulator/emulator" -avd Generosity_API_36
```

```bash
./gradlew :androidApp:installDebug
"$ANDROID_HOME/platform-tools/adb" shell am start -n org.generosity.app/.MainActivity
./gradlew :androidApp:connectedDebugAndroidTest
```

The connected test task removes its test installation when finished;
run `:androidApp:installDebug` again for manual use afterward.

For iOS, open `iosApp/Generosity.xcodeproj`, select scheme `Generosity` and
an iPhone simulator, then Run. No Apple Developer account is required for
the simulator. Xcode 27 shows the simulator in Device Hub.

```bash
# Regenerate only after editing the project specification.
xcodegen generate --spec iosApp/project.yml

# This Mac has the iPhone 16 Pro / iOS 18.6 simulator installed.
xcodebuild -project iosApp/Generosity.xcodeproj -scheme Generosity \
  -destination 'platform=iOS Simulator,name=iPhone 16 Pro,OS=18.6' \
  -derivedDataPath iosApp/build/DerivedData \
  -parallel-testing-enabled NO -collect-test-diagnostics never \
  CODE_SIGNING_ALLOWED=NO test
```

Avoid interacting with the simulator during UI tests. The command above
disables full system diagnostic collection, which timed out in this local
Xcode setup; normal test results and failure attachments remain available.

### Local Verification

Verified on 2026-10-25:

- 25 shared tests each on JVM, Android host, and iOS ARM64 simulator.
- 2 Android UI tests on `Generosity_API_36` (Android 16 / API 36).
- 2 iOS UI tests on iPhone 16 Pro / iOS 18.6.
- Android debug APK and iOS simulator app builds.
- `Shared.xcframework` generation and `SharedBridge` Swift package build
  for ARM64 and Intel iOS simulators.
- English/Spanish localization-key coverage.

## Current Limits

The app uses in-memory repositories, sample users, and a fixed demo clock.
Sign-in is a local demonstration, not backend authentication; data is lost
when the app process restarts. Payments and confirmation of contributions
remain backend integration points.

The SwiftUI screens still render their own sample data. `SharedBridge` is
prepared for integration, but it is not yet wired into those screens.
The Xcode app can run independently of the bridge. Apple command-line
tools alone cannot link the iOS framework or run its native tests; use
full Xcode. On Apple Silicon, the native ARM64 tests run; Gradle skips
the Intel simulator test task.

