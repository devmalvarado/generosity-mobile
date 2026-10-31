## Why

Earlier plans deliberately excluded operational and growth features that the
user now requests as specifications. Their scope, authority and release gates
need explicit contracts instead of remaining implicit backlog items.

## What Changes

- Allow organizations to apply/claim centers and request reviewed verification, without self-granting staff privileges or implying government endorsement.
- Add scoped platform operations, authorized data connectors and native staff workspaces using the existing backend.
- Extend events with bounded recurrence, waitlists, contextual communication and separately gated paid admission.
- Add multi-jurisdiction donation tax-document workflows, disabled until issuer, jurisdiction, provider and reviewed rules are configured.
- Add non-financial PDF certificates and shareable badges based on validated helping, with revocation and verification.
- Add privacy-safe comparisons of validated helping by country, neighborhood and postal area, with explicit metrics and population-normalization limits.

## Capabilities

### New Capabilities

- `center-onboarding`: Organization applications, existing-center claims, reviewed memberships and ownership recovery.
- `organization-verification`: Evidence review, named verification scope/issuer, expiry, revocation and appeals.
- `platform-operations`: Scoped administration, support/fraud queues, audited interventions and release controls.
- `authorized-data-connectors`: Licensed catalog/schedule integration, provenance, conflict handling and connector disablement.
- `event-series-and-waitlists`: Bounded recurring occurrences, exceptions, fair waitlists and expiring seat offers.
- `participant-communications`: Contextual messages/announcements, consented delivery, blocking/reporting and moderation.
- `native-center-workspace`: Role-separated Android/iOS center management with portal/backend parity.
- `donation-tax-documents`: Jurisdiction/issuer-gated tax documentation, private retrieval and correction lifecycle.
- `recognition-credentials`: Earned certificates/PDFs and badges, controlled sharing and live authenticity status.
- `regional-help-rankings`: Aggregate territorial comparisons with validated activity, geography provenance and fair interpretation.
- `paid-event-ticketing`: Provider-hosted paid admission, atomic inventory, verified tickets, cancellations and refunds, separate from donations.

### Modified Capabilities

No synchronized baseline capability changes. Earlier phase exclusions remain
historical delivery boundaries, not a ban on this later change. Regional
comparisons extend the reference phase's no-leaderboard rule only for aggregate
places, not named people or spending. Native consumer needs remain read-only;
the new authenticated staff workspace is an explicit separate mode.

## Impact

- Depends on the foundation, reference and community changes; uses the [web participation plan](../add-web-participation-and-sharing/proposal.md) for browser helping and shared public entry.
- Reuses identity, events, contribution/payment ledgers, moderation, inbox and backend storage. Adds no second backend, unrestricted superuser role or silent data migration.
- Adds server-authorized operations and documents; shared KMP contracts; Android/SwiftUI adapters; mobile-first portal queues; English/Spanish status and accessibility coverage.
- All implementations and production enablements remain pending. Tax/provider/jurisdiction selections are launch gates, not guessed legal rules or approval to charge.

## Confirmed Decisions and Interpretation

On 2026-10-26 the user requested specs for previously excluded features and
clarified rewards as PDF certificates/badges, plus comparisons of neighborhoods
and postal codes by country. Rewards here are non-financial recognition, not
cash, transferable credit or paid shortcuts to organizer eligibility. Territorial
aggregation uses where validated help occurred, not inferred home addresses;
metric details are proposed in design, not an assertion that places are morally
better. Paid events are a separately gated capability, not donation receipts.

The user chose a multi-country fiscal framework with issuance blocked until
country and provider are configured. Official claims must identify their actual
issuer and scope; a Generosity review is not tax status, payment-provider
onboarding or government certification. The [scope review](../../../docs/scope-review.md)
maps old exclusions to existing/new contracts and preserves non-feature safety
constraints such as no public household data or direct credential collection.
