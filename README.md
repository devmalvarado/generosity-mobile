# Generosity

Generosity is a cross-platform mobile app for iOS and Android that helps people discover nearby Kindness Centers and understand how they can help through time, goods, or money.

The shared contribution flow connects a center to its needs, offers of help, and personal impact. It tracks need priority and status, remaining goods quantities, and contributions. Only confirmed or fulfilled contributions count toward impact. Android observes this state through `GenerosityAppState`.

The current prototype uses sample imported Kindness Center data and supports
Spanish and English. Both apps now default to the first supported device
language and retain a system/Spanish/English override in Settings across
restarts, independently of account state. On 2026-10-25 the user expanded the planning scope to
include backend APIs, real authentication, durable storage/sync and
center-authorized contributions, plus a mobile-first center web portal based
on the supplied landing. A separate PHP/Laravel API now implements accounts,
public catalog, staff-scoped needs and transactional contributions with MySQL/MariaDB.
Native/portal HTTP integration, account sync and delivery remain pending.
See the [backend setup](backend/README.md) and [hosting decision](docs/backend-php-mysql.md).
See the [feature spec index](openspec/changes/define-generosity-mobile-app-foundation/spec-index.md).
Reference-aligned styling of existing native screens is being implemented from
[mobile-visual-alignment](openspec/changes/define-generosity-mobile-app-foundation/specs/mobile-visual-alignment/spec.md),
with native orange/neutral themes, branded discovery, center previews,
priority rows and labeled profile metrics. See [visual roles and assets](docs/native-visual-style.md)
and [verification evidence](docs/testing/native-visual-verification.md).
The concept's new navigation, favorites, achievements and streaks remain separate.
Those additional functions are now specified in the separate
[reference feature plan](openspec/changes/add-reference-product-features/spec-index.md),
including moderated reviews, events and externally hosted payments with release
gates. Neither plan is an implementation-completion or production-payment claim.
The subsequent [community campaign and coverage plan](openspec/changes/add-community-campaigns-and-coverage/spec-index.md)
adds earned organizer access, reforestation/cleanup, coordinated meals, voluntary
needs assessment and privacy-safe comparison of needs with center capacity.
It is also specified only, with eligibility and sensitive-data release gates.
The [center-mediated assistance plan](openspec/changes/add-center-mediated-assistance/spec-index.md)
adds suitable receiving-center discovery, a consented heads-up before a possible
visit/call, and support only after center validation. It separates goods receipt
from beneficiary handover and adds medicine-specific safeguards. This is also
planning only; the person seeking help does not need an app or account.
The [center directory field plan](openspec/changes/define-center-directory-details/spec-index.md)
defines the supplied profile fields and the approved minimums: identity,
categories and a designated public contact. Other fields can remain explicitly
unknown; fees, beneficiary counts, admission availability and verification have
separate meanings. Contact-only listings do not require public coordinates.

## Product Boundaries

- Product name: Generosity.
- Initial languages: Spanish and English.
- Initial data source: imported Kindness Center catalog data.
- Mobile platforms: iOS and Android.
- Shared logic: Kotlin Multiplatform.
- iOS UI: SwiftUI.
- Android UI: Jetpack Compose.
- Native maps: Google Maps on Android behind a replaceable renderer; Apple MapKit on iOS. See [configuration and MapLibre migration boundary](docs/native-maps.md).
- Backend: PHP 8.4 / Laravel 13 with MySQL/MariaDB; local API and tests implemented, Hostinger deployment pending.
- Web: public landing and mobile-first center need publishing/management are planned; see the [portal assessment](docs/center-portal.md). The source still lives in the older PoC.
- Follow-up scope: reference navigation/feed, favorites/sharing, public profiles, moderated reviews, gamification, inbox, events and gated external-provider donations.
- Community scope: registered users earn organizing access through validated prior help/level/points; household follow-up is optional and private, and territorial maps use approved aggregates. Organizer status never grants center staff or blanket case access.
- Center-mediated assistance: consented private referrals, center-assessed needs and center-directed support are planned. A heads-up does not enable purchases/donations; medicine requires separate qualified-service gates, and no unrestricted beneficiary cash transfer is added.
- Out of scope: official organization verification, public staff self-enrollment, handling card/bank credentials, tax receipts, general admin UI and automated third-party data harvesting. Provider-hosted payments require separate production approval.

## Repository Structure

- `shared/`: Kotlin Multiplatform shared domain, repository interfaces, imported sample catalog, use cases, and tests.
- `androidApp/`: Android Compose app foundation and Android localization resources.
- `iosApp/`: SwiftUI app foundation, localization resources, and the `SharedBridge` integration package.
- `backend/`: PHP HTTP API, relational migrations, OpenAPI contract and real MariaDB tests.
- `data/`: sample imported catalog data.
- `assets/brand/`: supplied-logo sources, SVG/PNG/ICO exports and the [downloadable brand kit](assets/brand/README.md).
- `docs/`: implementation notes and verification checklists.
- `openspec/`: OpenSpec proposals, specs, and task tracking.

## Backend

The PHP replacement lives on `codex/backend-php-mysql`. The prior complete state
is checkpoint `36eab89`, preserved on `codex/mockup-backend-kotlin`.
Follow [backend/README.md](backend/README.md) to run the API at localhost:8088
and its local email inbox at localhost:8028. No production deployment is implied.

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

