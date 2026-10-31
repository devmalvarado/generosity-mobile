# Design

## Context

See [proposal.md](proposal.md) for the approved minimum-publication decision.
Source inspection on 2026-10-26 found:

- `KindnessCenter` has localized name/description/address, a category set,
  mandatory coordinates, optional phone/email/website, a coarse verification
  enum and needs. It has no legal name, representative, fees or beneficiary data.
- `DiscoverKindnessCentersUseCase` searches the address and sorts all records by
  coordinates. Android's marker conversion maps every valid coordinate directly.
  The new public projection must precede both operations, not just hide UI text.
- SwiftUI still uses separate sample models; shared integration is a foundation
  prerequisite. The JSON import reader and authoritative backend are also pending.
- [center-public-profile](../add-reference-product-features/specs/center-public-profile/spec.md)
  already owns profile edits, media, timezone schedules and catalog revisions.
  [organization-verification](../add-platform-expansion-features/specs/organization-verification/spec.md)
  owns reviewed claims; [center-intake-services](../add-center-mediated-assistance/specs/center-intake-services/spec.md)
  owns service participation and current receiving eligibility.

## Goals / Non-Goals

**Goals:** Extend one canonical record and editor with explicit value meaning,
auditable declarations and a safe public projection. Keep catalog imports,
native clients and browser views consistent without manufacturing completeness.

**Non-Goals:** A new taxonomy engine, general-purpose form builder, beneficiary
registry, admission booking system, fee collection, financial audit, separate
verification workflow or production publication of the user's example.

## Decisions

### 1. Minimums And Typed Sections

Publication requires server-assigned/stable identity, a nonblank public name,
at least one supported category and a designated public contact route. A phone
or email, or an approved institutional website/social contact page, can satisfy
the route. Syntax validation and staff confirmation do not promise successful
contact or establish organization verification. A private name/phone does not
satisfy the public minimum. Removing the last route withdraws the active public
record into needs-review while preserving owned contribution history and staff
access. A new publication requires replacement contact or an explicitly approved
safe institutional route, never a copied private value.

Optional information is modeled in typed sections, with a small shared status
vocabulary (`known`, `not_informed`, `not_applicable`, `withheld`) and provenance
where required. A withheld public projection contains no value or sensitive
explanation. Freshness is separate: an old known value is not magically current.
Invalid supplied values fail validation; unknown is not a repair for bad input.
Do not turn every field into an unrestricted metadata map.

| Supplied field | Proposed representation and boundary |
| --- | --- |
| Category | Nonempty set of supported stable codes, existing OR filtering; no inference of service activation. |
| Name | Nonblank public display name; retain known proper spelling and existing localization fallback. |
| Legal name | Optional exact source string, distinct from display name; scoped edits do not transfer ownership. |
| Activities | Localized plain text and optional existing service references, not automatic permission to operate. |
| Hours | Existing authoritative schedule/timezone/exceptions plus explicitly scoped service schedules. |
| Recovery fees | Service-scoped terms described below, not a payment or donation record. |
| Address | Public address/coordinates only when designated; private operational location is stored separately. |
| Responsible person | Optional name and role with public-designation evidence; no account-role mapping. |
| Phone/email/social/website | Typed contact entries with public/private designation and last confirmation context. |
| Current beneficiaries | Dated, scoped aggregate reports with explicit measure and permitted public release. |
| New-beneficiary availability | Service-scoped dated declaration; no derived vacancies or admission reservation. |
| Ways to contribute | Descriptive accepted methods plus links to current needs/intake and eligible actions. |
| Source of resources | Multiple declared source types, optional explanations and as-of dates, not accounting evidence. |
| Verified | Read-only projection of scoped claims or unconfirmed legacy assertions. |
| Important/other attributes | Approved typed attributes with a featured flag controlling display order only. |

The proposed source-type vocabulary is donations, recovery fees, grants,
public support, own revenue, other and not informed. Labels are localized; no
source, percentage, donor identity or tax status is inferred. This is descriptive
funding information, not a ledger or financial-statement upload capability.

