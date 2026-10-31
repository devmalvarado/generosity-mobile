## ADDED Requirements

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

### Requirement: Reminder Management
Registered users SHALL be able to create, edit, disable and delete helping reminders with a localized title and a valid future date/time. A durable saved reminder SHALL be distinguishable from an authorized scheduled notification. Editing or deleting SHALL replace or cancel the previous schedule rather than add duplicates.

#### Scenario: Edit a scheduled reminder
- **WHEN** a user moves a reminder to a later time
- **THEN** its previous notification is cancelled and only the updated schedule remains active

#### Scenario: Notification permission is denied
- **WHEN** the user tries to enable a reminder without notification permission
- **THEN** the app explains the missing permission and does not claim that a notification is scheduled

### Requirement: Restart-Safe Reminder Delivery
Local reminders SHALL remain scheduled or be restored from durable data after supported app/device restarts. The app SHALL reconcile OS pending requests with saved records and prevent duplicate logical delivery. Missed reminders SHALL be shown as overdue when revisited, without sending an unbounded backlog of alerts.

#### Scenario: Restart before due time
- **WHEN** the user restarts the device before a saved enabled reminder is due
- **THEN** the app/platform restores eligible scheduling and does not create a second reminder record

### Requirement: Effective Nearby Delivery
Nearby eligibility SHALL use a permitted current location, the configured distance threshold and an eligible current catalog record. It SHALL deduplicate a center's alerts within the configured cooldown. Unsupported background behavior SHALL be identified in settings; foreground-only support SHALL not be described as continuous tracking.

#### Scenario: Remain inside the nearby range
- **WHEN** repeated location updates arrive for the same center inside its cooldown
- **THEN** they do not generate repeated nearby alerts

#### Scenario: Background location is unavailable
- **WHEN** platform authorization or runtime constraints prevent background checks
- **THEN** proximity evaluation waits for a supported state and the UI does not claim always-on detection

### Requirement: Backend Urgent Alert Delivery
The backend SHALL deliver opted-in urgent-need alerts for current needs matching selected categories or an explicit All interests choice. Each installation SHALL register its current locale, token and permission/preference state. Need/event IDs SHALL deduplicate retries; closed, expired or removed needs SHALL be suppressed before delivery attempts.

#### Scenario: Urgent need matches interests
- **WHEN** a new urgent need matches an opted-in account and a valid installation registration exists
- **THEN** an eligible localized alert is submitted to the platform delivery service once per logical event

#### Scenario: Token becomes invalid
- **WHEN** the delivery service reports an invalid registration
- **THEN** the backend deactivates it rather than retrying indefinitely

### Requirement: Notification Destination Safety
Opening an alert SHALL resolve its referenced center, need or reminder under the current session. Deleted data SHALL show an unavailable state; private destinations SHALL require the owning account. Alert text SHALL not include private notes or unnecessary personal data on the lock screen.

#### Scenario: Open an old need alert
- **WHEN** the need has since expired or been removed
- **THEN** the app explains its current state without allowing an invalid new offer

## MODIFIED Requirements

### Requirement: Notification Controls
Settings SHALL independently control nearby, urgent and reminder alerts, display actual OS authorization and persist preferences. Disabling SHALL immediately cancel eligible local schedules and stop local delivery; backend delivery SHALL stop after preference acknowledgement. Offline remote preference changes SHALL be marked pending. Already-delivered or in-flight messages SHALL not be represented as retractable.

#### Scenario: User disables urgent need alerts
- **WHEN** the user disables urgent need notifications
- **THEN** local delivery is suppressed immediately and the backend stops new deliveries once it acknowledges the change

#### Scenario: Disable while offline
- **WHEN** preferences change while the backend is unreachable
- **THEN** the device applies local restrictions, queues the update and shows that remote delivery settings await synchronization

#### Scenario: Permission revoked outside the app
- **WHEN** the user revokes OS notification permission and returns
- **THEN** settings reflect the denial and scheduling/delivery eligibility is reevaluated without assuming a grant
