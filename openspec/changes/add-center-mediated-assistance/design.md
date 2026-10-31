# Design

## Context

See [proposal.md](proposal.md) for the requested workflow and the user's explicit
decision: allow a heads-up first, but no case-specific purchase or donation until
actual contact and center validation.

Observed implementation, 2026-10-26:

- Shared `KindnessCenter` has public category, coordinates, contact and needs,
  but no intake-service participation, service availability or receiving role.
- `DiscoverKindnessCentersUseCase` filters public categories/text and can sort
  by great-circle distance. A `HEALTH` category does not establish medicine
  receiving, assessment or dispensing capability.
- `GenerosityAppState` still uses sample/in-memory data and demonstration
  authentication. Current contribution recording resolves public catalog needs.
  There is no private-case repository or durable authorization service yet.
- Android now injects a [neutral renderer](../../../docs/native-maps.md), with
  Google Maps in the native adapter. iOS retains MapKit and local sample data.
- The canonical backend and `portal/` remain planned. The older external landing
  is static; it cannot receive a heads-up or record a delivery.
- [Household follow-up](../add-community-campaigns-and-coverage/specs/household-needs-assessments/spec.md)
  already defines restricted cases/consent. This adds a separate minimal helper
  entry point, not a household interview or permission for helpers to read cases.
- [External donations](../add-reference-product-features/specs/external-donations/spec.md)
  already defines hosted checkout and a separate verified financial ledger.
  This feature reuses those gates; a case does not bypass provider readiness.

## Goals / Non-Goals

**Goals:**

- Use one foundation backend and one mobile-first portal, adding private intake
  contracts rather than duplicating public needs or inventing client authority.
- Keep the person independent of an app account and separate their private
  circumstances from the donor's owned contribution history.
- Represent real evidence and uncertainty at each step, so receiving a notice,
  attending, paying, receiving stock and handing it over cannot be confused.

**Non-Goals:**

- A registry of people seeking aid, cross-center identity matching, public case
  crowdfunding, a medical record system or automated assessment of honesty.
- A pharmacy, prescribing engine, courier marketplace, wallet, escrow service
  or universal claim of legal compliance across countries.
- Migrating maps, finishing the foundation backend, or claiming the existing
  sample sign-in can authorize real case handling in this planning change.

## Decisions

### 1. Public Service Catalog, Not Case Pins

Add a public `CenterIntakeService` linked to a stable center ID: service code,
approved roles, allowed/excluded goods, supported contact channels, public
instructions, hours/timezone, status, reviewed-at/valid-until and version.
Controlled medicine eligibility is separate from a staff-editable participation
toggle. Center staff can pause a service without deleting active obligations.

Match service suitability first, then distance from a permitted device/manual
origin. Keep unavailable/unknown services separate from ready matches; unknown
is never inferred from an empty need list. Sorting uses the shared distance
function until a separately reviewed travel-time integration exists. Label its
meaning and origin. Server acceptance rechecks the public service version.

Only public center projections reach map adapters. Do not send case IDs, precise
beneficiary coordinates, medication queries or contact details to a map, routing,
search or analytics provider. Use the current renderer boundary for markers;
the referral flow must remain identical after a future Android MapLibre swap.

Alternative rejected: put a needy person's pin on the map and match a nearby
category. That exposes the person and does not establish a capable receiver.

### 2. Minimal Heads-Up With Separate Permissions

The authenticated helper chooses one service and records that the person agreed
to a possible visit/call notice. Minimum payload: center/service/version,
structured category/channel, consent protocol/version/time and helper operation
ID. An estimated window is optional and explicitly not a booked appointment.
No name, permanent address, phone, medicine name, diagnosis, photo, prescription
or free-form clinical narrative is required or accepted in this initial form.

The backend generates a random subject reference scoped to this referral, not a
global person identifier. Optional contact fields, when explicitly permitted,
live in separately protected storage. Record permissions independently for:

- Notice to the named center for the stated purpose.
- Optional center follow-up contact.
- Progress disclosure to the referring helper.
- Minimal assistance details to an authorized donor, including separately
  permitted product-name disclosure for medicine.