Alternative rejected: require every bold field, or fill absent values with false,
zero, a copied legal name or all-day opening. That would contradict the user's
decision and make incomplete records look more certain than the evidence.

### 2. Categories And Attributes Stay Bounded

Preserve the nine existing category codes and multi-category sets. The earlier
discussion of additional shelter, blood-donation and dispensary categories is
not silently implemented by this field-contract change. Keep unsupported source
labels in the private import review, not silently collapse them into a different
category. Broader classifications and population tags need their separate update.

Start attributes with explicit definitions for step-free access, appointment
required and referral required. These are useful proposed examples, not claims
about any real center. Each definition specifies type, allowed states, public
disclosure and an English/Spanish label. Add other approved definitions through
versioned configuration, not arbitrary executable schemas. Retain unknown booleans.

Featured means prominent, not required, independently verified or more truthful.
Short additional descriptions use bounded plain text and content review; reserved
fields cannot be reintroduced through attributes. Automated validation cannot
guarantee detection of all personal information in prose, so publication preview,
staff responsibility and a correction/reporting route remain necessary.

Alternative rejected: let arbitrary key/value attributes override core fields.
That bypasses fee types, claim authority, privacy and localization.

### 3. Fees, Counts And Availability Are Different Facts

Fee terms reference a service, status, amount/range when applicable, currency,
unit/period, conditions, optional waiver text and effective/review dates. Store
money in the existing integer minor-unit convention with the currency's scale;
do not assume every currency has two decimals. Reject negative or inverted ranges.
Case-assessed fees display contact-for-assessment rather than an invented amount.
Older terms remain identifiable as historical/stale; never charge from this section.

Beneficiary reports distinguish residents at an instant, distinct people during
a period and service encounters during a period. All require nonnegative counts,
units, scope, source and a valid date/period before being labeled current. Do not
sum overlapping services or infer people from meal portions. Undated imported
values remain private review material, not current public counters.

For the user's example, the source supplies a public name, two category labels,
a named representative and an undated claim of 31 girls. It does not establish
legal name, a public contact route, current capacity, official verification or
permission to publish the representative. Preserve that distinction. Use a
synthetic equivalent in tests; do not add the real names or claim to sample data.

Admission declarations use `accepting_inquiries`, `waitlist`, `unavailable` or
`unknown`, with service, conditions, as-of and validity. Optional vacancies are
separate declared quantities. No subtraction from beneficiary count is allowed.
When intake-service participation exists, its authoritative availability feeds
the profile; the profile must not keep a conflicting second availability flag.
General profile availability never bypasses actual contact/assessment gates.

Public beneficiary aggregates require disclosure review, particularly for
children or protected facilities. Counts are not a live census and do not trigger
gamification, rankings, financial confidence or public case discovery.

Alternative rejected: one center-wide capacity integer or one recovery-fee amount.
Different services use incompatible units, access terms and charging periods.

### 4. Project Public Data Before Discovery

Persist optional restricted contacts/location separately from the public record.
The public API, search index, downloadable catalog, shared cache and map adapter
receive the same allowlisted projection, not a full record with fields hidden by
the view. Extend the native/shared domain to allow no public location; never use
`0,0`, a random coordinate or a city-center point as the center's exact position.

List/map share filters over public data. Contact-only centers can match name or
category and an explicitly published service area. Without public coordinates
they have no exact distance, nearest ordering, radius membership or route. Show
them as location-unavailable matches, not nearer/farther based on hidden data.
Keep MapKit/Google Maps types in their adapters and pass public markers only.

Public sharing, print summaries, social previews, structured page metadata and
media derivatives must use this projection too. A protected location cannot leak
through an address string, query result or EXIF while its pin is hidden. Contact
links open only on user action, with supported schemes; no server-side fetch of
arbitrary URLs is needed to validate a profile.

Revoke/expire controlled caches and search projections on disclosure withdrawal.
Do not bundle protected records in offline assets. Previously exported public
data or third-party copies cannot be guaranteed recoverable; communicate that
limit and avoid promising retroactive secrecy. Authorized private access uses
the existing center-role policy and does not grant blanket case access.

