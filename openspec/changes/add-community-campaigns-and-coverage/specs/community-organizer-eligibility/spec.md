## Purpose
Allow registered users to earn scoped community-organizing access through prior validated helping, without turning points into unrestricted authority or official verification.

## ADDED Requirements

### Requirement: Published Earned Eligibility
Any registered user SHALL qualify for community organizing when their account meets the active policy's minimum level, valid points and distinct center-validated completed-help threshold and is not suspended. The backend SHALL evaluate all criteria using authoritative records and a versioned policy. Missing thresholds SHALL disable organizing rather than supply invented defaults. Payments and self-reported activity SHALL not count.

#### Scenario: Earn organizing access
- **WHEN** a registered user meets every published criterion with valid prior help
- **THEN** the server grants community-organizing eligibility without requiring membership in a center

#### Scenario: Attempt to inflate eligibility
- **WHEN** a client supplies a higher balance, duplicate activity IDs or self-reported help
- **THEN** the server ignores those claims and checks the deduplicated authoritative activity and award records

#### Scenario: Policy is not configured
- **WHEN** the organizing feature has no approved active thresholds
- **THEN** browsing and participation remain available but campaign creation/publication is unavailable with an honest reason

### Requirement: Private Eligibility Progress
Profile and organizer entry SHALL show the owning user's actual criteria, progress, current eligibility and policy version in Spanish or English. Unknown or stale progress SHALL be identified; private history SHALL not become a public ranking. The policy SHALL explain that organizing access has no cash/redemption value and does not certify safety, professional competence or official organization status.

#### Scenario: User is below one threshold
- **WHEN** the user has enough points but insufficient qualifying completed activities
- **THEN** both apps and the portal show the unmet activity criterion and do not enable publication

### Requirement: Scoped Rechecked Authority
The server SHALL recheck eligibility and campaign assignment on creation, publication, new event scheduling, collaborator assignment and material edits. Eligibility SHALL grant no center staff, attendance-validation, analyst or household-record privileges. Sensitive fieldwork SHALL additionally require an approved protocol and explicit assignment. Offline changes SHALL remain pending until server acceptance.

#### Scenario: Eligible organizer requests household records
- **WHEN** an eligible organizer without a case assignment requests identifiable household data
- **THEN** the backend denies access even if that organizer owns the campaign

#### Scenario: Expired eligibility on a queued publication
- **WHEN** an offline publication reaches the server after the user's eligibility has been revoked
- **THEN** publication is denied and the draft is preserved without public exposure

### Requirement: Loss of Eligibility and Safe Handover
Evidence corrections, policy changes or suspension SHALL trigger eligibility reassessment. Loss SHALL block new commitments and place affected future activities on hold, preserving history and notifying participants without disclosing private disciplinary details. The owner SHALL retain a narrow cancellation/handover route unless safety restrictions prohibit it; an authorized operator SHALL then perform those actions. Handover SHALL require an eligible assignee's acceptance.

#### Scenario: Points correction removes eligibility
- **WHEN** a correction puts the organizer below the active policy threshold
- **THEN** new enrollment is stopped, existing commitments are not silently transferred or erased, and cancellation or accepted handover is available

### Requirement: Safeguarding and Audited Decisions
Users SHALL be able to report a campaign or organizer privately. Only a scoped safeguarding operator SHALL suspend organizing, review an appeal or approve a restricted handover, with actor, reason and time recorded. Reports alone SHALL not establish misconduct or automatically revoke points. A suspension SHALL not grant the operator access to unrelated household content.

#### Scenario: Organizer appeals a suspension
- **WHEN** an organizer submits an appeal
- **THEN** its review and outcome are recorded privately while the existing restrictions stay in force until an authorized decision
