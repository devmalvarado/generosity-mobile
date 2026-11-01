## Why

Generosity needs a way to cover operating costs and let companies, associations
and other organizations contribute collectively. Existing center onboarding is
for receiving/managing help; it does not model an organization helping through
its own resources and members. Community campaigns and territorial rankings do
not yet cover temporary organization/team competitions.

## What Changes

- Plan separately accounted platform revenue from modest sponsorship placements and paid supporting-business listings, with a view of actual cost coverage.
- Add a clearly labeled supporting-business directory, separate from the Kindness Center taxonomy and service-availability map.
- Let a person act personally or as an authorized organization representative, with invitations, bounded roles and organization-owned contributions/history.
- Attribute willing members' validated help to an organization and a time-bounded campaign without multiplying personal or global points.
- Add cooperative targets and optional temporary team/organization competitions, plus individually opted-in rankings visible only within the campaign, with published rules, comparable metrics and auditable corrections.
- Reuse existing recognition PDFs/badges; buying visibility, sponsoring costs or having greater revenue cannot purchase helping points or validation authority.

## Capabilities

### New Capabilities

- `platform-cost-funding`: Commercial plans, billing lifecycle and cost/revenue accounting separate from charitable donations.
- `contextual-sponsorships`: Small, disclosed, reviewed contextual placements with bounded delivery and no sensitive targeting.
- `supporting-business-directory`: Reviewed commercial profiles, paid visibility and evidence-backed helping claims.
- `organization-helping`: Organization identity, authorized members, contribution ownership and collective impact.
- `organization-campaign-competitions`: Voluntary time-limited campaigns, team and participant-only individual scoring, fair comparisons and results.

### Modified Capabilities

No synchronized baseline spec changes. Clarify the active `impact-gamification`,
`community-campaigns` and `recognition-credentials` plans to reference this narrow
extension. Their existing evidence, permission and privacy boundaries remain.
The general no-leaderboard rule still prohibits a global named-person ranking;
the new competition compares teams/organizations and, by the user's explicit
choice, individually opted-in participants inside that campaign only. Personal
history and non-campaign progress stay private. Geographical comparisons remain
a separate capability.

## Impact

Uses the existing PHP/Laravel API, MySQL/MariaDB, Generosity-only identity,
contribution/award ledgers, canonical mobile-first portal and KMP contracts.
Android and iOS gain equivalent participation and impact views. Organization
membership is not institute-wide SSO, employer identity federation or center staff
authority. No new backend, payment integration or ad SDK is installed by this plan.

All implementation tasks are pending. Prices, currencies, billing provider,
commercial seller, refund terms, ad placement limits and campaign scoring policy
need configuration before their corresponding features can go live. This request
does not authorize publication, subscriptions, real charges or enrolling employees.
