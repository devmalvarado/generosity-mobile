# kindness-center-needs Specification

## Purpose
Define how the mobile app displays read-only Kindness Center needs, supported help methods, urgency, expiration, and the boundary around money donation handoff.

## Requirements
### Requirement: Current Needs Display
The mobile app SHALL display current needs for a Kindness Center when those needs are available from imported data or future backend data.

#### Scenario: Center has current needs
- **WHEN** the user opens a Kindness Center with current needs
- **THEN** the app displays those needs on the center detail view

#### Scenario: Center has no current needs
- **WHEN** the user opens a Kindness Center without current needs
- **THEN** the app indicates that no current needs are listed

### Requirement: Need Help Methods
The mobile app SHALL represent needs by supported help methods: donating time, donating goods, and donating money.

#### Scenario: User views need help methods
- **WHEN** a need supports one or more help methods
- **THEN** the app displays the available help methods for that need

### Requirement: Money Donation Boundary
The mobile app SHALL NOT process bank payments, store payment details, or collect nonprofit bank account information as part of this mobile foundation.

#### Scenario: Money donation is not configured
- **WHEN** a need includes donating money but no backend-provided payment session or external payment handoff is available
- **THEN** the app does not start a payment flow

#### Scenario: Future payment handoff is configured
- **WHEN** a future backend provides an approved payment session or external payment handoff
- **THEN** the app can hand off the user without storing payment details locally

### Requirement: Urgent Needs
The mobile app SHALL distinguish urgent needs from regular needs when urgency metadata is available.

#### Scenario: Center has an urgent need
- **WHEN** the user views a Kindness Center with an urgent need
- **THEN** the app visually identifies the need as urgent

### Requirement: Need Expiration
The mobile app SHALL avoid presenting expired needs as current when expiration metadata is available.

#### Scenario: Need is expired
- **WHEN** a need has an expiration date in the past
- **THEN** the app excludes the need from the current needs list

### Requirement: Read-Only Needs
The mobile app SHALL treat Kindness Center needs as read-only content in this change.

#### Scenario: Center staff wants to publish a need
- **WHEN** a center staff member needs to create or update a need
- **THEN** the mobile app does not provide center-management controls
