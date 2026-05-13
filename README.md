# Generosity

Generosity is a cross-platform mobile app for iOS and Android that helps people discover nearby Kindness Centers and understand how they can help through time, goods, or money.

The initial mobile scope uses imported Kindness Center data and supports Spanish and English. Backend services, Kindness Center backoffice workflows, official center verification, direct bank/payment processing, admin tooling, and data import automation are outside this repository's first mobile foundation.

## Product Boundaries

- Product name: Generosity.
- Initial languages: Spanish and English.
- Initial data source: imported Kindness Center catalog data.
- Mobile platforms: iOS and Android.
- Shared logic: Kotlin Multiplatform.
- iOS UI: SwiftUI.
- Android UI: Jetpack Compose.
- Out of scope: backend repository, center portal/backoffice, official organization verification, direct bank/payment processing, tax receipts, moderation tooling, and admin tooling.

## Repository Structure

- `shared/`: Kotlin Multiplatform shared domain, repository interfaces, imported sample catalog, use cases, and tests.
- `androidApp/`: Android Compose app foundation and Android localization resources.
- `iosApp/`: SwiftUI app foundation and iOS localization resources.
- `data/`: sample imported catalog data.
- `docs/`: implementation notes and verification checklists.
- `openspec/`: OpenSpec proposals, specs, and task tracking.

## Tooling Notes

This repository defines a Gradle-based Kotlin Multiplatform project. A local Gradle installation or Gradle wrapper is required to build it. The current mobile foundation avoids implementing backend, payment, and backoffice behavior directly; those are future integration points behind shared repository and service boundaries.

