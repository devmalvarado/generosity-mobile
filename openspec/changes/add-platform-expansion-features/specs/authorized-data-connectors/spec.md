## Purpose
Keep permitted external catalog and scheduling data current through controlled connectors without unauthorized harvesting, sensitive-data leakage or destructive overwrites.

## ADDED Requirements

### Requirement: Authorized Source Configuration
Each connector SHALL require a reviewed permitted interface or supplied dataset, license/consent scope, allowed fields, update policy, owner and secret configuration before activation. Third-party page accessibility SHALL not establish reuse permission. A named service without a supported authorized interface SHALL remain unavailable; the system SHALL not invent integration success or bypass access controls.

#### Scenario: Request a Meal Train integration without an approved interface
- **WHEN** no permitted API/export agreement is configured
- **THEN** automatic synchronization stays disabled and only explicitly supported consented import alternatives are offered

### Requirement: Validated Staged Imports
Connector runs SHALL stage bounded data, validate schema/coordinates/timezones and show an auditable acceptance/rejection result before replacing current records. Stable source IDs and revisions SHALL make retries idempotent. Malformed or partial runs SHALL not wipe a usable catalog. Imported content SHALL be inert and untrusted, not executable instructions or authorization grants.

#### Scenario: Source returns an empty failed page
- **WHEN** a source outage resembles an empty dataset
- **THEN** the existing catalog is retained and the run is marked failed rather than deleting every center

### Requirement: Ownership and Conflict Preservation
Connectors SHALL preserve field provenance and local ownership. Staff edits, commitments, verified status and private records SHALL not be overwritten by lower-authority source data. Deletions and duplicates SHALL require explicit reconciliation with stable public identities/history preserved. Schedules SHALL import as reviewed drafts and never auto-enroll participants.

#### Scenario: Imported hours conflict with staff-maintained hours
- **WHEN** a connector supplies older values for a locally managed center
- **THEN** local data remains authoritative and the conflict is recorded for review

### Requirement: Minimal Safe Data Exchange
Connectors SHALL exchange only approved fields for their declared purpose. Household identifiers, contacts, dietary details, payment credentials and private rosters SHALL not enter general catalog feeds. User-authorized scheduling imports SHALL not imply marketing or public disclosure consent. Endpoint/redirect validation, timeouts, rate limits and bounded parsing SHALL protect server and external resources.

#### Scenario: External schedule includes recipient addresses
- **WHEN** a catalog/schedule connector receives fields outside its approved scope
- **THEN** those fields are rejected or discarded before persistence/publication and the incident is reported without logging their values

### Requirement: Controlled Operations and Exit
Operators SHALL inspect run status, freshness and safe diagnostics, pause new runs, rotate credentials and revoke a source. Revocation SHALL stop new access and apply license/retention obligations without erasing unrelated local history. Scheduled runs SHALL use bounded retries/backoff and expose stale data honestly in native/web views.

#### Scenario: Source authorization is withdrawn
- **WHEN** the configured data owner revokes permission
- **THEN** new fetches stop, affected publication is reviewed under the source terms and unrelated center-authored content remains intact