Staff reconfirms consent when the person contacts the center. The helper's
attestation does not authorize clinical processing, publicity or indefinite
contact. Without permission, public directions still work but no case is created.
No donation, contact details or app installation is a condition of asking for help.

Alternative rejected: create a full beneficiary profile or reuse an assisted
event enrollment. This request is about a private service referral, not an event
participant identity, and needs fewer initial fields and narrower permissions.

### 3. Backend State And Contact Evidence

Proposed referral states and guards:

| State/action | Actor and guard | Meaning |
| --- | --- | --- |
| `heads_up` | Authenticated helper, current service and notice consent | Server durably received a possible visit/call notice. |
| `acknowledged` | Recipient-center intake role | Center read/acknowledged the notice; no contact, seat or need acceptance is implied. |
| `assessing` | Assigned case worker records actual phone/on-site contact and reconfirms consent | The person has contacted the center; the need is under assessment. |
| `accepted` | Assigned worker, current service eligibility and required professional review | A scoped assistance request can be authorized. |
| `unable_to_assist` | Assigned worker, structured private reason | Center cannot accept; this is not a judgment that the person lied. |
| `expired` | Configured no-contact deadline | No contact established within the notice window; no fraud inference. |
| `cancelled` | Supported withdrawal/correction process | No further intake/funding; existing obligations reconcile separately. |

Keep contact evidence (method, actor, server time, source/assessment revision)
separate from state. Neither the app's visit-signal feature nor GPS can manufacture
contact. Out-of-order commands fail against the current version; retries recover
one operation result. Do not reopen an accepted/terminal case through a late
heads-up notification. A new consented encounter can create a new notice without
globally matching names or penalizing prior nonattendance.

Use durable events/outbox for private inbox updates. Push/email contains only a
generic notice and an authenticated destination; no patient or product details.
An opted-in channel or the portal inbox works without push. Notification delivery
is not proof the center has read the notice or the person received a message.

Alternative rejected: create a contribution immediately on sending the heads-up.
That would conflate an uncertain introduction with an assessed, fulfillable need.

### 4. No-App Access And Authorization

The helper can show, read out or print public center directions and a private
human-readable case reference. Keep the public center URL separate from the
reference: no case token in link previews, analytics, referrers or public QR
metadata. A QR representation is optional convenience, not authentication.
Rate-limited staff lookup requires the recipient-center intake role and then a
case assignment; possession of the paper cannot reveal private status on the web.

Intake queues initially expose only minimal notice fields to authorized staff.
Claiming a case establishes an auditable assignment, not organization-wide case
visibility. Existing household-case assignments do not automatically include this
case, and a helper does not gain a case-worker role by submitting a notice.

Helpers see submission receipt and only consented subsequent progress. A donor
gets an explicit scoped assistance projection, not access to the case repository.
If progress sharing is refused, buttons/errors must not leak arrival or approval;
case-specific funding requires its own permitted donor disclosure. The person
can instead seek center support without that donor relationship. An ordinary
unlinked center donation must not be presented as funding this person's case.

At phone/on-site contact, use a reviewed proportionate matching/consent procedure.
Do not make a reference the sole authorization for handover or require biometrics
or an app account. A lost reference leads to staff assistance, not a public name
search or mandatory government-ID upload. Unsupported representative/minor
situations use an approved professional/safeguarding route.

Alternative rejected: a bearer public status page or SMS-only confirmation. The
former leaks circumstances and the latter excludes people without a personal phone.

### 5. Private Assistance Targets And Transactional Allocations

Add `AssistanceRequest` as a private target distinct from public `CenterNeed`.
It references the accepted case revision, service, permitted donor audience,
approved lines/units, expiry and allowed methods. Do not insert it in the imported
catalog or let a guessed private ID fall back to a personal-help contribution.

Reuse contribution lifecycle and capacity primitives through an explicitly typed
target discriminator and backend authorization, not a parallel payment system.
For each line, transactions account for committed, in-flight, center-received,
handed-over, rejected/returned and releasable quantities. Goods offers and
center-funded procurement reserve the same remaining units. A funding quote
records amount in minor units, currency, fees when known, covered units and expiry.
Changes require donor review; there is no automatic substitution or overfunding.

