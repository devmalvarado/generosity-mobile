## Purpose
Recognize center-validated completed helping through auditable informational points, levels, achievements and streaks without monetization or fabricated activity.

## ADDED Requirements

### Requirement: Eligible Completed Activity
Only authoritative center-validated completed time/goods help and validated event attendance SHALL qualify. Self-reported activities SHALL remain separately visible but earn no points, achievements or streak days. Offered/confirmed-only help, app visits, reviews, favorites and payments alone SHALL not qualify. One activity represented in several systems SHALL keep one eligibility identity.

#### Scenario: Event attendance also appears in contribution history
- **WHEN** validated attendance creates a completed contribution for the same activity
- **THEN** gamification processes one eligible activity rather than awarding separately for event and contribution records

#### Scenario: User completes personal help
- **WHEN** a user records self-reported completion
- **THEN** personal history/impact may display it with that provenance but points and qualifying streak dates do not change

### Requirement: Versioned Audited Awards
The backend SHALL derive awards from eligible evidence under a published rule version, including thresholds, caps and effective dates. Each award SHALL retain source IDs and rule version and be replay-safe. Clients and center staff SHALL not set balances directly. An authorized operator's evidence invalidation SHALL append a reasoned correction and recompute affected totals without erasing the original audit trail.

#### Scenario: Duplicate completion event
- **WHEN** the same fulfillment event is delivered repeatedly or processing restarts
- **THEN** the point balance and achievement progress change only once

#### Scenario: Evidence is invalidated
- **WHEN** an authorized operator records that an eligible activity was invalid
- **THEN** dependent awards, levels and streak dates are recalculated with visible corrected state, without silently changing terminal contribution history

### Requirement: Non-Monetary Points and Levels
Points SHALL have no cash, credit, redemption or payment value. A user's level SHALL derive from the current valid points total and versioned thresholds, with localized labels and progress to the next level. No leaderboard or spending-based rank SHALL be introduced. Absent rule/data configuration SHALL show unavailable, not the reference's point balance or level.

#### Scenario: Level threshold reached
- **WHEN** an eligible award brings the total to a published level threshold
- **THEN** both apps show the resulting level and consistent remaining progress, without suggesting a financial benefit

### Requirement: Meaningful Achievement Collection
Impact SHALL show earned and locked achievements and a View all destination with each badge's criterion, progress and earned date when applicable. Achievements SHALL use stable IDs and eligible evidence, not purely decorative unlocks. Corrected evidence SHALL update dependent badge state. Missing progress SHALL be distinguished from zero progress.

#### Scenario: Inspect a locked achievement
- **WHEN** a user opens an unearned badge
- **THEN** the app explains its localized qualifying criterion and actual progress without awarding it for opening the screen

### Requirement: Local-Day Streaks
Streaks SHALL count consecutive calendar dates with eligible completed help, not launches or consecutive 24-hour periods. Dates SHALL use center-validated occurrence time and the account's effective recorded timezone; repeated activity on one date SHALL not add days. The displayed current streak SHALL be active when its latest date is today or yesterday, otherwise zero. Timezone changes SHALL not rewrite historical dates.

#### Scenario: Miss a qualifying day
- **WHEN** the most recent qualifying date is earlier than yesterday in the effective timezone
- **THEN** current streak is zero while the best historical streak remains accurate, with no fabricated grace-day activity

#### Scenario: Late validation of completed help
- **WHEN** a center validates an earlier actual activity date
- **THEN** that date, rather than the validation day, is reconciled into the streak once, including daylight-saving boundaries

### Requirement: Private Readable Progress
Impact SHALL show balance, level, current/best streak and achievement progress for the owning account. Any point chart SHALL label its time range and derive values from the award ledger, with an equivalent readable summary. Profile SHALL use the same level/points source. Empty/offline/corrected data SHALL be labeled; progress SHALL not be published as a ranking or shared automatically.

#### Scenario: Offline points screen
- **WHEN** the app cannot refresh a previously loaded points summary
- **THEN** it shows the cached revision and offline state without inventing new awards, chart bars or streak continuity
