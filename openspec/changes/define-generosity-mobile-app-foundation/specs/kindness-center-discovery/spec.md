## ADDED Requirements

### Requirement: Imported Kindness Center Catalog
The mobile app SHALL load Kindness Centers from an imported catalog source before requiring a live backend.

#### Scenario: Catalog is available
- **WHEN** the app starts with a valid imported catalog
- **THEN** the app displays available Kindness Centers from that catalog

#### Scenario: Catalog is unavailable
- **WHEN** the imported catalog cannot be loaded
- **THEN** the app displays an empty discovery state without crashing

### Requirement: Center Categories
The mobile app SHALL support filtering Kindness Centers by categories including children support, older adults, disability support, health, addiction recovery, community kitchens, education, vulnerable families, and organ or tissue donation.

#### Scenario: User filters by category
- **WHEN** the user selects one or more center categories
- **THEN** the app displays only Kindness Centers matching at least one selected category

### Requirement: Map-Based Discovery
The mobile app SHALL provide an interactive map view for discovering nearby Kindness Centers.

#### Scenario: User grants location permission
- **WHEN** the user grants location permission
- **THEN** the app centers discovery around the user's current location and shows nearby Kindness Centers

#### Scenario: User denies location permission
- **WHEN** the user denies location permission
- **THEN** the app still allows discovery through map browsing, location search, and category filters

### Requirement: List-Based Discovery
The mobile app SHALL provide a list view of Kindness Centers that uses the same search and filter criteria as the map.

#### Scenario: User switches from map to list
- **WHEN** the user opens the list view after applying filters
- **THEN** the list displays the same matching Kindness Centers represented on the map

### Requirement: Center Detail View
The mobile app SHALL provide a detail view for each Kindness Center with its name, location, categories, description, contact information when available, verification status when available, and current needs when available.

#### Scenario: User opens a center
- **WHEN** the user selects a Kindness Center from the map or list
- **THEN** the app displays the center detail view

### Requirement: Discovery Without Registration
The mobile app SHALL allow users to discover, filter, and view Kindness Center details without creating an account.

#### Scenario: Anonymous user browses centers
- **WHEN** an anonymous user opens the discovery experience
- **THEN** the user can browse the map, filter centers, and open center details

### Requirement: Real Map Interaction
Both apps SHALL render a geographic map with pan, zoom and selectable center markers at catalog coordinates. Marker selection SHALL open the corresponding preview/details. Map and list SHALL use the same active search/categories and retain them when switching views.

#### Scenario: Filter and select a marker
- **WHEN** a user filters by category and selects a visible marker
- **THEN** its center matches the filtered list and opens the same detail record

#### Scenario: Every result has direct detail access
- **WHEN** a user chooses View details on any result card, including a result other than the first
- **THEN** the app selects that exact center and opens its detail heading in a separate full-screen view without scrolling through the remaining catalog
- **AND** returning from details preserves the card-list position, map selection and filters

#### Scenario: Card selection is reflected on the map
- **WHEN** a user selects a result card in map discovery or returns to the map after selecting a center in the list
- **THEN** the map stays fixed above the independently scrolling cards, centers its camera on that center and distinguishes the selected marker and card
- **AND** a list-card tap opens the same center's separate detail view

#### Scenario: Scroll cards without moving the map
- **WHEN** the user scrolls the result cards
- **THEN** the map and discovery controls remain fixed and usable
- **AND** selecting a marker reveals its matching card in the list
- **AND** the opened detail view scrolls independently and offers an accessible Back action

#### Scenario: Catalog coordinates are outside the demonstration area
- **WHEN** a nonempty catalog first loads or search/category results change
- **THEN** the map frames the selected center if it remains among those results, otherwise requests bounds covering the result coordinates within the provider's native zoom limits
- **AND** it does not remain fixed to demonstration coordinates or fabricate a user location
- **AND** empty results remove markers without inventing a default center; manual pan/zoom remains usable between result or selection changes

#### Scenario: Map tiles unavailable
- **WHEN** the map provider cannot load geographic imagery
- **THEN** the app reports map unavailability while keeping cached list discovery, filters and details usable

