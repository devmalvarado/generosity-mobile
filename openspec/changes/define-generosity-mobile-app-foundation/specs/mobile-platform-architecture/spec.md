## ADDED Requirements

### Requirement: Kotlin Multiplatform Shared Module
The mobile app SHALL use Kotlin Multiplatform for shared business logic across iOS and Android.

#### Scenario: Shared domain rule is needed
- **WHEN** a rule applies equally to iOS and Android
- **THEN** the rule is implemented in the shared Kotlin Multiplatform module

### Requirement: Native iOS User Interface
The iOS app SHALL use SwiftUI for its native user interface.

#### Scenario: iOS screen is implemented
- **WHEN** an iOS user opens a Generosity screen
- **THEN** the screen is rendered with the native iOS UI layer

### Requirement: Native Android User Interface
The Android app SHALL use Jetpack Compose or Kotlin Compose for its native user interface.

#### Scenario: Android screen is implemented
- **WHEN** an Android user opens a Generosity screen
- **THEN** the screen is rendered with the native Android UI layer

### Requirement: Shared Domain Models
The shared module SHALL define platform-neutral domain models for Kindness Centers, center categories, center needs, help methods, users, contributions, reminders, notifications, and visit signals.

#### Scenario: Platform reads center data
- **WHEN** either platform needs Kindness Center data
- **THEN** it uses the shared domain model rather than defining an incompatible platform-only model

### Requirement: Repository Boundary
The shared module SHALL access Kindness Center data through repository interfaces that can support imported data initially and backend data later.

#### Scenario: Data source changes from import to backend
- **WHEN** a future backend data source becomes available
- **THEN** the app can integrate it behind the repository boundary without changing core discovery behavior

### Requirement: Platform Service Adapters
Location, maps, notifications, authentication, and payment handoff SHALL be represented through platform service adapters rather than direct shared-module dependencies on platform APIs.

#### Scenario: Shared logic needs current location
- **WHEN** shared discovery logic needs a location input
- **THEN** the platform layer provides platform-neutral location data through an adapter

### Requirement: Cross-Platform Behavioral Parity
Both native apps SHALL consume the shared domain state for discovery, needs, contribution validation, history, localization selection and notification eligibility. For the same account, catalog revision and inputs, they SHALL produce equivalent domain results. Platform-specific presentation SHALL not replace shared rules with sample-only behavior.

#### Scenario: Same contribution on both platforms
- **WHEN** Android and iOS submit equivalent offers against the same catalog state
- **THEN** both display equivalent validation or backend outcomes and the same synchronized remaining quantity

#### Scenario: Navigate away and return
- **WHEN** a user repeatedly opens and closes discovery or contribution screens
- **THEN** state remains consistent and a single action does not create duplicate submissions

### Requirement: Replaceable Native Map Rendering
Android SHALL initially render with Google Maps and iOS SHALL retain Apple MapKit. Map SDK types, camera objects and provider IDs SHALL stay out of shared domain models, state and repositories. Android SHALL expose a small renderer contract using catalog IDs and neutral coordinates so a later MapLibre adapter can replace Google without changing business rules. Location and place search SHALL remain separate from rendering.

#### Scenario: Replace the Android renderer
- **WHEN** another renderer is injected for the same shared discovery state
- **THEN** it receives the same filtered center IDs and coordinates and sends selection through the same shared action without changing contribution or discovery rules

#### Scenario: Map SDK reference leaks into shared logic
- **WHEN** a Google Maps or MapLibre SDK reference is introduced outside the permitted native rendering adapter
- **THEN** the Android map-boundary verification fails before the change is accepted

### Requirement: Untrusted Client Boundary
Shared mobile validation SHALL provide feedback but SHALL NOT replace backend authorization, capacity or lifecycle validation. Native platform APIs SHALL supply permission/location results without fabricating grants or letting client state establish server identity.

#### Scenario: Client skips a validation check
- **WHEN** a modified client sends an unauthorized or invalid contribution directly
- **THEN** the backend rejects it independently of the app's shared validation

## MODIFIED Requirements

### Requirement: Out-of-Scope System Boundary
This change SHALL include a separately running backend for identity, catalog, persistence, sync, notifications and authorized contribution actions, plus a mobile-first center web portal. Native and web clients SHALL use backend contracts, not embed server authority. Consumer native apps SHALL keep needs read-only. Official organization verification, direct payment processing and tax receipts SHALL remain outside this scope.

#### Scenario: Feature requires backoffice or payment infrastructure
- **WHEN** a requested behavior requires official verification or payment processing
- **THEN** the app keeps it as a future integration rather than simulating it

#### Scenario: Feature requires persistent backend state
- **WHEN** an authenticated user records or synchronizes a contribution
- **THEN** the mobile app uses the included backend service and reports pending or failed requests honestly

#### Scenario: Center staff publishes a need
- **WHEN** authorized staff creates a need in the center web portal
- **THEN** it uses the shared backend and publishes into the catalog consumed by native apps without adding center-management controls to consumer mobile screens
