# Reference Product Features

Planning date: 2026-10-25. This follow-up adds **10 capability specs**, separate
from the foundation's **16**. All new tasks are pending. Structural validation
does not mean the backend, native features or payments are implemented.

## Image Coverage

| Visible element | Contract | Tasks |
| --- | --- | --- |
| Map / Needs / plus / Impact / Profile; avatar/member date; I want to help | [mobile-product-navigation](specs/mobile-product-navigation/spec.md): new routes, need-or-personal-help chooser, contextual center/method entry, account summary and preserved list/settings access. | 11.1-11.4 |
| All / Urgent / Nearby and View all needs | [global-needs-feed](specs/global-needs-feed/spec.md): anonymous feed, combined filters, center constraint, real/manual origin, radius, pagination and current availability. | 3.1-3.4 |
| Heart in center preview/detail | [saved-centers](specs/saved-centers/spec.md): local guest favorites, owned sync, explicit import and saved list. | 4.1-4.4 |
| Share icon | [center-sharing](specs/center-sharing/spec.md): public native links, mobile web fallback and safe payloads. | 5.1-5.3 |
| Center photo and Open now | [center-public-profile](specs/center-public-profile/spec.md): authorized profile/media/hours, timezones, exceptions and provenance. | 2.1-2.5 |
| Stars and review count | [center-reviews](specs/center-reviews/spec.md): completed-help eligibility, pending moderation, averages, reporting and withdrawal. | 6.1-6.5 |
| Points, level, mini-chart, streak, badges and View all | [impact-gamification](specs/impact-gamification/spec.md): audited awards, thresholds, qualifying days and private progress. | 8.1-8.5 |
| Bell and unread state | [notification-inbox](specs/notification-inbox/spec.md): owned/local notices, read state, retention and safe destinations. | 9.1-9.4 |
| Events and volunteering | [volunteer-events](specs/volunteer-events/spec.md): center publication, seats, enrollment, cancellation and validated attendance. | 7.1-7.6 |
| Donate money and monetary total | [external-donations](specs/external-donations/spec.md): hosted provider, verified outcomes, refunds/disputes and per-currency totals with production gates. | 10.1-10.8 |

## Already Covered

The [foundation index](../define-generosity-mobile-app-foundation/spec-index.md)
covers welcome, guest/sign-in/registration, device language, visual styling,
real maps/location, search/categories, center needs, time/goods forms,
contribution history/impact units, reminders, nearby/urgent delivery, contact,
visits, persistence, backend and mobile-first center need publishing.
Do not duplicate these as new features or claim they already work end to end.

The screenshot's exact names, counts, balances and addresses are sample values,
not product requirements. Store badges and the marketing strip do not request
store publication or an in-app promotional screen. Official verification and
tax receipts remain outside these changes.

## Approved Rules

- Plus opens a choice between a published need and self-reported personal help.
- Only center-validated completed help earns points, achievements and streak days; personal activity remains separate.
- Reviews require completed help and independent moderation.
- Monetary donations use an external provider; selecting it and satisfying recipient/release conditions are prerequisites, not completed setup.

Numeric scoring, retention and radius defaults are documented proposals in
[design.md](design.md), not individually approved values from the image.

## Delivery Order

1. Complete required foundation identity, authority, storage, native parity and portal contracts.
2. Deliver profile/catalog, feed, favorites and public links with their API and native tests.
3. Add moderated reviews and events, then connect validated activity to awards and inbox records.
4. Activate functional navigation after its destinations work; the preceding visual-only pass still preserves old routes.
5. Exercise payments only in sandbox until a separate production decision satisfies all gates.

The [task checklist](tasks.md) separates backend/shared, Android, iOS and web
work. The [proposal](proposal.md) defines scope; [design](design.md) resolves
ownership, state machines, migration and operational prerequisites. Nothing
was archived, deployed or charged by creating these artifacts.

## Community Extension

The subsequent [community campaign and coverage plan](../add-community-campaigns-and-coverage/spec-index.md)
adds earned organizer access, reforestation/cleanup, coordinated meals and
consented assessment/territorial analysis. It reuses these event and award
contracts with explicit community ownership. The `volunteer-events` spec here
governs center-owned events; community publication does not grant staff powers,
and organizer-reported completion alone still earns no awards or reviews.

## Directory Field Extension

The later [directory field contract](../define-center-directory-details/spec-index.md)
defines the user-supplied profile details and minimum publication rule while
reusing this change's profile, media and schedule behavior. It adds explicit
unknown values, public/private disclosure, fee and beneficiary/admission semantics,
and contact-only discovery; neither plan is evidence of implemented functionality.
