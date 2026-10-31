# Proposal

## Why

Someone approached for help needs to find a nearby center that actually accepts
that type of request, give usable contact directions, and support the person
through the center instead of handing over unrestricted cash. Generosity needs
an accountable, consented path from an uncertain heads-up to center assessment
and documented assistance, without labeling unmet requests or missed visits as fraud.

## What Changes

- Publish explicit, current center intake and collection services: accepted need
  categories, walk-in/phone channels, hours, constraints and availability.
- Match the nearest suitable participating centers in the existing map/list,
  rather than treating the nearest health-category center as a medicine depot.
- Allow an authenticated helper to send a minimal, consented heads-up that a
  person may visit or call. The person needs no app, account, phone or address.
- Distinguish sent, center acknowledged, actual contact and assessed/accepted
  need. A heads-up is neither a reservation nor proof that assistance is needed
  or delivered. **User decision:** validate with the center before enabling any
  purchase or donation tied to the case; heads-up can happen beforehand.
- Let the center issue a private approved assistance request, then let donors
  commit suitable goods, arrange direct delivery to the center, or use the
  existing gated hosted-donation flow. No beneficiary cash transfer is added.
- Track separate commitments, center receipts and beneficiary handovers, with
  partial fulfillment, failures, cancellation, reconciliation and private support.
- Add medicine-specific eligibility, professional review, safe sourcing,
  receipt/handling and privacy gates. Generosity does not prescribe, diagnose
  or treat an ordinary collection point as a licensed medicine dispenser.
- Support Spanish/English Android, iOS and mobile-first browser flows, including
  printable/readable center directions and a non-account beneficiary route.

## Capabilities

### New Capabilities

- `center-intake-services`: Explicit center participation, service availability
  and provider-neutral discovery of suitable intake/collection locations.
- `consented-center-referrals`: Minimal heads-up, no-app contact reference,
  center assessment, scoped disclosure, withdrawal and non-stigmatizing outcomes.
- `center-mediated-fulfillment`: Approved case requests, bounded donor commitments,
  center-directed goods/money, receipts, handovers and honest contribution evidence.
- `medicine-support-safeguards`: Gated medicine assistance with qualified review,
  safe procurement/handling and separation of clinical information from donation UI.

### Modified Capabilities

None of the synchronized baseline requirements is replaced. These new contracts
extend the active discovery, private-case and external-donation plans; ordinary
public center needs and anonymous map browsing continue unchanged.

## Impact

- Shared KMP: neutral service/referral/request/receipt models, repository
  contracts, validation/state projections and separate privacy-safe donor views.
- Native apps: suitable-center filters, heads-up/contact status and approved
  assistance UI. Google Maps/MapKit adapters only render public center markers;
  future MapLibre migration cannot alter referral or funding rules.
- Planned backend: center-scoped intake permissions, consent records, private
  cases, atomic assistance allocations, auditable transitions, notification
  outbox, protected retention and existing provider payment reconciliation.
- Planned canonical `portal/`: mobile-first service settings, restricted intake
  queue, assessment and receipt/handover workspace; no second portal or service.
- Dependencies: foundation identity/backend/center portal; community private-case
  access/consent conventions; reference payment gates and notification inbox;
  web participation for full browser donor parity. None is assumed implemented.
- Production medicine intake remains disabled until country/service-specific
  professional, privacy and handling policies are reviewed and configured.
  General center onboarding/verification is not medicine authorization.
- Not included: medical diagnosis or prescribing, generic prescription uploads,
  direct cash or reimbursement to beneficiaries, app-held escrow, an e-commerce
  or courier network, public individual need maps, beneficiary trust scores,
  identity/face matching, or a guarantee that the workflow eliminates misuse.

This change creates planning artifacts only. No backend, center case management,
medicine purchasing or production payment functionality is implemented here.
