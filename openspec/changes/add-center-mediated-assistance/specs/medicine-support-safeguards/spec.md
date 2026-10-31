## Purpose
Enable carefully gated medicine assistance through eligible centers and qualified partners while keeping prescribing, sensitive clinical records and unsafe donation handling outside ordinary donor workflows.

## ADDED Requirements

### Requirement: Country And Service Authorization Gate
Medicine intake, procurement, storage and dispensing SHALL be enabled only for the specific country, center/partner role and reviewed operating policy. Each permitted role SHALL have current evidence of the required authority and handling arrangements. General organization verification, center consent, visits or points SHALL NOT establish medicine authorization. Missing or expired configuration SHALL block new medicine commitments.

#### Scenario: Ordinary collection center offers medicine help
- **WHEN** a center without an approved medicine operating scope enables the category
- **THEN** the system does not advertise it as a medicine intake/dispensing location or enable medicine commitments; public contact information remains available without implying qualification

#### Scenario: Authorization expires with an open request
- **WHEN** the approved service or professional-partner authorization expires
- **THEN** new procurement is suspended and existing obligations enter an authorized continuity/support process without erasing receipts or pretending the need is fulfilled

### Requirement: Qualified Review Before Procurement
Before medicine purchasing or funding, the eligible center SHALL obtain the required review from an authorized professional/partner and approve a precise product/quantity or bounded procurement request. Prescription validation, where required, SHALL occur through that qualified service. Generosity SHALL NOT diagnose, prescribe, suggest doses or authorize substitutions; a helper's report or photo SHALL not replace review.

#### Scenario: Person asks for a named medicine
- **WHEN** a person calls or visits requesting a particular product
- **THEN** staff records the private assistance process, but donor purchasing remains disabled until the required qualified review approves the request

#### Scenario: Approved item is unavailable
- **WHEN** the donor cannot source the approved item and proposes an alternative
- **THEN** the app requires a revised professionally reviewed request and donor confirmation, not an automatic substitute or dose conversion

### Requirement: Minimal Health Disclosure
Clinical records and prescription handling SHALL remain with the qualified service in the initial release; generic case uploads SHALL not accept them. Donors SHALL receive only necessary approved fulfillment details. Disclosure of a specific medicine to a donor SHALL require separate informed permission because it can reveal health information. Declining that disclosure SHALL leave center/partner procurement as an available option when otherwise eligible.

#### Scenario: Person declines sharing the medicine name with the helper
- **WHEN** the person permits center coordination but not product disclosure to the donor
- **THEN** the donor sees a generic approved assistance purpose and an eligible center-mediated funding route, not the diagnosis, prescription, product or beneficiary identity

#### Scenario: Sensitive content is entered in an ordinary notice
- **WHEN** a helper attempts to upload a prescription or clinical narrative through the heads-up flow
- **THEN** the unsupported field/upload is not accepted and no such content enters the public catalog, maps, analytics or notifications

### Requirement: Approved Sourcing And Handling
Medicine requests SHALL specify approved sourcing, packaging, remaining shelf-life, transport, receipt and storage conditions under the configured country/service policy. Expired, opened, patient-returned or untraceable products SHALL not be accepted through this flow. Controlled products, cold-chain needs and other special handling SHALL stay disabled unless their separate authorized process is configured and reviewed.

#### Scenario: Donor offers leftover medicines
- **WHEN** a donor offers opened, patient-returned or unverified products instead of approved newly sourced goods
- **THEN** the center rejects them for this assistance flow, no fulfillment is credited, and any disposal guidance uses the approved local policy rather than directing redistribution

#### Scenario: Order needs special transport
- **WHEN** an approved product requires handling the selected center or delivery route cannot support
- **THEN** purchase/delivery instructions remain blocked until an eligible professional route is selected and reviewed

### Requirement: Professional Receipt And Safe Handover
Authorized recipients SHALL verify required product identity, quantity, condition, expiry and provenance before accepting medicine stock. Mismatches, suspected damage or recalls SHALL be quarantined/rejected under the service policy and SHALL not satisfy the request. Dispensing/handover SHALL follow the authorized professional process; a donor receipt or delivery scan SHALL not establish safety, clinical benefit or completion.

#### Scenario: Wrong product reaches the center
- **WHEN** received medicine differs from the approved request or fails required handling checks
- **THEN** it remains unavailable for beneficiary handover, the request stays unresolved and the donor sees only the permitted discrepancy/next-step status

#### Scenario: A recall is reported after handover
- **WHEN** the responsible professional service identifies an affected delivered product
- **THEN** its approved follow-up process is invoked with scoped access, and history records the issue without erasing delivery evidence or sending health details to unrelated donors

### Requirement: Urgent Care And Safeguarding Boundary
Medicine assistance SHALL not be presented as emergency care or a reason to delay it. Potential urgent-risk and protected-person cases SHALL use reviewed local professional/safeguarding routes, not automated diagnosis or a donor waiting queue. Unsupported consent/representation SHALL block private collection, while public care/contact information remains available. Financial sponsorship SHALL not buy access to clinical records.

#### Scenario: Request cannot safely wait for center sponsorship
- **WHEN** the person or staff indicates an urgent situation under the reviewed service protocol
- **THEN** the interface prioritizes the configured local urgent-care contact route rather than promising a donor-funded medicine arrival or assessing severity itself

#### Scenario: A representative lacks a supported consent process
- **WHEN** the request concerns a minor or someone unable to use the standard consent flow without an approved representative procedure
- **THEN** the helper is directed to the authorized service without collecting sensitive personal or clinical records through the ordinary referral form