Google Maps additionally needs `MAPS_API_KEY` in ignored `local.properties`
or the build environment and a Google Play Services device/Google APIs emulator.
Without those, the app keeps list discovery usable and shows a map-unavailable
state. The installed AOSP emulator does not support live Google tiles.
See [native map setup](docs/native-maps.md) before testing real cartography.

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

For iOS, first run `./gradlew :shared:prepareSharedBridge` after a fresh clone
or shared Kotlin changes. The app now links the local SharedBridge package;
its generated XCFramework is intentionally not committed.
Then open `iosApp/Generosity.xcodeproj`, select scheme `Generosity` and
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

Mobile translation resources, Unicode rules and the steps to add another
language are documented in [localization](docs/localization.md). Run
`sh scripts/check-localization.sh --self-test` to validate all enabled languages.

Verified on 2026-10-25:

- 25 shared tests each on JVM, Android host, and iOS ARM64 simulator.
- 2 Android UI tests on `Generosity_API_36` (Android 16 / API 36).
- 2 iOS UI tests on iPhone 16 Pro / iOS 18.6.
- Android debug APK and iOS simulator app builds.
- `Shared.xcframework` generation and `SharedBridge` Swift package build
  for ARM64 and Intel iOS simulators.
- English/Spanish localization-key coverage.

Android map slice verified on 2026-10-26: APK build, 4 marker unit tests,
7 Android UI tests, 25 shared tests each on JVM/Android host, SDK boundary
guard, localization coverage and strict foundation OpenSpec validation.
Lint: 0 errors, 47 warnings (dependency/SDK updates and unused resources).
Live Google tiles and iOS were not exercised in this slice; see
[native map acceptance](docs/native-maps.md).

Language settings verified on 2026-10-30: 33 shared tests each on JVM and
Android host, 34 on ARM64 iOS (including observer lifecycle), 4 Android unit
tests, 10 Android connected tests and 6 iOS UI tests pass. Coverage includes
regional/ordered device languages, unsupported-language fallback, persisted
manual override, return to device language and localized search retention.
Both apps build; Android lint passes with 0 errors and 47 existing warnings.
Localization coverage, map-provider boundaries and strict foundation OpenSpec
validation pass. Intel iOS simulator tests are skipped on this ARM64 Mac.

The subsequent iOS discovery integration on 2026-10-30 passes 36 shared ARM64
iOS tests, 11 Android connected tests and 9 iOS UI tests. New checks cover
direct shared-state/iOS-controller equivalence, closed/expired need exclusion,
priority, observer replacement, one offer after repeated navigation, marker
selection, category retention and empty results without stale detail content.
The duplicate Swift catalog/models were removed; MapKit remains native.

Native visual alignment verified on 2026-10-31: 12 Android connected tests
and 11 iOS UI tests pass; the additional Spanish Android tour at 200% text
and both iPhone SE tours at maximum accessibility text/dark appearance pass.
Android lint has 0 errors and 47 warnings. Eight visual tasks are complete;
live Google map acceptance and the full manual accessibility matrix remain
open. See [screenshots, test conditions and limitations](docs/testing/native-visual-verification.md).

## OpenSpec

OpenSpec 1.15.0 was verified as the latest stable release on 2026-10-25.
Project instructions are generated in `.agents/skills/`. See
[OpenSpec maintenance](docs/openspec-maintenance.md) for installation,
validation and archive rules.

```bash
openspec --version
openspec list --json
openspec validate --all --strict --no-interactive
```

## Current Limits

The foundation OpenSpec change was **reopened on 2026-10-25** after its
archived checklist overstated completion. See the
[implementation audit](openspec/changes/define-generosity-mobile-app-foundation/implementation-status.md),
[remaining tasks](openspec/changes/define-generosity-mobile-app-foundation/tasks.md),
and [user-provided design reference](docs/design-reference.md).
Main specs describe required behavior, not a claim that the prototype is complete.

Native roots now use the PHP API, real clocks, protected account-bound cache and
a durable operation queue. Bundled data is restricted to tests/debug fixtures.
Entered goods/time offers, personal help, server history, owner actions and the
authenticated center workspace are implemented locally. Guest visits and
notification preferences persist on the device; account visits synchronize.
The [integration report](docs/testing/backend-integration.md) distinguishes local
test evidence from live acceptance. The catalog, account, and visit endpoints,
and their two migrations, are on the published API (`1adf57a`). A production
catalog read returned no centers. The center workspace is not on the live
landing until those portal files reach `main`. Google/Apple credentials, Facebook,
phone verification, actual alerts/reminders, GPS/manual origin and complete
account lifecycle remain pending. Welcome routing exists; illustration/legal
destinations and the full accessibility matrix remain open. Payments are disabled.

SwiftUI discovery, map/list selection, current needs and language now consume
`SharedBridge` through one cancellable subscription owned by the root store.
The duplicate Swift sample catalog, search and need-expiry rules were removed.
Both apps use the same catalog/account contracts and shared lifecycle rules.
Profile displays server history with explicit personal-help provenance and
unavailable metrics; notification delivery remains pending on both platforms.
The Xcode app now requires the staged bridge. Apple command-line
tools alone cannot link the iOS framework or run its native tests; use
full Xcode. On Apple Silicon, the native ARM64 tests run; Gradle skips
the Intel simulator test task.

