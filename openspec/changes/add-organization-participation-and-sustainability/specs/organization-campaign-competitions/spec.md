## Purpose
Coordinate voluntary organizational helping goals and temporary team/individual competitions using independently validated evidence and non-financial points.

## ADDED Requirements

### Requirement: Authorized Campaign Ownership
An organization campaign SHALL reuse existing campaign/event mechanics and identify an assigned human coordinator. Creation/publication SHALL require current organization authority, earned organizer eligibility and verified private mobile contact. Organization wealth, sponsor purchases or combined member points SHALL not substitute for the responsible person's permissions or center partnership acceptance.

#### Scenario: Company has money but no eligible coordinator
- **WHEN** it tries to publish a volunteer event
- **THEN** publication is unavailable until an eligible assigned coordinator accepts responsibility, while ordinary organization helping remains available

### Requirement: Time-Bounded Published Rules
Each campaign SHALL publish cooperative or competitive mode, eligible teams, registration deadline, start/end instants, timezone, validation cutoff, metric, caps, ties and visibility before enrollment. Material rules SHALL freeze when enrollment opens. Team rosters SHALL lock by the published deadline at or before the start. Missing scoring configuration SHALL disable competitive scoring rather than invent numbers.

#### Scenario: Organizer changes the score after people join
- **WHEN** a material rule change is requested
- **THEN** it requires a cancelled/replacement campaign with renewed acceptance rather than silently recalculating the active competition under new rules

### Requirement: Voluntary Team Participation
Members SHALL explicitly join the campaign and selected team before its enrollment deadline. A person SHALL belong to at most one team within a competition. Team transfers SHALL follow the published pre-lock rules and SHALL not move already attributed evidence. Late helpers SHALL retain ordinary helping access without retroactive competitive enrollment. Campaign membership SHALL not reserve event seats.

#### Scenario: Coordinator imports an employee list
- **WHEN** invitations are created
- **THEN** nobody is enrolled, ranked or placed into an event until they individually accept the relevant actions

### Requirement: Evidence-Based Campaign Points
Scores SHALL derive from independently center-validated completed help under a versioned campaign rule. Each canonical activity SHALL count at most once in one selected competitive campaign and team. Personal award balances SHALL remain under their existing rules. Money, organization revenue, ad purchases, invitations, clicks, promises and self-reported activity alone SHALL earn no campaign points.

#### Scenario: Same attendance reaches several projections
- **WHEN** event, contribution, organization and campaign updates reference the same activity
- **THEN** eligible points count once in that competition and no duplicate personal or global award is issued

### Requirement: Transparent Comparable Standings
Standings SHALL disclose period, metric, evidence coverage and provisional/final state. Raw points and points per enrolled participant SHALL be separate views. Normalized rates SHALL use the frozen enrollment count, not revenue, employee headcount or only active scorers; missing/disallowed denominators SHALL yield unavailable rates. Ties SHALL follow the frozen rule without paid priority.

#### Scenario: Large and small companies compare results
- **WHEN** participants switch from total points to points per enrolled participant
- **THEN** both values use the disclosed enrollment snapshot and no claim about the generosity of all employees or company wealth is inferred

#### Scenario: Low-scoring member leaves
- **WHEN** their membership ends after roster lock
- **THEN** removing them from the frozen denominator cannot improve the team's normalized score

### Requirement: Restricted Ranking Visibility
Standings SHALL be visible only to enrolled campaign participants by default. Public organization/team aggregates SHALL require separate consent and configured disclosure thresholds. Individual standings SHALL always remain inside the campaign; no public participant list or global personal ranking SHALL be created. Organization membership alone SHALL not authorize access. Declining ranking visibility SHALL not remove earned personal recognition.

#### Scenario: Employer requests employee performance export
- **WHEN** the requested data includes personal helping outside the campaign or a list of nonparticipants
- **THEN** it is denied and only permitted campaign aggregates are available

### Requirement: Separate Voluntary Individual Ranking
Each person SHALL separately opt into an individual campaign ranking after previewing the displayed name or alias, score scope and audience. Team enrollment SHALL not imply individual ranking consent. A coordinator SHALL not opt in another person. Only eligible activity for that campaign SHALL contribute to the displayed score; unrelated history and the account's lifetime balance SHALL remain private.

#### Scenario: Member joins a team without individual visibility
- **WHEN** they decline the individual ranking option
- **THEN** they may still help and contribute under their chosen team-attribution permission, while no identifying individual ranking entry appears

#### Scenario: Nonparticipant opens a copied ranking link
- **WHEN** a guest, former participant or organization administrator not enrolled in that campaign requests the ranking
- **THEN** the API denies access and previews, search metadata and cached public responses disclose no names or scores

### Requirement: Withdrawal of Individual Visibility
A person SHALL be able to withdraw individual-ranking consent without losing personal awards or ordinary campaign participation. The server SHALL remove their identifying ranking entry and invalidate private rank caches, including on revocation and account switch. Separately consented team attribution SHALL remain independent. The UI SHALL explain that previously seen or externally captured information cannot be recalled.

#### Scenario: Participant hides their individual entry
- **WHEN** they withdraw individual-ranking consent
- **THEN** subsequent authorized ranking views omit them, personal awards remain, and team totals change only if team-attribution permission is also withdrawn

### Requirement: Finalization and Corrections
Activity occurrence time SHALL determine campaign inclusion; independent validation must meet the published cutoff for initial finalization. Results SHALL remain provisional until reconciliation completes. Duplicate or corrected evidence SHALL recompute scores idempotently. Later corrections SHALL produce a versioned revised result and update affected recognition, preserving audit history instead of silently rewriting a winner.

#### Scenario: Help finishes before the deadline but validation is later
- **WHEN** validation arrives before the configured validation cutoff
- **THEN** the eligible activity enters the campaign using its actual completion time and is counted once

#### Scenario: Winning evidence is invalidated
- **WHEN** an authorized independent correction removes qualifying help
- **THEN** the result and affected certificates/badges show their revised status with an auditable explanation

### Requirement: Recognition and Safe Withdrawal
Campaign recognition SHALL reuse evidence-backed certificates/badges with actual team/organization, period and criteria. Rewards SHALL have no cash/redemption value and grant no administrative permissions. Cancellation or withdrawal SHALL stop future participation under disclosed rules while preserving underlying contributions and required audit history; private notifications SHALL avoid beneficiary details.

#### Scenario: Campaign is cancelled after completed help
- **WHEN** its competitive phase stops
- **THEN** participants retain actual help history and eligible personal awards, and the campaign does not issue a fabricated final winner

### Requirement: Cross-Platform Participation
Mobile-first web, Android and iOS SHALL support campaign discovery, enrollment, team context, separate individual-ranking consent, personal contribution and standings in Spanish/English. Screens SHALL distinguish pending validation, unavailable/suppressed data and stale results. Ranking lists SHALL be paginated and accessible without maps, hover controls or a required desktop. Identifying individual standings SHALL require online authorization and SHALL not be durably cached.

#### Scenario: Offline user views standings
- **WHEN** the latest score revision cannot be fetched
- **THEN** permitted cached aggregates are labeled stale, identifying individual standings stay unavailable, and the client does not invent points or declare a final winner
