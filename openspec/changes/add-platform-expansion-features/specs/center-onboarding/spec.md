## Purpose
Let organizations request a center presence or claim an imported center through accountable review without self-assigning staff privileges or duplicating existing records.

## ADDED Requirements

### Requirement: Private Applications and Claims
Registered applicants SHALL submit a private organization application or claim an existing center, with contact verification, representation evidence and explicit consent for submitted data. Submission SHALL grant no publishing, payout or verification status. The system SHALL flag possible duplicates without exposing another applicant's private evidence.

#### Scenario: Claim an imported center
- **WHEN** a user requests control of an existing catalog entry
- **THEN** that entry remains unchanged and the user sees a pending application rather than staff controls

### Requirement: Independent Membership Review
An authorized reviewer other than the applicant SHALL approve, request changes or reject an application with recorded rationale. Approval SHALL explicitly identify center, account and permitted roles and require the recipient's acceptance. Reviewed membership SHALL remain distinct from organization verification, tax eligibility and provider recipient onboarding.

#### Scenario: Applicant tries to approve their own request
- **WHEN** an applicant with another operational role attempts self-approval
- **THEN** the server rejects the decision and leaves the review pending for an independent reviewer

### Requirement: Controlled Staff Lifecycle
Permitted center owners SHALL invite bounded staff roles through expiring account-bound invitations. Grant, revoke, transfer and recovery actions SHALL be audited and enforce current authorization. Ownership transfer SHALL require accepted independent review; email/domain possession alone SHALL not seize a center. Revocation SHALL stop subsequent protected writes and clear stale staff views.

#### Scenario: Revoked staff submits a cached edit
- **WHEN** a former staff member sends an offline edit after revocation
- **THEN** the backend denies it without replacing current center content

### Requirement: Duplicate and Dispute Resolution
Reviewers SHALL resolve duplicate applications and contested claims without silently deleting public center IDs, contributions or memberships. Merges SHALL preserve canonical redirects and provenance and require review of ownership conflicts. Disputes SHALL offer a private appeal route and targeted restrictions, not publish allegations or infer wrongdoing from a report alone.

#### Scenario: Two parties claim the same center
- **WHEN** conflicting claims are received
- **THEN** existing ownership is not automatically overwritten and a scoped review preserves both claims privately

### Requirement: Accessible Application Tracking
Applicants SHALL track pending, changes-requested, approved, rejected and withdrawn states in the mobile-first portal in Spanish/English, with safe notifications and retention information. Evidence SHALL be private, bounded, validated and scanned before reviewer access. Withdrawal SHALL not erase audit records required under the approved retention policy.

#### Scenario: Replace a rejected evidence file
- **WHEN** the applicant uploads a permitted replacement on a phone
- **THEN** the new version is validated privately and the prior review/provenance remains traceable without exposing either file publicly
