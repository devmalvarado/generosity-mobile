# Native Maps

## Provider Decision

Decision recorded on 2026-10-26: start with **Google Maps on Android** and
keep **Apple MapKit on iOS**. Android must remain replaceable with MapLibre
without changing catalog, search/filter, contribution or other business rules.
MapLibre is not installed and no runtime provider switch is introduced.

The Android placeholder has been replaced by a Google Maps Compose adapter.
The development key is configured locally and restricted to the Android package,
debug signing certificate and Maps SDK for Android. `Generosity_Google_API_34`
provides the working Google APIs ARM64 test image: live tiles, pan/zoom and
card/pin/detail interactions were observed on 2026-11-01. Remaining acceptance
is tracked separately below.
The older AOSP `Generosity_API_36` remains useful for testing unavailable Google
services. The no-map state still allows anonymous list/detail discovery.

## Boundaries

```text
shared: catalog -> discovery rules -> GenerosityUiState
                                      |
                         Android DiscoveryScreen
                                      |
                           CenterMapRenderer
                                      |
                      GoogleCenterMapRenderer

iOS: SwiftUI -> existing MapKit view
```

- `shared` owns `Coordinates`, catalog IDs, filtering, selected center and
  business rules. No map SDK dependency was added there.
- [CenterMapRenderer](../androidApp/src/main/java/org/generosity/app/ui/map/CenterMapRenderer.kt)
  accepts only public marker IDs, localized titles, shared coordinates,
  selected ID, optional discovery-origin coordinates/label, selection callback, locale and a Compose modifier.
- `toMapMarkers` projects the already-filtered results without querying a
  provider, geocoding addresses or substituting provider place IDs. Invalid
  coordinates are omitted from markers, never clamped to invented locations;
  the center remains accessible in the list.
- [GoogleCenterMapRenderer](../androidApp/src/main/java/org/generosity/app/ui/map/google/GoogleCenterMapRenderer.kt)
  alone owns Google coordinates, markers, camera, availability and SDK lifecycle.
  Initial framing and changes to result coordinates fit the catalog, or focus
  the selected result when it remains visible. Selection focuses that center
  and raises its marker above others. Manual pans are retained between result
  or selection changes; translated titles alone do not reset the camera.
  Camera state belongs to presentation, not the user's discovery origin.
- [MainActivity](../androidApp/src/main/java/org/generosity/app/MainActivity.kt)
  selects the concrete renderer. The screen and tests can accept another
  implementation without modifying `GenerosityAppState` or its repositories.
- The adapter is a bounded Android package, not a new KMP module or a generic
  mapping framework. Extracting a Gradle library is unnecessary for one consumer.
- `:androidApp:verifyMapProviderBoundary`, also part of `check`, rejects Google
  Maps and MapLibre SDK references in shared Kotlin and in Android code outside
  `ui/map/google`. The deliberate provider package allowlist must be updated
  when introducing a real alternative adapter, not expanded to business code.

iOS now renders the same shared catalog and selected ID through SharedBridge.
The native `MKMapView` adapter frames the actual catalog coordinates instead of the former fixed
Mexico City demonstration region. It focuses a selected center and fits new
filtered results when the selection is absent. MapKit types remain in native
presentation; camera movement does not claim to locate the user. Its coordinator
updates annotations and camera from shared selection without a two-way SwiftUI
region binding, avoiding stale camera updates overwriting a new selection.
Initial framing waits for a nonzero map layout and fits projected bounds with
48-point edge padding, recalculating after map-size changes. A newer selection
replaces any pending initial frame. MapKit's native zoom limits can prevent an
entire intercontinental catalog from fitting in a small view; users can pan,
filter results or select any card to focus its marker directly.

The map, name search and category chips stay above an independently scrolling
result list. Map height adapts to the available screen (40%, bounded to 100–300
points/dp). Chips remain horizontally scrollable with enlarged text. Selecting
a card focuses its pin without moving the map; selecting a pin reveals its card.
Every card has a View details action that opens a separate full-screen detail
with its own scrolling and Back action. List-card taps also open that detail.
Closing it retains list position, active filters and map selection. Cards expose
selected state visually and to accessibility services.

