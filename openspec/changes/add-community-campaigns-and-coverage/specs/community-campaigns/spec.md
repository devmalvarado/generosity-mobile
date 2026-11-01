## Purpose
Coordinate community-led helping campaigns and their concrete activities while preserving event capacity, safety, independent validation and honest outcome reporting.

## ADDED Requirements

### Requirement: Community Campaign Ownership
Eligible registered organizers SHALL create private drafts and publish campaigns with stable type codes for reforestation, cleanup, coordinated meals or needs assessment. A campaign SHALL identify its responsible organizer, localized purpose, coarse public area and dated activities. An optional center partnership SHALL require that center's acceptance; naming a center SHALL not imply endorsement or grant its staff powers.

#### Scenario: Publish a community cleanup without a center
- **WHEN** an eligible user publishes a valid cleanup campaign
- **THEN** it is labeled community-organized and can recruit participants without inventing a sponsoring center

#### Scenario: Claim an unapproved partnership
- **WHEN** an organizer proposes a center as partner before its authorized staff accepts
- **THEN** public views show no center endorsement and center-linked validation remains unavailable

#### Scenario: Organize on behalf of a company
- **WHEN** an eligible organizer requests organization ownership under the organization-helping extension
- **THEN** current organization assignment and verified mobile contact are also required, and the human responsible remains identified without granting center-validation authority

### Requirement: Campaign Activities Reuse Event Rules
Each campaign activity SHALL have its own stable ID, time interval, timezone, safe meeting location and bounded capacity. Community organizers SHALL manage only their assigned activities. The volunteer-event enrollment, retry, cancellation and material-rescheduling rules SHALL apply, with this owner type replacing center publication authority only. Campaign membership SHALL not reserve every activity or grant attendance-validation authority.

#### Scenario: Reserve a cleanup session
- **WHEN** two registered users attempt its last seat concurrently
- **THEN** exactly one seat commits, and neither request reserves another session in the campaign

#### Scenario: Change a meeting time after enrollment
- **WHEN** an organizer changes an activity's time after reservations exist
- **THEN** cancellation and an explicitly joined replacement are required rather than silent rescheduling

### Requirement: Activity-Specific Plans
Before publication, campaigns SHALL provide the relevant plan: planting site, permission status and maintenance for reforestation; work area and waste destination for cleanup; recipient consent and dated slots for meals; approved questionnaire, assigned field team and consent protocol for assessments. Materials, accessibility, responsible contact and safety constraints SHALL be visible to participants without exposing private beneficiaries.

#### Scenario: Reforestation has no maintenance plan
- **WHEN** an organizer attempts to publish a planting activity without required site and maintenance information
- **THEN** publication is rejected with field-specific errors and the private draft is retained

#### Scenario: Assessment protocol has not been approved
- **WHEN** an organizer has sufficient points but no approved fieldwork protocol
- **THEN** the assessment activity cannot be published or collect household responses

### Requirement: Participation Without Organizer Privileges
Guests SHALL browse public campaigns and activities; enrollment and material commitments SHALL require authentication but not organizer-level points. Materials SHALL use explicit units, requested quantities and atomic remaining availability. A commitment SHALL remain distinct from receipt, attendance and completed impact; duplicates SHALL not consume capacity twice. Participants SHALL retain cancellation/history access.

#### Scenario: New user volunteers tools
- **WHEN** a registered user below the organizing threshold commits an available quantity of tools
- **THEN** the commitment can be accepted without granting organizing permissions or recording a completed donation

### Requirement: Independent Completed-Help Validation
Organizer reports and participant declarations SHALL remain unvalidated until authorized staff of an accepted partner center independently validates actual completed help. A person SHALL not validate their own help or activity they organize. Without eligible validation, records SHALL remain separately labeled and earn no gamification or center-review eligibility. One activity SHALL retain one eligibility identity across event, material and contribution records.

#### Scenario: Organizer also has center staff credentials
- **WHEN** that organizer tries to validate their campaign's attendance using the staff role
- **THEN** the backend rejects self-validation and requires a different authorized center staff member

#### Scenario: Campaign has no partner center
- **WHEN** the organizer records that participants finished the cleanup
- **THEN** history shows organizer-reported completion, without points, streak credit or eligibility to review an unrelated center

### Requirement: Separate Outputs and Outcomes
Campaigns SHALL distinguish planned targets, reported outputs, independently validated results and later observations, retaining units, date, reporter and evidence provenance. Planted trees SHALL not imply surviving trees; collected waste SHALL not imply verified disposal; scheduled meals SHALL not imply delivery. Corrections SHALL preserve an audit trail. Survey counts SHALL not produce rewards or claims of resolved needs.

#### Scenario: Record a later planting observation
- **WHEN** an authorized participant records surviving trees at a later date
- **THEN** that observation is attached to the original planting cohort with its date and provenance, not counted as a second planting or automatic award

### Requirement: Lifecycle and Private Notifications
Organizers SHALL be able to pause or cancel future activities with a reason, stopping new commitments and notifying affected participants once. Already completed records SHALL remain. Cancellation SHALL reconcile meal slots and obsolete reminders; resumption SHALL recheck eligibility and current schedules. Push SHALL require preferences and permission, and its payload SHALL contain no household address, dietary details or private roster.

#### Scenario: Cancel a campaign with scheduled meal deliveries
- **WHEN** an authorized organizer cancels the campaign
- **THEN** future slots are cancelled, their assignees and recipient receive private status updates, and prior delivery history is retained

### Requirement: Discoverable Mobile-First Campaign Flows
Native Needs/events SHALL expose campaigns and Profile SHALL expose owned campaigns and participation. Eligible users SHALL reach organizer management in the same mobile-first portal. Spanish/English, accessible labels, enlarged text and honest loading/offline/error states SHALL cover every flow. Phone layouts SHALL work from 320 CSS pixels without page overflow. The central plus action SHALL retain its approved need-or-personal-help choice.

#### Scenario: Manage a campaign from a small phone
- **WHEN** an eligible organizer edits a Spanish campaign at 320 CSS pixels with enlarged text and the keyboard open
- **THEN** fields, errors, save/cancel and navigation remain reachable without overlap, hover-only controls or a required desktop view
