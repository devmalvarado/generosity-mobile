## Purpose
Include people who cannot use the app or browser through accountable coordinator assistance, without fabricated accounts, duplicated capacity or automatic rewards.

## ADDED Requirements

### Requirement: Consented Coordinator Entry
An authenticated assigned coordinator SHALL register consented in-person/telephone participation for permitted activities using a non-account participant reference. The record SHALL retain actor, consent method/time and minimal necessary contact data. No email, smartphone or invented login SHALL be required. Assistance SHALL not grant center roles, consent to marketing or access to unrelated household cases.

#### Scenario: Participant has no phone or email
- **WHEN** an assigned coordinator records a consenting participant's attendance reservation
- **THEN** a private non-account reference and accessible confirmation route are available without fabricating credentials

### Requirement: Common Inventory and Lifecycle
Assisted commitments SHALL use the same capacity, validation, cutoff, cancellation and retry rules as self-service. The coordinator SHALL see authoritative outcomes, not bypass full events or quantities. The acting account and participant subject SHALL remain distinct. Offline entries SHALL be pending, not reserved. Payment credentials SHALL never be collected by this flow.

#### Scenario: Assisted and web requests compete
- **WHEN** an assisted entry and a browser enrollment request the last seat concurrently
- **THEN** only one commits and the other receives current availability

### Requirement: Private Confirmation and Correction
Participants SHALL receive their chosen permitted confirmation channel or a paper/reference alternative and a way to correct/cancel through authorized assistance. Roster access SHALL remain assigned and minimal. A public QR or knowledge of a participant's name SHALL not reveal commitments or authenticate a correction. Applicable retention and deletion rules SHALL be explained without requiring account creation.

#### Scenario: Someone presents another participant's name
- **WHEN** an unverified person requests private changes using only a name
- **THEN** the coordinator follows the approved identity/consent check before disclosure or mutation

### Requirement: Controlled Account Association
A participant who later registers SHALL explicitly request association of their own assisted records through a verified claim/review process. Matching names or contact details alone SHALL not transfer history. Association SHALL preserve canonical activity IDs, reconcile duplicate reservations once and require review for conflicts. Sensitive case/referral data SHALL not transfer through this process.

#### Scenario: Existing self-service record overlaps a claim
- **WHEN** a verified account claim identifies an assisted reservation for the same person and occurrence
- **THEN** the overlap is reviewed/reconciled without double capacity, duplicated history or erasing validation provenance

### Requirement: Validation Remains Independent
Assisted attendance and material delivery SHALL distinguish reported from independently center-validated completion. The participant SHALL receive no account awards before verified association; association itself SHALL not validate help. Only eligible evidence under the existing award policy SHALL qualify once after association. Coordinators SHALL not earn rewards for registering others or validate their own organized activity.

#### Scenario: Claim unvalidated assisted history
- **WHEN** a user associates a coordinator-reported activity with their account
- **THEN** it remains unvalidated and adds no points, certificate eligibility or review entitlement
