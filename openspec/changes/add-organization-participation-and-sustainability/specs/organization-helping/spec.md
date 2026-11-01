## Purpose
Let companies and other organizations contribute through accountable human representatives and willing members while preserving individual ownership, independent validation and private data.

## ADDED Requirements

### Requirement: Organization Identity and Representation
An organization SHALL have a stable helping identity separate from people, beneficiary centers and advertisers. Its initial owner SHALL authenticate, verify private mobile contact for group creation and establish representation under a configured review policy. Company revenue or employee count SHALL not determine eligibility. Email-domain possession or payment alone SHALL not grant ownership.

#### Scenario: Applicant names an existing company
- **WHEN** no accepted representation evidence exists
- **THEN** the application remains pending and cannot act as that company's owner

### Requirement: Accepted Scoped Membership
Owners SHALL invite owner, coordinator or member roles through expiring account-bound invitations requiring acceptance. Coordination SHALL be scoped to assigned campaigns/contributions. Grants, revocations and ownership changes SHALL be auditable, with independent review for disputed ownership. Membership SHALL not grant center validation, household access or an employer view of personal history.

#### Scenario: Former coordinator retries a saved request
- **WHEN** their membership or assignment has been revoked
- **THEN** the backend rejects the write, even with a previously valid operation key or cached organization view

### Requirement: Explicit Acting Context
Every contribution SHALL identify the authenticated human actor and the contributing subject, person or organization. Users SHALL review that context before submission. The server SHALL validate current authority and preserve subject/provenance through history and corrections. Switching account or organization SHALL clear private drafts/caches and queued writes SHALL reauthorize online.

#### Scenario: Member changes organization before sending
- **WHEN** a draft belongs to the prior organization
- **THEN** it cannot silently submit under the new organization or expose the prior organization's private details

### Requirement: Organization Contributions Reuse Help Rules
Authorized representatives SHALL offer an organization's time, goods or configured monetary support through existing need, capacity, idempotency and cancellation rules. Center fulfillment SHALL require independent validation. A representative or staff member SHALL not validate help for an organization they represent. Paying commercial fees SHALL remain separate from this contribution lifecycle.

#### Scenario: Company offers supplies to a center
- **WHEN** authorized center staff confirms and later records actual receipt
- **THEN** the contribution and organizational history advance under the same lifecycle as other help, without being copied into every employee's record

### Requirement: Voluntary Member Attribution
Members SHALL explicitly choose whether their eligible activity is attributed to one organization/team and campaign. Declining SHALL preserve normal helping and earned personal recognition. Organization coordinators SHALL not enroll members by default, take personal points or expose unrelated history. Leaving SHALL revoke access without transferring historical attribution to a new employer.

#### Scenario: Employee helps personally
- **WHEN** they decline organizational attribution
- **THEN** their contribution and awards stay personal and the employer sees no private activity or nonparticipation report

### Requirement: Non-Duplicated Collective Impact
Organization totals SHALL derive from canonical validated evidence with units, period and provenance. Bulk organizational contributions SHALL not award personal points to each member or to the submitting coordinator. Actual member participation SHALL require its own evidence. Organization roll-ups and member records SHALL not be summed as additional global help; money SHALL remain currency-specific and earn no points by itself.

#### Scenario: Twenty employees participate in one event
- **WHEN** independently validated attendance creates member records and an organization summary
- **THEN** each actual participation counts once and the summary does not add twenty more participations or duplicate the shared goods donation

### Requirement: Accessible Organization Workspace
Mobile-first web, Android and iOS SHALL expose authorized organization selection, owned contributions and consented collective impact in Spanish/English. Member lists, requests and history SHALL be paginated. Internal operations SHALL remain private; publishing organizational impact SHALL require authorized approval and applicable aggregation rules without exposing beneficiaries or personal member histories.

#### Scenario: Large organization opens its history on a phone
- **WHEN** thousands of members or contributions exist
- **THEN** the client loads bounded pages with clear pending/error state and never downloads the full private member directory to display totals
