## Why

Helping also happens through community-led reforestation, cleanup, coordinated
meals and voluntary household outreach. Generosity needs to connect that work
to a privacy-preserving picture of unmet needs and service capacity so people
can compare where additional help could be useful, rather than count centers.

## What Changes

- Let registered users organize community campaigns after meeting a published, server-verified level, points and completed-help policy. Numeric thresholds remain undecided, not implicitly approved.
- Support campaigns with dated activities, volunteers, materials, safety arrangements and separately reported outcomes; reuse the event engine instead of creating another reservation system.
- Coordinate recipient-approved meal delivery slots with portions, preferences, cancellation and private delivery information.
- Collect voluntary household needs using a versioned minimal questionnaire, optional separately consented private follow-up and restricted referrals. No public household map or compulsory census.
- Compare aggregate demand against comparable center services, capacity, schedules and accessibility; evaluate additional sites, outreach, capacity changes and relocation scenarios with uncertainty and human review.
- Extend the same mobile-first portal to eligible organizers, assigned field workers and scoped analysts; add native discovery, participation and private assessment flows in English and Spanish.
- Keep center-validated completed help as the only source of gamification. Campaign ownership, survey volume, beneficiary receipt and organizer reports do not themselves award points or review eligibility.

## Capabilities

### New Capabilities

- `community-organizer-eligibility`: Earned organizing access, versioned eligibility checks, scoped permissions and suspension handling.
- `community-campaigns`: Community ownership, activity types, campaign/event coordination, participation, safety and outcome provenance.
- `meal-support-coordination`: Consented meal schedules, atomic slot commitments, private delivery details and receipt history.
- `household-needs-assessments`: Consent-first field collection, optional private case follow-up, referrals, access/retention and safe aggregate release.
- `territorial-coverage-analysis`: Comparable demand/supply inputs, accessible capacity allocation, coverage gaps and reproducible what-if decisions.

### Modified Capabilities

None of the seven synchronized baseline specs changes. These additive
contracts build on two active, unimplemented plans. `volunteer-events` in the
reference plan continues to govern center-owned events; `community-campaigns`
adds an explicitly different owner type using its reservation mechanics.
Community event publication does not grant center staff powers. The original
plus action and the center-only gamification/review rules remain unchanged.

## Impact

- Depends on the [foundation](../define-generosity-mobile-app-foundation/spec-index.md) for real identity, durable storage/sync, native parity, backend, center authority and the portal; depends on [reference features](../add-reference-product-features/spec-index.md) for events, award ledger, navigation and inbox. These prerequisites are not working services yet.
- Extends shared KMP models, repository contracts, validation and client state; SwiftUI/Compose screens; the canonical mobile-first portal; the same backend/database and notification outbox. No parallel backend or fork of the older landing.
- Adds restricted case storage, consent records, analyst assignments, service-capacity inputs and server-side geospatial analysis. Existing great-circle discovery distance is not travel time or a coverage model.
- Changes no current runtime behavior during this planning turn. Main specs remain unsynchronized and all new tasks remain pending until implementation and verification.

## Decisions and Limits

Confirmed by the user on 2026-10-25: any registered user can become an organizer
after sufficient prior help/level/points; household follow-up is optional and
private with consent, while maps use aggregates. Organizing is not official
verification, a professional qualification or permission to inspect households.

Exact eligibility thresholds, sensitive-work safeguards, retention, aggregate
disclosure rules, service units and the initial pilot region require explicit
release decisions. Unconfigured gates stay closed. This change includes narrow
safeguarding/analyst operations, not a general admin suite. It excludes mandatory
enumeration, public beneficiary identities, automatic center closure, individual
benefit eligibility decisions, medical advice, land-use approval automation,
AI scoring of people, payments, external Meal Train account integration and
unbounded recurring-event generation.
