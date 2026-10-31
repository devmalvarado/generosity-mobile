# Mobile Foundation

Generosity starts as a mobile-first repository with shared Kotlin Multiplatform business logic and native user interfaces.

This document describes the intended architecture, not completion status.
The foundation change was reopened on 2026-10-25. See the
[implementation audit](../openspec/changes/define-generosity-mobile-app-foundation/implementation-status.md)
and [task checklist](../openspec/changes/define-generosity-mobile-app-foundation/tasks.md).

## Scope

The mobile foundation includes imported Kindness Center discovery, localized Spanish and English app text, read-only needs display, anonymous browsing, registered-profile-ready state, contribution tracking models, notification preferences, reminder models, and explicit visit signals.

The user expanded the active scope on 2026-10-25 to include backend APIs,
authentication, durable storage/sync and center-authorized contribution
transitions. The supplied landing and a mobile-first center web workspace for
need publishing/management were subsequently included. These remain planned.
Official verification, payments, tax receipts, moderation and general admin UI
remain outside the change. See the [spec index](../openspec/changes/define-generosity-mobile-app-foundation/spec-index.md)
and [portal assessment](center-portal.md).

The separate [reference product follow-up](../openspec/changes/add-reference-product-features/spec-index.md)
now specifies new navigation, engagement features, center media/hours, review
moderation, events and provider-hosted donations. These do not retroactively
change what the foundation's visual-only tasks claim to implement. Direct
payment-data handling, official verification and tax receipts remain excluded.

## Architecture

Shared Kotlin code owns platform-neutral models, repository interfaces, imported catalog access, filtering, sorting, fallback localization rules, notification eligibility, contribution rules, and visit-signal rules.

Android uses Jetpack Compose. iOS uses SwiftUI. Maps, permissions, notifications, authentication, and future payment handoff remain platform services exposed to shared logic through platform-neutral inputs and adapters.

As of 2026-10-30, iOS discovery observes the root `GenerosityStore` and renders
shared `KindnessCenter` / `CenterNeed` records directly. Search, category
selection, ordering, selected-center resolution and current-needs rules come
from the same `GenerosityAppState` used by Android. `SharedPresentation.swift`
only converts public coordinates for MapKit and maps domain label codes to
native resources; imported-text fallback delegates to Kotlin. No independent
Swift catalog or need-expiry filter remains. The root owns one observer across
tab navigation and the store stops it when released. This completes discovery
wiring, not the separate contribution, history, storage or notification tasks.

The 2026-10-26 map decision uses Google Maps on Android and retains Apple
MapKit on iOS. Android rendering is injected through a small provider-neutral
contract, leaving shared discovery/business rules unchanged for a later MapLibre
migration. See [native maps](native-maps.md) for implementation and remaining
credential/device acceptance; this does not complete current-location support.

## Localization

Spanish and English are the initial supported languages. Domain labels use stable shared codes. Native apps resolve those codes into localized platform strings. Imported center content supports localized text fields and deterministic fallback when a selected language is missing.

