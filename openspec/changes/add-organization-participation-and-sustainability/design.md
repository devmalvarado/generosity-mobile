## Context

Requested on 2026-11-01: cover costs with small ads or paid business visibility;
allow organizations, including large employers, to help as organizations; allow
members to accumulate points in campaigns or temporary competitions. The user
also explicitly chose voluntary individual rankings visible only inside the
campaign, alongside team/organization standings.

Existing plans already cover center applications, independently validated help,
community events, points, recognition documents and geographical comparisons.
The new landing intake requests a Kindness Center location; it does not establish
a donating organization or commercial advertiser. Keep those purposes explicit.

## Scope and first delivery

Use five bounded capabilities, reusing existing infrastructure. Start with
organization identity and contribution attribution, then voluntary cooperative
campaigns and team comparisons. Commercial profiles and manually reviewed
sponsorship placements can follow independently once their billing and operation
decisions are configured. A programmatic advertising network, enterprise SSO,
payroll deductions, employee surveillance, cash prizes, point redemption and a
general social network are outside this change.

## Decisions

### Organization and human authority

An organization is a durable helping actor, not a shared login or a special class
of person. Supported type codes initially distinguish company, nonprofit,
educational institution, community organization and other. A company may have
many locations/teams; those are not automatically beneficiary centers or public
business listings. Do not collect revenue, employee payroll or business size as
identity requirements or score inputs.

The human still authenticates through Generosity. Membership roles are owner,
coordinator and member: owners manage representation, invitations and billing;
coordinators manage explicitly assigned contributions/campaigns; members choose
their own participation and attribution. Platform operations reviews contested
representation and ownership transfers. Knowing an email domain or paying does
not establish ownership. Each request includes a scoped organization context that
the server rechecks. Organization creation is group creation and requires the
creating account's verified private mobile number; public browsing and ordinary
personal helping retain their existing requirements.

Campaign organizers also retain the earned organizer eligibility and phone rules.
A new company can contribute without buying those privileges; an eligible,
accepted coordinator must own event creation. Invitations require acceptance,
expire and can be revoked. Joining an organization grants neither center staff
access nor household/medical-case access. Representatives cannot independently
validate their own organization's help using another role.

### Contribution and score provenance

Record the authenticated actor separately from the contributing subject
(`person` or `organization`). The subject, center/need and optional attribution
are explicit before submission. Server idempotency and reservation rules remain
authoritative. Organization money follows the existing hosted donation gates;
commercial fees follow a different ledger and recipient purpose.

A bulk organization goods donation is one contribution; it is not copied into
every employee's history. A coordinator's click earns no personal points.
Employees performing actual help may have independently evidenced participation
records with their own canonical identities. Organization summaries reference
these records; they never sum the organization roll-up and its member rows as
additional help. Sponsor money, company revenue, employee headcount, shares and
invitations do not generate points. Money totals stay currency-specific.

Personal awards retain their published rule. A campaign score is a projection of
eligible evidence under a separately published, frozen campaign rule, not a new
deposit into personal balances or an organizer-level multiplier. Use one selected
organization/team and at most one competitive campaign per canonical activity.
An organization total and a campaign view may reference that same activity, but
the global ledger counts it once. Corrections propagate through every projection.

### Campaigns and temporary comparisons

Support voluntary cooperative targets, team/organization standings and optional
individual standings inside a campaign. Individual visibility requires a separate
opt-in, even after joining a team, and a preview of the chosen display name or
campaign alias and score scope. Only currently enrolled campaign participants may
read it; being an organization administrator alone does not grant that access.
No public share/preview, search index, general employee export or global personal
leaderboard is created. Declining does not reduce points or block participation.
Organizations cannot publish unrelated personal history or nonparticipation.
Withdrawal removes the individual entry and clears cached rank/name data across
clients; separately consented team attribution may remain, and personal awards
are retained. Explain that previously viewed external screenshots cannot be recalled.

Reuse community campaigns and event reservations rather than creating another
event engine. Each challenge has registration deadline, start/end instants, an
IANA timezone, validation cutoff, eligible evidence, scoring/caps, ties and
visibility. Dates and rules are visible before opt-in and frozen when enrollment
opens; material changes require cancellation/replacement and renewed enrollment.
Roster/teams lock at the published registration deadline, at or before the start.
Late helpers may help normally, without retroactive entry into that competition.

