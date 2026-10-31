## Context

See [proposal.md](proposal.md) for the motivation and approved product choices.
This is a third active planning change, not an implementation. Source inspection:

- `shared/src/commonMain/kotlin/org/generosity/domain/Models.kt` has sample center categories, point fields, needs and contributions, but no campaign, case, service-capacity or consent models.
- `domain/Repositories.kt` has basic center/user/contribution contracts, not a server authorization boundary or sensitive case store.
- `domain/Geo.kt` calculates great-circle distance for discovery. `ContributionFlowTest.kt` checks a roughly 300-meter example; neither establishes network accessibility or territorial coverage.
- The [reference change](../add-reference-product-features/design.md) plans center-owned events, center-only awards, inbox and native routes. They are prerequisites, not installed services.
- The older static landing is assessed in [center-portal.md](../../../docs/center-portal.md). Canonical `portal/` and the backend do not exist yet; do not fork that prototype into a second operational system.

## Goals / Non-Goals

**Goals:** one event/identity/award system; community organizing as earned scoped
access; useful privacy-preserving service comparisons; native participation and
field capture with a genuinely phone-usable management portal.

**Non-Goals:** a generic campaign-builder framework, social feed/chat, automatic
recurrences, individual risk scoring, external Meal Train integration, new
payment flows, certification of organizers or automatic allocation of aid to
identified people. No runtime code or production data is changed here.

## Decisions

### 1. Extend Ownership, Not the Event Engine

Add a campaign with owner account, activity kind, coarse area, responsible
contact, optional accepted partner center, version and lifecycle. Its concrete
sessions reference the existing event contract with an explicit owner kind:
`center` or `community_campaign`. Existing center-only publication applies to
the former; organizer eligibility and assignment apply to the latter. Center
staff authority is never inferred from campaign membership or a typed name.

Reuse the event capacity transaction, enrollment IDs, replacement rule,
contribution linkage and inbox outbox. Material commitments are quantity-based,
separate from one-seat attendance, with a single operation identity for retries.
Meal slots add portion and recipient rules rather than reusing seats as meals.
Do not auto-migrate center events into campaigns or change existing enrollments.

Campaign state: draft, published, paused, cancelled, completed. Each session
retains its own dates and state. Completing a campaign requires future sessions
to be resolved, but never invents attendance, delivered materials or outcomes.
Pause/eligibility hold blocks new commitments and future execution pending
coordinator resolution; notify existing participants and meal recipients.
Cancellation closes future slots and reminders while preserving past records.
Resumption revalidates dates and reconfirms affected commitments; material
time/location changes still use explicit cancellation/replacement.

Alternative rejected: a second event service with independent capacity and
attendance. It would risk overbooking and double awards for the same activity.

### 2. Earned Eligibility Is Not a Universal Trust Role

Use a versioned server policy:

`registered AND active AND level >= L AND valid_points >= P AND distinct_validated_help >= H`

`L`, `P` and `H` have no selected numeric values. All are required; the policy
operator must approve them and effective dates before enabling creation. Level
and points come from the reference award ledger; qualifying help uses canonical
activity IDs, not event/contribution duplicates. Awards are not spent or traded
for access. Show private progress and the active rule version. No per-user
secret exceptions or paid shortcut. Existing center-led help provides the
initial qualification path, without needing a community organizer first.

Recheck on protected writes and when evidence, policy or suspension changes.
An eligibility projection may speed reads, but must not authorize a stale
queued publication. Preserve cancellation and controlled transfer to an eligible
account that accepts; a safeguarding operator handles restricted accounts.

Minimal authorization boundaries, enforced per object on the backend:

| Actor | Allowed scope | Not granted by that role |
| --- | --- | --- |
| Registered participant | Own enrollments, material/meal commitments, private assigned instructions | Publishing, case access, validation |
| Eligible organizer | Own campaigns, assigned sessions, minimal roster, reported outputs | Center authority, all household records, analyst access |
| Accepted partner center staff | Independently validate actual help for partnered activities; own service-capacity records | Their own or organized activity validation; unrelated cases |
| Assigned collector | Approved questionnaire and bounded assigned fieldwork lease | Bulk case search or territorial raw-data export |
| Assigned case worker | Explicit case fields and consented referral subset | Other campaigns/centers or unrestricted analysis |
| Data steward | Protocol/assignment/disclosure approval, retention and rights workflow | Automatic unrestricted case-content access |
| Scoped analyst/reviewer | Approved aggregate inputs, scenarios and decision records | Contacts, precise household coordinates or live catalog changes |
| Safeguarding operator | Reports, organizing restrictions, appeal and controlled handover | Unrelated sensitive case contents |

