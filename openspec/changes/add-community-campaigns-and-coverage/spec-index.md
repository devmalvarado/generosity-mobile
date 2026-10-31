# Community Campaigns and Territorial Coverage

Planning date: 2026-10-25. Five new capability specs extend the foundation and
reference-feature plans. All implementation tasks are pending; no real household
data has been collected and no analysis or campaign service has been deployed.

## Coverage

| User need | Spec | Tasks |
| --- | --- | --- |
| Registered users earn the ability to organize through prior help, level and points | [community-organizer-eligibility](specs/community-organizer-eligibility/spec.md): server policy, private progress, scoped rights, holds and handover. | 1, 2 |
| Reforestation, cleanup, meal and assessment campaigns | [community-campaigns](specs/community-campaigns/spec.md): activity plans, sessions/materials, accepted partners, reported outcomes and independent validation. | 3 |
| Meal train-style coordination | [meal-support-coordination](specs/meal-support-coordination/spec.md): recipient-approved dates/portions, private instructions, cancellations and receipt provenance. | 4 |
| Voluntary door-to-door needs assessment and optional follow-up | [household-needs-assessments](specs/household-needs-assessments/spec.md): separate consent, protected cases, offline collection, narrow referrals and safe aggregate release. | 5, 6 |
| Compare needs with centers to make better placement/capacity decisions | [territorial-coverage-analysis](specs/territorial-coverage-analysis/spec.md): service capacity, accessibility, gaps, candidate comparisons, uncertainty and human review. | 7 |

The [proposal](proposal.md) defines scope, [design](design.md) records authority,
privacy, algorithms and release gates, and [tasks](tasks.md) separates backend,
shared KMP, native, portal and integration work. Group 8 verifies whole flows.

## Approved Decisions

- Any registered user can qualify to organize after sufficient prior validated help, level and points; exact thresholds are not selected yet.
- Follow-up is private, optional and consented. Analytical/public maps use aggregate areas, never identifiable household pins.
- The existing center-only completed-help rule still governs points, achievements, streaks and center-review eligibility. Organizers cannot validate their own activity, even if they also hold a staff role.
- The central plus action stays need-or-personal-help. Campaign discovery uses Needs/events and organizer access uses Profile and the mobile-first portal.

## How Coverage Is Interpreted

Compare compatible service units over the same period with usable capacity and
accessibility. Distinguish observed unmet needs, unknown capacity, unmeasured
areas and privacy suppression. Multiple nearby centers are not evidence that
one is unnecessary. Simulate outreach, hours, added capacity, new sites or
relocation and show who could gain or lose coverage; no automatic center closure.

Numbers derived from voluntary outreach describe observed data, not a complete
population census or a proven maximum of social good. Planting, waste collection
and meals also have separate reported/validated outcomes rather than one score.

## Dependencies and Delivery

1. Complete the relevant [foundation](../define-generosity-mobile-app-foundation/spec-index.md) identity, native parity, sync, authority and portal tasks.
2. Complete the [reference features](../add-reference-product-features/spec-index.md) event engine, award ledger and inbox prerequisites. Center-owned events retain their contract; community ownership is an extension, not unrestricted center publication.
3. Deliver earned organizing and ordinary community activities, followed by private meal coordination.
4. Approve protocol, retention, offline access and disclosure rules before field collection; use synthetic households until those gates pass.
5. Deliver approved aggregate analysis and human-reviewed scenarios using explicit service/routing inputs. Validate phone layouts and privacy across APIs, exports and native clients.

No capability is archived or synchronized into the main specs here. Numeric
thresholds, privacy parameters and pilot data/routing choices remain documented
launch gates, not invented defaults. Implementation requires a separate request.

The separate [center-mediated assistance follow-up](../add-center-mediated-assistance/spec-index.md)
adds a helper's minimal consented heads-up and center-validated assistance without
requiring household fieldwork or organizer status. It reuses private consent/access
conventions, not blanket case permissions or offline field-collection rights.
