## Purpose
Schedule bounded recurring activities and fairly offer released places while preserving occurrence-specific consent, capacity and attendance history.

## ADDED Requirements

### Requirement: Bounded Timezone-Aware Series
Authorized event owners SHALL define a recurrence with timezone, start/end duration, explicit end/count and generation limit. A preview SHALL show concrete dates and daylight-saving handling before publication. Each occurrence SHALL retain a stable ID and independent capacity/state. Repeated generation SHALL not duplicate occurrences; organizer eligibility SHALL be rechecked for new publication.

#### Scenario: Weekly series crosses a daylight-saving change
- **WHEN** an owner previews that series
- **THEN** local dates/times and ambiguous or missing times are explicit and publication requires resolving invalid occurrences

### Requirement: Safe Occurrence Changes
Owners SHALL choose a single occurrence or future uncommitted occurrences when editing a series. Completed history SHALL remain unchanged. Time/venue changes to committed occurrences SHALL follow cancellation/replacement rules with explicit participant acceptance, not automatic migration. Cancelling a series SHALL reconcile each affected enrollment, reminder and paid-ticket obligation.

#### Scenario: Change next month's recurring venue
- **WHEN** some future occurrences already have enrollments
- **THEN** those occurrences require explicit replacement handling while unaffected history and unedited occurrences retain identity

### Requirement: Voluntary Fair Waitlists
Users or authorized assisted coordinators SHALL explicitly join or leave a full occurrence's waitlist. One participant SHALL have at most one active enrollment, offer or waitlist entry per occurrence. Ordering SHALL use server acceptance time with a stable tie-breaker; payment amount, points and invitations SHALL not buy priority. A waitlist entry SHALL not count as attendance or a reservation.

#### Scenario: High-point user joins after another participant
- **WHEN** both await the same seat
- **THEN** the published queue policy keeps the earlier participant first rather than prioritizing points

### Requirement: Atomic Expiring Seat Offers
When capacity becomes available, the server SHALL offer it to the next eligible waitlisted participant before new direct reservations. The offer SHALL hold capacity atomically until a disclosed server-time deadline. Explicit acceptance SHALL recheck eligibility and paid checkout when applicable. Declined/expired offers SHALL release once and advance the queue; late payment SHALL follow ticket reconciliation, not overbook.

#### Scenario: Offer expires while another request arrives
- **WHEN** expiration, acceptance and another reservation race for the same seat
- **THEN** one valid transition owns the capacity and late requests receive the current outcome without duplicate allocation

### Requirement: Consistent Accessible Queue Status
Web and native views SHALL show owned waitlist/offer status, deadline and accept/decline actions in Spanish/English without exposing other participants. Notifications SHALL respect channel consent and be deduplicated; assisted participants SHALL have a coordinator/paper-contact route. Queue policies and changes SHALL be auditable without promising admission.

#### Scenario: Waitlisted participant cannot install the app
- **WHEN** an offer becomes available
- **THEN** the permitted browser or assisted route can accept it before the same deadline without requiring native push
