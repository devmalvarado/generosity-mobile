# Participation API

Local PHPUnit evidence from 2026-11-04. It is not a production round trip,
and it does not complete any task in
`openspec/changes/add-web-participation-and-sharing`. Migration
`2026_11_03_000003_create_participation_tables` has not been deployed.

`ParticipationContractTest` and the OpenAPI route-coverage assertion passed
on the isolated `generosity_test` database: 12 tests, 314 assertions. The
clock inside those tests was fixed at `2026-11-04 15:00:00`. No mail, push,
or points row was produced. Reviewer ids, the meal withdrawal channel, the
organizer threshold, the consent version, and operator ids used by individual
tests were process-local fixtures. They are not selected production values.
Empty configuration still returns 503 and writes nothing.

## What the API does

Public reads, with no feature flag, are `GET /api/v1/campaigns`,
`GET /api/v1/campaigns/{id}`, `GET /api/v1/occurrences`,
`GET /api/v1/occurrences/{id}`, `GET /api/v1/meals`,
`GET /api/v1/meals/{id}`, `GET /api/v1/opportunities/{kind}/{id}`,
`GET /api/v1/needs/{id}/progress`, and
`GET /api/v1/outcome-updates/{id}`. Lists contain published rows only. A
direct draft is 404 and does not substitute another record. A cancelled
occurrence stays cancelled with an empty `alternatives` list. Meal
`private_details` and campaign `private_plan` are not selected. Progress
keeps `reported` unknown and `fulfilled` false. Preview ignores `channel`,
rejects `next` and `url`, and records no attribution, QR image, or remaining
seat promise.

Authenticated writes use a bearer token and a UUID `Idempotency-Key`. The
nine participation flags default to `off`. Campaign publication returns 503
when the points threshold is absent, and still returns 503 when a number is
configured, because no points ledger exists. Meal commitment and withdrawal
return 503 while `MEAL_WITHDRAWAL_CHANNEL` is empty. Assisted-claim review
returns 503 while `ASSISTED_CLAIM_REVIEWER_IDS` is empty. Email follow
returns 503 while consent or delivery is unconfigured, and no follow row is
written. The operator funnel returns 503 while `FUNNEL_OPERATOR_IDS` is
empty. `shareSheetOpens` and `returnParticipation` stay null.

With the flags enabled in the test database, center staff can publish a
future occurrence. One account and one assisted subject share the same seat
counter. A duplicate enrollment does not increment `reserved`. Cancellation
before the start releases one seat and a second cancel does not decrement
again. A venue or time change with reserved seats returns
`replacement_required`. Attendance after the end is recorded once by other
center staff as one `center_validated` fulfilled time contribution. Assisted
attendance creates no contribution and no points. A subject record creates
no user and no membership. A claim requires a subject id; a name is rejected
and contact is not returned. Approval sets `claimed_user_id` and copies no
enrollment or contribution. The claimant sees that assisted enrollment only
after approval.

An approved meal plan does not open commitments until a withdrawal channel
is configured. A later fixture channel rejected a partial quantity and did
not send a notice. Outcome text with a private key or an email address is
rejected. Withdrawal clears the public body. Inbox and push follows record
`sent: false`. Unsubscribe works while `follows.update` is off.
`GET /api/v1/me/notifications` is owner-scoped.
`GET /api/v1/me/notification-preferences` returns `{ "saved": false }` when
no row exists.

## Not verified

The landing does not call these routes. No browser, Android, or iOS journey
was run. No last-seat race was run. No production database was migrated.
Payments, provider login, QR images, native app links, mail, push delivery,
points, retention purges, and reviewer selection remain unavailable.

## Authorization and version closeout — 2026-11-04

The focused suite now passes 15 tests / 341 assertions. Current center authority
is rechecked before assisted cancellations and scoped idempotent receipts; revoked
staff cannot use historical actor IDs or accepted operations to regain access.
Outcome publication checks the effective flag at the publish transition, while
withdrawal remains available. Approved assisted associations prevent a second
reservation; overlapping claims stay conflicted for explicit review rather than
merging capacity or validation evidence. Public occurrence reads now include the
current version so staff can refresh after enrollment changes.

These are local safety/contract corrections. They do not provide the remaining
race, reviewer, browser/native or production acceptance for the full feature.
