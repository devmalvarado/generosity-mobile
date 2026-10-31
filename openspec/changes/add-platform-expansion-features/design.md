## Context

See [proposal.md](proposal.md) and the [scope review](../../../docs/scope-review.md).
This repository still has sample/in-memory native state and no implemented
backend or canonical portal. `Models.kt` has a coarse verification enum and
placeholder point fields; it cannot represent issuer evidence, certificates,
regional comparisons, tax records or tickets. Existing event, payment, award,
moderation and community contracts are active plans, not ready services.

The user's 2026-10-26 choices add recognition PDFs/badges, aggregate geographical
comparisons and a multi-country fiscal framework with issuance disabled until
configuration. This design describes staged extensions; it does not activate
production payments or override personal-data protections.

## Goals / Non-Goals

**Goals:** give deferred product functions testable ownership and lifecycle
contracts; reuse existing domain services; separate community recognition,
organization claims, fiscal documents and commercial tickets.

**Non-Goals:** transferable money-like points, named-person leaderboards,
unrestricted surveillance/admin access, scraped private data, automatic center
closure, a general social network, or rebuilding backend/UI frameworks. Generic
form builders, microservices and endless recurrence are implementation expansion,
not necessary product features. The scope review records these distinctions.

## Decisions

### 1. Phase Precedence Without Rewriting History

All new capabilities are additive to seven baseline specs and depend on the
active foundation/reference/community plans. Earlier exclusions apply to their
phases. Make precise exceptions rather than erase constraints:

| Earlier rule | Later extension | Still unchanged |
| --- | --- | --- |
| Controlled staff provisioning only | Public application/claim plus independent reviewed membership | Signup never grants a staff role |
| Verification outside foundation | Issuer-scoped review and lifecycle | Visits/ratings are not verification |
| Native consumer needs are read-only | Explicit server-authorized staff workspace | Public/organizer mode cannot edit center content |
| No leaderboards in reference phase | Opted-in aggregate territorial comparisons | No named-person, spending or home-address ranking |
| Points are non-financial | Earned PDF/badge recognition | No cash redemption or purchased eligibility |
| One-off/free volunteer events | Bounded series, waitlists and gated ticketed admission | Buying a ticket is not helping evidence |
| No tax receipts in foundation | Country/issuer/provider-gated fiscal adapters | Ordinary payment receipt or certificate is not a tax document |
| Manual imported catalog | Authorized scheduled connectors | No unlicensed harvesting or silent staff-data overwrite |

Do not synchronize or archive planning deltas before their implementations and
tests. Main specs are unchanged by this planning task.

### 2. Separate Membership, Verification and Payment/Fiscal Authority

Store application/claim records independently from published centers. Applicant
contact verification proves a contact channel, not representation. Review can
request changes, approve a bounded membership, reject or resolve a conflict.
Acceptance creates a scoped membership invitation; existing catalog IDs and
ownership remain stable until explicit approval. Owners can invite permitted
staff, but ownership transfer/recovery needs independent review. No mandatory
tax status for a community center merely to exist in the catalog.

A verification claim has issuer, scope, criteria version, evidence references,
effective dates and status. Do not treat the old `OFFICIALLY_VERIFIED` enum as
proof; unknown imported provenance becomes unconfirmed until reviewed. Native
and browser projections show the same public claim metadata. Organization
review is distinct from payment-provider seller/recipient checks and fiscal
issuer eligibility, each with independent gate state.

Private organization evidence uses bounded validated/scanned uploads, separate
storage, short-lived authorized downloads, audit and retention. These are not
household uploads or public center photos. Two-person review applies to sensitive
membership/verification overrides under an explicit policy; applicants and
represented staff cannot review themselves. Expiry/revocation updates public
projections and dependent gates, not historical contributions. Appeals are
recorded without automatically restoring privileges.

### 3. Operations Reuses Domain Commands

