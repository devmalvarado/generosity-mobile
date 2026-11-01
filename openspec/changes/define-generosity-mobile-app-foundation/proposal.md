## Status: Reopened on 2026-10-25

This foundation was prematurely archived as
`2026-05-13-define-generosity-mobile-app-foundation`. At the user's request,
it is active again; the original creation date and artifacts are retained.
The seven capability specs were already synchronized to `openspec/specs/`.
Their presence is a contract, not proof that every platform implements it.
See [implementation-status.md](implementation-status.md) for the code audit
and [tasks.md](tasks.md) for corrected completion states.

The audit documents the shared contribution rules consolidated in `d489614`.
The user subsequently requested specifications for all remaining features,
device-default language with a settings override, and the welcome screen in
the [visual reference](../../../docs/design-reference.md). On 2026-10-25 the
user explicitly added backend scope and approved center-authorized
confirmation/fulfillment with self-reported personal help. These new deltas
are proposed behavior, not implementation or already-synchronized main specs.

The user subsequently supplied the existing landing at
`/Users/manuel.alvarado/Documents/Development/PoCs/generosity/portal` and
requested center need publishing with a mobile-first experience. The public
landing and authenticated center web portal are now included, superseding
the earlier portal exclusion. See [portal assessment](../../../docs/center-portal.md).

The user also requested closer visual alignment with the supplied mobile
concept, explicitly choosing visual design only and deferring new functions.
Keep existing destinations and flows; apply the reference's visual language
to discovery, center detail, current needs and profile/impact without adding
favorites, achievements, streaks or the concept's new navigation.

The user's later request for these missing functions is tracked separately in
[add-reference-product-features](../add-reference-product-features/spec-index.md).
This foundation retains its scoped visual-only tasks and exclusions; the
follow-up activates functional navigation and integrations after prerequisites.

## Why

Generosity needs a complete center-to-need-to-contribution-to-history flow on
both native apps. The current prototype loses data, duplicates iOS rules and
does not integrate real identity, location, scheduling or server authority.
This expanded foundation defines those missing contracts, including the
backend explicitly requested by the user, without claiming they are built.

## What Changes

- Keep anonymous navigation, search, categories, needs and English/Spanish support.
- Use the device language by default, with a persistent override in user settings.
- Add a branded welcome screen with guest, sign-in and registration actions.
- Align existing native screens with the reference's brand, spacing, hierarchy and compact controls, preserving their flows and truthful data states.
- Connect iOS to shared state and add durable local storage, real clocks and native maps/location.
- Add backend identity integration, durable data, authenticated APIs and multiuser synchronization.
- Scope Google, Apple and Facebook entry to Generosity web/Android/iOS; add private mobile contact details and possession verification only for explicitly approved feature gates. Facebook and mobile verification are planned extensions, not implemented behavior.
- Replace fixed contribution values with forms; add server-authorized transitions, history and correctly labeled impact.
- Load and refresh a real catalog; implement contacts, visit queues, reminders and opted-in nearby/urgent alerts.
- Extend the existing public landing with an authenticated, mobile-first center workspace for need publishing and contribution actions through the same backend.
- Keep official verification, real payments and automated external data harvesting out of scope.

## Capabilities

### New Capabilities

- `mobile-welcome`: First-screen appearance, navigation and accessibility.
- `mobile-visual-alignment`: Reference-aligned visual language and screen composition for existing native flows, with accessible layouts and visual acceptance.
- `local-data-persistence`: Restart recovery, account isolation, offline outbox and conflict handling.
- `user-authentication`: Real registration, Generosity-only social sign-in, explicit linking, session recovery, sign-out and feature-specific mobile verification.
- `imported-center-catalog`: Validated file import, backend catalog refresh and last-known-good data.
- `app-time`: Real timestamps, expiration and timezone-aware reminders.
- `contribution-lifecycle`: Center-authorized transitions, cancellation, capacity and self-reported activities.
- `backend-platform`: Authenticated API, persistence, sync, authorization and service operation.
- `center-web-portal`: Public landing, authorized need authoring, publication and contribution management, with mobile-first browser acceptance.

### Modified Capabilities

- `app-localization`: Device-first resolution, persistent settings and live updates.
- `mobile-platform-architecture`: Native/shared parity and the expanded backend boundary.
- `kindness-center-discovery`: Real maps, location/manual fallback and contact actions.
- `kindness-center-needs`: Server-synchronized status and remaining capacity.
- `user-contribution-profile`: Input forms, detailed history, truthful impact units and private progressive mobile contact settings.
- `location-notifications`: Effective delivery, reminder management and opt-out handling.
- `center-verification-signals`: Durable explicit visits and authenticated upload without official verification.

The original seven capability deltas remain in this reopened change because
their baseline was previously synchronized. New/modified behavior is tracked
here until implementation and review; no new feature is marked complete.

## Impact

- Affected systems: shared KMP state/repositories, SwiftUI and Compose apps,
  local storage, localized resources, public landing/center web portal,
  backend service/database and API contracts.
- External integrations: identity provider, map/location services and platform push delivery.
- Language changes affect welcome, settings, forms, errors, dates, notifications and browser screens in both Spanish and English.
- Existing demo data is not imported as real accounts, transactions or verified activity.
- Out of scope: public organization self-enrollment/official verification,
  payment processing, tax receipts, scoring/rewards, ratings, favorites, events,
  general admin UI, moderation and automated third-party data harvesting.
