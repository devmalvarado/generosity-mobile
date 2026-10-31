## Context

See [proposal.md](proposal.md) for scope and user decisions. This follow-up
depends on the active foundation, not only its seven synchronized main specs.
The foundation includes planned backend/auth/sync/portal work that is still
unimplemented. Its visual-only pass keeps old routes; this change intentionally
introduces functional destinations later. Neither change is ready to archive.

Source inspection on 2026-10-25 found:

- `shared/src/commonMain/kotlin/org/generosity/domain/Models.kt` has centers without photos/hours, profiles without avatar/join date, three notification types and placeholder point fields. There are no review, favorite, event, inbox or award-ledger models.
- `ContributionModels.kt` separates offered/confirmed/fulfilled states. Impact currently counts some entries rather than measured duration; the foundation already specifies correcting units.
- Android `ui/GenerosityApp.kt` has Map/List/Profile/Settings and sample sign-in; SwiftUI `ContentView.swift` has the same destinations but its own sample data.
- Each platform has two smoke tests for anonymous discovery/language/details and guest profile, not coverage of these new functions.
- The external landing has static forms and no operational backend. Its media/review/event management screens do not exist.

## Goals / Non-Goals

**Goals:** Add cohesive feature contracts on the existing shared/native/server
boundaries, reuse the foundation's authority and persistence, keep guest
discovery useful and make every visible control resolve to a real outcome.

**Non-Goals:** New UI/backend frameworks, microservices, native center
administration, public role self-enrollment, direct card processing, tax
receipts, redeemable rewards, rankings, recurring/paid events, waitlists, chat
or production charges during implementation tests. Review moderation is a
scoped operation, not a general admin console.

## Decisions

### One Backend and Native Shared Contracts

Extend the foundation's versioned API, transactional store and outbox rather
than creating a portal-specific database. Add stable domain IDs/repositories
in KMP; Android and iOS consume the same state. Native layers own maps, sharing,
associated links, accessibility and provider-browser handoff. Web staff views
use the same API. Server validation always repeats client validation.

New durable domains: favorite relations, public media/schedules, review
revisions/moderation reports, award evidence/ledger, inbox/read state,
events/enrollments/attendance and provider intents/payment adjustments.
Private account sync and public catalog revisions remain distinct. Additive
fields carry schema/version compatibility; older clients receive a supported
shape or a documented upgrade response, not an ambiguous enum crash.

Alternative rejected: treating all additions as fields on `Contribution`.
Attendance, payment and review moderation have different owners and state
machines. Link their stable IDs instead, and build read-only history/impact
projections without counting a linked activity twice.

### Functional Navigation After Styling

Deliver Map/Needs/Impact/Profile with the center plus action as an operation,
not an empty fifth tab. Map retains a map/list segmented mode. Profile contains
settings, saved centers, upcoming/past activities and history; Impact presents
metrics, points and all achievements. Profile may keep a compact points/level
summary so its existing points contract remains true. Both read one source.

Need cards open existing need/center detail. View all from center detail applies
an explicit center constraint to the feed; the main helping action and method
buttons retain that center while the user chooses a compatible current need.
Plus offers published-need discovery
or personal help, with sign-in only when required for a write. The bell opens
the inbox. Events are a visible secondary entry in Needs and center details,
not a new top-level tab. Keep a back stack and per-destination filter/scroll
state; migrate prior List/Settings entry state to their new locations.

Alternative rejected: removing old routes before new ones exist. Activate the
new shell only after its routes and honest empty/error states pass native tests.
The foundation's visual preservation requirements govern its intermediate
stage; `mobile-product-navigation` governs this subsequent stage.

Account cards add authoritative creation month/year and optional provider or
approved avatar metadata, with initials fallback. No separate personal-avatar
upload feature is needed to satisfy this image. Review aliases are separate
public content and are not copied from private identity without consent.

### Feed and Favorites

Feed query parameters include mode, search, categories, optional origin/radius
and cursor/revision. Shared code owns mode semantics, local cache projection
and labels; the server owns complete result pagination and current eligibility.
Initial Nearby radius is a configurable 5 km, displayed to users; All/Urgent
do not silently acquire the radius restriction. Location query logs must redact
coordinates, and no continuous location history is stored.

Favorite writes set desired state with expected version and an operation ID,
not a toggle command. This prevents lost-response retries reversing a choice.
Keep guest storage distinct, offer an explicit deduplicating import after
sign-in, and never relabel an old account's queued writes. A stale multi-device
write shows current state and allows explicit reapplication.

### Public Profiles, Media and Sharing

