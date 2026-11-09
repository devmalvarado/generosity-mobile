## ADDED Requirements

### Requirement: Side-by-side applications
The system SHALL build production and preview from the same source with distinct
identifiers, localized display names, identifiable icons and login callbacks.
Existing production installations SHALL retain their storage identity.

#### Scenario: Both apps are installed
- **WHEN** production and preview builds are installed on one device
- **THEN** they coexist as Generosity and Generosity Pruebas with different icons
- **AND** signing out, changing preferences or queuing work in one does not alter the other

### Requirement: Preview resource isolation
Preview SHALL use a separate API/database and provider configuration. It SHALL
reject known production API/Firebase settings and fail startup if its database,
origins, cookie scope or outgoing mail configuration violates isolation.

#### Scenario: Production configuration is accidentally copied
- **WHEN** a preview build contains the production API/Firebase project or a
  preview server points at the production database or parent-domain cookie
- **THEN** validation fails before that build or deployment starts serving requests

#### Scenario: Providers have not been provisioned
- **WHEN** a preview build has no dedicated Firebase configuration
- **THEN** it does not inherit production Firebase credentials
- **AND** preview push stays unavailable until separately configured

### Requirement: Environment-bound authentication
Preview and production SHALL accept only their configured native callback clients
and retain PKCE/state validation. A supplied mismatched channel header SHALL be
rejected on API and hosted authentication requests.

#### Scenario: Preview tries a production callback
- **WHEN** a preview sign-in supplies the production native client identifier
- **THEN** the backend rejects the context and never returns a production callback

### Requirement: Explicit preview portal packaging
The portal SHALL support a separately packaged preview API origin and channel
without editing each API caller or changing the production landing deployment.

#### Scenario: Preview artifact is prepared
- **WHEN** the preview packaging command is run with a valid separate HTTPS origin
- **THEN** it writes an isolated build artifact containing that origin and preview channel
- **AND** production source configuration and published hosting are not changed

### Requirement: API-first publication with preserved environment state
Coordinated API and portal releases SHALL publish required API routes before
uploading the dependent portal files. API release packages SHALL exclude private
environment files, runtime credentials and backups; normal code publication SHALL
retain existing environment data and SHALL NOT import a preview database or reset
production. An initial catalog import SHALL require explicit opt-in.

#### Scenario: Required API is unavailable
- **WHEN** the feature API or configured account/intake/legal prerequisite checks fail
- **THEN** the portal workflow waits for the API release
- **AND** it fails without uploading portal files if those checks do not recover

#### Scenario: Release changes the configuration schema
- **WHEN** the new API files are extracted
- **THEN** the publisher removes the previous Laravel configuration cache before bootstrapping providers
- **AND** it applies incremental migrations and rebuilds configuration/route/view caches using the retained private environment

#### Scenario: Authentication asset inventory is invalid
- **WHEN** the public authentication assets contain a directory or an unsupported file type
- **THEN** the publisher rejects that inventory before any remote mutation
- **AND** generated CSS, PNG, WOFF2 and TXT/MD notices remain supported