Use the existing portal shell for permission-scoped queues: support/abuse,
onboarding/verification, connector runs, privacy rights, fiscal/provider
reconciliation and role provisioning. Require strong sessions and reauthentication
for consequential operations. A support role is not universal case access.
Use independent approval for sensitive role/recipient configuration changes.

Mutations go through domain commands with expected version, actor, reason and
operation ID. Never edit database point balances or provider-paid flags from
an admin form. Corrections use the evidence ledger and invalidate dependent
credentials/aggregates; refunds reconcile through providers. Reports are evidence
to assess, not automatic proof of fraud. Distinguish closing a public listing
for an authorized operational reason from the forbidden automatic geographic
recommendation to close a real center.

No silent impersonation. Scoped audited exports omit credentials and unrelated
case content. Gates can stop new activity while cancellation, refund, rights,
unsubscribe and history remain available. This applies least privilege and
per-request checks described by [OWASP authorization guidance](https://cheatsheetseries.owasp.org/cheatsheets/Authorization_Cheat_Sheet.html).

### 4. Authorized Integration, Not General Harvesting

Connector configuration names allowed interface/license, owner, fields,
provenance, schema version, endpoint allowlist, credentials and limits. Stage
each fetch and distinguish complete, partial, unchanged, failed and deliberately
empty results. Publish validated revisions atomically using stable source IDs;
manual staff ownership and live commitments take precedence. Handle withdrawals
and duplicates by reviewed reconciliation, not delete-and-reimport.

Start with licensed catalog sources and consented scheduling imports. An external
Meal Train connector is only a candidate: no supported API or partnership is
assumed. Without a permitted interface it remains disabled; a supported supplied
file can be reviewed instead. Never export private recipients or rosters through
catalog synchronization. Schedule imports produce drafts, not reservations.
Use bounded established parsers, safe text rendering, rate/backoff limits and
server-side endpoint validation to avoid arbitrary fetch/execution. Disablement
preserves local history and follows source-retention obligations.

### 5. Recurrence and Waitlists Share Inventory

Use an established recurrence/timezone library implementing a bounded supported
subset of [iCalendar recurrence](https://www.rfc-editor.org/rfc/rfc5545), not a
home-grown date incrementer. Store rule/version, IANA zone, count/end limit,
exceptions and generation horizon. Preview concrete occurrences, resolving DST
ambiguity before publication. Stable occurrence identity survives retries; a
changed committed time/venue uses explicit cancellation/replacement.

A waitlist is occurrence-specific. Order by accepted server timestamp and stable
ID. Offer the next eligible participant a bounded seat hold before admitting
new direct reservations. The same database transaction constrains confirmed
enrollments, unexpired offers and checkout holds. Expiry/decline releases exactly
once; acceptance rechecks current authority and ticket requirements. One active
participant position per occurrence includes claimed assisted duplicates. Queue
and hold deadlines are configurable and visible, not selected silently here.
No paid/point-based queue jump. Assisted users receive equivalent coordinator
contact and deadlines, not a requirement for push access.

### 6. Contextual Communication and Native Staff Mode

Implement participant-to-assigned-coordinator threads and organizer announcements,
not an unrestricted person-search/direct-message network. Replies to broadcasts
stay private. Bound inert text and safe links; exclude arbitrary attachments in
the first implementation. Sensitive meal/case data stays behind its existing
access-checked route rather than being copied into chat or lock screens.

Sending uses idempotent IDs and explicit pending/acknowledged state. Notification
delivery reuses inbox/outbox and separate opt-ins. Add mute/block/report and
moderation with private evidence/appeals; retention and staff reassignment remove
access under policy. Do not claim end-to-end encryption when reported content
is reviewable. Essential cancellation/support is independent of blocked chat.

Native staff mode is an explicit role/active-center route. Shared KMP exposes
contracts/state/validation; Android and SwiftUI render forms using the same
backend authority as the portal. Scope includes needs, profile/hours/media,
events and contribution actions. Web-only reviews can open safe authenticated
routes without token-bearing share URLs. Never infer staff rights from community
organizer points or cached membership. Both platforms test conflicts/revocation,
separate from merely compiling a shared interface.

### 7. Fiscal Documents Are Jurisdiction Adapters

Store document request, issuer, jurisdiction, effective policy version, protected
required fiscal fields, donation evidence and provider correlation. Versioned
adapters determine required formats and issuance/correction conditions. No
country is enabled initially; language or donor profile geography does not
select a legal entitlement. Authorized fiscal review must approve the exact
issuer/provider/rules before collecting unnecessary identifiers or issuing.

Distinguish donation receipt from payment receipt, recognition certificate and
event-ticket invoice. No assumed deductibility of volunteer time or arbitrary
goods valuation. Document types and valuation evidence are jurisdiction-specific.
Authoritative provider acknowledgment moves a request to issued; uncertain
timeouts reconcile before retry. Retain immutable issued artifacts with current
cancelled/replaced linkage; a refund is not automatically fiscal cancellation.
Protect signing keys/provider secrets server-side and use private downloads.

The [SAT's 2026 donation CFDI material](https://www.sat.gob.mx/minisitio/DonatariasAutorizadas/documentos/CFDI_Donatarias2026.pdf)
and [IRS acknowledgment guidance](https://www.irs.gov/charities-non-profits/charitable-organizations/charitable-contributions-written-acknowledgments)
illustrate different documentation frameworks. They are research references,
not sufficient jurisdiction configuration, legal advice or authorization to
issue in either country. Provider selection, fiscal review and production
approval remain separate release work.

### 8. Recognition Credentials Are Not Money or Accreditation

Read canonical eligible evidence from the existing award/achievement ledger.
Issue one credential identity for owner/criterion/evidence scope/rule version;
render localized PDF, badge and equivalent accessible HTML from that record.
Use established PDF/QR tooling with safe templates, bounded inputs, no external
URL fetch from user text and actual issuer authority. Do not add points for
generating/downloading credentials. Assisted history requires verified account
association and independently validated evidence before credential eligibility.

Credentials default private. Public sharing requires explicit field selection;
QR links use opaque non-enumerable IDs and disclose minimal approved metadata,
never source participant/case details. Verification is a server lookup of current
status, not proof from trusting a PDF's text. Corrected evidence revokes/replaces
the credential and updates verification without pretending previously downloaded
copies disappear. State clearly that a helping recognition is not professional
accreditation or a tax receipt. Monetary or transferable redemption is not
requested by the user's certificate/badge clarification.

### 9. Regional Comparisons Use Observed Helping, Not Home Locations

The initial proposed primary metric is unique consenting accounts with eligible
completed help in the selected region/period. Secondary views are distinct
activities, actual hours and separately compatible goods units; never a sum of
money, trees, hours and people. Publish the selected method/version. Geography
comes from the authoritative event location or supported center service area,
with provenance and manual reviewed correction where needed. Do not use home
address/GPS trails; missing or ambiguous location goes to unattributed totals.

Use country-qualified postal strings and country/locality-qualified neighborhood
IDs with boundary-source versions. Labels distinguish physical event location
from center-supported area, especially for remote help. An activity cannot
silently select whichever location improves a score. Counts are distinct within
each region; a person active in two neighborhoods counts once in each but once
in their containing country's total. Overlapping postal/neighborhood regions
are alternative partitions, not quantities to sum. Use common timestamp bounds
for compared regions and display the period timezone.

Offer raw volume and, only with a matching licensed population source, a separate
rate per disclosed population unit. A proposed rate is participants per 1,000
residents, labeled as recorded activity at that place, not a participation rate
of those residents: helpers may live elsewhere. Geographic activity density is
not a moral score. Denominator version/date and selection bias remain visible;
do not normalize unsupported postal areas by invented populations.

Public aggregation is opt-in without affecting personal points/recognition.
Apply distinct-person minimums, fixed approved periods/filters, complementary
suppression and temporal-differencing checks to tables, maps, exports and top-N
lists. Small/unknown regions are unavailable, not bottom-ranked zeros. Reuse
the community aggregate-release protections, but use validated helping events,
not household survey responses. Never infer need or service coverage from these
rankings; territorial coverage analysis answers a different question.

Recompute on evidence corrections, consent withdrawal, canonical association or
boundary changes. Record stable ties by public region ID under a published
method. No individual names, spending ranks, paid boosts or organizer permission
changes based on regional placement. This explicitly extends the prior
no-leaderboard phase only for aggregate places.

### 10. Paid Admission Is a Separate Provider Purpose

Use the payment-provider boundary but distinguish `donation` from `event_ticket`
intents and seller readiness. Never enable ticketing merely because donations
are configured. Prices/fees, terms, currencies and cancellation rules are public
before checkout; no live charge without separate approval. Initial scope is
one authenticated buyer/participant seat per occurrence, no resale, wallet or
unreviewed payment-on-behalf collection.

Create a bounded atomic inventory hold, then an idempotent hosted checkout.
Validate provider success server-side against the intent and active allocation.
If success arrives after released capacity or cancellation, retain the actual
financial result and initiate visible refund/support reconciliation; never add
an extra seat or call a refund successful before provider confirmation. All
duplicate/out-of-order events converge on one ledger/ticket identity.

Tickets use opaque private references and current server check-in status. Online
check-in is required initially: offline UI cannot guarantee replay prevention.
Cancellation blocks admission and sales, reconciles refund/dispute policy and
preserves history. Ticket receipts are not charitable tax documents. Paid
attendance is not the volunteer-event validation path: only separate actual
independently validated helping can earn points/certificates/regional credit.

## Risks / Trade-offs

- Expanding roles increases attack surface -> object-scoped backend permissions, strong operator sessions, independent sensitive decisions and role matrix tests.
- Verification badges can overpromise -> explicit issuer/scope/expiry and independent payment/fiscal gates.
- Regional comparisons favor large or better-recorded areas -> raw versus normalized views, opt-in/sample limitations, suppression and no moral labels.
- Credentials can outlive corrections -> current verification status, revocation links and no promise to recall external copies.
- Recurrence and delayed payments race capacity -> one inventory transaction, bounded holds and adversarial reconciliation tests.
- Fiscal rules vary/change -> no enabled country by default; reviewed versioned adapters with a disable switch and continuing retrieval.
- External data can corrupt owned records -> staging, source ownership and controlled withdrawal/merge, not blind scraping.

## Migration Plan

1. Land foundation/reference/community domain prerequisites; preserve current IDs and seven baseline specs.
2. Add applications, claims and operational role scopes; migrate ambiguous legacy verification to unconfirmed metadata rather than invent provenance.
3. Add connectors, bounded occurrences, waitlists, communication and native staff routes with mixed-old/new record tests.
4. Enable private credentials against synthetic then validated evidence. Publish regional comparisons only after geography, methodology and disclosure policy review.
5. Keep fiscal issuance and ticket charging in sandbox until distinct issuer/provider/jurisdiction/operational approvals and replay/refund tests pass.
6. Roll back new action entry points while keeping accepted history, cancellation, rights, credential status and financial/document reconciliation operational.

## Required Release Selections

Role/proof review policies, moderation staffing, retention, approved data sources,
recurrence horizon/offer lifetime, geography/boundary/population sources, aggregate
privacy thresholds, credential templates/issuer scope, payment sellers and fiscal
country/provider policies remain launch-blocking configuration. These choices
do not authorize real verification claims, data imports, charges or tax emission
during planning. All screens must pass mobile-first ES/EN accessibility checks.
