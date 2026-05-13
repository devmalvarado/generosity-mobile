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