Extend the catalog with optional cover asset metadata, weekly hours, date
exceptions, timezone and field provenance. Center-owned edits are versioned
and protected from stale imports. Use a proven timezone library for overnight
and offset-boundary evaluation, not hand-written offset arithmetic. Incomplete
hours mean unknown; date exceptions override weekly intervals.

Start with one cover photo per center, no gallery. Upload through bounded,
authenticated processing: validate real image bytes/type/size, reject active
formats, strip metadata, create safe public derivatives and keep source files
nonpublic. Record rights and alternate text; a safe decode failure preserves
the old asset. Do not fetch arbitrary user-supplied remote URLs as an ingestion
shortcut. Public pages and native apps use the same approved derivative.

Sharing uses an owned/configured HTTPS domain and native associated-link
configuration, with public mobile center details as fallback. No domain is
assumed deployed. Validate host/path/ID; query parameters cannot grant access.
Native share cancellation is not tracked as successful delivery or impact.

### Independent Review Moderation

Eligibility is one or more center-validated completed nonmonetary activities,
including event attendance normalized into a linked contribution. Payment
success alone and self-reported activity do not qualify. One current review
per user/center uses versioned pending/approved/rejected/hidden/withdrawn
revisions. Keep the last approved revision public while an edit is pending;
author withdrawal removes all public revisions. Moderator restore applies
only to policy-hidden content, never an author's withdrawal.

Provide a minimal mobile-first moderator queue in the portal under a distinct
operator-provisioned role. Center assignments do not imply that role, and a
moderator cannot decide reviews for a center they manage. Record decisions and
reasons. Public averages/counts update in the same transaction as visibility;
reports are private, rate-limited and do not trigger automatic takedowns.

Document content rules, reporting, author appeal/contact and an accountable
operator before launch. If no moderator is available, new submissions stay
pending rather than bypassing review. No automatic sentiment-based rejection.

### Initial Gamification Policy

The user approved eligibility, not numeric rules. The following is a concrete
proposed v1 policy for implementation review; production enablement must record
the accepted version. Different approved thresholds remain data, not new code
or a change to the eligibility contract.

| Item | Proposed v1 behavior |
| --- | --- |
| Qualifying activity | Center-validated completed time/goods help or attended event, deduplicated by activity ID. Exclude money, personal declarations and engagement actions. |
| Points | 10 points for a calendar day with at least one qualifying activity; cap at one award per account/day, independent of quantity, duration, centers or spending. |
| Levels | Thresholds 0, 50, 150, 300 and 600 points for levels 1-5, with localized descriptive labels and no moral/financial ranking. |
| Achievements | First qualifying activity; 10 distinct qualifying days; 3 distinct centers helped; a 7-day qualifying streak. Each has a stable ID and explicit criterion. |
| Streak | Consecutive qualifying local dates; current remains active if the latest is today/yesterday, otherwise zero; keep best historical length. |
| Chart | Last 30 local calendar days of awarded points, labeled with period and an accessible textual summary; a correction changes the affected bucket. |

Use a ledger of source/rule/date awards and corrections. Process a completion
once even if event attendance and contribution messages both arrive. Determine
the activity date from staff-validated actual occurrence time, not the client's
device clock or a later confirmation date. Record the account timezone schedule;
initially use its selected IANA zone or explicit UTC fallback. Zone changes take
effect at the next day boundary, never rewrite earlier dates, and cannot create
a second daily award. Recompute on late evidence or controlled invalidation.

Existing time/goods lifecycle remains terminal after fulfillment. Invalidating
award evidence appends an award correction, not an undocumented contribution
transition. Restrict this action to an auditable operational role. Private
points are neither money nor proof of official center verification.

### Inbox and Event Delivery

Use the existing durable event outbox to derive inbox records and separately
attempt opted-in push delivery. In-app transactional notices do not depend on
OS push permission; optional urgency/proximity/reminder categories still honor
their preferences. Use explicit event-notification preference for event push.
Read writes are idempotent desired-state operations. Nearby guest notices stay
local and do not turn into server location records.

Proposed inbox retention is 90 days, configurable and disclosed; expiring an
inbox row does not remove its contribution/payment record. Query read counts
from retained authorized rows. Deleting/retrying events must not create negative
badges or leak private notices on account switch. Older links resolve current
cancelled/deleted target state rather than resurrecting actions.

### Volunteer Events and Attendance

Use separate event and enrollment records. Draft/published/cancelled/completed
event state and enrolled/cancelled/attended/no-show enrollment state are not
donation statuses. Reserve seats transactionally; one active enrollment per
account/event. Allow owner cancellation until event start. With enrollments,
time/venue changes require cancel-and-replace with no automatic transfer.
This avoids an underspecified rescheduling/reconfirmation workflow in v1.