## Search a neighborhood, address or city

Search area is separate from Search centers. An explicit submission queries
`MKLocalSearch` (address results) on iOS and Android's `Geocoder`; no new Maps
Places key or server endpoint is used. Native results are suggestions: the user
chooses one of up to five full-address labels, preferably including city/country.
The query is limited to 200 characters and lookup waits at most 15 seconds.
Edits, dismissal and newer requests invalidate old responses. Empty/error states
allow another search and preserve the previous catalog/discovery origin.

The selected `DiscoveryArea` contains only a label and validated coordinates.
Shared discovery filters the existing catalog to **10 km straight-line distance**,
sorts nearest first, and intersects name and category filters. Card distances
are explicitly from the selected place, not from the device. Clearing the area
restores the catalog under the other filters. A successful lookup with no nearby
catalog centers shows an empty list and frames the searched area; it does not
invent centers or claim the area has no services. Map gestures do not change the
search origin. Selection takes camera precedence over the origin.

Search text goes to the platform's lookup provider; no GPS permission is requested.
The selected area is session-only, never persisted or uploaded to Generosity's
API. Android checks `Geocoder.isPresent()`, uses the asynchronous API on 33+ and
an IO dispatcher on older supported versions. A present service can still fail;
the AOSP emulator is not evidence of live geocoding availability on every device.
Native place lookup remains independent of the replaceable map renderer.

## iOS current location — 2026-11-01

Map and list expose **Use my location**. A root-owned `PermissionService` requests
When In Use authorization and one Core Location fix, then feeds its coordinates
to the existing shared `DiscoveryArea` filter. Native/shared business rules are
unchanged: 10 km straight-line radius, nearest first, intersected with the current
name/category filters. A blue origin marker and device-distance labels distinguish
this origin from a searched address. Clearing the area restores catalog browsing.

If permission is already granted, discovery acquires a fix on entry. Foreground
return refreshes an expired fix; the button requests a new fix at any time.
An automatic GPS refresh preserves an open center detail, including while the
expired origin is cleared or the new radius excludes that center. Detail data
is refreshed from the catalog; a removed center is not retained. Explicitly
choosing or clearing a discovery area still clears selection.
Acquisition stops after success, failure, 15 seconds, manual-area selection or
backgrounding. There is no continuous/background tracking. Samples must have
valid coordinates, accuracy 0–5,000 m and age -5–120 seconds. Reduced accuracy
or uncertainty over 1,000 m is labeled approximate. Denial/revocation clears a
device origin and offers Settings/manual search. Failures do not replace an
existing manual origin; late callbacks cannot overwrite a newer manual choice.

Coordinates are session-only and never enter backend requests, cache/outbox,
preferences or visit signals. The map marker represents the accepted sample;
it is not MapKit's continuously updating user-location layer. Android's equivalent
is documented below, independently of its Google map key. Cross-platform string catalogs
retain identical ES/EN keys for this increment.

Validation: seven `CurrentLocationTests`, three `MapFramingTests` and three
simulator UI checks passed. UI coverage exercises the system permission dialog
(grant and denial), simulated coordinates, nearby discovery, Spanish/English
labels, clearing the origin and card/pin/detail selection. Simulator coordinates
are test fixtures. Subsequent physical checks on iPhone 14 Pro (dark/Spanish)
and iPhone 17 (light/English, viewed through QuickTime) confirmed current-location
discovery, nearby distances and matching map/list results after device permission
was enabled. On the 14 Pro, live Coyoacán lookup selected a manual origin and
Use my location restored nearby GPS results without opening Settings.
No real GPS coordinates are recorded in this document. If permission
was previously denied, tapping Use my location now explains the restriction in an
alert with Open Settings; returning after authorization retries acquisition.

## Android current location — 2026-11-02

`MainActivity` supplies `AndroidDeviceLocationSource` to the production Compose
root. `DiscoveryLocation` owns foreground acquisition/expiration and manual-vs-device
intent across map/list tabs; the existing shared `DiscoveryArea` applies 10 km
straight-line filtering, nearest-first ordering and current query/categories.
Google/MapLibre types never enter location acquisition or shared business logic.

