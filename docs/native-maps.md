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
  selected ID, selection callback, locale and a Compose modifier.
- `toMapMarkers` projects the already-filtered results without querying a
  provider, geocoding addresses or substituting provider place IDs. Invalid
  coordinates are omitted from markers, never clamped to invented locations;
  the center remains accessible in the list.
- [GoogleCenterMapRenderer](../androidApp/src/main/java/org/generosity/app/ui/map/google/GoogleCenterMapRenderer.kt)
  alone owns Google coordinates, markers, camera, availability and SDK lifecycle.
  Initial framing fits the first nonempty catalog result. Filters do not keep
  snapping the user's camera back. Compose saveable state retains framing and
  camera across map/list transitions; it is UI state, not a discovery origin.
- [MainActivity](../androidApp/src/main/java/org/generosity/app/MainActivity.kt)
  selects the concrete renderer. The screen and tests can accept another
  implementation without modifying `GenerosityAppState` or its repositories.
- The adapter is a bounded Android package, not a new KMP module or a generic
  mapping framework. Extracting a Gradle library is unnecessary for one consumer.
- `:androidApp:verifyMapProviderBoundary`, also part of `check`, rejects Google
  Maps and MapLibre SDK references in shared Kotlin and in Android code outside
  `ui/map/google`. The deliberate provider package allowlist must be updated
  when introducing a real alternative adapter, not expanded to business code.

The existing iOS view still uses MapKit and sample Swift models. Connecting it
to the shared store is foundation task 5.4, not completed by this Android change.
MapKit types must remain in native presentation when that work is implemented.

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
  catalog ID to shared selection and shows its title; the existing details below
  the list use the same selected record. No provider place/business lookup is used.
- `isMyLocationEnabled` is false. Browsing does not request location permission,
  fabricate a GPS fix or upload a user location trail. Current location and
  manual place search remain separate tasks 5.6-5.9. A map camera is never
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

No live Google tile, paid usage, GPS or new iOS runtime verification is claimed.
