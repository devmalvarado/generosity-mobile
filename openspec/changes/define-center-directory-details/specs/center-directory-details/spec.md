## Purpose
Define a trustworthy, maintainable center directory record with minimal publication requirements, explicit missing data and safe disclosure of identity, operations, costs and beneficiary capacity.

## ADDED Requirements

### Requirement: Minimum Public Record
New publication SHALL require a stable center ID, nonblank public name, at least one supported category and one validated, explicitly public contact route. Other highlighted fields SHALL NOT be mandatory solely because they are prominent. Private drafts SHALL allow incomplete data. Missing or invalid mandatory fields SHALL block publication with field-specific errors; optional unknowns SHALL not require invented values.

#### Scenario: Publish a minimally complete center
- **WHEN** authorized staff provides the required identity, categories and public telephone but no legal name, fees, hours or beneficiary count
- **THEN** publication can succeed with explicit missing-data states and without inferred free service, opening hours or capacity

#### Scenario: No usable public contact
- **WHEN** staff attempts publication with only a private representative contact or an invalid public route
- **THEN** the record remains a private draft and the editor explains the missing public contact without exposing the private one

### Requirement: Explicit Data Meaning
Optional fields SHALL distinguish known values, not informed, not applicable and withheld from public disclosure. Zero, false, free and unavailable SHALL be explicit assertions, not defaults for blanks. Invalid supplied values SHALL produce validation errors, not silently become unknown. Public responses SHALL not expose a withheld value or a sensitive reason for withholding it.

#### Scenario: Unknown fee and unknown accessibility
- **WHEN** fee and accessibility fields have not been supplied
- **THEN** the profile shows not informed rather than free service or inaccessible premises

#### Scenario: Explicit zero versus missing count
- **WHEN** a dated beneficiary report explicitly records zero people
- **THEN** it is displayed as a reported zero, while a missing report remains unknown

### Requirement: Public And Legal Identity
The record SHALL distinguish public name, optional legal name, multiple supported categories and the responsible person's role. Legal names SHALL preserve their supplied spelling rather than be generated from public names or translated. Profile edits SHALL NOT change center identity, ownership, verification or payment-recipient authority. Categories SHALL describe classification, not automatically activate services or professional authorization.

#### Scenario: Center has more than one category
- **WHEN** a center has supported child-support and community-kitchen classifications but no confirmed legal name
- **THEN** both classifications appear, legal name remains not informed and medicine intake or residential admission is not inferred

#### Scenario: Representative name changes
- **WHEN** staff updates the displayed responsible person's name or role
- **THEN** the change does not grant that person an account, ownership or verification authority

### Requirement: Activities And Service Hours
Activities SHALL have localized descriptions and links to declared services when available. General hours SHALL reuse the authoritative timezone, weekly schedule and exceptions; service-specific intake/collection hours SHALL remain distinct. Missing schedules SHALL remain unknown. Describing an activity or showing open status SHALL not assert current admission, receiving capacity or service authorization.

#### Scenario: Center opens before its food collection service
- **WHEN** general opening hours include the current time but the collection service does not
- **THEN** details distinguish the two schedules and do not advertise immediate food reception

### Requirement: Recovery Fee Disclosure
Recovery fees SHALL be scoped to a service with explicit free, fixed, range, case-assessed or unknown status. Monetary values SHALL include currency and charge basis/period, use nonnegative valid amounts and carry effective/review dates. Conditions and declared waivers SHALL be visible when supplied. Fees SHALL not be presented as donations, collected by this profile feature or inferred from resource sources.

#### Scenario: Monthly fee and free meal service
- **WHEN** a center declares a monthly lodging fee and separately declares meals free
- **THEN** each service shows its own terms and currency without labeling the whole center free or opening donation checkout for the fee

#### Scenario: Supplied amount lacks a currency
- **WHEN** staff enters a fee amount without its currency or charge basis
- **THEN** publication of that change is rejected with a specific error rather than guessing a currency or displaying a misleading price

### Requirement: Public Location Protection
Public address, coordinates and directions SHALL be explicitly designated for disclosure. Missing or protected locations SHALL retain a contact-only profile without fabricated coordinates. Private location SHALL NOT enter public APIs, search/ranking, distance calculations, map/routing providers, bundles, previews or metadata. Exact private locations SHALL require separate scoped authorization; public contact directions SHALL never imply entry permission.

#### Scenario: Residential location is protected
- **WHEN** the center publishes only an institutional contact and optional approved service area
- **THEN** users can find its contact-only listing without an exact pin, hidden-coordinate distance, route or residential address in any public payload

#### Scenario: Address supplied without a geocoded position
- **WHEN** a public address is known but reliable public coordinates are absent
- **THEN** the address can appear in details while the map omits a pin and does not substitute zero coordinates or a guessed location

### Requirement: Designated Public Contacts
Responsible-person name/role, institutional phone, email, social links and website SHALL be separately typed and designated public before disclosure. Person-specific publication SHALL record authorization; private operational contacts SHALL remain protected. Links SHALL be validated as supported safe destinations and opened only through explicit user action. Unpublishing the last public route SHALL trigger publication review, never expose a private fallback.

#### Scenario: Private representative and public switchboard
- **WHEN** staff supplies a private representative phone and a public institutional phone
- **THEN** only the institutional number is exposed in profiles, search, share payloads and contact actions

#### Scenario: Unsupported social URL
- **WHEN** a supplied social or website destination contains an executable or unsupported scheme
- **THEN** the editor rejects it and public clients do not turn it into an active link

### Requirement: Dated Aggregate Beneficiary Reports
Beneficiary counts SHALL identify quantity, unit, population/service scope, measurement date or period and source. Current residents, distinct people served and service encounters SHALL remain separate measures, not be summed without compatible definitions. Public reports SHALL contain no individual beneficiary records and SHALL pass disclosure review for sensitive groups. An undated count SHALL not be presented as current or validated impact.