Discovery automatically gets one fix when permission is already granted. Use my
location requests Android coarse/fine foreground permissions together when needed;
approximate access alone is sufficient. The native source uses the existing
AndroidX `LocationManagerCompat.getCurrentLocation` with cancellation for enabled
network/GPS providers and the platform fused provider on Android 12+. A recent,
permission-filtered OS last-known fix is reused first: approximate requests can
be throttled by Android even in the foreground. Cached and new fixes use the same
age/accuracy validation; an old sample never becomes the user's current origin.
This does not add a Play Services location dependency.
The first valid fix wins and cancels the other providers. Acquisition is bounded
to 15 seconds, stops on pause/disposal, and refreshes expired fixes on resume.
There is no background permission, service, continuous tracking or notification request.

Accepted fixes have finite/in-range coordinates, supplied accuracy from 0–5 km
and elapsed-realtime age from -5 to 120 seconds. Coarse permission or uncertainty
over 1 km displays an approximate label. A permission downgrade discards the old
precise fix before obtaining a newly permitted sample. A blue origin marker and
localized distance/scope labels identify the accepted point. The marker is static;
Google's independent My Location layer stays disabled.

Denied permission, disabled device location and failed/expired acquisition have
visible feedback. Open Settings targets app permissions or device location as
appropriate. On return the controller retries the device-origin intent; revocation
removes a device origin. Manual selection/clear cancels and ignores pending fixes,
and is not overridden on resume. Errors preserve an existing manual area. Device
origin changes preserve open center details through the existing shared API.
Coordinates stay in session memory and do not enter Generosity API calls, cache,
outbox, preferences, visit signals or notification delivery.

Verification: eight `DiscoveryLocationTest` unit cases cover age/accuracy/invalid
coordinates, approximate access, no unsolicited prompt, denial/revocation,
Settings-return retry, disabled services, timeout, cancellation/late callbacks,
manual precedence, background refresh and precision downgrade. Two
`CurrentLocationTest` cases execute Compose against controlled sources on Android
14: map/list parity and clear/retry, correct Settings intents, service disable,
revocation and return. Eight existing `MapDiscoveryTest` regressions also passed.
These controlled-source cases are not evidence of physical GPS accuracy.

Manual native-provider verification on the Google APIs Android 14 emulator:
the system permission prompt accepted Approximate / While using the app;
an emulator GPS fix at 19.35, -99.16 produced the blue origin, approximate label,
10 km scope and nearby Coyoacán catalog cards over live Google tiles. The final
APK also acquired that simulated fix automatically with permission already
granted; an immediate force-stop/relaunch, without another sensor injection or
button press, restored discovery from the fresh OS cache. A stale, throttled
coarse provider correctly reached the 15-second unavailable state instead of
claiming an old position was current. Physical GPS remains unverified.