After the event ends, assigned staff validates actual attendance/duration.
Create one linked time contribution with audited offered/confirmed/fulfilled
transitions under that staff action, not by trusting an attendee check-in.
Reuse its eligibility ID for points and reviews; the event and contribution
must not become two hours entries. No-show duration is zero and earns nothing.
Roster data is center-scoped and private. Event changes create durable inbox
notices and cancel stale reminder schedules on device reconciliation.

### Provider-Hosted Donations and Financial Authority

Provider selection remains open. Implement a single provider adapter behind a
contract, with provider-hosted checkout and controlled eligible-recipient IDs.
Do not implement a wallet, hold funds, collect card data or accept a client
recipient bank account. Provider onboarding/recipient readiness is distinct
from the app's official-verification label and requires a controlled operator
process. Identity, supported regions/currencies, known fees and the recipient
must be clear before the user explicitly enters checkout.

Persist a donor intent before session creation; retain its idempotency key
across retries and verify server-side amount/currency/recipient binding. The
return URL only opens a status view. Verify authentic provider events and
reconcile current provider state before updating a payment ledger. Duplicate
events do not repeat accounting; late success after UI cancellation remains
discoverable. Authenticated receipt access must not leak between users.

Use a financial state machine distinct from center fulfillment. Store integer
minor units with currency metadata, immutable payment/adjustment history and
provider correlation, not raw credentials. Currency totals group separately:
verified paid principal less refunds and disputed principal, without subtracting
the same portion twice; cap reductions at that payment's principal. Provider
settlement/fees are distinct and unknown values remain unknown. A dispute's
final resolution restores or excludes its amount once. No spending-based points.

Hosted checkout does not by itself prove legal, provider or store readiness.
Record recipient eligibility, terms, refund/dispute ownership, supported
regions/currencies and required approvals before enabling production. Use only
sandbox transactions in implementation tests. A kill switch stops new intents
but leaves reconciliation and historical access running. Refund execution can
initially use authorized provider operations; Generosity must reconcile results
and offer a support route without claiming every request will be approved.

Security reference: server-owned amounts and verification are supported by
[OWASP payment integration guidance](https://cheatsheetseries.owasp.org/cheatsheets/Third_Party_Payment_Gateway_Integration_Cheat_Sheet.html).
Authenticated event verification and retry handling are illustrated in
[Stripe's webhook documentation](https://docs.stripe.com/webhooks).
These are implementation references, not a decision to select Stripe.

## Risks / Trade-offs

- Visual-only and functional navigation conflict -> Explicit staged precedence, state migration and new routes tested before activation.
- More catalog consumers expose stale data -> Stable IDs, revision-bound pages, atomic ownership-aware writes and honest offline indicators.
- Moderation becomes a social responsibility -> Separate role, content policy, queue ownership, reporting/appeal and no unattended auto-publication.
- Point farming or delayed confirmation -> Daily caps, center evidence, immutable rule versions and occurrence-date reconciliation; no payment rewards.
- Payment events arrive late/out of order -> Durable intents and event IDs, provider reconciliation, adjustment ledger and an operational disable switch.
- Expanded scope outruns the unfinished foundation -> Deliver independent slices only after their prerequisite contracts pass; do not mark placeholder UIs complete.

## Migration Plan

1. Complete relevant foundation authority, persistence, native parity and portal tasks; publish additive API contracts and database migrations with compatibility tests.
2. Add public profile/feed/share/favorite contracts and clients, then reviews with staffed moderation. Keep all new states unavailable rather than simulated when dependencies are missing.
3. Add events, normalized attendance, the award ledger and inbox. Verify cross-device IDs, calendar boundaries and all ownership checks with isolated fixtures.
4. Enable the new native navigation after destinations pass small-phone/localization/accessibility tests; migrate existing list/settings/history access and preserve saved preferences.
5. Implement monetary flow in sandbox only. Production remains disabled pending the separate provider/recipient/operational release gates.
6. Roll back new entry points with capability configuration if necessary, preserving durable data, old read routes and payment-event reconciliation. Do not drop tables or erase accepted activity to revert UI.

## Deferred Selections

Before the relevant implementation/release tasks, record the selected provider,
public link domain, media storage/runtime limits, assigned moderator/support
operators and accepted configurable policy values. Provider selection must be
capable of these contracts; otherwise revise the spec instead of silently
weakening verification. No production accounts or external deployments were
created by this planning work.
