## Context

See [proposal.md](proposal.md). The existing static landing is still in the older
PoC; [the portal assessment](../../../docs/center-portal.md) identifies simulated
submissions and mobile layout risks. Canonical web/backend implementations are
not present. Shared repository contracts currently cover centers, users and
contributions, not browser transactions or assisted participants. Existing
`center-sharing` specifies a public browser detail but not full participation.

## Goals / Non-Goals

**Goals:** a complete link-to-help journey without installation, one source of
capacity/identity, accountable assisted access and honest outcome distribution.

**Non-Goals:** porting SwiftUI/Compose screens verbatim, a second backend, native
feature parity before the first web release, automatic social posting, contacts
harvesting or invented viral-growth guarantees. Household fieldwork stays in its
separately protected workflow, not public PWA storage.

## Decisions

### One Web Product, One Backend

Extend the canonical `portal/` planned by the foundation rather than create a
separate citizen website. Public discovery/participation, staff and operator
routes have different authorization, not separate catalogs or accounts. Reuse
the backend contracts and idempotency keys for commitments and availability.
Client validation is assistance; the server decides authorization and capacity.

Web UI uses semantic responsive components appropriate to the portal's chosen
implementation. KMP remains the native shared business/state layer; web uses
the same API schemas and contract fixtures without requiring a Kotlin/JS port.
Do not add a UI framework merely to reuse a few validators. Native clients gain
stable opportunity routes and account-association/update models only as needed.

Start with public list/detail and complete goods/time/event/meal participation,
then owned commitments/history/preferences/inbox. Monetary actions reuse the
separate provider gate; unsupported routes say unavailable. Account entry is
required at protected actions, not at browsing. Use the selected identity
provider, secure browser sessions, CSRF protection and allowlisted return state.
Retain opportunity intent but never auto-submit after authentication. If an
embedded social browser cannot authenticate reliably, offer an ordinary-browser
route without losing public opportunity identity or exposing credentials.

### Progressive Enhancement Without False Offline Success

