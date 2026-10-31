## Purpose
Show genuine center ratings and opinions from eligible helpers, with explicit public identity, independent moderation and abuse reporting.

## ADDED Requirements

### Requirement: Completed-Help Eligibility
Only authenticated users with authoritative center-validated completed help at that center SHALL submit a review. Personal activities, visits, favorites, offered/confirmed-but-unfulfilled help and payment success alone SHALL not establish eligibility. The backend SHALL verify eligibility and permit at most one current review per user/center, with a rating of one to five whole stars and optional bounded text.

#### Scenario: Ineligible direct request
- **WHEN** a user with only a pending offer or self-reported visit posts directly to the review endpoint
- **THEN** the backend rejects the review without changing the public rating or count

### Requirement: Review Ownership and Public Consent
Authors SHALL preview and explicitly submit the rating, text and public alias. Email, private notes and precise contribution data SHALL not be published. Authors SHALL be able to edit or withdraw their review; another account SHALL not modify it. Text SHALL render inertly and comply with documented content rules.

#### Scenario: Preview before submission
- **WHEN** an eligible user prepares a review
- **THEN** the app identifies the alias/content that will be public and no review is published merely by leaving the form

#### Scenario: Author withdraws a review
- **WHEN** the author requests withdrawal and the backend acknowledges it
- **THEN** public text and its aggregate contribution are removed while an access-restricted audit record preserves the action

### Requirement: Independent Moderation
New reviews and edited revisions SHALL remain pending until an authorized moderator approves or rejects them with an audited reason. Center staff SHALL not approve, suppress or edit reviews of their own centers. A previously approved revision SHALL remain visible during review of an edit unless withdrawn or hidden for a recorded policy reason. Rejection SHALL be visible to the author, not falsely reported as publication.

#### Scenario: Pending edit of a published review
- **WHEN** an author changes an approved rating and text
- **THEN** the prior approved revision alone remains in public views/aggregates until the new revision is approved, with no duplicate count

#### Scenario: Center staff attempts moderation
- **WHEN** a center account attempts to approve or remove its own center's review
- **THEN** the backend denies the operation even if the client exposes a crafted moderation control

### Requirement: Consistent Public Aggregates
Public rating SHALL be the arithmetic mean of the latest approved, non-withdrawn, non-hidden review per eligible author/center, shown to one decimal with the review count. Empty aggregates SHALL show no reviews rather than a fabricated score. Approval, hiding and withdrawal SHALL update aggregate and public revisions atomically; paginated review lists SHALL use stable IDs and ordering.

#### Scenario: Hide a previously approved review
- **WHEN** a moderator hides a review for a recorded policy violation
- **THEN** the rating/count and public list exclude it consistently and no duplicate moderation event changes the result twice

### Requirement: Report and Resolve Abuse
Signed-in users SHALL be able to report reviews with a bounded reason. Reports SHALL enter a private, rate-limited queue without automatic deletion or public reporter identity. Moderators SHALL have audited approve/reject/hide/restore actions; restore SHALL not override author withdrawal. Authors SHALL have a documented support/appeal route. Unavailable moderation SHALL keep unapproved content private.

#### Scenario: Repeated reports
- **WHEN** a user repeatedly reports the same review
- **THEN** reports are deduplicated or rate-limited, the reporter remains private and the published state changes only through an authorized decision
