## 1. Project Foundation

- [x] 1.1 Scaffold the Kotlin Multiplatform project structure for shared logic, iOS, and Android targets.
- [x] 1.2 Configure the shared module build so iOS and Android can consume common domain code.
- [x] 1.3 Add baseline app entry points for the iOS SwiftUI app and Android Compose app.
- [x] 1.4 Add project documentation that names the product "Generosity", documents Spanish and English as initial supported languages, and keeps backend, backoffice, verification, and payment processing out of this mobile scope.

## 2. Shared Domain Model

- [x] 2.1 Define shared models for KindnessCenter, CenterCategory, CenterNeed, HelpMethod, UserProfile, Contribution, Reminder, NotificationPreference, VisitSignal, LocalePreference, and LocalizedText.
- [x] 2.2 Define verification status values that distinguish imported, visit-signaled, and officially verified centers without allowing mobile visit signals to become official verification.
- [x] 2.3 Define repository interfaces for Kindness Center catalog access, needs access, user profile data, contribution data, reminders, notification preferences, and visit signals.
- [x] 2.4 Add shared validation and mapping tests for the core domain models, localized text fallback, and stable domain label codes.

## 3. Imported Catalog Data

- [x] 3.1 Choose and document the initial imported catalog format, including how localized Spanish and English text fields are represented.
- [x] 3.2 Implement an imported catalog data source behind the Kindness Center repository interface.
- [x] 3.3 Add sample imported Kindness Center data covering all required categories.
- [x] 3.4 Handle missing or invalid imported catalog data with a non-crashing empty state.

## 4. Localization

- [x] 4.1 Configure Spanish and English localization resources for the iOS app.
- [x] 4.2 Configure Spanish and English localization resources for the Android app.
- [x] 4.3 Implement locale preference handling using in-app language choice, supported device locale, and fallback locale.
- [x] 4.4 Map shared domain label codes for categories, help methods, verification states, notification types, and empty states to localized platform strings.
- [x] 4.5 Add localization coverage checks or tests to prevent missing Spanish or English strings for app-controlled text.

## 5. Discovery Experience

- [x] 5.1 Implement shared filtering and sorting use cases for category, location, and search criteria.
- [x] 5.2 Build the Android discovery map and list screens using Compose.
- [x] 5.3 Build the iOS discovery map and list screens using SwiftUI.
- [x] 5.4 Implement Kindness Center detail screens on Android and iOS using shared domain data.
- [x] 5.5 Verify anonymous users can browse, filter, and open center details without registration.

## 6. Needs and Help Methods

- [x] 6.1 Implement shared needs models and read-only needs retrieval.
- [x] 6.2 Display current needs on center detail screens when available.
- [x] 6.3 Display urgent needs distinctly when urgency metadata is available.
- [x] 6.4 Hide expired needs from current needs lists when expiration metadata is available.
- [x] 6.5 Enforce the money donation boundary so the app does not process payments or collect bank/payment details.

## 7. User Profile and Contributions

- [x] 7.1 Implement anonymous and registered user state handling in shared logic.
- [x] 7.2 Add profile screens that can display registered user identity, contribution history, and generosity points when data is available.
- [x] 7.3 Ensure generosity points are presented as non-monetary and non-redeemable.
- [x] 7.4 Implement user-defined helping activity recording when profile and storage capabilities are available.
- [x] 7.5 Implement empty states for registered users with no contribution history.

## 8. Notifications and Reminders

- [x] 8.1 Implement shared notification preference models for nearby centers, urgent needs, and reminders.
- [x] 8.2 Add platform permission flows for notifications and location access.
- [x] 8.3 Implement reminder scheduling for user-created helping reminders.
- [x] 8.4 Add notification controls for disabling nearby center, urgent need, and reminder notifications.
- [x] 8.5 Ensure location-based alerts are disabled unless the user grants both notification and location permissions.
- [x] 8.6 Generate app-controlled notification titles and bodies using the user's active supported locale.

## 9. Visit Signals and Verification Boundary

- [x] 9.1 Implement explicit user visit-signal submission from the center detail experience.
- [x] 9.2 Prevent passive visit-signal submission based only on proximity.
- [x] 9.3 Ensure submitting a visit signal never changes a center to officially verified in mobile state.
- [x] 9.4 Add localized UI copy and state handling that makes official center verification a future backend/backoffice workflow.

## 10. Verification

- [x] 10.1 Add unit tests for shared discovery, filtering, needs expiration, notification preference, contribution, localization fallback, and visit-signal logic.
- [x] 10.2 Add Android UI checks for anonymous discovery, filtering, center details, needs, profile empty states, and language switching.
- [x] 10.3 Add iOS UI checks for anonymous discovery, filtering, center details, needs, profile empty states, and language switching.
- [x] 10.4 Run OpenSpec validation for the change and fix any proposal, design, spec, or task issues.