The donor can buy externally and carry/send approved goods using the center's
instructions. An external order/tracking reference is evidence of reported
procurement, not an integrated shop, delivery receipt or authorization to disclose
beneficiary data to a courier. This release does not build retailer/courier APIs.

For money, reuse hosted checkout to the controlled eligible-center recipient.
Provider metadata uses opaque financial correlation, not beneficiary identity,
diagnosis, product name or public case URL. No cash payout to the person, helper
reimbursement or platform-held escrow is created. The center agrees to the
approved purpose; an app label cannot itself enforce segregation of bank funds.

Provider success and center acceptance remain independent. An expired allocation
does not erase a real late charge: reconcile it, block duplicate procurement and
route unapplied funds to configured support/refund handling. Never silently turn
them into a general donation or promise a refund before provider confirmation.

Alternative rejected: separate uncoordinated goods and money counters. They could
fund and buy the same final item twice or release an in-flight obligation early.

### 6. Receipts, Handover And Honest Impact

Record structured, versioned fulfillment events with actor/time, quantity/unit,
condition and evidence provenance. Minimum transitions distinguish donor-reported
purchase/shipment, center-accepted receipt and beneficiary handover. A carrier
scan is not a center receipt; a center receipt is not beneficiary delivery.
Partial outcomes retain the balance and location/responsibility of undelivered
goods. Rejected/returned goods never silently re-enter available supply.

The no-app handover procedure belongs to authorized center staff. Do not demand
beneficiary photos, signatures published to donors, diagnoses or public gratitude.
Keep donor-visible receipts free of clinical/person data; receipt attachments
are not a required new upload feature. Corrections append evidence rather than
rewriting terminal history. Record clinical benefit as unknown, not inferred
from successful delivery.

For this case-specific path, completed-help recognition requires the final
center-validated handover and independent validation when staff is also donor or
referrer. Use the canonical contribution identity to prevent receipt, payment,
referral and handover from generating multiple awards. Existing generic public
center donations retain their own rules; this does not turn every center-stock
delivery into a claim about a particular beneficiary. Case data does not feed
territorial maps, public impact or rankings; any later aggregate integration needs
its own approved disclosure policy. Referrals and assessments never earn points.

Alternative rejected: donor-only completion or a successful-payment badge as
proof of assistance. These do not establish receipt by the intended person.

### 7. Medicine Is A Separately Gated Service

Configure country, authorized center/partner role, professional review route,
permitted item classes, valid authorization evidence, approved supply and handling
conditions, incident/recall process and policy version before enabling medicine
commitments. A center can coordinate a pharmacy without being authorized to
dispense. A self-selected service checkbox cannot approve either role.

Prescription/clinical records stay with the qualified service in this release.
The app retains only the review attestation/reference and minimum operational
fields necessary for the authorized case. An approved medicine item can itself
reveal health information: share it with a donor only under explicit permission.
Otherwise, the center/partner procures under a generic approved funding purpose.
No fundraising flow gives the donor a right to the person's prescription.

Buy only the reviewed product/quantity from an approved source. Product changes
return to professional review and donor consent. Opened, returned, expired or
untraceable medicines are not eligible here; special-handling classes remain
disabled without a separately approved process. Staff records product/provenance,
expiry/condition checks before acceptance; discrepancies cannot fulfill the case.
This is an operational safety design, not country-specific medical/legal advice.

Use reviewed local urgent-care and safeguarding contact routes, never an automated
diagnosis, universal emergency number or a promise that sponsorship is timely care.

