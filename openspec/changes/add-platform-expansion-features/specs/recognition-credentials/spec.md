## Purpose
Recognize validated helping with accessible PDF certificates and verifiable digital badges without creating financial rewards, professional qualifications or unsupported impact claims.

## ADDED Requirements

### Requirement: Evidence-Based Recognition
Certificates and badges SHALL derive only from eligible center-validated completed help and versioned achievement criteria. Each credential SHALL retain issuer, owner, criterion, evidence identities and issuance time. Self-reported work, sharing, purchases and attendance reservations SHALL not qualify. Issuance SHALL not spend points or grant cash, transferable credit, tax status or professional certification.

#### Scenario: User requests a certificate for reported personal help
- **WHEN** no eligible validated evidence exists
- **THEN** no earned credential is issued and the user sees the unmet criterion rather than a fabricated certificate

### Requirement: Idempotent Private Issuance
The backend SHALL issue at most one active credential per owner/criterion/evidence scope and policy version, with controlled replacement links. Claiming assisted records SHALL preserve canonical evidence and independent validation. Users SHALL preview the display name and permitted details before downloading; issuers SHALL not impersonate another center or authority.

#### Scenario: Concurrent certificate downloads
- **WHEN** web and iOS request the same earned certificate
- **THEN** both receive the same credential identity rather than duplicate awards or inconsistent issuers

### Requirement: Accessible PDF and Badge Outputs
Owners SHALL download a readable Spanish/English certificate PDF and badge representation with an equivalent accessible HTML view. Outputs SHALL include actual issuer, criterion, relevant period/units, issue date and verification reference without private beneficiary data. Long names and enlarged text SHALL not truncate essential details; badge metadata SHALL identify its evidence scope without exposing private records.

#### Scenario: Certificate includes validated volunteering time
- **WHEN** evidence supports actual hours for the selected period
- **THEN** the PDF shows those hours with provenance, not a count of reservations converted into hours

### Requirement: Controlled Sharing and Verification
Credentials SHALL be private by default. Owners SHALL explicitly choose public fields before generating a shareable verification page/QR. Verification SHALL expose only approved metadata and current authenticity status, with non-enumerable identifiers and abuse limits. Withdrawal of sharing SHALL remove public personal details; no verification endpoint SHALL leak emails, contacts or source case records.

#### Scenario: Owner withdraws public sharing
- **WHEN** someone later follows the old verification link
- **THEN** personal details are no longer disclosed and the system does not pretend external PDF copies were remotely erased

### Requirement: Correction Revocation and Authenticity
Invalidated evidence or erroneous issuance SHALL update credential status with audited reason and replacement/revocation linkage. Verification SHALL show the current state; client-edited PDFs SHALL not change server claims. Corrections SHALL reconcile dependent achievements/rankings without issuing duplicate points. Historic downloads SHALL carry a reference to live status rather than promise permanent validity.

#### Scenario: Source attendance is invalidated
- **WHEN** authorized correction removes the credential's qualifying evidence
- **THEN** the credential becomes invalid or is replaced under policy, and its verification page no longer confirms the obsolete claim

### Requirement: Scoped Organizational Recognition
Organization/team certificates SHALL identify the actual recipient, campaign or activity period, criteria version and validated collective evidence. Member credentials SHALL require that person's eligible participation and sharing choice. Neither organization membership nor a paid listing/sponsorship SHALL issue an earned helping certificate. Revised competition results SHALL update affected recognition using the same correction lifecycle.

#### Scenario: Company requests certificates for its whole workforce
- **WHEN** only some workers have validated qualifying participation
- **THEN** the organization may receive its evidenced collective credential, while personal credentials are issued only for qualifying individuals and no employer receives their private history

#### Scenario: Share a credential earned during a private competition
- **WHEN** its owner publishes permitted certificate fields
- **THEN** the public view omits individual campaign ranking positions, other participants and private standings, while retaining the credential's allowed evidence scope
