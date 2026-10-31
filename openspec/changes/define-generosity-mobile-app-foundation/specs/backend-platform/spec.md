## Purpose
Provide the authoritative service for catalog, accounts, contributions, preferences and explicit visits, supporting both native apps and the center web portal without payment processing.

## ADDED Requirements

### Requirement: Documented Mobile API
The backend SHALL provide versioned, documented contracts for public catalog reads, center-scoped need management, profile/preferences, contributions/history, reminders, visits, device registration and sync. Responses SHALL use stable IDs, domain/error codes, timestamps and pagination for growing collections. Both native apps and the web portal SHALL use the same authoritative contracts.

#### Scenario: Anonymous catalog access
- **WHEN** a caller without credentials requests public centers or needs
- **THEN** the API returns public discovery data without exposing account data or requiring registration

#### Scenario: Contract validation
- **WHEN** client/server contract tests run
- **THEN** successful, unauthorized, invalid, conflict and unavailable responses match the published API schema

### Requirement: Durable Transactional Authority
The backend SHALL durably store accounts, center-role assignments, catalog revisions, contribution records/events, preferences, reminders, visits and operation outcomes. Related capacity and contribution changes SHALL commit atomically. Process restarts SHALL not lose acknowledged writes or reset quantities.

#### Scenario: Restart after acknowledging an offer
- **WHEN** the service restarts after returning a successful offer response
- **THEN** history, reservation and operation outcome remain available and consistent

### Requirement: Scoped Access and Role Provisioning
The backend SHALL enforce current-user access and center-scoped staff actions on every request. Only authorized operators SHALL provision center staff or publish imports through an auditable operational path. Assigned staff SHALL manage only their center's needs and contributions. No native or web client SHALL self-grant a role. Center membership SHALL NOT change official verification status.

#### Scenario: Staff from center A accesses center B
- **WHEN** staff assigned only to A requests B's private contribution actions
- **THEN** access is denied, even when the caller knows the contribution identifier

#### Scenario: Provision staff without a portal
- **WHEN** an authorized operator assigns a user to a center through the controlled operational interface
- **THEN** the assignment and actor are audited and no official verification is inferred

### Requirement: Browser Request Protection
The backend SHALL authenticate and authorize browser writes independently of client UI and origin checks. Browser credential handling SHALL resist cross-site request forgery; cross-origin access SHALL be restricted to configured origins. Session expiry/revocation SHALL deny further private reads and writes. Secrets SHALL not be embedded in public web assets or exposed in URLs and diagnostics.

#### Scenario: Forged browser write
- **WHEN** an untrusted site attempts a need write using ambient browser credentials without the required request protection
- **THEN** the request is rejected without changing the need, even if the victim has a valid staff session

#### Scenario: Staff access is revoked during a session
- **WHEN** an operator revokes a center assignment and the browser still holds its prior session
- **THEN** subsequent requests for that center's private data or actions are denied by the backend

### Requirement: Incremental Account Synchronization
Synchronization SHALL return authorized changes and deletions using stable versions/cursors, with a documented full-resync path for expired cursors. Duplicate or overlapping pages SHALL be safe to apply. Public catalog sync SHALL remain separate from private account sync.

#### Scenario: Resume interrupted synchronization
- **WHEN** a client repeats a page after interruption
- **THEN** records are merged once by ID/version without losing later changes or exposing another account

#### Scenario: Cursor is no longer valid
- **WHEN** the backend cannot honor a saved cursor
- **THEN** it requests full resynchronization while the client preserves its separate unsent operations

### Requirement: Trusted Writes and Safe Failures
The service SHALL validate payloads, enforce idempotency and rate limits, use authenticated encrypted transport outside local development, and keep secrets outside mobile bundles and source control. Failures SHALL return stable error codes without credentials, stack traces or private data. Retries SHALL not repeat committed side effects.

#### Scenario: Invalid or excessive requests
- **WHEN** a caller submits malformed data or exceeds an endpoint limit
- **THEN** the service returns a documented validation or rate-limit outcome with no partial domain write

### Requirement: Reliable Notification Events
Accepted events that require alerts SHALL be recorded durably with the corresponding domain write. Delivery workers SHALL retry transient failures without generating duplicate logical alerts and SHALL recheck opt-in and current data eligibility. Invalid or signed-out device registrations SHALL stop receiving private messages.

#### Scenario: Push delivery is unavailable during a need update
- **WHEN** the catalog update succeeds but the delivery provider fails
- **THEN** the update remains committed and its eligible alert can be retried without republishing the need

### Requirement: Operable and Testable Service
The backend SHALL include reproducible local startup, schema migrations, isolated test data, health/readiness checks and backup/restore verification for durable data. Production deployment SHALL require configured identity, notification and catalog sources; demo identities SHALL not be silently enabled.

#### Scenario: Clean local environment
- **WHEN** a developer follows the documented local setup
- **THEN** the API, database and explicitly labeled development fixtures start and authorization/concurrency integration tests can run without production credentials

#### Scenario: Recovery verification
- **WHEN** a backup is restored into an isolated environment
- **THEN** acknowledged contributions, capacity, ownership and history remain consistent
