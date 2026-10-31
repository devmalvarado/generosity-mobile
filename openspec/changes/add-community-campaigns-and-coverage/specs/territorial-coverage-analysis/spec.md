## Purpose
Compare privacy-safe observed needs with accessible service capacity and evaluate territorial alternatives transparently, without treating center density or incomplete samples as proof of optimal placement.

## ADDED Requirements

### Requirement: Comparable Demand and Service Inputs
Analysis SHALL match aggregate observed needs to versioned service codes, units and periods. Authorized center staff SHALL describe actual offered services, eligibility conditions, schedules, accessibility, capacity, shared resource pools and update dates. Center categories, volunteer seats, requested donations and opening hours alone SHALL not imply service capacity. Unknown values SHALL remain unknown rather than become zero or unlimited supply.

#### Scenario: Center requests groceries
- **WHEN** a catalog center publishes a grocery donation need but has no meal-service capacity record
- **THEN** analysis does not treat requested groceries as meals available to households

#### Scenario: Quantities use incompatible units
- **WHEN** one source describes households per month and another describes meals per day without an approved conversion
- **THEN** they remain separate or excluded with an explicit reason instead of being summed

### Requirement: Bounded Accessible Allocation
Coverage estimates SHALL respect compatible service, period, eligibility, available capacity and travel constraints. Across overlapping areas, each demand unit SHALL be allocated at most once and shared center capacity SHALL not be reused. Results SHALL separate allocated observed demand, unmet observed demand, unknowns and unused modeled capacity. No record SHALL be assigned to a real household or reserve a service through this analysis.

#### Scenario: Two areas compete for one center
- **WHEN** two areas each have eight comparable demand units and their only reachable center has ten units for that period
- **THEN** the model allocates at most ten total, reports at least six unmet observed units and does not show both areas fully served

### Requirement: Explicit Accessibility Assumptions
Each run SHALL identify geography resolution, travel mode, threshold, routing source/version and applicable schedules. Network travel estimates SHALL not be replaced silently by straight-line distance. Without a suitable routing source, results SHALL be labeled proximity-only or unavailable, with location uncertainty disclosed. Barriers and missing accessibility data SHALL not be interpreted as confirmed access.

#### Scenario: Nearby center is across an impassable barrier
- **WHEN** the selected routing model finds no valid route within the allowed travel conditions
- **THEN** that center does not satisfy reachable capacity merely because its map marker is close

#### Scenario: Routing service is unavailable
- **WHEN** a network-based run cannot obtain required routes
- **THEN** it fails transparently or offers a separately labeled proximity scenario, without inventing travel minutes

### Requirement: Coverage Gaps and Evidence Limits
The interface SHALL distinguish unmet observed demand, unmeasured areas, stale sources, suppressed values and potential service overlap. It SHALL display observation dates, sample coverage, duplicate uncertainty and missing capacity. Convenience outreach SHALL not be presented as a representative population census or causal impact estimate. Multiple nearby centers SHALL not alone produce an excess, closure or relocation verdict.

#### Scenario: No households were surveyed in an area
- **WHEN** an area has no usable observations
- **THEN** it is labeled unmeasured, not free of needs or overprovided because a center is present

### Requirement: Reproducible What-If Comparisons
Assigned analysts SHALL compare a baseline with explicit candidate sites, outreach sessions, hours, capacity changes or relocations. A run SHALL retain input revisions, assumptions, method/version, objective and constraints. Relocation SHALL remove the modeled source capacity before adding destination capacity. Comparisons SHALL report gained/lost coverage by area/service, costs when supplied and sensitivity to uncertain inputs, without changing the live catalog.

#### Scenario: Moving a center helps one area but harms another
- **WHEN** an analyst simulates moving the same capacity to a candidate site
- **THEN** both gained and lost coverage are shown against the same baseline, with no duplicated capacity or automatic address update

### Requirement: Human-Reviewed Decisions
Recommendations SHALL be labeled decision support, not proof of maximum good or a ranking of people's worth. A scoped reviewer SHALL record the considered options, evidence limits, affected-service trade-offs and accept/defer/reject rationale before a scenario becomes a proposed action. Unknown costs SHALL block cost-effectiveness claims. No calculation SHALL automatically close a center, deny household help or award gamification.

#### Scenario: Model shows unused capacity
- **WHEN** a reviewer sees unused modeled capacity near another center
- **THEN** the interface exposes service compatibility, data gaps and alternatives such as outreach or changed hours, without labeling that center unnecessary

### Requirement: Scoped Privacy-Safe Results
Only explicitly assigned analysts SHALL create territorial scenarios; center staff SHALL manage only their own service inputs. Public and native map results SHALL use separately approved aggregate releases. Private analytical runs and exports SHALL enforce the same household suppression rules, including derived comparisons. No role SHALL obtain raw household locations or contacts through analysis. Revoked source consent SHALL invalidate affected results for refresh.

#### Scenario: Analyst exports a comparison
- **WHEN** a scoped analyst exports a run with a suppressed small cohort
- **THEN** the export preserves suppression and cannot recover the hidden cohort through baseline/scenario subtraction

### Requirement: Usable Explainable Coverage Views
The mobile-first portal SHALL offer map and equivalent list/table views with service, period and scenario selection; native Map SHALL expose published aggregate coverage without changing center discovery. Legends SHALL separate unknown, suppressed and zero values. Spanish/English labels, units, dates, accessible text and small-screen layouts SHALL remain consistent. A saved scenario SHALL show stale/unavailable state when its inputs change.

#### Scenario: Compare results on a phone without the map
- **WHEN** an analyst uses the tabular view at 320 CSS pixels with enlarged Spanish text
- **THEN** baseline, candidate, gained/lost coverage, assumptions and uncertainty remain readable without relying on color or hover interactions
