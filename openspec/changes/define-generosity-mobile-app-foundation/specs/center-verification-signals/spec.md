## MODIFIED Requirements

### Requirement: Verification Status Display
The mobile app SHALL show a checkmark beside the center name only when its verification status is `officially_verified`, in discovery cards, map previews and center details. The icon SHALL have a localized accessible label (Verified / Verificado). Imported and visit-signaled records SHALL show neither a verification badge nor an import-origin label. Internal provenance and verification states SHALL remain unchanged.

#### Scenario: Center has verification status
- **WHEN** the user views a center with `officially_verified` status
- **THEN** its name is accompanied by an accessible verification checkmark

#### Scenario: Center is imported or has a reported visit
- **WHEN** the user views an imported or visit-signaled center
- **THEN** no verification checkmark, Imported listing / Ficha importada label, or public visit-status label appears
- **AND** the internal status remains available without promoting the center to verified

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

## ADDED Requirements

### Requirement: Durable Explicit Visit Receipt
An explicit visit SHALL be durably stored before displaying local success. A registered user's visit SHALL be submitted or queued under that account with a stable operation ID. The app SHALL distinguish saved locally, upload pending, server received and failed states. Retries SHALL not create duplicate backend signals.

#### Scenario: Registered user records a visit offline
- **WHEN** the user explicitly reports a visit without connectivity
- **THEN** the app saves a pending signal that survives restart and uploads once when the same account is authorized and connected

#### Scenario: Storage or upload fails
- **WHEN** saving or uploading a signal fails
- **THEN** the UI shows the appropriate failure/pending state instead of claiming server receipt

### Requirement: Guest Visit Ownership
Anonymous users SHALL be able to save an explicit visit locally without creating an account. Guest visits SHALL remain separate from account data and SHALL not be uploaded or assigned to a later signed-in account without an explicit choice.

#### Scenario: Sign in after a guest visit
- **WHEN** a user signs in on a device containing local guest visits
- **THEN** those visits remain local unless the user explicitly chooses to associate and submit them

### Requirement: Location-Optional Visit Evidence
A user SHALL be able to report an explicit visit without location evidence. Attaching coordinates SHALL require a separate explicit choice and current permission. The backend SHALL validate the center/account association without treating coordinates or submission volume as proof of organizational legitimacy.

#### Scenario: User denies location access
- **WHEN** an explicit visit is reported with location permission denied
- **THEN** the visit can be stored without coordinates and its center remains unverified unless independently verified elsewhere

#### Scenario: Repeated signals for an imported center
- **WHEN** the backend receives multiple user visit signals
- **THEN** it preserves their reported-evidence provenance and does not automatically mark the center officially verified
