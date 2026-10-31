## ADDED Requirements

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

### Requirement: User-Entered Contribution Forms
Need-linked offers SHALL collect user-selected help method, goods quantity/unit when applicable, time activity/availability/duration when applicable, and optional notes. The form SHALL display the selected center/need and current availability. Neither platform SHALL silently substitute fixed demo quantities, activities or availability.

#### Scenario: Offer goods
- **WHEN** a registered user enters three units for a need with ten available
- **THEN** the submission contains three units, the displayed need ID and the user's notes rather than a hard-coded quantity

#### Scenario: Offer time
- **WHEN** a user supplies an activity, availability and positive planned duration
- **THEN** the recorded offer retains those values and their units

### Requirement: Form Validation and Submission Feedback
Forms SHALL reject nonpositive quantities/durations, missing required activity/availability and values outside the API's documented limits. They SHALL show localized field/domain errors, prevent duplicate submissions while a request is in flight, and retain input on errors. Queued, accepted and rejected outcomes SHALL be visually distinct.

#### Scenario: Invalid quantity
- **WHEN** a user enters zero, a negative quantity or a quantity exceeding known remaining capacity
- **THEN** the form explains the error and does not claim that an offer was accepted

#### Scenario: Registration required during helping
- **WHEN** a guest attempts to record an offer
- **THEN** the app offers real sign-in/registration, preserves the intended need and entered draft, and revalidates availability after authentication

### Requirement: Personal Activity Form
Registered users SHALL have a personal-help form not tied to a published need, collecting a meaningful activity, occurrence/schedule details, help method and applicable quantity or duration. The form SHALL label the activity self-reported and SHALL not offer a simulated monetary payment.

#### Scenario: Record personal volunteering
- **WHEN** a user records their own completed volunteering with a duration and note
- **THEN** history shows it as self-reported personal help with the entered duration, not as a center-verified action

### Requirement: Detailed Contribution History
History SHALL show each owned record's title, center/need or personal origin, method, quantity/duration, authoritative lifecycle state, synchronization state and relevant timestamps. It SHALL provide permitted detail/actions, pagination and distinct loading, empty, offline and error states. Retried operations SHALL not appear as duplicate entries.

#### Scenario: Pending offline cancellation
- **WHEN** a user requests cancellation without connectivity
- **THEN** history shows cancellation pending separately from the last acknowledged lifecycle state until the backend responds

### Requirement: Truthful Impact Units
Impact SHALL distinguish planned/confirmed from completed time, use recorded durations rather than entry counts, and group goods by compatible units. Missing durations or monetary amounts SHALL display unavailable rather than invented totals. Money totals SHALL require confirmed provider data; no such provider is included here. Personal self-reported impact SHALL be labeled separately.

#### Scenario: Time contribution has no measured duration
- **WHEN** a historical record lacks duration data
- **THEN** it is not counted as one hour and the app indicates that duration is unavailable

#### Scenario: Money provider is not configured
- **WHEN** the impact view has no trusted monetary data
- **THEN** it shows an unavailable monetary metric rather than converting contribution counts into currency

## MODIFIED Requirements

### Requirement: Impact Qualification
Only authoritative confirmed or fulfilled records SHALL qualify for impact. Pending, offered, rejected and cancelled entries SHALL be excluded. Goods SHALL be totaled by compatible unit; supported centers/needs SHALL count distinct IDs. Self-reported personal help SHALL be separated from center-confirmed impact. Confirmed and completed totals SHALL be labeled distinctly, with no invented points or monetary value.

#### Scenario: Contribution is still an offer
- **WHEN** a user's history contains only offered or cancelled contributions
- **THEN** those entries remain excluded from personal impact

#### Scenario: Contributions qualify for impact
- **WHEN** a user's history includes confirmed or fulfilled goods contributions with compatible units for the same center
- **THEN** shared impact sums their quantities and counts the center only once

#### Scenario: Goods use incompatible units
- **WHEN** qualifying records contain both liters and item counts
- **THEN** impact reports separate unit totals rather than summing them into one unlabeled number
