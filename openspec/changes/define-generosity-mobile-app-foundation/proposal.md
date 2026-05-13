## Why

Generosity needs an initial product and technical foundation for a cross-platform mobile app that helps people discover nearby Kindness Centers and understand how they can help. This change defines the first mobile scope before backend, backoffice, payment processing, and center-managed workflows are implemented in separate systems.

## What Changes

- Define the mobile app foundation for iOS and Android under the product name "Generosity".
- Introduce discovery of imported Kindness Center data through an interactive map, location search, category filters, and center detail views.
- Define how the mobile app presents center needs when they are available from imported or future backend-provided data.
- Support anonymous browsing while preparing for registered user profiles, contribution history, generosity points, reminders, and user-defined ways to help.
- Support a multilingual mobile experience with Spanish and English as the initial languages.
- Capture the distinction between user visit signals and official Kindness Center verification.
- Establish Kotlin Multiplatform as the shared business-logic layer, with native SwiftUI for iOS and Jetpack Compose/Kotlin Compose for Android.
- Exclude backend services, Kindness Center backoffice/portal workflows, direct bank/payment processing, and organization onboarding from this initial mobile-app change.

## Capabilities

### New Capabilities

- `kindness-center-discovery`: Covers finding imported Kindness Centers by location, map interaction, category, and center details.
- `kindness-center-needs`: Covers displaying current needs for Kindness Centers and the help methods users can choose from.
- `user-contribution-profile`: Covers anonymous and registered user behavior, contribution tracking, generosity points, reminders, and user-defined helping activities.
- `location-notifications`: Covers location-aware and urgency-based notification behavior from the mobile app perspective.
- `center-verification-signals`: Covers mobile-side signals that a user visited a Kindness Center and the boundary between those signals and official center verification.
- `app-localization`: Covers Spanish and English localization for user-facing mobile content, domain labels, imported content fallback, and notifications.
- `mobile-platform-architecture`: Covers the expected cross-platform mobile architecture using Kotlin Multiplatform with native iOS and Android UI layers.

### Modified Capabilities

- None.

## Impact

- Affected systems: new iOS app, new Android app, shared Kotlin Multiplatform module, imported Kindness Center data model, localization resources, future backend API contracts.
- External dependencies: mobile maps/location services, push notification services, authentication provider, future backend services, future payment/banking provider.
- Out of scope for this change: backend repository, Kindness Center backoffice or portal, official organization verification workflow, direct bank/payment processing, admin tooling, and data import automation.
