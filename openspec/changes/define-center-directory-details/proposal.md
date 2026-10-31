# Proposal

## Why

The supplied center record needs explicit field semantics so people can understand
services, costs, access and ways to help without mistaking missing information for
free service, available places or verified status. The user confirmed that bold
fields are prominent, not all mandatory: publication needs identity, categories
and a public contact route; other missing data must remain explicit.

## What Changes

- Define the directory record for public/legal name, multiple categories,
  activities, hours, recovery fees, address, responsible contact, phone,
  email/social/web, beneficiary counts, admission availability, ways to
  contribute, resource sources, verification and featured/additional attributes.
- Require a stable identity, nonblank public name, supported categories and at
  least one public contact route for new publication. Incomplete records may
  remain private drafts; optional unknown fields do not force invented values.
- Distinguish unknown, withheld and not-applicable data from zero, false or free;
  show provenance and freshness for time-sensitive center declarations.
- Keep beneficiary counts aggregate and dated, admission separate from occupancy,
  and recovery fees separate from donations and payment processing.
- Protect personal contacts and sensitive addresses at the public projection
  boundary, including map payloads, sharing, search and catalog bundles. A center
  without public coordinates can remain discoverable through a contact-only list.
- Reuse existing profile/hours, scoped verification, contribution and intake
  contracts instead of creating a second editor, payment system or case registry.
- Document Spanish/English native and mobile-first portal presentation and
  editing with honest missing-data states.

## Capabilities

### New Capabilities

- `center-directory-details`: Publication minimums, field meanings, disclosure,
  fees, beneficiary/admission declarations and structured profile attributes.

### Modified Capabilities

- `kindness-center-discovery`: Detail fields use the directory contract; list/map
  share filters while contact-only records without public coordinates stay
  available in the list without exposing or fabricating a map location.

## Impact

- Shared KMP: directory value types, public-location projection, validation,
  serialization compatibility and localized view state. Existing category codes
  and center IDs remain stable; broader category expansion is a separate decision.
- Backend/imports: scoped profile updates, publication gates, public/private
  separation, provenance and non-destructive migration of incomplete records.
- Android and iOS: richer detail sections, missing/contact-only location states,
  no private coordinates in Google Maps/MapKit adapters or external handoffs.
- Canonical portal: extend the planned center profile editor and public mobile
  pages, not the old static landing or an additional backoffice.
- Dependencies: foundation auth/catalog/portal/native parity; reference public
  profiles/hours; platform-expansion verification claims; center intake-service
  availability. These dependencies remain planned, not assumed operational.
- Compatibility: current non-null address/coordinates and generic verification
  status need an explicit versioned migration. Unsupported legacy public clients
  must not receive protected location/contact data to preserve their old shape.
- Exclusions: real center onboarding/data publication, a complete new taxonomy,
  beneficiary identities, admission reservations, fee collection, accounts or
  financial audits, new verification issuance, and production payments.

This change is planning only. The user's named example is source material for
field meaning, not verified directory data or authorization to publish a person.
