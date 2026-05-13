# location-notifications Specification

## Purpose
Define notification behavior for nearby Kindness Centers, urgent needs, and user-created reminders, including opt-in requirements, permission boundaries, and user controls.

## Requirements
### Requirement: Notification Opt-In
The mobile app SHALL require explicit user opt-in before sending push notifications or location-based notifications.

#### Scenario: User has not opted in
- **WHEN** the app detects a nearby center or urgent need
- **THEN** the app does not send a notification to that user

#### Scenario: User opts in
- **WHEN** the user enables notifications through the app and platform permission flow
- **THEN** the app can send eligible notifications according to the user's preferences

### Requirement: Location Permission Boundary
The mobile app SHALL require explicit location permission before using the user's location for nearby-center notifications.

#### Scenario: Location permission is denied
- **WHEN** the user denies location permission
- **THEN** the app disables nearby-center notifications that depend on current location

### Requirement: Nearby Center Alerts
The mobile app SHALL support notifying opted-in users when they are near an eligible Kindness Center.

#### Scenario: User enters nearby range
- **WHEN** an opted-in user enters the configured range for an eligible Kindness Center
- **THEN** the app sends or schedules a nearby-center notification according to platform constraints

### Requirement: Urgent Need Alerts
The mobile app SHALL support notifying opted-in users about urgent needs that match their configured interests when urgent need data is available.

#### Scenario: Urgent need matches preferences
- **WHEN** an urgent need matches an opted-in user's notification preferences
- **THEN** the app sends or displays an urgent-need notification

### Requirement: Reminder Alerts
The mobile app SHALL support reminder notifications for helping activities explicitly scheduled by the user.

#### Scenario: Reminder time arrives
- **WHEN** the scheduled time for a user-created helping reminder arrives
- **THEN** the app sends or displays the reminder notification

### Requirement: Notification Controls
The mobile app SHALL provide controls for disabling notification categories including nearby centers, urgent needs, and reminders.

#### Scenario: User disables urgent need alerts
- **WHEN** the user disables urgent need notifications
- **THEN** the app stops sending urgent need notifications to that user