Standings default to enrolled participants only. Public organization/team totals
require separate consent and an approved aggregation threshold; individual
rankings always remain inside the campaign and no participant list is public.
Absolute totals and points per enrolled participant are separate
views. The normalized denominator is the enrollment snapshot, not employee
headcount, revenue or just active scorers. Departures do not shrink it to improve
a score. Missing or non-disclosable denominators yield unavailable comparisons.
This supports comparing large and small organizations without claiming either is
morally better. No ad purchase changes results or tie-breaking.

Completion time determines whether help falls within the campaign; validation
must arrive by its separately published cutoff for final results. Until then
results are provisional. Later evidence corrections create a revised result and
update recognition; they do not erase the prior outcome silently. Leaving an
organization revokes authority immediately but does not transfer historical
awards to a new employer. Withdrawing ranking consent removes identifying entries
and excluded evidence from rankings while retaining personal help history under
the applicable retention policy. Do not remove a departed member from a frozen
denominator merely to inflate a rate.

### Commercial visibility and cost coverage

Use manual, reviewed contextual sponsorships first: a small labeled card/banner
on configured public discovery surfaces, with a text alternative and a dismissal
control. No full-screen interruption, rewarded ad, sound, tracking SDK or ad in
authentication, contribution submission, case/referral or medicine flows. Initial
targeting uses page topic or an explicitly selected public area, not inferred
beneficiary need, health, precise location or employee behavior. Failure to load
an ad cannot prevent helping.

Supporting businesses have a separate directory and optional layer, with business
type, safe public contact, service area, localized description and reviewed
helping claims. Paying buys a disclosed commercial listing for a period, not a
Kindness Center category, verification badge, care-service acceptance, organic
need ranking or customer-data access. A business that also provides an accepted
center service follows the separate center onboarding/service contracts.

Commercial orders identify seller, organization payer, product, price, currency,
term, renewal and cancellation terms. Prefer a fixed-term listing/sponsorship as
the initial product; recurring renewal, if offered, requires explicit consent.
No pricing tier, amount, provider or trial is assumed. Reuse verified provider
event processing, not donation ledger entries. Eligibility requires both payment
state and approved content; a forged return cannot activate a placement. Expiry,
refund, dispute and cancellation reconcile entitlement without deleting history.
Production checkout on each platform waits for its applicable billing/store
review; working web payment does not automatically authorize native checkout.

The cost view separates planned, incurred and paid hosting/domain/email/SMS/map/
provider/operational expenses from invoiced, collected, refunded and net commercial
income. Unknown expenses remain unknown; different currencies are not summed.
Only authorized operations may see detailed finance records. Optional public
transparency uses approved aggregates, period and coverage. Neither platform fees
nor organization sponsorships are portrayed as beneficiary donations or fiscal
donation receipts; no percentage of charitable donations is diverted by this plan.

## Architecture and release dependencies

Backend owns roles, billing, attribution, validation, scoring, corrections and
aggregate permission checks. Reuse existing policies, transactions, outbox and
Hostinger cron jobs; process projections in bounded batches with retry checkpoints.
KMP owns typed organization/campaign contracts and client presentation state,
not an alternate scoring authority. Native UI adapters and the mobile-first web
portal use the same IDs, versions and localized stable status codes. Account or
organization switching clears private drafts/caches; pending offline operations
reauthorize online. Lists and score projections are paginated for large employers.

Open decisions: commercial seller/provider and prices; supported currencies and
refund rules; placement inventory/frequency; review staffing; representation and
retention policy; organization/public cohort disclosure thresholds; campaign
scoring/caps and initial supported campaign types. They gate only the affected
features. Existing browsing, ordinary helping and personal recognition continue.

## Verification

Test unauthorized organization context, invite replay/revocation, former members,
cross-account caches, independent validation, bulk-goods attribution and projection
deduplication. Test frozen rosters, cutoff/timezone boundaries, late evidence,
withdrawal and corrections. Billing tests use provider sandbox only, including
duplicate/out-of-order events, refund, failed renewal and expiry. Verify organic
discovery remains unchanged by payment. Verify Spanish/English, 320 CSS pixels,
200% text, keyboard/screen-reader flows and Android/iOS/web agreement. No feature
is complete from models or a successful build alone; archive only after evidence.
