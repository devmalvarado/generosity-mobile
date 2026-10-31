## Purpose
Provide trustworthy center photos, contact/profile metadata and local opening status to native discovery and public web pages, with authorized maintenance.

## ADDED Requirements

### Requirement: Authorized Profile Maintenance
Assigned staff SHALL edit their center's public name, description, categories, address/location, contacts, cover photo and hours through the mobile-first portal. Writes SHALL validate fields, use expected versions and retain actor/time provenance. This access SHALL not allow changing center identity, staff roles, official verification or payment-recipient mappings.

#### Scenario: Update another center or protected status
- **WHEN** staff submits another center's ID or attempts to set official verification or payment details
- **THEN** the backend denies the protected change without partially updating the profile

### Requirement: Safe Representative Media
Cover photos SHALL have recorded rights/provenance, meaningful alternative text and a validated image format/size. Published derivatives SHALL omit location/device metadata and executable content. Rejected uploads SHALL leave the previous image intact. Missing/failed photos SHALL use a neutral category fallback, not an unrelated building or copied reference photograph.

#### Scenario: Invalid uploaded file
- **WHEN** staff uploads unsupported, oversized or non-image content
- **THEN** validation rejects it before public serving and the existing center photo remains available

#### Scenario: Photo is removed or fails to load
- **WHEN** a public client cannot load the approved photo
- **THEN** identity remains readable with a non-photographic fallback and no claim that a substitute image depicts the center

### Requirement: Structured Local Hours
Opening schedules SHALL define a valid center timezone, weekly intervals, explicit closed days and date-specific exceptions. Overlapping or invalid intervals SHALL be rejected. Overnight hours SHALL retain their start-day meaning. Missing or incomplete schedules SHALL produce unknown status, not assume closed or always open.

#### Scenario: Holiday exception overrides weekly hours
- **WHEN** the center's local date has an explicit closed exception
- **THEN** the center is shown closed even if its weekly schedule normally opens that day

#### Scenario: Overnight or daylight-saving boundary
- **WHEN** an opening interval spans midnight or a timezone offset change
- **THEN** status follows the center's local schedule and date exceptions, independent of the viewing device's timezone

### Requirement: Truthful Opening Status
Clients SHALL display open/closed/unknown based on real time and the authoritative schedule, with available hours and last-update context. Status SHALL be reevaluated on resume and schedule boundaries. Open status SHALL NOT imply verification, staff presence, available goods capacity or guaranteed walk-in service.

#### Scenario: App resumes after closing time
- **WHEN** a client resumes after a scheduled closing boundary
- **THEN** the label updates instead of retaining the earlier open badge

### Requirement: Shared Public Revisions
Accepted profile changes SHALL appear through the same versioned catalog in native map/list/detail and public web pages. Imports SHALL preserve newer center-managed fields or require explicit conflict reconciliation. Public metadata SHALL not expose uploader identifiers, private media originals or staff contact details that were not explicitly designated public.

#### Scenario: Stale import after an hours update
- **WHEN** an import contains an older schedule than the staff-published revision
- **THEN** the newer schedule is not silently overwritten and the operator receives a resolvable conflict