Alternative rejected: filter sensitive markers only in Compose or SwiftUI. The
private data would still exist in API responses, local storage and search results.

### 5. Reuse Authority And Maintenance

Use the reference profile's expected-version updates, source ownership and audit
trail. Drafts can be incomplete; a publication transaction validates required
fields, permitted public content and all supplied typed values together. Use
operation IDs to reconcile a lost save response before retrying. Keep recoverable
input in the current authorized session and clear private input on sign-out or
role loss, without mixing another account's drafts.

Read-only verification uses the platform-expansion claim contract. A legacy enum
or checkbox is provenance, not evidence for a new active claim. A legal-identity
change must invoke the owning claim policy for dependent scopes; it never changes
recipient banking or bypasses independent review. General display-name changes
remain distinct from legal-entity/ownership changes.

Descriptions of accepted support link to existing needs and permitted intake
services. Hosted donations stay behind existing release gates. No new notification
category is introduced; profile-edit notices reuse the existing private inbox
when needed and contain no representative, beneficiary or protected-location data.

Alternative rejected: rebuild onboarding, payment authority or verification as
additional editable fields in the profile form.

### 6. Platform Responsibilities

- KMP: typed public models, validation, missing-state/freshness projection,
  service references and category filters; no native SDK types or private catalog.
- Backend: canonical fields, private storage, authorization, publication state,
  projections, versioning, claims/service joins and source-aware imports.
- Android: detail sections and contact-only discovery using shared state;
  accessible localized labels and explicit external-contact actions.
- iOS: equivalent SwiftUI sections after shared bridging, not another independent
  set of profile business rules or sample-data substitutions.
- Web: extend the canonical mobile-first profile editor and anonymous detail
  route with separate public preview, field errors and declaration dates.

Phone layouts start at 320 CSS pixels with 200-percent text, long Spanish/English
values, accessible names, keyboard/focus and real Safari/Chrome checks. Section
headings and missing values are data presentation, not decorative blank cards.
Use typed controls: multiple selection for categories, date/time inputs for dates,
selectable unknown states, numeric/currency fields for fees and safe link editors.

## Risks / Trade-offs

- Incomplete minimums hide legacy public entries -> retain IDs/history privately
  for review; never invent contact or silently delete records.
- A declared number looks verified -> label source, measure/date and claim scope;
  optional data stays optional and stale information stays visibly stale.
- Sensitive data leaks through derived fields -> test projections, search order,
  caches, URLs, exports and public media, not only rendered text.
- Arbitrary prose contains private information -> bounded fields, designated
  publication and human review/correction; no perfect automated-redaction claim.
- New optional location breaks older clients -> version public contracts and
  provide a documented upgrade path without exposing a private fallback.

## Migration Plan

1. Deliver the relevant foundation/profile prerequisites and introduce versioned
   directory sections behind an inactive publication gate. Add synthetic contract
   fixtures; no live center or person from the conversation is imported.
2. Stage existing records by stable ID. Map absent optional fields to not informed;
   quarantine invalid supplied values or missing publication minimums for review.
   Preserve contributions, private history and newer staff edits transactionally.
3. Publish only approved public projections. Replace mandatory public-coordinate
   assumptions in shared discovery, native markers and browser clients together.
   Clients unable to understand the new shape get a safe supported response or
   explicit upgrade requirement, never fabricated coordinates or private fields.
4. Reconcile the foundation's pending `kindness-center-discovery` delta before
   archive: apply that stage first, then this change's two modified requirements.
   Preserve its separate real-map requirements and do not overwrite these privacy
   exceptions through a later archive of the older change.
5. Rollback disables new profile publication/edits while retaining last safe public
   projections, current disclosure restrictions and private revision history.
   An older serializer must not republish withdrawn fields or coarse verified flags.

Release configuration still needs reviewed availability/fee freshness windows,
attribute bounds, disclosure rules and accountable review/support owners. These
are policy values within the defined contract; missing configuration leaves the
affected declaration unconfirmed or private rather than selecting a demo default.