Platform contracts: [Android foreground permissions](https://developer.android.com/develop/sensors-and-location/location/permissions/runtime),
[native providers and cached locations](https://developer.android.com/reference/android/location/LocationManager)
and [single-fix compatibility API](https://developer.android.com/reference/androidx/core/location/LocationManagerCompat).

## External map actions — 2026-11-01

Both apps expose map actions directly below the selected center's address.
iOS uses `MKMapItem.openInMaps()` for Apple Maps and a Google universal HTTPS
search URL. Android uses that same Google URL or an implicit `geo:0,0?q=…`
intent; an unavailable geo handler falls back to the HTTPS URL. These adapters
use native APIs in the presentation layer and do not add provider types to shared
business logic, a Google API key, a package visibility query, or a dependency.

Only finite coordinates within latitude/longitude bounds are accepted. Google
queries contain only catalog coordinates; Apple/Android native actions also use
the localized center name. URI construction encodes query values. No device
position, user identity, credential, visit, or contribution accompanies the action.
Denied GPS and missing embedded Google Maps configuration do not block opening
a center externally. The destination app controls its own permissions and any
subsequent route request. Missing handlers produce a localized error in the detail.

Contracts: [Google Maps universal URLs](https://developers.google.com/maps/documentation/urls/get-started),
[Android map intents](https://developer.android.com/guide/components/intents-common#Maps),
[Apple map items](https://developer.apple.com/documentation/mapkit/mkmapitem/openinmaps(launchoptions:)).

Validation: two `ExternalMapTests` verify every fixture's exact coordinates,
localized Apple name and invalid-coordinate rejection. Two iOS UI tests verify
both buttons for first/non-first centers in ES/EN and the denied-GPS alert.
Android's three `MapActionsTest` cases cover finite/range checks, accented name
encoding, missing geo fallback and total handler failure. One `MapDiscoveryTest`
captures actual detail-button intents for two centers in both languages. All eight
passed on the iOS 18.6 simulator and Android API 36 emulator; Android intents are
intercepted, so this does not certify a particular installed map app's rendering.

The signed update was installed on both iPhones. On the physical iPhone 14 Pro,
Google Maps opened the selected center's exact coordinate marker and Back returned
to the same detail. Apple Maps launched but displayed its first-run introduction;
that onboarding was left for the owner, so the Apple destination's final rendering
was not visually accepted. The iPhone 17's map, list and second-center detail
were subsequently observed through QuickTime; the owner also reported Apple's
first-run prompt. See [physical QA](testing/native-visual-verification.md#physical-ios-follow-up--2026-11-01)
for the GPS/detail regression found during that journey and its verification.

## Google Configuration

Maps Compose is pinned to **8.3.0**, compatible with the repository's SDK 36
toolchain. Version 9.0.1 was evaluated but its AAR metadata requires SDK 37 and
newer Compose dependencies. No SDK/target upgrade or dependency version forcing
was introduced. Upgrading this pin should include native map regression tests.
See the [upstream 8.3.0 version catalog](https://github.com/googlemaps/android-maps-compose/blob/v8.3.0/gradle/libs.versions.toml).

1. Use a Google Cloud project configured for Maps SDK for Android. Review the
   required billing setup and usage controls before enabling real usage; this
   repository does not create a Cloud project or enable billing.
2. Restrict its Android key to package `org.generosity.app` and the SHA-1 of
   the actual signing certificate. Use separate development/release keys;
   release restrictions must match the certificate used for installed builds,
   including Play App Signing when applicable.
3. Restrict API access to Maps SDK for Android. No Places or web-service API
   key is needed for the current catalog-marker renderer.
4. Put `MAPS_API_KEY=<your-restricted-key>` in the ignored root
   `local.properties`, alongside `sdk.dir`, or supply `MAPS_API_KEY` in the build
   environment. The environment takes precedence; an empty value disables the
   map. Rebuild/reinstall after changing it. Do not commit the value.
5. Run on an Android device with Google Play Services, or create a supported
   emulator with a **Google APIs** or **Google Play** image. The existing AOSP
   `Generosity_API_36` is useful for fallback tests, not live Google tiles.

```bash
./gradlew :androidApp:signingReport
./gradlew :androidApp:installDebug
```

The key is injected into `com.google.android.geo.API_KEY` in the merged
manifest. Android client keys are extractable from an APK; keeping the value
out of Git does not replace application/API restrictions. Do not include
merged manifests or private build logs in published artifacts.
Configuration references: [Android setup](https://developers.google.com/maps/documentation/android-sdk/config)
and [API key protection](https://developers.google.com/maps/api-security-best-practices).

## Android development configuration verified — 2026-11-01

Google Cloud project `generosity-v01` contains the development key
`generosity-android-desarrollo`. Its saved restrictions allow only Maps SDK for
Android and package `org.generosity.app` with the local debug certificate SHA-1
`71:E3:02:05:BB:33:A7:9C:39:D6:5D:01:E2:D2:97:C0:7E:65:F2:1B`.
The value is present only in ignored `local.properties`; a local assertion
confirmed that the merged debug manifest uses it without printing the value.

`:androidApp:assembleDebug` and `:androidApp:verifyMapProviderBoundary` passed.
`apksigner` verified the APK and its certificate matched the Cloud restriction.
The APK was installed in `Generosity_Google_API_36`, Android 36 Google APIs ARM64
revision 7, after the owner explicitly accepted its SDK license. This is a local
debug build; production signing and Play App Signing require their own restrictions.

Initial software-rendered boots showed a System UI ANR. The new AVD was changed
to host GPU, 720×1600, 280 dpi and two cores, preserving its app data and backing
up its original configuration. A cold boot completed; SurfaceFlinger reported
the Apple M2 Pro renderer and the launcher was visually observed without that
dialog. However, the owner still reported unresponsive input; subsequent logs
showed ANRs in Android System Intelligence and the Android keyboard before
Generosity launched. This boot therefore does not establish usable interaction.

The alternative `Generosity_Google_API_34` (Android 14, Google APIs ARM64 revision
14, host GPU, 720×1600, 280 dpi, two cores) responds to input. Its older Play
Services (23.18.18) initially closed the app with `NoClassDefFoundError` for
`org.apache.http.ProtocolVersion` when loading the legacy Maps renderer.
The manifest now declares optional `org.apache.http.legacy`, the workaround
documented for Maps SDK 20.0.0 in [Google's release notes](https://developers.google.com/maps/documentation/android-sdk/release-notes).
The merged manifest was checked, assembly and provider-boundary validation passed
again, and the updated APK was installed without clearing app data.

Manual acceptance on this image passed: guest entry and live catalog retrieval;
Google tiles and attribution; native zoom and pan; card selection focusing its
orange pin; another pin revealing the matching non-first card; that card opening
CASA CUNA “LA PAZ” details; and Back preserving the selected card/pin and list
position. Selecting CASA HOGAR JARDIN DE VIDA then focused Coapa correctly.
The updated process remained running and its inspected log contained no fatal
exception or Maps authorization failure. These are live provider checks, not
only test-renderer or compilation evidence.

Tasks 5.2/17.4 remain open for the rest of their acceptance matrix, including
live provider/network failure and recovery, filtered map/list parity and safe
insets across device configurations. Android current-location task 5.6 and
physical Android/release-signing validation remain separate. Android 16's ANRs
were not resolved; use the Android 14 Google APIs image for this local preview.

## Runtime Behavior And Limits

- Missing configuration or unavailable Google Play Services shows a localized
  unavailable state, while the same filtered list and details stay usable.
  Services can be checked again with Retry; a missing build key requires rebuild.
- Loading feedback appears outside the map so Google attribution and controls
  are not covered. After 15 seconds without `onMapLoaded`, the app reports
  delayed loading and offers Retry without blocking the list. A late successful
  callback clears the feedback. Retry recreates the SDK view, not shared state.
- This timeout is not a diagnosis of bad credentials or a network outage.
  Google does not expose a general tile/auth-error callback here. Its
  [loaded callback](https://developers.google.com/android/reference/com/google/android/gms/maps/GoogleMap.OnMapLoadedCallback)
  does not certify all subsequent pans or offline areas; those require manual
  validation. The list remains available even after an initially loaded map.
- Map gestures and marker clicks are native Google behavior. A marker sends its
  catalog ID to shared selection and reveals the selected card; the details
  action uses that same record. No provider place/business lookup is used.
- Android `isMyLocationEnabled` is false. Opening a map does not itself request location permission,
  fabricate a GPS fix or upload a user location trail. Current location is supplied
  by the independent foreground location controller described above; manual place
  search uses an explicitly selected origin. A map camera is never
  presented as the user's location or used to create visit signals.
- No custom Google tile cache, offline tile download or transfer of Google
  imagery/content to another renderer is implemented.

## Future MapLibre Migration

1. Choose and review a licensed tile/style source, attribution, hosting,
   credentials, budget and offline policy independently of the renderer.
   Installing MapLibre alone does not supply production cartography.
2. Implement `CenterMapRenderer` in an Android MapLibre adapter. Convert the
   neutral marker coordinates there; preserve IDs, localized titles, selection,
   list fallback, lifecycle handling, saveable camera and attribution.
3. Select it in `MainActivity`; replace the Android mapping dependency and
   Google-only manifest/configuration. Remove unused Google dependencies and
   update the boundary allowlist. Do not change shared state or repository APIs.
4. Run the same contract/UI checks and real-map acceptance matrix. Verify
   coordinate conversion, camera framing, pan/zoom, touch/accessibility and
   network/error behavior on the new provider. A passing fake renderer test is
   not proof of working MapLibre tiles.

Location acquisition, place search, routing and tile services are independent
decisions. Adding them must not smuggle Google-specific objects, provider IDs
or responses into domain models and persisted catalog data.

## Verification

```bash
./gradlew :androidApp:assembleDebug :androidApp:testDebugUnitTest \
  :androidApp:verifyMapProviderBoundary :androidApp:lintDebug
./gradlew :shared:jvmTest :shared:testAndroidHostTest
./gradlew :androidApp:connectedDebugAndroidTest
```

Automated coverage includes marker identity/coordinates, invalid/empty data,
localized fallback, renderer substitution, shared filters/selection, map/list
state retention, delayed-load feedback and usable details without a map.
The SDK-free probe deliberately does not pretend to display geographic tiles.

2026-11-01 fixed map and manual-origin update: 73 shared tests pass on JVM and
Android host, and 76 on iOS Simulator arm64. The iOS observation/offer fixture
now explicitly uses local demo sign-in; production bridge sign-in still blocks
real accounts from offering against demo data. Android's nine map/navigation,
contribution and visual tests pass; the updated place-search test also passes
with success, failure, empty results and explicit clearing. Unit tests, provider
boundary, localization and lint pass. The iOS checklist and both language visual
tours pass after adapting full-screen detail navigation; the every-card test
uses XCTest's native scroll-to-button behavior and verifies unchanged map bounds.
Three native MapFramingTests cover layout/selection and empty searched-area bounds.

Manual iOS check against the published catalog: search Coyoacán, CDMX, select
the returned address, see a nearest result at 0.7 km and filter older-adult centers
without losing the origin. Scrolling the card list retains the map above it.
The native geocoder's actual outage/timeout acceptance remains open on iOS.
Android lookup tests use an injected result/error function; no live Android
geocoder or Google tiles are claimed on the AOSP emulator. No physical-device
or GPS verification is claimed for this change.

2026-11-01 navigation correction: Android `MapDiscoveryTest` covers all result
cards, direct detail actions, selected cards, returning from list to map,
filtered markers and unavailable-provider fallback (six passing UI tests).
Five Android unit tests and the map-provider boundary check pass. iOS
`MapFramingTests` verifies distant coordinates, initial layout, immediate
selection, manual pan retention, empty results and replacement of a pending
frame. `testEveryCardSelectsItsPinAndOpensItsOwnDetail` and
`testSearchHidesStaleDetailAndKeepsQueryAcrossTabs` exercise real MapKit in the
iPhone 16 Pro simulator. The published catalog was also checked manually:
pins render and selecting a center card focuses its geographic location.
These checks do not validate Google tiles: the build still has no Maps key,
and the available Android emulator is the AOSP image described above.

Executed on 2026-10-26: Android debug APK build; four Android marker unit tests;
seven Android UI tests on the API 36 AOSP emulator; 25 shared tests each on JVM
and Android host, rerun rather than accepted from cache; SDK boundary guard;
localization coverage script; strict foundation OpenSpec validation. Android
lint completed with zero errors and 47 warnings concerning dependency/SDK
updates and existing unused resources. These warnings were not suppressed.

Before closing task 5.2, verify with a restricted key and Google-enabled device:

- Real tiles and attribution at the catalog coordinates; one, many and no markers.
- Pan/pinch/zoom without parent scrolling stealing the gesture; filtered
  markers match the list and their click opens the corresponding record.
- Camera retention after tab changes, background/resume and Activity recreation;
  repeat to detect lifecycle leaks or crashes.
- Missing/rejected key, disabled service, missing/outdated Play Services and
  interrupted connectivity. Verify delayed feedback, retry and usable list/detail
  fallback without hiding required attribution or classifying a timeout as an
  authoritative provider error.
- Both languages, long titles, enlarged text, TalkBack and a small phone.

No live Google tile, paid usage, physical-device or GPS verification is claimed.
