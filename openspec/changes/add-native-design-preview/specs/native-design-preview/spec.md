## ADDED Requirements

### Requirement: Complete native design inventory
Preview builds SHALL provide native SwiftUI and Compose counterparts for all 34 committed source prototype routes, using the existing centralized design tokens and branding, without embedding HTML or a WebView. Production SHALL restrict those renderers to connected destinations as specified below.

#### Scenario: Explore the inventory
- **GIVEN** a Preview build and the server enabling all native preview groups for the current viewer
- **WHEN** the viewer opens design exploration
- **THEN** all 34 routes and their relevant auxiliary forms are navigable with native controls
- **AND** Spanish and English content, device appearance and scalable text are supported

### Requirement: Independent fail-closed groups
The server SHALL initialize every `preview.*` group to off and expose only effective booleans through the existing account-aware feature API. Existing administrator authorization, version checking and auditing SHALL apply.

#### Scenario: Limited tester access
- **GIVEN** a Preview build with only `preview.community` enabled for a verified tester
- **WHEN** that tester opens the design inventory
- **THEN** only the community routes are listed
- **AND** cross-group links are absent unless their target group is also enabled
- **AND** guests and other accounts do not inherit tester access

#### Scenario: Grant removal or account change
- **WHEN** refreshed effective access removes a group or the account changes
- **THEN** unavailable routes cannot be opened or acted upon
- **AND** previous sample inputs and interactions are discarded
- **AND** no cached grant enables the preview after a failed feature refresh

### Requirement: Isolated fictional interactions
Unsupported destinations and the isolated harness SHALL keep samples in memory without invoking real accounts, repositories, GPS, messaging, notifications, checkout or fiscal issuance. Connected destinations SHALL reuse the mobile integration without importing fixture inputs. Screens SHALL omit repeated session banners while retaining action-specific validation.

#### Scenario: Offer and sample receipt
- **WHEN** the viewer uses the isolated fixture harness and submits a positive quantity and date in the sample offer form
- **THEN** sample history shows the pending offer
- **AND** cancellation and sample center confirmation/receipt update only that session
- **AND** no real contribution or validated points are generated

#### Scenario: Consent and irreversible operations
- **WHEN** a sample referral, assessment or assisted participation lacks its required consent
- **THEN** the form does not advance
- **AND** sample payments and fiscal issuance remain disabled even with their preview flag enabled
- **AND** sample deletion or phone verification cannot affect an actual account or phone number

### Requirement: Consistent native discovery
In the isolated fixture harness, the illustrative map, result list and center detail SHALL share search, category, resource-source filter and selected center state, with accessible empty and offline states.

#### Scenario: Select a blood bank
- **WHEN** the viewer filters to blood donation and selects a result or its pin
- **THEN** the same fictional center is selected in map and list
- **AND** its detail opens and back navigation preserves filters
- **AND** saving it updates the session's saved-center list

### Requirement: Platform-native navigation bars
iOS SHALL use system TabView and NavigationStack toolbars, inheriting Liquid Glass on supported operating systems and the system appearance on older versions. Android SHALL use Material 3 NavigationBar, NavigationBarItem and TopAppBar. Both SHALL expose labeled, selected destinations and only tabs allowed by current feature groups.

#### Scenario: Navigate without session banners
- **WHEN** the viewer changes tabs or opens a nested route
- **THEN** the destination and selected tab stay consistent
- **AND** the screen inventory remains available from the top toolbar
- **AND** no persistent fictional-session banner or technical map footer reduces the content area

#### Scenario: No enabled primary tabs
- **GIVEN** a Preview build with only an auxiliary feature group such as community enabled
- **THEN** the allowed content and inventory remain accessible without an empty tab bar

### Requirement: Map-first discovery header
The map SHALL display the shared brand wordmark centered in the navigation header instead of a text title. List and filter buttons SHALL appear on the left, and the notifications shortcut and screen inventory on the right. The notifications shortcut SHALL respect its feature group. The header SHALL omit the close X; exiting the design area SHALL remain available in its inventory when a real dismissal action exists.

#### Scenario: Search and inspect map results
- **WHEN** the viewer searches using the floating capsule over the map
- **THEN** the active data source (shared live state or isolated fixture session) updates pins and the selected center
- **AND** the capsule provides a magnifier and a permission-aware location action in connected mode or a simulated-location action in the isolated harness
- **AND** list selection, filters, notifications and inventory open their existing native destinations
- **AND** search and toolbar controls have localized accessible names and native touch targets

#### Scenario: Map header appearance
- **THEN** the map has one centered wordmark without a duplicate logo/list/filter row
- **AND** iOS uses Liquid Glass for the floating search on supported systems, with native material on earlier versions
- **AND** Android uses Material 3 controls and an elevated capsule

### Requirement: Connected mobile destinations
Supported destinations SHALL reuse the existing mobile state, account, backend repository, cache, outbox, forms and map adapters. Preview inventories SHALL distinguish connected and fixture routes. Production inventories SHALL contain only connected routes, without connected/design labels. Supported flows SHALL include discovery, center/contact/visits, current needs, goods/time and personal offers, history/impact, account/welcome/settings, and inbox.

#### Scenario: Default design in both mobile channels
- **GIVEN** the iOS or Android app uses the production or Preview release channel
- **WHEN** the user enters as a guest, signs in or relaunches after entering
- **THEN** the connected design is the main interface without an exploration button or exit to the old layout
- **AND** the existing welcome, account isolation and effective group restrictions remain in force
- **AND** missing or revoked groups show loading/unavailability with retry rather than granting fixture access or restoring the old layout
- **AND** production exposes only supported connected destinations; granting a preview group does not expose its fixture routes in production

#### Scenario: Production route restriction
- **GIVEN** a production build with every group enabled
- **THEN** its screen inventory and direct navigation allow only connected routes
- **AND** fixture destinations such as reviews, certificates, events, phone verification and payments remain inaccessible
- **AND** missing or revoked groups preserve the existing loading/unavailability and retry behavior without displaying mock data

#### Scenario: Partial offer and retry
- **GIVEN** the account and feature controls permit contributions
- **WHEN** the user offers part of a real need from the new layout
- **THEN** the existing form validates and persists the exact entered quantity with the real need ID before closing
- **AND** an offline retry preserves its idempotency key and account ownership
- **AND** history and impact reflect the authoritative synchronization response

#### Scenario: Empty, offline or unavailable service
- **WHEN** a supported service is empty or fails
- **THEN** the screen shows its empty/error state and available real cache
- **AND** no fictional center, profile, contribution, points or notification is substituted
- **AND** missing feature endpoints remain fail-closed

#### Scenario: Fixture boundaries
- **GIVEN** a Preview build or the isolated debug harness
- **WHEN** a user explores a route without a deployed mobile integration
- **THEN** its fields remain isolated in memory
- **AND** fixture center IDs cannot open or mutate unrelated real centers
- **AND** account changes discard fixture state and never transfer cached private records across accounts

#### Scenario: Native settings and navigation
- **WHEN** the user changes language/preferences or opens a notification in the connected layout
- **THEN** the existing persistence and permission handling applies
- **AND** the notification opens the real destination center
- **AND** layouts preserve their existing preview-group boundaries and backend authorization
