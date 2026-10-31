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

#### Scenario: Permission granted but no fix
- **WHEN** authorization exists but location cannot be obtained
- **THEN** the app offers manual browsing/search and does not present a fixed city as the user's location

#### Scenario: Revoke location permission
- **WHEN** the user revokes access in device settings and returns
- **THEN** location updates and proximity alerts stop while manual discovery remains available

### Requirement: Manual Discovery Origin
Users SHALL be able to browse the map and select a searched place as a discovery origin without GPS permission. The app SHALL distinguish distance from that selected place from distance to the user. Failed or empty place searches SHALL preserve existing results and allow retry.

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
