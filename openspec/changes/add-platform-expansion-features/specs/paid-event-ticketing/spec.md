## Purpose
Offer explicitly gated provider-hosted event admission with consistent seat inventory and refund history, separate from charitable donations and validated volunteer helping.

## ADDED Requirements

### Requirement: Explicit Paid-Admission Gate
Paid events SHALL require approved seller/provider onboarding, region/currency, displayed price/fees, cancellation/refund policy and support ownership. Donation-provider readiness SHALL not automatically enable ticket sales. Users SHALL review the total and explicitly accept checkout. Production gates SHALL remain closed until separately approved; clients SHALL never collect card/bank credentials.

#### Scenario: Center can receive donations but cannot sell tickets
- **WHEN** staff attempts to publish paid admission
- **THEN** ticket purchase remains disabled until the distinct seller/admission gate is satisfied

### Requirement: Capacity-Bound Checkout Holds
Authenticated purchases SHALL atomically hold one eligible participant seat until a disclosed deadline and create an idempotent provider-hosted session. Holds, free enrollments, waitlist offers and paid tickets SHALL share inventory. Retries SHALL not create duplicate charges or holds. Provider return URLs SHALL not establish paid status or ownership.

#### Scenario: Two buyers select the final seat
- **WHEN** checkout requests race for that seat
- **THEN** at most one active hold is accepted and no unreserved second purchase is initiated

### Requirement: Verified Admission and Late Results
Only authenticated reconciled provider success matching intent, seller, amount and currency SHALL issue a ticket when its capacity allocation remains valid. Duplicate/out-of-order results SHALL be replay-safe. Success after released capacity or event cancellation SHALL enter explicit refund/support reconciliation, not overbook or disappear. A failed refund SHALL remain visible as unresolved, not be reported completed.

#### Scenario: Payment succeeds after its hold expired
- **WHEN** the seat has already been allocated elsewhere
- **THEN** no extra admission is issued and the actual payment is retained with the required refund/support workflow

### Requirement: Private Ticket Validation
Tickets SHALL use non-enumerable references with current server validity and an authorized check-in route. Check-in SHALL be idempotent and distinguish valid, used, cancelled and refunded tickets without exposing billing details. A copied QR SHALL not create another admission. Private browser retrieval SHALL work without installation; loss of connectivity SHALL not be presented as authoritative validation.

#### Scenario: Reuse a checked-in ticket
- **WHEN** a second scanner submits its reference
- **THEN** current used status is shown and no second admission or attendance record is created

### Requirement: Cancellation and Financial Reconciliation
Event cancellation SHALL stop sales/admission, notify ticket holders and initiate the applicable provider refund process. Partial/full refunds, disputes and replacements SHALL preserve the ledger and update ticket validity under the published policy once. Requests SHALL not equal successful refunds. Disabling new sales SHALL retain provider-event handling, refunds and owned history.

#### Scenario: Organizer cancels a paid occurrence
- **WHEN** cancellation commits
- **THEN** tickets become unusable for that occurrence and each actual payment shows its real refund/reconciliation state rather than an invented immediate refund

### Requirement: No Paid Shortcut to Helping Rewards
Ticket purchase and admission SHALL remain separate from donations, fiscal donation evidence, volunteer contributions and gamification. Paying/attending alone SHALL not earn helping points, organizer eligibility, recognition credentials or regional ranking credit. Independently validated actual volunteering at the event SHALL require a separate canonical activity, not conversion of the ticket into volunteer hours.

#### Scenario: Paid attendee checks in
- **WHEN** admission is validated
- **THEN** ticket history updates but helping impact, certificates and regional rankings remain unchanged unless separate eligible help is validated
