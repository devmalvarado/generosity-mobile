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