#### Scenario: Android key or Google services unavailable
- **WHEN** Android lacks map build configuration or usable Google Play Services
- **THEN** anonymous users retain the same filtered list/detail flow and receive a localized unavailable state without a location permission prompt

#### Scenario: Initial map loading does not finish
- **WHEN** the provider does not report completed initial loading within the bounded waiting period
- **THEN** Android offers retry and the list, describes loading as delayed rather than diagnosing an unreported credential error, and clears feedback if loading later succeeds

### Requirement: Permission-Gated Current Location
The app SHALL request location in context, use an actual permitted location fix and distinguish unavailable, approximate or stale results. Distances SHALL derive from the selected origin, not demo coordinates. Denial or revocation SHALL stop location-dependent behavior without blocking anonymous discovery.

#### Scenario: Grant current-location access
- **WHEN** the user grants permission and a usable fix becomes available
- **THEN** nearby discovery centers on that fix and distances reflect it on both map and list

#### Scenario: Choose the device as discovery origin
- **WHEN** the user chooses Use my location in map or list discovery
- **THEN** Android and iOS request foreground authorization if needed and obtain a single permitted location fix, with a 15-second acquisition timeout after authorization
- **AND** the shared 10 km straight-line radius, nearest-first ordering and existing name/category filters apply to that fix
- **AND** a distinct origin marker and localized distance labels distinguish the device origin from a searched address
- **AND** an already authorized discovery session may acquire location on entry and refresh an expired fix on foreground return without repeatedly prompting or tracking in the background

#### Scenario: Approximate, stale or invalid device location
- **WHEN** Android or iOS receives a device location
- **THEN** only valid coordinates with a nonnegative horizontal accuracy of at most 5 km and a timestamp no older than 120 seconds or more than 5 seconds in the future may become the discovery origin
- **AND** reduced-accuracy authorization or uncertainty above 1 km is explicitly labeled approximate without requiring precise-location access
- **AND** invalid/stale fixes, provider errors and timeouts leave manual discovery available with a localized retry message rather than inventing a device position
- **AND** Android validates sample age using elapsed realtime, so changing the wall clock cannot make an old fix current
- **AND** Android may reuse the operating system's permission-filtered last-known location only when it passes the same freshness and accuracy checks, avoiding needless acquisition on a quick restart
- **AND** an Android permission downgrade replaces the prior precise fix with a newly permitted approximate fix rather than merely relabeling precise coordinates

#### Scenario: Return to an open detail after location expires
- **WHEN** the user returns from an external app while a center detail is open and the device fix needs refreshing
- **THEN** clearing the expired fix and applying its replacement preserve the selected center and its detail, even when the new discovery radius excludes it
- **AND** the detail continues to refresh from the catalog and does not retain a center that the catalog removes
- **AND** explicitly choosing or clearing a manual area still clears the previous selection

#### Scenario: Manual origin supersedes location acquisition
- **WHEN** the user selects or clears a manual area while a device-location request is pending
- **THEN** both platforms cancel the request and ignore late fixes; they do not overwrite the chosen area or restore the device origin automatically
- **AND** device coordinates remain in memory for discovery only, with no persistence, API upload, visit submission or notification delivery caused by obtaining a fix

#### Scenario: Permission granted but no fix
- **WHEN** authorization exists but location cannot be obtained
- **THEN** the app offers manual browsing/search and does not present a fixed city as the user's location

#### Scenario: Retry after iOS location permission was denied
- **WHEN** the user taps Use my location while location access is denied or restricted
- **THEN** the app shows an explicit explanation with Open Settings and a dismiss action, rather than silently repeating the same request
- **AND** after the user grants access in Settings and returns, the app retries acquisition and applies the permitted fix without requiring a restart