Operator provisioning is controlled by the existing backend authority. Build
only these narrow operational queues, not a general-purpose admin suite. The
same person can hold multiple roles but permissions remain object-scoped and
conflict-of-interest checks still apply. Points alone are insufficient for field
collection; an approved protocol and trained assignment are additional gates.

### 3. Activity Plans, Outcomes and Independent Evidence

Use stable codes and type-specific required fields, not an open-ended form
designer. Keep public and private sections separate at the API level.

| Activity | Plan | Reported output | Later outcome |
| --- | --- | --- | --- |
| Reforestation | Site/permission status, planting plan, materials, maintenance responsibility | Trees planted by cohort and date | Survival observations at stated dates, without inventing survival rates |
| Cleanup | Work boundary, equipment/safety constraints, waste destination | Collected amount with unit and measurement method | Disposal confirmation with provenance, not assumed from collection |
| Coordinated meals | Approved recipient plan, dated windows, portions and assigned donors | Donor-reported delivery | Recipient receipt, separately labeled |
| Needs assessment | Approved questionnaire, voluntary consent and trained team | Usable consented responses and quality metadata | Consented referrals/follow-up outcomes, not survey-count rewards |

Site permissions and safety readiness are coordinator attestations or approved
protocol evidence, not a government permit generated by Generosity. Initial
evidence is structured, bounded and attributed; exclude sensitive photographs
or unrestricted upload galleries. A claim, attendance and verified result must
remain different facts.

Community reports without an accepted center partner remain unvalidated.
Independent authorized partner staff can validate actual time/goods help and
reuse the canonical contribution/evidence identity. The validator cannot be
the participant or the organizer of that activity, even with dual roles.
Meal receipt and survey counts do not become points. Legitimate fieldwork time
can qualify only through the same independent center-validation path, never
through the number or sensitivity of household responses. Do not merge
operational outputs into gamification or claim that an environmental output is
a proven long-term benefit.

### 4. Meal Support Is a Private Scheduling Workflow

A recipient can approve and manage support without creating an app account:
use a named coordinator's documented consent/rights channel, or an authenticated
recipient route when available. Record how authority/identity was checked;
do not treat an organizer's unchecked checkbox as recipient consent. No public
recipient lookup. A representative's authority is recorded without collecting
identity-document images in this first version.

Store public campaign summary separately from private recipient identity,
instructions and consent. Slots have a concrete date/window/timezone, portion
unit and remaining requirement. Reservations use atomic quantity checks and
idempotent desired-state commands. Statuses distinguish reserved, donor-reported
delivery, recipient-confirmed receipt, cancelled and missed; coordinator proxy
confirmation is labeled. Late claims do not erase prior cancellation history.

Delivery details require recipient/coordinator approval of the assigned donor,
not just a slot reservation. Until approval, show pending coordination; the
coordinator can decline/release the commitment without revealing private data.
Fetch details through authenticated, assignment-checked routes within a
configured window, never embedded in public links or push. Exact
window/retention are release configuration, not guessed values. The recipient
can pause/withdraw and donors can decline changed instructions. Dietary changes
need a fresh acknowledgment, never a guarantee of medical or allergen safety.

