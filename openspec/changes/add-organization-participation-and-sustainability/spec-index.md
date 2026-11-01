# Organization participation and sustainability

Requested 2026-11-01. Five new capabilities; planning only, no production changes.

| User need | Spec | Task section |
| --- | --- | --- |
| Know whether revenue covers hosting and other operating costs | [platform-cost-funding](specs/platform-cost-funding/spec.md) | 5 |
| Small ads or sponsorships | [contextual-sponsorships](specs/contextual-sponsorships/spec.md) | 6 |
| Charge supporting businesses for appearing | [supporting-business-directory](specs/supporting-business-directory/spec.md) | 7 |
| Help using a company's or association's own identity/resources | [organization-helping](specs/organization-helping/spec.md) | 1–2 |
| Members accumulate points toward a temporary campaign, with team and opt-in individual standings | [organization-campaign-competitions](specs/organization-campaign-competitions/spec.md) | 3–4 |

## Existing work reused

- [Center onboarding](../add-platform-expansion-features/specs/center-onboarding/spec.md) handles beneficiary-center applications. It is distinct from donating organizations and paid listings.
- [Community campaigns](../add-community-campaigns-and-coverage/specs/community-campaigns/spec.md) supplies activities, reservations and independent validation.
- [Organizer eligibility](../add-community-campaigns-and-coverage/specs/community-organizer-eligibility/spec.md) supplies earned permissions and verified phone for creation.
- [Gamification](../add-reference-product-features/specs/impact-gamification/spec.md) supplies canonical evidence and audited personal awards.
- [External donations](../add-reference-product-features/specs/external-donations/spec.md) supplies charitable money lifecycles; commercial billing has a separate purpose/ledger.
- [Recognition](../add-platform-expansion-features/specs/recognition-credentials/spec.md) supplies PDFs/badges and correction semantics.
- [Regional rankings](../add-platform-expansion-features/specs/regional-help-rankings/spec.md) compares places, independently of organization/team standings.

## Order and unresolved choices

Implement organization permissions and real contributions first, then opt-in
campaigns/collective points, then optional competition and recognition. Paid
directory/sponsorship work can proceed separately after billing and content review
decisions. Basic helping does not depend on commercial activation.

The user approved team/organization comparisons and voluntary individual rankings
visible only within the campaign. Personal history outside that scope stays
private. No prices, providers or numeric scoring policy were selected.
See [design](design.md) and [tasks](tasks.md). Nothing is ready to archive.