#### Scenario: Android permission denied or device location disabled
- **WHEN** a user requests current location without Android authorization
- **THEN** the app requests coarse and fine foreground permissions together and accepts approximate access alone
- **AND** denial, including repeated denial without another system prompt, produces visible localized feedback and an Open Settings action for the app's permissions
- **WHEN** Android device location services are switched off
- **THEN** the app removes any device origin, explains the disabled service and opens device location settings on request
- **AND** returning after permission/service activation retries the pending device-origin intent, while a manual-origin choice is preserved
- **AND** acquisition uses native Android location APIs separately from the map renderer, stops on pause and never requests background location or notification permission for discovery

### Requirement: Open a center in an external map
Anonymous and registered users SHALL be able to open the selected center's exact catalog coordinates in an external map from its detail screen, independently of device-location permission and embedded-map configuration.

#### Scenario: Open a center on iOS
- **WHEN** a user opens any center detail
- **THEN** it offers localized Open in Google Maps and Open in Apple Maps actions
- **AND** Google Maps uses a universal HTTPS map-search URL with the center's latitude and longitude, allowing the app or browser to handle it
- **AND** Apple Maps receives a native map item at those same coordinates with the localized center name

#### Scenario: Open a center on Android
- **WHEN** a user chooses Google Maps or device maps from a center detail
- **THEN** Google Maps receives a universal HTTPS coordinate-search URL, while device maps receives an implicit geo intent with encoded coordinates and center name
- **AND** the system's selected compatible app handles the geo intent; if none is installed, the app tries the Google Maps HTTPS alternative

#### Scenario: Invalid coordinates or unavailable handlers
- **WHEN** coordinates are non-finite or outside latitude/longitude bounds
- **THEN** no external map action is created and a localized unavailable message is shown
- **WHEN** no handler can open a valid map destination
- **THEN** the detail remains usable and displays a localized error, without marking a visit or changing contribution data

#### Scenario: External maps preserve privacy and selected identity
- **WHEN** the user opens an external map after changing center, language, or discovery origin
- **THEN** the destination still contains the selected center's coordinates, never the device's current coordinates, account identifiers, or tokens
- **AND** opening the map does not request GPS permission, automatically start navigation, or record a visit

#### Scenario: Revoke location permission
- **WHEN** the user revokes access in device settings and returns
- **THEN** location updates and proximity alerts stop while manual discovery remains available

### Requirement: Manual Discovery Origin
Users SHALL be able to browse the map and select a searched place as a discovery origin without GPS permission. The app SHALL distinguish distance from that selected place from distance to the user. Failed or empty place searches SHALL preserve existing results and allow retry.

#### Scenario: Choose any address as origin
- **WHEN** the user submits a neighborhood, street address or city in Search area
- **THEN** the native provider returns up to five selectable address suggestions, independently of catalog-name search
- **AND** only selecting a suggestion updates discovery, using valid coordinates and a 10 km straight-line radius
- **AND** name/category filters remain active and results are ordered by distance from that place

#### Scenario: Clear area or find no nearby centers
- **WHEN** the selected address has no matching centers within 10 km
- **THEN** the app frames that area with an empty catalog-results message, without inventing locations
- **AND** clearing the area restores results under the existing name/category filters

#### Scenario: Lookup fails or is superseded
- **WHEN** the provider returns no places, fails or exceeds 15 seconds
- **THEN** the app explains the outcome and permits retry without replacing the previous origin/results
- **AND** responses to edited, dismissed or superseded queries cannot replace newer suggestions
- **AND** submitted queries are at most 200 characters and the chosen area is session-only

#### Scenario: Search a city without GPS
- **WHEN** location is denied and the user selects a valid searched city
- **THEN** discovery uses that city as origin and labels distances accordingly

### Requirement: Available Contact Actions
Center details SHALL display provided phone, email and website values and expose native call, compose and browser actions only for valid supported destinations. Missing fields SHALL not create fabricated actions. Contact actions SHALL not require account registration or access to the user's contacts.

#### Scenario: Open a center website
- **WHEN** an anonymous user selects a valid HTTPS website
- **THEN** the platform opens that destination without changing the center's verification state

#### Scenario: Invalid destination or unavailable handler
- **WHEN** contact data has an unsupported scheme or no suitable native handler exists
- **THEN** the app prevents unsafe navigation or reports the unavailable action without crashing
