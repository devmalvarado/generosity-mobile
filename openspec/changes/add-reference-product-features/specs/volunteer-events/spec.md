## Purpose
Let centers publish volunteer activities and users enroll safely, with capacity control and validated attendance rather than treating reservations as completed impact.

This capability governs center-owned events. The later
[community-campaigns](../../../add-community-campaigns-and-coverage/specs/community-campaigns/spec.md)
contract adds a separate owner type using the same reservation mechanics,
without granting community organizers center staff or validation authority.

## ADDED Requirements

### Requirement: Authorized Event Publication
Assigned center staff SHALL manage private drafts and publish events with localized title/description, activity, start/end instants, timezone, public venue and positive capacity. Invalid or past intervals SHALL be rejected. Published events SHALL be browsable anonymously from Needs and center detail. Private drafts and attendee data SHALL not enter public responses.

#### Scenario: Publish a valid event
- **WHEN** staff publishes an upcoming event for their assigned center
- **THEN** both native apps and public event views receive the same stable ID, times, venue and available capacity

### Requirement: Atomic Enrollment
Enrollment SHALL require authentication, reserve one seat per user/event and commit capacity atomically. Duplicate requests SHALL return the existing enrollment. Full, cancelled, started or closed events SHALL reject new enrollment. An offline request SHALL not imply a reserved seat until acknowledged. Public sign-up SHALL never grant staff authority.

#### Scenario: Compete for the final seat
- **WHEN** two users request the event's last available seat concurrently
- **THEN** only one enrollment commits and the other receives current availability without overbooking

#### Scenario: Guest enters enrollment
- **WHEN** a guest chooses to join and authenticates
- **THEN** the intended event is restored and revalidated before an explicit enrollment submission

### Requirement: Cancellation and Material Changes
Users SHALL be able to cancel their enrollment before the displayed start-time cutoff, releasing its seat exactly once. Staff SHALL be able to cancel a non-completed event with a reason, preserving enrollment history and notifying affected users. With enrollments present, changing event times or venue SHALL require cancellation and a replacement event, not silent rescheduling. Capacity SHALL not be lowered below reserved seats.

#### Scenario: Retried cancellation
- **WHEN** a user repeats an acknowledged enrollment cancellation
- **THEN** the seat is released only once and the cancelled enrollment remains in history

#### Scenario: Organizer changes an enrolled event's venue
- **WHEN** staff attempts to replace the venue after people have enrolled
- **THEN** the update is rejected with a cancellation/replacement route and no user is automatically enrolled into a different event

### Requirement: Validated Attendance and Impact
After an event ends, only assigned staff SHALL validate attendance and actual duration for an enrolled participant. Duration SHALL be nonnegative and not exceed the event interval; positive attendance SHALL create or reference exactly one center-validated completed time contribution. No-shows, cancelled enrollments and enrollment alone SHALL earn no completed impact, points or review eligibility.

#### Scenario: Repeated attendance submission
- **WHEN** staff validates the same attendee repeatedly after the event
- **THEN** one linked contribution and one eligibility source feed history, duration totals, gamification and review eligibility

#### Scenario: User claims their own attendance
- **WHEN** a participant attempts direct attendance validation
- **THEN** the backend denies it and the activity remains unvalidated

### Requirement: Owned Activities and Notifications
Profile SHALL expose the user's upcoming/past enrollments and current event status. Event publication changes, cancellations and enrollment outcomes SHALL create deduplicated owned inbox events where relevant. Event push/reminder delivery SHALL require its explicit preference and OS permission; cancellation SHALL remove obsolete local reminders. Notices SHALL not expose attendee lists.

#### Scenario: Event cancelled after a reminder was scheduled
- **WHEN** an event cancellation synchronizes to the participant's device
- **THEN** the activity shows cancelled, the old reminder is cancelled and any permitted notice targets that current state

### Requirement: Minimal Participant Disclosure
The portal SHALL expose only information needed by assigned staff to manage their event's participants. Other users and centers SHALL not access attendee identities, notes or attendance actions. Registration and roster endpoints SHALL enforce rate limits and account/center scope independently of the UI.

#### Scenario: Another center requests the roster
- **WHEN** staff from a different center knows the event identifier
- **THEN** the backend denies private roster access and attendance changes