Provide lightweight public HTML/metadata before optional map/media. Installation
is an enhancement, not a condition; [web.dev's PWA guidance](https://web.dev/learn/pwa/progressive-web-apps)
describes this fallback approach. The service worker caches only reviewed static
assets and sanitized public data with revision/freshness, never private APIs,
documents, credentials or household data. Exclude those routes explicitly rather
than assuming sign-out clears every browser cache.

Keep initial browser protected writes online-confirmed. A lost-response retry
uses the same server operation ID; an offline form is visibly unsent and requires
explicit retry, not hidden background submission. Warn before losing unsent
input. Installed PWA mode uses the same restrictions. New service-worker
versions must not discard in-progress forms; test upgrade and rollback. Push
is optional and feature-detected, with inbox/email alternatives where opted in;
[WebKit documents the home-screen requirement for its iOS web push](https://webkit.org/blog/13878/web-push-for-web-apps-on-ios-and-ipados/).

### Canonical Invitations and Safe Distribution

Use stable public routes for center, need, campaign, occurrence and sanitized
meal opportunity. Never link to a private recipient, case, donor note or delivery
instruction. App associations share those routes and always retain web fallback.
Public GETs, crawlers and QR scans never create commitments or authentication.
Unknown/removed/cancelled destinations resolve honestly, not to another record.

Generate preview metadata/QR artwork from a dedicated public projection. QR
art includes a readable URL, date and optional assistance contact. Avoid static
remaining-seat claims; the destination checks current inventory. Use explicit
share/copy/download controls with no background sends. Channel attribution is
an optional opaque campaign/channel code, not sender account identity or a
referral reward. Sanitize/allowlist parameters, prevent open redirects and
exclude private routes from search indexing and preview responses.

### Assisted Subjects Are Not Accounts

Introduce a private participant reference scoped to the necessary coordination
context, with optional contact and consent provenance. Store acting coordinator
separately from participant subject. Reuse the event/commitment engine and
capacity transactions; the authenticated actor requirement does not imply the
subject must own an account. No fake email or shadow login.

Initial assisted scope is time, goods and activity/meal participation where
authorized, not charging someone else's payment details or distributing case
access. Meal recipient/coordinator approval still gates private instructions.
Provide a printed reference or agreed contact channel and an audited correction
route. A reference is not a public bearer credential to change private data.

Later account association requires authenticated user consent plus a verified
claim, such as a one-time challenge through a previously approved contact or
independent staff review of a no-contact claim. Names/contact similarity alone
are insufficient. Claims have bounded lifetime/retries; never expose matching
records before authorization. Conflicting claims enter review.

Maintain canonical activity IDs across association and resolve duplicate seats
in one audited transaction. Do not silently merge accounts. The claim does not
validate activity; only pre-existing or later independent eligible evidence can
feed the award ledger once. Household/referral records are never swept into this
association. Retain published cancellation/support routes for non-account users.

### Outcomes, Subscriptions and Measurement

Create an attributed update with draft/published/corrected/withdrawn status,
public evidence summary and separate media publication permissions. Display
requested, committed, reported and independently completed quantities with units
and timestamps; no promise that commitments equal resolved need. Reuse safe media
validation/moderation rather than allow arbitrary uploads. Exclude identifiable
beneficiary stories by default; publication permission is independent of aid.

Store follows privately by center/campaign and channel. Email contact ownership
must be verified; unsubscribe works without native installation, and stops
optional delivery promptly. Essential transaction state remains readable in
owned history. No contact-list import or implied marketing enrollment.

Measure stages separately: public visit estimate, server-accepted commitment,
validated canonical completion and later participation in another activity.
Counts have period, definition and coverage. Optional browser analytics respects
consent; operational completion counts derive from the ledger, with aggregate
privacy controls and no retroactive identity linkage. Document attribution
loss from copied links, blocked analytics and cross-device use. A share sheet
open is not a delivered invitation. Do not optimize rewards around these counts.

### Phone-First Verification

Reuse device/browser locale defaults and ES/EN override. Cover 320/360/390/768/1280
CSS pixels, 200-percent text, keyboard, screen readers and real mobile browser
keyboards. Check in-app social browsers and browsers without install/push
capabilities. Provide list alternatives and defer maps/images on slow networks.
Test signed-out back navigation, account changes, multiple tabs, service-worker
upgrade, stale capacity, retry and shared-device privacy. Native smoke tests must
exercise matching destinations and unchanged public/plus behavior.

## Risks / Trade-offs

- Lower entry friction can increase spam -> real authentication for self-service writes, rate limits and current authorization; no forced installation as a security substitute.
- Assisted records can be claimed incorrectly -> independent identity/consent verification, scoped data and conflict review, never name-only matching.
- Public previews can outlive withdrawal -> invalidate controlled copies and show corrected hosted status; explain external copies cannot be recalled.
- Offline availability looks authoritative -> stale labeling and online-confirmed commitments; no private PWA cache.
- Measuring invitations can create surveillance -> aggregate optional attribution, defined retention/suppression and no sender/recipient graph.

## Migration Plan

1. Complete required foundation identity/portal transactions and reference/community domain flows; continue native support.
2. Add public browser routes and participation behind route-level flags; keep canonical center links compatible.
3. Add assisted subject migrations without rewriting account IDs; test capacity and association races before enabling coordinator writes.
4. Enable sanitized sharing, outcome updates and consented delivery; measure actual participation with disclosed limitations.
5. Roll back new entry/collection flags without disabling existing commitments, cancellation, unsubscribe, rights requests or history.

## Release Configuration

Public domain/app associations, identity callbacks, email sender, consent and
retention rules, assisted claim/review ownership and safe analytics settings must
be configured/tested before use. No messages are sent and no participants are
created by these planning artifacts.
