# Mobile Foundation

Generosity starts as a mobile-first repository with shared Kotlin Multiplatform business logic and native user interfaces.

## Scope

The mobile foundation includes imported Kindness Center discovery, localized Spanish and English app text, read-only needs display, anonymous browsing, registered-profile-ready state, contribution tracking models, notification preferences, reminder models, and explicit visit signals.

The mobile foundation does not include backend services, a Kindness Center portal or backoffice, official center verification, direct bank/payment processing, tax receipts, moderation tooling, or admin operations.

## Architecture

Shared Kotlin code owns platform-neutral models, repository interfaces, imported catalog access, filtering, sorting, fallback localization rules, notification eligibility, contribution rules, and visit-signal rules.

Android uses Jetpack Compose. iOS uses SwiftUI. Maps, permissions, notifications, authentication, and future payment handoff remain platform services exposed to shared logic through platform-neutral inputs and adapters.

## Localization

Spanish and English are the initial supported languages. Domain labels use stable shared codes. Native apps resolve those codes into localized platform strings. Imported center content supports localized text fields and deterministic fallback when a selected language is missing.

