## Purpose
Define real-time behavior, timestamp authority, date-only expiration and timezone-aware reminder scheduling consistently across mobile and backend systems.

## ADDED Requirements

### Requirement: Real Clock and Server Authority
Production flows SHALL use a real clock instead of fixed demo dates. The backend SHALL use its own current time to validate need expiration and record accepted lifecycle events. Offline client timestamps SHALL be retained as submitted context, not authoritative proof. Tests SHALL be able to supply deterministic clocks.

#### Scenario: Device clock differs from server
- **WHEN** a device submits an offer for a need already expired by server time
- **THEN** the backend rejects it even if the local screen still considered it current

#### Scenario: App remains open past expiration
- **WHEN** time passes while the app is foregrounded or the app resumes
- **THEN** current needs are reevaluated rather than remaining current until a process restart

### Requirement: Unambiguous Expiration
Timestamp expirations SHALL use explicit offsets and be compared as instants. A legacy date-only need expiration SHALL be inclusive through that UTC calendar date and expire at the next UTC midnight. Both apps and the backend SHALL apply the same rule and label dates for the user's active locale.

#### Scenario: Date-only boundary
- **WHEN** a need expires on 2026-12-31
- **THEN** it is current through that UTC date when otherwise active, and expired at 2027-01-01T00:00:00Z

### Requirement: Reminder Instant and Timezone
Reminder creation SHALL interpret the chosen local date/time using a displayed timezone and save a corresponding absolute instant. A later device timezone change SHALL preserve that instant and update the displayed local time. Invalid or ambiguous daylight-saving times SHALL require correction or an explicit occurrence choice.

#### Scenario: Travel after scheduling
- **WHEN** the device timezone changes after a reminder is saved
- **THEN** the reminder retains its absolute due instant and displays the equivalent time in the new zone

#### Scenario: Ambiguous local time
- **WHEN** the selected wall-clock time occurs twice or does not exist in the chosen timezone
- **THEN** the form asks for a valid unambiguous time before saving

### Requirement: Localized Dates and Durations
User-facing dates, times, quantities and durations SHALL use the active locale without changing stored instants or numeric values. A time-contribution count SHALL NOT be formatted as an hours duration.

#### Scenario: Change language with an existing reminder
- **WHEN** the user changes from English to Spanish
- **THEN** the displayed date/time format changes but the reminder instant does not
