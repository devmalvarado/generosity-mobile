# Native Maps

## Provider Decision

Decision recorded on 2026-10-26: start with **Google Maps on Android** and
keep **Apple MapKit on iOS**. Android must remain replaceable with MapLibre
without changing catalog, search/filter, contribution or other business rules.
MapLibre is not installed and no runtime provider switch is introduced.

The Android placeholder has been replaced by a Google Maps Compose adapter.
Live Google tiles are not yet verified: no API key is configured, and the
existing `Generosity_API_36` emulator uses an AOSP image without Google APIs.
The no-map state still allows anonymous list/detail discovery.

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
  selected ID, optional searched-origin coordinates, selection callback, locale and a Compose modifier.
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
5. Run on an Android device with Google Play Services, or create an API 36
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
- `isMyLocationEnabled` is false. Browsing does not request location permission,
  fabricate a GPS fix or upload a user location trail. Current location remains pending in tasks 5.6–5.7; manual place
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