#### Scenario: User supplies thirty-one girls without a date
- **WHEN** an intake source states that there are thirty-one girls but supplies no measurement date or counting definition
- **THEN** the source can be retained privately for review, while the public current-beneficiary field remains not informed until a dated, permitted aggregate is established

#### Scenario: One person uses two services
- **WHEN** the center reports meal visits and residents for overlapping populations
- **THEN** the profile labels separate units/periods and does not add them into a purported count of unique beneficiaries

### Requirement: Service-Specific Admission Availability
Availability SHALL be declared per service with accepting inquiries, waitlist, unavailable or unknown status, effective time, review/expiry information and public entry conditions. Optional place counts SHALL include a unit and date. Stale availability SHALL be visibly unconfirmed. Current beneficiaries SHALL not imply remaining places, and public availability SHALL not reserve admission or bypass the center's assessment.

#### Scenario: Center houses thirty-one people and accepts inquiries
- **WHEN** no maximum capacity or current place count is supplied
- **THEN** the profile shows accepting inquiries with its review date, not an invented number of vacancies or a guaranteed place

#### Scenario: Availability becomes stale
- **WHEN** the configured validity window expires while a user views a cached profile
- **THEN** current availability is shown as requiring confirmation and service actions recheck their own backend eligibility before proceeding

### Requirement: Supported Ways To Contribute
The profile SHALL separate declared ways to help from current actionable needs and receiving services. Time, goods and money SHALL retain their existing eligibility and lifecycle rules. Donation descriptions SHALL not bypass provider gates, create fee collection or expose personal bank details. Unsupported or paused methods SHALL explain availability instead of simulating a completed offer or payment.

#### Scenario: Center accepts donations but payments are not enabled
- **WHEN** the profile describes financial support without an eligible hosted-provider route
- **THEN** users can read the declared method but cannot start an invented checkout, while eligible goods/time needs remain usable

### Requirement: Declared Resource Sources
Resource sources SHALL support multiple stable types and optional localized explanations with source/review dates. Center statements SHALL be labeled as declarations, not audited finances, solvency, tax eligibility or proof of fund use. Private donor identities, account numbers and unapproved financial evidence SHALL not appear. Unknown sources SHALL not imply that the center lacks resources or receives public funding.

#### Scenario: Center reports donations and recovery fees
- **WHEN** staff declares those sources without audited financial evidence
- **THEN** both appear as center-declared sources without financial assurance, donor identification or a tax-deductibility claim

### Requirement: Scoped Verification Presentation
The directory's verification section SHALL consume existing claim authority, scope, validity and provenance, not a staff-editable boolean. Unsupported legacy true/false values SHALL not create active official claims or fraud labels. Profile completeness, legal name, visits and beneficiary counts SHALL not verify a center. Private evidence SHALL remain private, and identity changes SHALL recheck dependent claims under their owning policy.

#### Scenario: Imported verified checkbox has no evidence
- **WHEN** a source says verified without a supported issuer, scope and validity
- **THEN** the directory labels the assertion unconfirmed rather than displaying a current official badge

### Requirement: Structured Featured And Additional Attributes
Attributes SHALL use approved stable codes, localized labels, declared types, allowed values, disclosure rules and source/review dates. Featured status SHALL control presentation only, not publication requirements or truth. Unknown boolean values SHALL remain unknown. Free-form extras SHALL be bounded inert text and SHALL not override protected identity, fees, location, admission, verification or personal-data fields.

#### Scenario: Accessibility is prominently displayed
- **WHEN** step-free access is a featured attribute but the center has supplied no value
- **THEN** the section shows not informed and does not block an otherwise valid minimal record

#### Scenario: Additional attribute tries to bypass protected fields
- **WHEN** staff supplies an extra field purporting to set official verification or publish a private beneficiary/contact record
- **THEN** validation rejects that use rather than treating it as an unrestricted public metadata channel

### Requirement: Authorized Versioned Maintenance
Publication and edits SHALL use current center-scoped authorization, expected versions, validated field types and actor/source timestamps. Imports SHALL preserve newer staff edits and stable IDs, report conflicts and quarantine missing mandatory fields rather than fabricate them. Withdrawal of public fields SHALL invalidate controlled public projections/caches without claiming to recall external copies. A full re-import SHALL not erase linked history.

#### Scenario: Legacy import lacks public contact
- **WHEN** an operator migrates a record with no publishable contact route
- **THEN** its stable identity and history are retained in a needs-review state outside new public publication, without publishing a representative's private contact as a substitute

#### Scenario: Concurrent edit and public-contact withdrawal
- **WHEN** a stale import or cached editor attempts to restore a withdrawn contact
- **THEN** the write conflicts with the current revision and cannot republish it without explicit authorized review

### Requirement: Consistent Accessible Directory Views
Android, iOS and public mobile-first web SHALL present the same authorized record and missing-data meanings in Spanish/English. The center portal SHALL distinguish public preview from private editing, preserve recoverable failed input and show success only after a durable response. Proper/legal names SHALL not be auto-translated. Long text, large text, offline freshness and contact-only locations SHALL remain accessible without overlap or fake data.

#### Scenario: Anonymous visitor views an incomplete profile
- **WHEN** a guest opens a minimally published center on a narrow phone with large Spanish text
- **THEN** identity and contact remain usable, unknown fee/count/availability fields are explicit and no account or app installation is required for the public web view

#### Scenario: Publication response is lost
- **WHEN** a staff save loses its network response
- **THEN** the editor reconciles the operation before reporting publication or retrying, without overwriting newer data or exposing private input after sign-out
