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