This models coordinated meal giving, not a clone or integration of a third-party
service. [Meal Train's own description](https://www.mealtrain.com/about/) provides
context for dates, recipient preferences and volunteer coordination; its data
exposure choices are not adopted as Generosity's privacy policy.

### 5. Consent, Case Storage and Field Collection

An assessment response contains campaign/questionnaire version, minimal coded
needs, units/period, coarse geography, date, consent receipt and quality state.
It is not a `CenterNeed`: one describes household demand, the other requests
donations to a center. Do not publish a household response as a center need.

Optional follow-up adds a separate encrypted case/identifier record. Precise
coordinates, where separately consented and necessary for follow-up, stay in
that record and never enter public geometry, analysis or routing requests.
Separate referral consent names the recipient center, fields and purpose.
Use a shared pseudonymous response key for authorized revisits, not covert
deduplication by address, phone or device fingerprint. Non-follow-up respondents
can receive a private random receipt code for correction/withdrawal without
providing a name or account; store only its protected verifier. Explain that a
lost code and no identifier may prevent finding a particular response.

Start with approved structured questionnaires and coded unknown/declined
answers; no medical diagnoses, unrestricted narrative, photos, recordings or
identity documents. Device GPS is optional and not a continuous route log.
An incomplete interview does not imply no needs. Do not record address-linked
refusals. Collector accounts do not own respondent rights.

Native field drafts use platform-protected encrypted storage and an explicit
short-lived assignment lease with configured maximum lifetime. A disconnected
device cannot receive immediate remote revocation: enforce the lease locally,
check revocation on reconnect and block/purge at expiry. Do not claim instant
offline remote wipe. Protect against clock rollback; do not use the normal
unbounded contribution cache or plaintext backups. The browser case workspace
is online-only for sensitive data in the first version. Upload rechecks consent,
assignment and schema; rejected drafts cannot silently enter aggregate data.

Rights requests revoke active grants and mark responses withdrawn before
asynchronous deletion/recomputation. Log access metadata, not survey contents.
Track downstream referral deletion and backup expiration. Preserve only minimal
non-identifying audit evidence under an approved retention policy; do not keep
deleted sensitive input snapshots merely to reproduce an old chart.

Alternative rejected: a shared map of household pins available to every
organizer. It would expose residents while confusing participation with need.

### 6. Aggregate Releases Are a Privacy Boundary

Server-side aggregation emits approved geography/service/period cohorts with
provenance and quality flags. Public clients and analysts consume releases, not
raw household records. A threshold alone is not an anonymity guarantee: data
stewards approve minimum cohort size, geography, sensitive-category rules,
complementary suppression and release timing after disclosure-risk review.
Where distinct-household counts cannot be established conservatively, suppress
or coarsen rather than assume each anonymous response is a different household.

Initially use fixed approved filter combinations and release batches, not
arbitrary polygon queries or live counters that reveal single arrivals.
Totals, downloads and baseline/candidate comparisons must obey the same policy
to prevent subtraction attacks. Analysts cannot bypass it. Keep suppressed,
unknown and true zero separate. Restricted case lists are distinct operational
views, never an exception that exposes raw points through the map API.

Withdrawal invalidates dependent releases/scenarios and schedules safe refresh.
An already distributed irreversible aggregate may not support individual
removal; explain that limit during consent. Do not claim that deleting a contact
automatically anonymizes all derivatives. These safeguards reflect the risk
framing in [OCHA's data-responsibility guidance](https://centre.humdata.org/data-responsibility/);
they do not establish legal compliance for an unselected operating region.

### 7. Compare Service Coverage, Not Center Counts

Add a service-capacity record owned by each center: service code, unit, period,
eligibility/access conditions, usable capacity, resource pool, schedule,
provenance and last confirmation. Unknown capacity is excluded and counted as
unknown, never unlimited. Need quantities, volunteer seats and category tags
cannot substitute for supply. Capacity is available for the modeled demand
after existing obligations, not assumed gross capacity. Treat shared staff/
facility limits as explicit pooled constraints, not independent supply per tag.

Analysis inputs are approved aggregate releases, catalog/service revisions and
an explicitly selected origin approximation per coarse area. Network routing
uses only aggregate origins and public centers; retain travel mode, graph/date,
time assumptions and location uncertainty. Exact household data is not sent to
external routing providers. If routing is absent, only a separately labeled
proximity comparison is available; `Geo.kt` distance never becomes minutes.

The initial model compares one compatible service/unit/period at a time:

- Demand `D[a]` is usable observed unmet demand in area `a`, not population prevalence.
- `x[a,c]` is modeled allocation to a reachable, compatible center; `sum(c, x[a,c]) <= D[a]`.
- `sum(a, x[a,c]) <= available_capacity[c]`, plus any shared-pool limits. Require explicit consistent resource units; unsupported conversions block that run.
- Maximize served observed units under the declared constraints, then minimize travel burden for ties. Record solver status, tie rule and runtime bounds; a feasible timeout is not labeled optimal.
- Report allocations, remaining observed demand, unused modeled capacity, excluded/unknown inputs and gained/lost coverage by area. Do not sum unlike services into one moral score.

Use an established optimization engine, not a hand-written optimizer; the
[OR-Tools optimization overview](https://developers.google.com/optimization/introduction)
documents suitable linear/integer and network-flow approaches. Select/pin the
backend-compatible engine during implementation and verify it with hand-solved
fixtures. The constraints are this product's proposed model, not a conclusion
from that source. Indivisible service units require integer constraints.

Analysts supply candidate sites or interventions; the first version compares
them rather than searching all land parcels. A relocation subtracts source
capacity before adding destination capacity. Compare new service points, mobile
outreach, adjusted hours and capacity additions on the same data snapshot.
Show costs only when provided with currency/period; no invented exchange rates
or ROI. Run sensitivity alternatives for uncertain capacity, travel threshold
and demand; no statistical confidence claim without a justified sampling model.
Convenience outreach cannot establish that an unvisited area has no needs.

Reviewers record trade-offs and accept/defer/reject rationale. An accepted
scenario is a proposal, not permission to close a center, change its address or
deny a person support. Community/center consultation remains a real operational
step. Store result metadata and input version references; privacy withdrawal
takes precedence over reproducing sensitive historical inputs.

### 8. Cross-Platform and Mobile-First Boundaries

- Shared KMP: stable models/codes, form validation, repository interfaces, state and localized formatting; no client-authoritative eligibility, consent access or solver decisions.
- Android Compose and SwiftUI: Needs/events campaign discovery, enrollment/meal flows, Profile organizer/progress entry, protected field forms, approved coverage layer in Map and equivalent readable summary. Preserve the plus action.
- Backend: authority, transactions, consent/case isolation, eligibility projections, deletion, aggregate release, analysis jobs and outbox. One foundation service/database, with restricted data access boundaries rather than another deployable service by default.
- Native adapters: secure storage, optional location, notification permission and maps. External map/routing requests never carry private case coordinates.
- Canonical portal: campaign plans, meal coordination, narrow fieldwork/rights queues, own-center service inputs and analyst/reviewer workspace. Preserve public landing and existing center needs flows; organizer role does not impersonate center staff.

Use single-column phone forms and map/list alternatives, no desktop-only grid
editor. Verify at 320/360/390/768/1280 CSS pixels, Spanish/English, 200-percent
text, keyboard and screen reader, with 44-pixel primary targets. Browser
screenshots in Chromium/WebKit supplement real Safari/Chrome keyboard checks.
Do not put location, recipient details or sensitive survey text in telemetry,
URLs, external translators or lock-screen notification content.

## Risks / Trade-offs

- Thresholds can invite farming -> server ledger, independent validation, duplicate identity checks and no reward for survey volume; threshold policy remains reviewable.
- Earned access may delay new organizers -> keep volunteering open and show honest progress; do not claim points certify safety.
- Community efforts without a partner cannot earn awards -> keep attributed activity history; retain the user's center-only validation decision rather than create a self-verification loophole.
- Household disclosure and field safety -> consent separation, limited assignments, expiring offline access, no raw map/export and approved protocol before collection.
- Sparse/bias-prone data can stigmatize areas or misdirect services -> suppress small groups, label unknowns, show sensitivity and require human/contextual review.
- A mathematically feasible model is not observed social benefit -> report units, lost as well as gained coverage, costs and evidence limits; measure outcomes separately.
- Privacy reduces reproducibility -> keep method/version metadata but invalidate withdrawn inputs instead of retaining personal data indefinitely.

## Migration Plan

1. Complete foundation identity/sync/portal and reference event/award/inbox prerequisites; prove their tests before activating new authority.
2. Introduce additive ownership/schema fields with old center event semantics unchanged. Test mixed-owner records, import ownership and downgrade reads.
3. Pilot eligibility and ordinary community activities with synthetic data. Add meal support only after recipient rights/access gates pass.
4. Configure and approve protocol, retention, offline lease and disclosure rules before any real household collection. Use synthetic households until then.
5. Pilot aggregate analysis with explicitly supplied service data, known-answer models and scoped reviewers; publish only approved releases.
6. Roll back entry points/new collection through capability flags while preserving cancellation, rights/deletion, existing commitments and audit operations. Do not drop records or leave scheduled deliveries without notice.

## Release Configuration Still Required

These are launch-blocking parameter decisions, not permission to invent defaults:

- Eligibility `L/P/H`, policy owner, effective date and change communication.
- Pilot geography, approved service vocabulary/units, partner centers and verified capacity revision owners.
- Field protocol, training/assignment approvers, recipient/representative consent process and complaint/rights contact.
- Retention/deletion/backup periods, offline lease lifetime, meal detail-access window, disclosure thresholds/coarsening and approved release combinations.
- Routing source/license and operating modes; backend-compatible optimizer/version and bounded run limits.

No payment-provider choice, legal determination, collection deployment or public
map publication is performed by completing this design.
