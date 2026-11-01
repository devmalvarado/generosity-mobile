## Purpose
Provide accountable operational administration and incident handling while preserving scoped authority, independent decisions and immutable transaction provenance.

## ADDED Requirements

### Requirement: Least-Privilege Operational Access
The mobile-first operations workspace SHALL require strong authenticated operator sessions and explicit permissions for role provisioning, moderation, support, verification, fiscal/provider operations and privacy requests. No general administrator role SHALL imply access to all cases or payment credentials. Sensitive grants/actions SHALL require reauthentication and configured independent approval.

#### Scenario: Support operator requests household evidence
- **WHEN** an operator without case-specific permission requests sensitive case content
- **THEN** access is denied despite their access to the support workspace

### Requirement: Auditable Scoped Interventions
Administrative mutations SHALL record actor, target, expected version, reason and outcome and be idempotent where retried. Operators SHALL not directly rewrite point balances, paid states or terminal contribution evidence. Corrections SHALL use domain correction/provider workflows. Conflicts SHALL preserve newer work; support access SHALL never silently impersonate a user.

#### Scenario: Operator attempts to mark a payment paid
- **WHEN** an operator submits a manual paid-state override without verified provider evidence
- **THEN** the backend rejects it and offers the authorized reconciliation/support route

### Requirement: Fraud and Abuse Case Review
Reports and configured signals SHALL create scoped review cases with evidence provenance, status and appeal. Signals SHALL not alone establish fraud. Authorized reviewers SHALL apply reasoned proportional restrictions while preserving cancellation, support and rights routes. Public content SHALL not disclose private allegations, reporter identities or investigative records.

#### Scenario: Repeated reports target an organizer
- **WHEN** multiple reports refer to the same alleged incident
- **THEN** they are linked for review rather than automatically multiplying sanctions or deleting points

### Requirement: Operational Release and Recovery Controls
Authorized operators SHALL manage versioned capability gates, queue failures and reconciliation status with tested rollback. Disabling new payments, publication or collection SHALL preserve outstanding reconciliation, refunds, cancellations, rights requests and history. Secrets SHALL remain protected server-side; exports SHALL be scoped, audited, redacted and subject to retention.

#### Scenario: Disable a failing provider integration
- **WHEN** new provider transactions are disabled
- **THEN** existing intents and refund/receipt reconciliation continue and no accepted obligation is silently erased

### Requirement: Mobile-First Accountable Queues
Operations SHALL offer scoped searchable queues, evidence-safe detail, localized reasons and explicit confirmation of consequential actions. Spanish/English phone layouts SHALL work from 320 CSS pixels with keyboard and screen-reader access. Each queue SHALL have an assigned handling policy and auditable handover; missing staffing/configuration SHALL keep dependent release gates closed.

#### Scenario: Case is reassigned on a phone
- **WHEN** an authorized operator hands a case to another permitted operator
- **THEN** assignment and access change visibly with an audit record, without exposing unrelated queue items

### Requirement: Separate from center staff
The operations workspace SHALL NOT be served at `/kindness-center` or inside the helper flow. A center staff assignment SHALL NOT grant operator permissions. An operator permission SHALL NOT grant center publishing.

#### Scenario: Center staff opens operations
- **WHEN** a person assigned only to a Kindness Center opens the operations workspace
- **THEN** access is denied and no platform statistics or award controls appear

### Requirement: Defined usage statistics
An operator with statistics permission SHALL see only aggregates the server recorded, each labeled with its period, definition, and coverage. A figure that was not measured SHALL be unavailable rather than shown as zero. The view SHALL NOT invent sessions, distances, or success rates, and SHALL NOT include beneficiary, household, or payment details. Center staff and helpers SHALL NOT see platform-wide statistics.

#### Scenario: A count was not instrumented
- **WHEN** an operator opens usage statistics and a requested figure has no recorded source
- **THEN** that figure is unavailable and the page does not display a fabricated zero or session count

### Requirement: Test award cleanup
An operator with test-cleanup permission SHALL void awards the server marks as test-origin, with a reason. The action SHALL recompute affected totals and retain the audit trail. It SHALL NOT accept a target balance, remove real center-validated evidence, or erase contribution history. If test-origin marking is not configured, the action SHALL stay unavailable.

#### Scenario: Void points left by tests
- **WHEN** an operator voids test-origin awards and states a reason
- **THEN** those awards are recomputed out of the totals, the original records remain auditable, and real validated evidence is unchanged

#### Scenario: Cleanup is not configured
- **WHEN** test-origin marking is absent
- **THEN** the cleanup action stays unavailable and no balance can be typed in its place
