## Purpose
Coordinate dated meal support for consenting recipients with reliable commitments and minimal private delivery information, without assuming that a reservation is a delivered meal.

## ADDED Requirements

### Requirement: Recipient-Approved Meal Plan
A meal campaign SHALL require recorded approval from the recipient or a documented authorized representative before scheduling deliveries. Approval SHALL cover dates, portions, necessary preferences and permitted recipients of delivery details. The recipient SHALL be able to pause, correct or withdraw through a private channel without installing the app. Public content SHALL omit identity, address, medical narrative and dietary restrictions.

#### Scenario: Organizer sets up support for someone else
- **WHEN** an eligible organizer enters a meal plan without recorded recipient approval
- **THEN** it stays private and unavailable for commitments or address disclosure

#### Scenario: Recipient withdraws approval
- **WHEN** the recipient withdraws through the supported private channel
- **THEN** future deliveries stop, assignees receive a generic cancellation notice, and private access is revoked under the retention policy

### Requirement: Bounded Dated Meal Slots
Each slot SHALL have a local date, delivery window, timezone, portion unit, required quantity and cutoff. The system SHALL reject invalid or past scheduling and commit available portions atomically with stable operation IDs. Reservations SHALL identify the responsible user and quantity; an offline request SHALL not claim availability. Adding dates SHALL create explicit slots, not an unbounded repeating commitment.

#### Scenario: Two donors reserve remaining portions
- **WHEN** simultaneous requests exceed a slot's remaining portions
- **THEN** accepted commitments do not exceed the requirement and rejected requests show current availability without silent partial acceptance

### Requirement: Assignment-Limited Delivery Disclosure
Only the assigned donor approved by the recipient or authorized coordinator SHALL receive minimal delivery instructions within the approved access window. Coordinators and recipients SHALL retain scoped access. Reservation alone SHALL not disclose an address, contacts or dietary details. Private views SHALL require authenticated authorization; public links, lock-screen notices and analytics SHALL exclude those fields.

#### Scenario: Another volunteer opens a delivery link
- **WHEN** an authenticated but unassigned volunteer knows the slot identifier
- **THEN** private delivery information is denied even if the volunteer belongs to the same campaign

#### Scenario: Unreviewed donor reserves a slot
- **WHEN** a newly assigned donor has not been approved for private delivery access
- **THEN** the donor sees a pending coordination state and no address or dietary details until approval or cancellation

### Requirement: Safe Changes and Cancellation
Recipient or coordinator changes to delivery windows SHALL require affected donors to explicitly accept a replacement commitment. Changed dietary instructions SHALL require acknowledgment before further delivery actions. Donor cancellation SHALL release unfulfilled future capacity once and notify the coordinator/recipient; late cancellation SHALL be flagged for coordination, not hidden or presented as delivery. Past receipt records SHALL not be overwritten.

#### Scenario: Recipient changes a dietary restriction
- **WHEN** instructions change after a donor has reserved a slot
- **THEN** the donor is notified privately and must acknowledge the new instructions or cancel; the previous acknowledgment is not reused

### Requirement: Delivery and Receipt Provenance
Meal history SHALL distinguish reservation, donor-reported delivery, recipient-confirmed receipt, cancellation and missed delivery. A coordinator recording receipt on someone's behalf SHALL identify that provenance. Duplicate updates SHALL not add portions twice. Receipt alone SHALL not create center-validated help, gamification or review eligibility, and paid meals SHALL not imply an in-app payment transaction.

#### Scenario: Donor reports delivery without recipient confirmation
- **WHEN** a donor marks portions as delivered
- **THEN** the slot shows reported delivery with receipt pending, not independently verified impact or a points award

#### Scenario: Receipt update is retried
- **WHEN** the same confirmed receipt operation is replayed
- **THEN** delivered/received quantities and history reflect one receipt only

### Requirement: Private Accessible Scheduling
Both native apps SHALL support slot discovery, commitment, private instructions and owned history; the mobile-first portal SHALL manage recipient-approved plans and coordination. Spanish/English labels, local dates, portions and status SHALL be consistent. Users SHALL be able to decline unsupported dietary needs; the system SHALL not promise allergen safety, clinical suitability or successful delivery.

#### Scenario: Volunteer cannot meet a restriction
- **WHEN** an assigned volunteer reviews instructions they cannot safely meet
- **THEN** they can decline/cancel without exposing the restriction publicly and the coordinator sees the unfilled requirement