Design basis: [WHO donation principles](https://www.who.int/teams/health-product-and-policy-standards/medicines-selection-ip-and-affordability/donations)
emphasize recipient needs/participation and local policy with suitable handling.
They are not a license or a substitute for a qualified local operating review.
Alternative rejected: treat medicines like ordinary household goods solely because
the user or center selected a health category.

### 8. Privacy, Retention And Client Boundaries

Initial private intake is online-authorized and memory-bound on clients. This is
not the community fieldwork flow's separately encrypted/offline collection lease.
No sensitive case service-worker cache, durable plain-text draft, URL parameters,
public analytics events or device backup. Expiry, sign-out or assignment loss clears
private views/input; public center discovery continues offline from permitted data.
Use opaque operation IDs to reconcile lost responses before resubmitting.

The backend enforces scoped projections on every read/write/export, including
associated resources. Separate protected contact/consent storage from the minimal
ledger needed for financial reconciliation. Configure retention and a no-account
rights/support contact before launch. Withdrawal blocks new disclosure/actions;
audit and required financial records remain minimized under the reviewed policy.
Track downstream deletion and backup expiry without promising to recall a paper
reference or data already legitimately disclosed outside the system.

[OWASP authorization guidance](https://cheatsheetseries.owasp.org/cheatsheets/Authorization_Cheat_Sheet.html)
supports deny-by-default and per-request checking. Tests must exercise guessed
IDs, another center, another donor, revoked assignments and exports, not merely
hidden buttons. No general administrator gets implicit clinical access.

Platform division:

- KMP: neutral service/request/state contracts and validation, typed repositories,
  safe donor projections, error codes, filters and distances; no client authority.
- Android/iOS: native map/list entry, consent/heads-up/status, approved assistance
  actions and native external handoffs; adapters cannot receive private case data.
- Backend: identity, permissions, case/consent storage, transaction rules, versioned
  events, notifications, financial reconciliation, policy gates and retention.
- Canonical web: public directions, authenticated helper/donor routes and staff
  service/intake/receipt workspace. Apply 320px-up, 200-percent text, keyboard,
  screen-reader, long Spanish/English and real Safari/Chrome acceptance.

## Risks / Trade-offs

- No-show or unmet need misread as fraud -> neutral statuses, no beneficiary scoring,
  no global identity matching, private reports and a staffed correction route.
- Center/donor collusion -> independent validation, least privilege, auditable
  revisions, bounded evidence and human review. This reduces risk, not guarantees truth.
- Digital exclusion -> public written/phone directions, human-readable reference,
  center-mediated rights and no mandatory account/phone for the person.
- Stale availability or urgent need -> service rechecks, clear uncertainty, no
  appointment guarantee and separate reviewed urgent-care routes.
- Medical disclosure through purchase details -> minimal separate consent and
  center procurement when the person declines donor product disclosure.
- Items/money in flight during withdrawal -> stop new work but reconcile existing
  obligations; do not promise automatic recovery, escrow or unsupportable refunds.
- Privacy vs donor assurance -> show accountable operational evidence, not sensitive
  documents or a claim of guaranteed recovery. Private disputes remain possible.

## Migration Plan

1. Implement foundation identity/backend and private authorization prerequisites.
   Add inactive schema/configuration with separate public and private projections;
   existing centers and demonstration data are not automatically opted in.
2. Pilot reviewed non-medical services using real center staff, consent and support
   procedures. Exercise heads-up -> contact -> accepted -> goods -> receipt ->
   handover and failure/withdrawal cases across native apps and mobile-first web.
3. Enable medicine only for reviewed country/service/partner configurations and
   supported representative/urgent-care protocols. Validate the no-clinical-upload
   and product-disclosure boundaries with the responsible service.
4. Enable hosted case donations only after existing provider release gates and
   mixed goods/money allocation, late-payment and refund tests pass. No production
   charge is authorized by this document or sandbox success.
5. Rollback switches stop new service participation, referrals, commitments or
   medicine operations independently while retaining authorized history, rights,
   outstanding goods handling and provider-event reconciliation. Never drop case
   records or erase obligations to disable the feature.

## Release Configuration

Still required, without changing these contracts: pilot centers/countries,
qualified partners and authority-review owners; approved consent/representation
scripts and languages; service refresh/no-contact windows; role/assignment policy;
retention/deletion and support contacts; receiving/return/storage/recall policies;
quote/commitment timeouts; provider recipient mapping and refund responsibility.
Missing policy keeps the affected action disabled, not silently assigned a demo value.
