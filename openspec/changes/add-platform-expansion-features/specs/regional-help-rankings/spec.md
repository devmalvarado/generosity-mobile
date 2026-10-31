## Purpose
Compare aggregate validated helping across countries, neighborhoods and postal areas with transparent metrics and privacy limits, without ranking named people or treating incomplete data as moral worth.

## ADDED Requirements

### Requirement: Validated Geographical Attribution
Rankings SHALL use eligible completed help with canonical evidence IDs and an authoritative activity/center geography, never inferred home address, device tracking or self-entered postal code as proof. Country, neighborhood and postal identifiers SHALL be namespaced, versioned and retain leading zeros. Missing or ambiguous geography SHALL be reported as unattributed rather than guessed.

#### Scenario: Participant changes their profile postal code
- **WHEN** that account edits its residence preference
- **THEN** past helping remains attributed to its validated activity geography and no ranking credit moves with the profile

### Requirement: Comparable Explicit Metrics
Each comparison SHALL show geography level, common period, included evidence, metric/unit, data coverage and update time. Unique validated participants SHALL be distinct from activity counts, hours and compatible goods units. Money, purchases, certificates and invitation volume SHALL not create ranking credit. Overlapping geographies SHALL not be summed as disjoint populations; no mixed-unit generosity score SHALL be invented.

#### Scenario: One user helps in two neighborhoods
- **WHEN** both have eligible activity in the selected period
- **THEN** each area counts that user once, while the containing region deduplicates them rather than adding the two neighborhood counts

### Requirement: Population-Normalized Views
Raw participation and per-population views SHALL be labeled separately. A normalized rate SHALL require a licensed authoritative denominator matching the geography/version and disclosed population date, unit and method. Missing/incompatible denominators SHALL produce unavailable rates, not zeros or fabricated estimates. A small or self-selected sample SHALL not imply that all residents help at the displayed rate.

#### Scenario: Postal area has no matching population denominator
- **WHEN** users switch to the normalized comparison
- **THEN** that area's rate is unavailable with an explanation while eligible raw volume can remain visible under privacy rules

### Requirement: Opt-In and Small-Cohort Protection
Use of owned activity in public comparisons SHALL require disclosed participation permission; declining SHALL not remove earned personal recognition. Releases SHALL enforce approved distinct-person thresholds, sensitive-area suppression and protection from filter/time differencing. Rankings SHALL expose no participant list or household observations. Missing privacy policy SHALL disable publication, and suppressed areas SHALL not rank last as zero.

#### Scenario: A neighborhood has one qualifying volunteer
- **WHEN** its cohort fails the approved disclosure threshold
- **THEN** the area is suppressed/coarsened consistently across table, map, downloads and totals rather than identifying the volunteer

### Requirement: Auditable Recalculation and Anti-Gaming
Ranking projections SHALL deduplicate canonical evidence across event/contribution/assisted records and recompute after corrections, consent changes or boundary revisions. Method changes SHALL have effective versions and comparable historical labeling. Suspicious activity SHALL enter authorized review rather than automatic public accusations. Ordering and ties SHALL follow the published metric policy, not payment or undisclosed promotion.

#### Scenario: Duplicate attendance feeds several systems
- **WHEN** the same activity reaches the ranking through event and contribution updates
- **THEN** it changes eligible counts once and a later invalidation removes its dependent contribution once

### Requirement: Respectful Accessible Comparisons
Web and native Impact SHALL offer country selection, neighborhood/postal level, period and metric with equivalent map/list views in Spanish/English. Labels SHALL describe recorded participation, not the best or worst people. Unknown, stale and suppressed data SHALL remain distinct. No place's position SHALL control household aid eligibility, center closure or an individual's organizer permissions.

#### Scenario: User opens a low-ranked area's details
- **WHEN** that area has limited platform participation
- **THEN** the interface shows coverage/denominator limitations and helping opportunities, not a claim that residents are ungenerous
