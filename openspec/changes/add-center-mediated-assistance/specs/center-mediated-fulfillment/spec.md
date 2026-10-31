## Purpose
Support an assessed private need through the receiving center with bounded donor commitments and separately attributable evidence of procurement, receipt and beneficiary handover.

## ADDED Requirements

### Requirement: Approved Private Assistance Request
A case-specific assistance request SHALL require center-validated contact and acceptance, current consent, an eligible receiving service, defined items/units or purpose, supported methods, expiry and version. Donor views SHALL expose only permitted fulfillment fields. Neither the case nor its request SHALL enter public catalog/feed/search automatically. Closing or changing the request SHALL preserve linked history.

#### Scenario: Approved need becomes actionable
- **WHEN** the center accepts the need after contact and publishes its approved private request to an authorized donor
- **THEN** that donor can review current permitted support methods without seeing the person's clinical notes, identity or other requests

#### Scenario: Only a heads-up exists
- **WHEN** an actor tries to offer goods, initiate case checkout or obtain purchasing instructions before approval
- **THEN** the backend rejects the action, even if the client enables its controls

### Requirement: Explicit Center-Directed Donor Action
Authenticated donors SHALL review the approved item/purpose, quantity, center identity, receiving location/channel and current instructions before explicitly committing. Goods SHALL go to the approved center or its approved professional partner. Money SHALL use the existing eligible-center hosted-provider flow. This feature SHALL not enable unrestricted cash, personal-account transfers or reimbursements to the person or referring helper.

#### Scenario: Donor buys and sends permitted goods
- **WHEN** a donor chooses a current approved goods line and commits an allowed quantity
- **THEN** the app supplies the center-approved receiving instructions and private delivery reference, without exposing a beneficiary home location or claiming the purchase occurred

#### Scenario: Hosted donations are not configured
- **WHEN** the case is accepted but provider, recipient or operating-region gates are incomplete
- **THEN** money stays unavailable while eligible goods support can proceed without an invented payment receipt or personal bank destination

### Requirement: Shared Bounded Allocation
The backend SHALL atomically reserve approved quantities or budgets across in-kind and funded procurement for the same request. Operations SHALL use stable identities and current versions. Financial procurement SHALL require a reviewed bounded quote and currency; stale quotes SHALL require a new donor decision. Competing commitments SHALL not buy or fund the same remaining units twice.

#### Scenario: Goods and money compete for the final unit
- **WHEN** one donor commits the last item while another attempts to fund purchase of that same item
- **THEN** only one eligible allocation succeeds and the other donor receives updated availability without a charge or duplicate purchase instruction

#### Scenario: Response is lost or a price changes
- **WHEN** a donor retries an acknowledged commitment or submits an obsolete procurement quote
- **THEN** the retry resolves the original operation once, while the obsolete quote is rejected without silently increasing the amount or substituting goods

### Requirement: Separate Financial And Assistance Evidence
Provider-confirmed payment, center allocation of funds, procurement, center receipt and beneficiary handover SHALL remain separate auditable facts. Staff SHALL not mark payments paid; checkout return or a receipt image SHALL not prove payment. An unallocatable late payment SHALL be recorded and reconciled through support/refund procedures, not duplicate procurement or silently fund another purpose. No app-held escrow is implied.

#### Scenario: Money arrives after the request closes
- **WHEN** a genuine provider result confirms payment after its allocation expires or the request closes
- **THEN** the original payment remains in the financial ledger, no automatic purchase or redirection occurs, and its unapplied amount enters the configured reconciliation/refund route

#### Scenario: Payment succeeds before delivery
- **WHEN** a donation is verified but procurement or handover has not occurred
- **THEN** the donor sees paid with the separate pending assistance status, not a claim that the person received help or recovered

### Requirement: Attributable Receipt And Handover
Assigned center staff SHALL record accepted/rejected quantities and condition at receipt, and separately record partial or final beneficiary handover with actor, time, method and permitted evidence. Courier status or donor declaration SHALL not establish center receipt or handover. Confirmation SHALL use a private no-app procedure, without mandatory photographs, biometric scans, diagnoses or public recipient testimony.

#### Scenario: Courier says delivered
- **WHEN** the carrier or donor reports delivery but assigned center staff has not accepted the goods
- **THEN** history retains an external delivery report and awaits center receipt rather than marking the request fulfilled

#### Scenario: Center receives five units and hands over three
- **WHEN** staff accepts five units and records a handover of three
- **THEN** history distinguishes received five from delivered three, the remaining two are accounted for, and the request is not fully fulfilled

### Requirement: Reconciliation Of Changed Or Undelivered Help
Cancellation, withdrawal, unsuitable goods, uncollected items, changed need and service closure SHALL use explicit support outcomes. Reservations SHALL be released once only when no in-flight obligation remains. Returns, refunds or repurposing SHALL follow agreed policies and any required new consent; no automatic cash conversion or false reversal is allowed. Donors and beneficiaries SHALL have private support/correction routes.

#### Scenario: Person does not collect goods already received
- **WHEN** the collection window ends with accepted goods still at the center
- **THEN** the case stays unfulfilled or closes with an unresolved/alternative outcome, and staff follows the agreed return/storage process instead of claiming delivery or automatically donating the goods elsewhere

#### Scenario: Cancellation races with shipment or checkout
- **WHEN** cancellation arrives while goods are in transit or payment status is uncertain
- **THEN** the server preserves the obligation for reconciliation, blocks new conflicting allocations and does not release capacity or announce a refund prematurely

### Requirement: Honest Progress And Recognition
Owned history SHALL distinguish requested, committed, received, handed over, cancelled and disputed quantities alongside separate financial outcomes. Referral/contact/payment alone SHALL not earn validated-help recognition. Case-linked recognition SHALL require independently center-confirmed completed handover, use the canonical contribution identity once and support corrections. No beneficiary identity or medical outcome SHALL enter public impact or rankings.

#### Scenario: Helper also holds a center role
- **WHEN** the referring donor attempts to validate their own case-linked contribution
- **THEN** a separate authorized validator is required and no points, badge or completed-help claim is generated by self-approval

#### Scenario: Handover is corrected
- **WHEN** authorized review corrects a mistaken completed handover
- **THEN** history retains the correction and relevant impact/recognition reconciles without duplicate credit or disclosure of the beneficiary

### Requirement: Localized Reliable Delivery Workflow
Android, iOS and mobile-first web SHALL expose equivalent authorized actions and server states in Spanish/English, with private inbox updates that do not require push. Updates SHALL be deduplicated and notifications SHALL omit beneficiary/medicine details. Offline or expired sessions SHALL not establish acceptance, purchase, receipt or handover; reconnect SHALL reconcile existing operation identities before retrying.

#### Scenario: Donor cannot receive push notifications
- **WHEN** the center accepts goods or records an authorized handover and push is denied
- **THEN** the donor can still read permitted progress in owned history/inbox or an explicitly opted-in channel without the beneficiary needing an account
