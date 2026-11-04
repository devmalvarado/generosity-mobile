## ADDED Requirements

### Requirement: Server-owned effective availability
Each implemented feature SHALL support `off`, `testers` and `all`. Tester access
SHALL require an active verified account explicitly listed for that feature and
SHALL NOT replace ordinary authorization. Unknown features SHALL be disabled.

#### Scenario: Tester-only release
- **WHEN** a feature is available only to a listed verified account
- **THEN** that account receives true and anonymous/other accounts receive false
- **AND** effective configuration does not disclose the tester list

### Requirement: Restricted audited controls
Only configured active, verified feature administrators SHALL change flags via
session-authenticated, CSRF-protected forms. Every change SHALL require a reason,
check the current version and atomically record the actor and before/after values.

#### Scenario: Conflicting edits
- **WHEN** two administrators submit the same prior version
- **THEN** only the first succeeds and the second receives a conflict without a second audit

#### Scenario: Unprivileged or forged request
- **WHEN** ordinary center staff, an unverified account or a CSRF-invalid request changes a flag
- **THEN** the request is rejected and persisted availability remains unchanged

### Requirement: Creation gates preserve existing obligations
The backend SHALL enforce contribution creation and new need publication at the
write boundary. A disabled feature SHALL NOT block history, donor cancellation,
center confirmation/fulfillment or editing/closing already-published needs.

#### Scenario: Flag disabled after a successful request loses its response
- **WHEN** the client retries the original acknowledged idempotency key/body
- **THEN** the server returns the original receipt without creating a duplicate

#### Scenario: New work is paused
- **WHEN** creation is disabled and a client submits a new contribution or publishes a draft
- **THEN** the API returns `feature_disabled` and does not apply that mutation
- **AND** drafts and resolution of previously accepted work remain available

### Requirement: Client refresh and durable pending operations
Shared/native state and the portal SHALL refresh effective configuration on entry,
foreground or explicit refresh, clear account-scoped permissions on account
changes, and default new actions off when configuration cannot be obtained.
A known paused outbox operation SHALL retain its original payload/key for retry.

#### Scenario: Account change or configuration outage
- **WHEN** a tester signs out, another account signs in, or configuration refresh fails
- **THEN** stale tester permissions cannot enable the new account's actions
- **AND** history/cache and queued operations are preserved

#### Scenario: Paused operation resumes
- **WHEN** the server re-enables a feature and the client synchronizes its paused operation
- **THEN** it retries with the same key/body and removes it only after acknowledgment

### Requirement: Progress is a display control
The progress flag SHALL govern progress presentation in both native apps and the
center portal without changing quantities, capacity, receipt accounting or API
access rules. Spanish/English and existing design resources SHALL be used.

#### Scenario: Progress is hidden
- **WHEN** the effective progress flag is false
- **THEN** the progress component is hidden while the server retains authoritative totals
- **AND** completion/cancellation of existing contributions remains possible
