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
