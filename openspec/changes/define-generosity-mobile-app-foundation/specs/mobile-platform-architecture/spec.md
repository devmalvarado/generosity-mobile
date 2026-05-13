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

### Requirement: Out-of-Scope System Boundary
The mobile app SHALL NOT implement backend services, Kindness Center backoffice features, official center verification, or direct bank/payment processing in this foundation change.

#### Scenario: Feature requires backoffice or payment infrastructure
- **WHEN** a requested behavior depends on backend, center portal, verification, or payment infrastructure
- **THEN** the mobile app treats that behavior as a future integration point
