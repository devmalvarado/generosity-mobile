## Context

Generosity is a new cross-platform mobile product for iOS and Android. The app connects people with nearby Kindness Centers through an interactive map and helps users understand how to support each center through time, goods, or money. The initial mobile experience must support Spanish and English.

The repository is new, so this design establishes the first mobile architecture and product boundaries. The initial data source will be imported Kindness Center data. Backend services, a Kindness Center backoffice/portal, official center verification, and direct bank/payment processing are expected to exist as separate systems and are not implemented by this mobile-app foundation.

## Goals / Non-Goals

**Goals:**

- Establish the product name as "Generosity".
- Build a shared mobile foundation with Kotlin Multiplatform for domain logic, data models, validation, and use cases.
- Use native UI technologies: SwiftUI for iOS and Jetpack Compose/Kotlin Compose for Android.
- Support Spanish and English for user-facing mobile UI, domain labels, notification text, and imported content when translations are available.
- Support discovery of imported Kindness Centers through map, list, category filters, search, and detail screens.
- Prepare the mobile domain model for center needs, help methods, anonymous users, registered users, contribution history, generosity points, reminders, visit signals, and notifications.
- Keep boundaries explicit so future backend, backoffice, verification, and payment systems can be integrated without rewriting the mobile domain model.

**Non-Goals:**

- Implementing backend services or API infrastructure.
- Implementing a Kindness Center portal, backoffice, or organization onboarding flow.
- Implementing official Kindness Center verification or staff account approval.
- Processing direct bank payments, storing payment details, or managing nonprofit bank accounts.
- Implementing tax receipts, donation compliance, fraud review, or moderation tooling.
- Automating data imports beyond consuming an imported catalog shape.
- Translating center-managed content that is not provided in the imported catalog or future backend data.

## Decisions

### Use Kotlin Multiplatform for Shared Logic

The shared module will own domain models, repository interfaces, validation rules, filtering, sorting, and use cases. iOS and Android will use platform-native UI layers.

Alternative considered: build with Flutter, React Native, or a single shared UI framework. This was not chosen because the app needs a native map, notification, permission, and platform UX feel while still avoiding duplicated business logic.

### Keep Maps and Permissions Platform-Specific

Map rendering, location permissions, notification permissions, and platform lifecycle behavior will live in the iOS and Android app layers. The shared module will expose platform-neutral inputs and outputs, such as location coordinates, search criteria, notification preferences, and center results.

Alternative considered: abstract every platform service behind shared interfaces immediately. This was not chosen because over-abstracting early would add complexity before the first native screens exist.

### Start with Imported Center Data Behind a Repository Boundary

The app will consume imported Kindness Center data through a repository interface. The initial implementation can load bundled or locally provided data, while future backend APIs can implement the same repository contract.

Alternative considered: design only for a live backend from day one. This was not chosen because the product needs to start with imported data and backend/backoffice work is out of scope for this change.

### Use Stable Domain Codes with Platform-Native Localization

The shared module will expose stable domain codes for categories, help methods, verification states, notification types, and other app-controlled labels. iOS and Android will localize those labels through their native localization resource systems. Imported center content can include localized text values where available and must have deterministic fallback behavior.

Alternative considered: store all localized strings in the shared Kotlin module. This was not chosen because each native UI layer needs idiomatic integration with platform localization, accessibility, previews, and app-store language behavior.

### Treat Needs as Read-Only Mobile Data for This Change

The mobile app will display Kindness Center needs when available from imported data or future backend data. Creating and managing needs belongs to the future Kindness Center backoffice, not this mobile app.

Alternative considered: allow centers to manage needs from inside the consumer mobile app. This was not chosen because center access, verification, permissions, and moderation require a separate operational surface.

### Separate Visit Signals from Official Center Verification

The mobile app can collect explicit user visit signals that help establish that an imported Kindness Center has been visited. A visit signal does not make a center officially verified. Official verification belongs to backend and backoffice systems.

Alternative considered: automatically verify a center after one or more visits. This was not chosen because location signals are not enough to prove organizational legitimacy or authorization.

### Prepare for Payments Without Processing Them

The mobile foundation may model "donate money" as a help method, but it will not process bank payments or store payment details. Direct bank/payment processing through the nonprofit organization's account must be handled by a future backend and payment provider integration.

Alternative considered: integrate payments directly in the mobile foundation. This was not chosen because payment processing introduces legal, fiscal, security, and operational responsibilities that should be designed separately.

## Risks / Trade-offs

- Location privacy risk -> Require explicit permission, provide useful manual discovery without location access, and avoid passive visit submission.
- Imported data can become stale -> Track import metadata and expose enough data-state information for the UI to communicate unavailable or outdated content.
- Future backend contracts may differ from the imported model -> Keep repository interfaces domain-focused and avoid binding UI directly to import file structure.
- Visit signals can be abused or misunderstood -> Require explicit user action and label them as signals, not official verification.
- Notifications can become intrusive -> Require opt-in preferences, category controls, and frequency limits.
- KMP/native UI increases integration work -> Keep shared code focused on stable business logic and let platform UI own platform-specific behavior.
- Payment expectations can leak into the MVP -> Keep payment processing out of scope and only enable money donation flows when a backend-provided payment session or external payment handoff exists.
- Localization drift can create inconsistent UX across platforms -> Keep domain labels keyed by shared stable codes and require Spanish and English resource coverage in both native apps.
- Imported content may be available in only one language -> Provide deterministic fallback to available content and avoid machine-translation assumptions in the mobile app.

## Migration Plan

This is a new repository and has no existing production users or data to migrate.

1. Scaffold the shared Kotlin Multiplatform module and native app targets.
2. Add imported Kindness Center data support behind repository interfaces.
3. Build discovery, detail, needs, profile-ready, notification-ready, and visit-signal-ready mobile flows incrementally.
4. Add backend, backoffice, official verification, and payment integrations in later changes.

## Open Questions

- Which imported data format will be used first: JSON, CSV, a static API export, or another source?
- Which authentication provider will be used for registered users?
- Which map providers will be used on iOS and Android?
- What countries and legal donation flows are in the first launch market?
- Which language should be the default fallback when neither Spanish nor English matches the device locale?
- What evidence should a future backend require before marking a Kindness Center as officially verified?
