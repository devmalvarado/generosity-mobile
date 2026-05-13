# center-verification-signals Specification

## Purpose
TBD - created by archiving change define-generosity-mobile-app-foundation. Update Purpose after archive.
## Requirements
### Requirement: Verification Status Display
The mobile app SHALL display a Kindness Center verification status when that status is available from imported data or future backend data.

#### Scenario: Center has verification status
- **WHEN** the user opens a Kindness Center with verification status
- **THEN** the app displays the status on the center detail view

### Requirement: User Visit Signal
The mobile app SHALL allow a user to submit an explicit visit signal for a Kindness Center when visit-signal capture is available.

#### Scenario: User submits visit signal
- **WHEN** the user explicitly marks that they visited a Kindness Center
- **THEN** the app records or queues a visit signal for that center

### Requirement: No Passive Visit Submission
The mobile app SHALL NOT submit a visit signal solely because the user is near a Kindness Center.

#### Scenario: User passes near a center
- **WHEN** the user's device is near a Kindness Center but the user takes no explicit visit action
- **THEN** the app does not submit a visit signal

### Requirement: Visit Signal Is Not Official Verification
A user visit signal SHALL NOT mark a Kindness Center as officially verified.

#### Scenario: Visit signal is submitted
- **WHEN** a user submits a visit signal for an unverified Kindness Center
- **THEN** the app does not change the center to officially verified

### Requirement: Official Verification Boundary
Official Kindness Center verification SHALL require a backend or backoffice workflow outside this mobile-app foundation.

#### Scenario: Center needs official verification
- **WHEN** a Kindness Center needs to become officially verified
- **THEN** the mobile app does not provide the full verification workflow for center staff

### Requirement: Minimal Visit Evidence
The mobile app SHALL collect only the minimum information needed for a visit signal and SHALL associate sensitive location evidence only with explicit user action.

#### Scenario: Visit signal includes location evidence
- **WHEN** the app attaches location evidence to a visit signal
- **THEN** the app does so only after explicit user action and applicable permission

