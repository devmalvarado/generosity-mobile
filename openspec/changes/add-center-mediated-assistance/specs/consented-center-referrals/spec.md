## Purpose
Let a helper send a minimal, consented heads-up to a chosen center and track actual contact and assessment without requiring the person seeking help to install software or expose private circumstances.

## ADDED Requirements

### Requirement: Consented Minimal Heads-Up
An authenticated helper SHALL be able to notify one eligible center that a person may visit or call about a service category. Submission SHALL record the person's informed permission for that recipient and purpose, using a supported understandable consent flow. Name, phone, address, diagnosis and documents SHALL not be required. Optional contact and status sharing with the helper SHALL have separate permissions.

#### Scenario: Person agrees to an advance notice
- **WHEN** a person agrees that the helper can alert the chosen center to a possible medicine-assistance visit
- **THEN** the helper can send the category, intended visit/call channel and optional estimated contact window without a diagnosis, prescription, identity document or account for that person

#### Scenario: Person declines notification
- **WHEN** the person does not consent to the heads-up
- **THEN** no identifiable referral or refusal record is created, while the helper can still share public directions and the person can contact the center independently

### Requirement: Heads-Up Is Not Acceptance
The system SHALL distinguish locally unsent, server-received, center-acknowledged, actual contact and assessment outcomes. Only durable server acknowledgement SHALL establish submission; only an authorized center action SHALL establish center acknowledgement. A heads-up SHALL NOT reserve attendance, validate a need, create a purchase/payment commitment or count as help delivered.

#### Scenario: Notice arrives but the person has not called
- **WHEN** the center acknowledges a heads-up but has no actual contact with the person
- **THEN** the helper sees awaiting contact, no appointment or accepted-need claim is shown, and case-specific purchasing and donation actions remain unavailable

#### Scenario: Notification delivery is retried
- **WHEN** the submission response or an inbox/push notification is lost and retried
- **THEN** the original notice is recovered by its operation identity without duplicate cases, repeated acknowledgement or sensitive text in lock-screen previews

### Requirement: Accessible Non-Account Reference
A referral SHALL provide a readable, non-enumerable reference for visiting or calling the named center, with an optional QR representation. The reference SHALL assist authorized staff lookup, not grant public case access or authorize fulfillment. Losing it SHALL lead to a private assistance route without mandatory account creation, biometrics or automatic identity matching across centers.

#### Scenario: Person calls from someone else's phone
- **WHEN** the person contacts the center using the reference without their own phone or account
- **THEN** assigned staff can privately locate the notice under the approved identity/consent procedure and record contact without requiring app installation

#### Scenario: A third party finds the paper reference
- **WHEN** someone enters a copied reference outside an authorized case session
- **THEN** no name, need, arrival state or donation details are disclosed, and repeated guessing is rate-limited

### Requirement: Contact Before Need Acceptance
Only assigned center staff SHALL record actual in-person or telephone contact and assess whether the center accepts the need. Contact channel, actor, time and outcome SHALL be auditable. Acceptance SHALL require actual contact, confirmed current consent and the applicable service review. Helper assertions, geolocation, notification delivery and elapsed time SHALL not substitute for center assessment.

#### Scenario: Person attends or calls and the center accepts
- **WHEN** assigned staff records actual contact and accepts the assessed need under the service policy
- **THEN** the backend can authorize a versioned assistance request while showing only consented status to the helper

#### Scenario: Client skips assessment
- **WHEN** a helper or modified client attempts to mark the need accepted or fund it before center validation
- **THEN** the backend denies the action without creating a commitment, checkout or purchase instruction

### Requirement: Case-Scoped Access And Disclosure
Private referral fields SHALL be visible only under current recipient-center, intake-role and case-assignment permissions. Helpers and donors SHALL receive only explicitly authorized progress and assistance fields, not clinical records or unrelated history. Organizer status, points, membership in another center or knowledge of an identifier SHALL not grant access. Reads and transitions SHALL be audited without sensitive diagnostic logs.

#### Scenario: Another center or donor requests private notes
- **WHEN** an actor outside the case assignment requests its private record or export
- **THEN** the backend denies access even if the actor has a center role or previously made a donation

#### Scenario: Person declines progress disclosure
- **WHEN** the person has not permitted the helper to learn contact or assessment outcomes
- **THEN** helper projections, available actions and error responses do not reveal arrival or approval, and any case-linked donor action requires its own permitted disclosure scope

### Requirement: Non-Stigmatizing Outcomes And Rerouting
No contact, expiry, withdrawn consent, inability to assist and an unconfirmed need SHALL remain distinct outcomes, not automatic fraud labels or public beneficiary scores. Staff SHALL be able to suggest alternatives; transmitting a case to another center SHALL require new recipient-specific permission. Concerns SHALL use a private evidence-based review/support route with correction and appeal.

#### Scenario: Person never arrives
- **WHEN** a heads-up reaches its configured follow-up deadline without contact
- **THEN** it can expire as no contact, cannot trigger purchases or rewards, and does not blacklist the person or prevent independent future requests

#### Scenario: Center cannot meet the need
- **WHEN** assessment finds the service unavailable or unsuitable
- **THEN** the center can close the referral with an appropriate private outcome and offer public alternatives without silently forwarding the person's record

### Requirement: Withdrawal And Retention
The person SHALL be able to correct or withdraw consent through the center without an account. Withdrawal SHALL stop new disclosure and funding, revoke case access and trigger configured deletion/retention procedures. Minimal required financial and audit records SHALL be separated from beneficiary identifiers. Already purchased/shipped goods and confirmed payments SHALL use documented reconciliation rather than fabricated reversal.

#### Scenario: Withdrawal while support is in progress
- **WHEN** the person withdraws after an approved donor action
- **THEN** new actions are blocked, private disclosure ceases, and existing goods/payment obligations enter the appropriate support or refund process without implying that funds or goods were automatically returned

### Requirement: Safe Localized Case Operation
Native and mobile-first browser flows SHALL use Spanish/English status and consent text, accessible controls and an approved process for other understood languages. Private intake SHALL require configured access/retention policies and online authorization; offline lookup SHALL be limited to public center data in the initial release. Referral creation, contact and assessment counts SHALL not earn points or reveal public beneficiary activity.

#### Scenario: Offline or expired authorization
- **WHEN** the helper or staff member cannot obtain current authorization for a private write
- **THEN** the UI does not report submission/acceptance, exposes no private persistent cache to another account, and keeps public center directions usable
