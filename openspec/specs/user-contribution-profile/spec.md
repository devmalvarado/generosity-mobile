# user-contribution-profile Specification

## Purpose
Define anonymous and registered user behavior for discovery, contribution history, generosity points, user-defined helping activities, and contribution reminders.

## Requirements
### Requirement: Anonymous User Mode
The mobile app SHALL support anonymous users for discovery and basic helping flows that do not require contribution history sync.

#### Scenario: User skips registration
- **WHEN** the user chooses not to register
- **THEN** the app allows discovery of Kindness Centers and viewing of needs

### Requirement: Registered User Mode
The mobile app SHALL support a registered user state for features that require identity, synchronization, or long-term contribution tracking.

#### Scenario: User signs in
- **WHEN** the user completes authentication through a supported provider
- **THEN** the app recognizes the user as registered

### Requirement: Contribution History
The mobile app SHALL allow registered users to view a history of recorded contributions when contribution data is available.

#### Scenario: Registered user has contributions
- **WHEN** a registered user opens their contribution history
- **THEN** the app displays recorded contributions associated with that user

#### Scenario: Registered user has no contributions
- **WHEN** a registered user opens their contribution history with no recorded contributions
- **THEN** the app displays an empty contribution history state

### Requirement: Generosity Points
The mobile app SHALL display generosity points for registered users when points data is available.

#### Scenario: User has points
- **WHEN** a registered user opens their profile and points data is available
- **THEN** the app displays the user's generosity points

### Requirement: Points Are Non-Monetary
Generosity points SHALL be informational and SHALL NOT be presented as cash, credit, a redeemable benefit, or a payment instrument.

#### Scenario: User views points
- **WHEN** the app displays generosity points
- **THEN** the app does not describe the points as redeemable money or financial value

### Requirement: User-Defined Helping Activities
The mobile app SHALL allow registered users to record their own helping activities when the required profile and storage capabilities are available.

#### Scenario: User records personal help
- **WHEN** a registered user records a helping activity not tied to a specific published need
- **THEN** the app saves the activity to the user's contribution history

### Requirement: Contribution Reminders
The mobile app SHALL allow registered users to create reminders for intended helping activities when notification permissions are enabled.

#### Scenario: User creates a reminder
- **WHEN** a registered user schedules a reminder for a helping activity
- **THEN** the app stores the reminder and schedules an eligible notification

### Requirement: Shared Contribution Recording
The shared module SHALL require registered user state before recording a contribution, SHALL resolve a selected need by its identifier against the current catalog, and SHALL reject missing, closed, expired or method-incompatible needs. Recorded offers SHALL start in the offered state and remain associated with the user and, when applicable, the selected center and need. Registration state in a demo SHALL NOT be treated as evidence of provider authentication.

#### Scenario: Anonymous user offers help
- **WHEN** an anonymous user submits a contribution offer
- **THEN** shared logic returns registration_required without changing the need or history

#### Scenario: Selected need no longer exists
- **WHEN** the app submits an offer for a need identifier absent from the catalog
- **THEN** shared logic returns need_not_found rather than silently recording personal help

#### Scenario: Registered user offers an accepted method
- **WHEN** a registered user submits a valid offer to an active, non-expired need that accepts the method
- **THEN** the contribution is recorded as offered and linked to that user, need and center

#### Scenario: Need does not accept the offered method
- **WHEN** a registered user offers a help method not accepted by the current need
- **THEN** shared logic rejects the offer without changing the need or history

### Requirement: Impact Qualification
The shared module SHALL include only confirmed or fulfilled contributions in personal impact aggregates. Offered and cancelled contributions SHALL NOT count as completed impact. Goods totals SHALL sum recorded goods quantities, while supported center and need totals SHALL count distinct non-null identifiers. These aggregates SHALL NOT imply payment confirmation or an implemented contribution-status transition workflow.

#### Scenario: Contribution is still an offer
- **WHEN** a user's history contains only offered or cancelled contributions
- **THEN** those entries remain excluded from personal impact

#### Scenario: Contributions qualify for impact
- **WHEN** a user's history includes confirmed or fulfilled goods contributions for the same center
- **THEN** shared impact sums their quantities and counts the center only once
