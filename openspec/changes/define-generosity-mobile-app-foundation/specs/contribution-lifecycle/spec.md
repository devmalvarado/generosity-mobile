## Purpose
Define backend-authorized contribution transitions, capacity accounting and the distinction between center-confirmed participation and self-reported personal help.

## ADDED Requirements

### Requirement: Contribution Transition Authority
Center-linked contributions SHALL start as offered after server acceptance. Only authorized staff of that center SHALL transition offered to confirmed and confirmed to fulfilled. The owning user SHALL be able to cancel offered or confirmed contributions before fulfillment. Cancelled and fulfilled SHALL be terminal in this scope.

#### Scenario: Center confirms an offer
- **WHEN** an authenticated staff member assigned to the contribution's center confirms an offered contribution
- **THEN** the backend records confirmed status with actor, timestamp and a new version

#### Scenario: User attempts self-fulfillment
- **WHEN** the owner tries to confirm or fulfill a center-linked contribution
- **THEN** the backend denies the transition and the app keeps the authoritative state

#### Scenario: Cancel after fulfillment
- **WHEN** an owner attempts to cancel a fulfilled contribution
- **THEN** the backend rejects the terminal-state transition without altering history or totals

### Requirement: Atomic Goods Capacity
The backend SHALL atomically validate and reserve bounded goods quantity when accepting an offer. Offered, confirmed and fulfilled quantities SHALL consume capacity until cancellation where allowed. Concurrent requests SHALL not exceed required quantity. Local previews SHALL remain provisional until acknowledgement.

#### Scenario: Two users offer the last units
- **WHEN** concurrent offers would jointly exceed remaining quantity
- **THEN** only quantities that fit are accepted and rejected offers receive current availability without recording a false success

### Requirement: Cancellation Releases Exactly Once
An accepted cancellation SHALL release its reservation exactly once in the same transaction as the status update. It SHALL reopen a quantity-covered need only if that need is otherwise available and non-expired. Cancellation SHALL NOT reopen an independently cancelled or expired published need.

#### Scenario: Retried cancellation
- **WHEN** a cancellation is retried after a lost response
- **THEN** the contribution remains cancelled and remaining capacity increases only once

### Requirement: Versioned Idempotent Transitions
Creation and transitions SHALL support stable operation IDs and expected record versions. Repeating an operation SHALL return its original result; conflicting or stale transitions SHALL return current state for reconciliation. An operation ID reused with different content SHALL be rejected.

#### Scenario: Cancellation races with fulfillment
- **WHEN** valid cancellation and fulfillment requests target the same confirmed version
- **THEN** one transition commits, the other receives a conflict, and neither capacity nor impact is applied twice

### Requirement: Explicit Self-Reported Personal Help
A registered user SHALL be able to record and complete their own helping activity without a published need. It SHALL carry self-reported provenance and no center-confirmation claim. A personal record SHALL NOT be reassigned to a center need to bypass staff authorization.

#### Scenario: Complete personal activity
- **WHEN** the owner records a completed personal helping activity
- **THEN** the backend stores it with self-reported provenance and history labels it separately from center-confirmed contributions

### Requirement: Auditable History and Impact
Accepted transitions SHALL preserve actor, timestamp, prior/new state and provenance. Impact SHALL recompute from authoritative confirmed/fulfilled records, separating self-reported personal activity and excluding pending, rejected or cancelled records. Retrying an event SHALL not duplicate history or totals.

#### Scenario: Confirmed offer is cancelled
- **WHEN** a cancellation commits for an offer previously included in impact
- **THEN** the history preserves both events, the cancelled item remains visible and its quantity no longer contributes to impact

### Requirement: Partial Contributions Use the Published Unit
Donors SHALL be able to offer positive whole quantities up to the unreserved remainder, using the published unit. Package contents SHALL not silently change that unit. Supplied conflicting units SHALL be rejected. Receipt SHALL require authorized center fulfillment; donor promises alone SHALL not count as delivered help.

#### Scenario: One box toward ten boxes
- **WHEN** the need requests ten boxes of ten items and a donor offers one box
- **THEN** committed quantity becomes one, remaining capacity is nine boxes and committed progress is ten percent, while received quantity stays unchanged until staff fulfills that offer

#### Scenario: Donor sends a different unit
- **WHEN** an API request supplies pieces for a need denominated in boxes
- **THEN** the request is rejected with no reservation and the donor must use the published unit

### Requirement: Truthful Accessible Need Progress
Native apps and the center portal SHALL show committed/target goods progress, exact counts, unit, percentage and unreserved remainder. Received totals SHALL count only center-fulfilled goods and remain separate; they are already included in commitments. Missing targets SHALL have no determinate bar, and unknown receipt data SHALL not become zero. Localized accessible labels SHALL explain progress without relying on color.

#### Scenario: Reservations fill the target before delivery
- **WHEN** all ten boxes are committed but the center has received one
- **THEN** the visible bar says one hundred percent committed, the separate received total says one of ten and the center detail retains the need while disabling further offers

#### Scenario: Cancellation releases a partial reservation
- **WHEN** a donor cancels nine unfulfilled boxes from the fully committed target
- **THEN** refreshed progress becomes one of ten, nine become available again, the received count stays one and retries do not change those totals twice

#### Scenario: Legacy cached catalog lacks receipt totals
- **WHEN** a client loads an older cached catalog without receipt data
- **THEN** it may show known commitment progress with receipt unavailable and must not present commitments as delivered goods

### Requirement: Negotiated Progress Synchronization
Need responses SHALL expose quantityReceived. Native sync SHALL include it for targeted needs only when progress_version=1 is requested, preserving the legacy shape otherwise. The shared parser SHALL accept missing receipt data and reject negative values, totals above commitments or received quantities without a target. Refresh/restart SHALL preserve acknowledged totals; offline offers SHALL not advance authoritative progress.

#### Scenario: Mixed client versions
- **WHEN** old and new native clients request the same catalog
- **THEN** both can parse their responses; only negotiated clients receive the new quantityReceived field, and old-client requests remain valid

#### Scenario: Fulfillment changes the snapshot
- **WHEN** center staff fulfills a goods contribution without changing its reserved quantity
- **THEN** negotiated received totals and the snapshot revision change so a client refresh reflects the delivery

#### Scenario: A center accepts loose items
- **WHEN** staff wants to collect one hundred items individually instead of ten sealed boxes
- **THEN** it publishes target one hundred with unit items before receiving commitments; no implicit box-to-item conversion occurs
