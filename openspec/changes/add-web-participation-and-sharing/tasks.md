# Tasks

Broad participation tasks remain pending; the scoped directory-map work below is locally complete. Dependencies are planned capabilities, not verified
services. Test with synthetic participants; production communication requires
separate channel and consent configuration.

A partial landing slice was exercised with intercepted browser fixtures on
2026-11-03. It does not complete any task below. See
[public discovery verification](../../../docs/testing/public-discovery.md).

Local participation API tests passed on `generosity_test` on 2026-11-04.
The migration is not deployed, empty configuration keeps the gated actions
unavailable, and no task below is complete. See
[participation API verification](../../../docs/testing/participation-api.md).

## 1. Contracts and Prerequisites

- [ ] 1.1 Verify foundation identity/portal transactions and relevant event/meal/inbox prerequisites with their integration tests; document blockers and the browser route/authorization matrix. Local `ParticipationContractTest` covers the new tables and gates. The browser route matrix and a production round trip are absent, so this task is not complete.
- [ ] 1.2 Add shared public opportunity and assisted subject API contracts; verify old account-bound records remain compatible and actor/subject distinctions cannot bypass authorization. PHP OpenAPI paths were added and the route-coverage assertion passed. Shared mobile contracts and old-client compatibility were not verified, so this task is not complete.

## 2. Public Web Participation

- [ ] 2.1 Add public center/need/campaign/occurrence/meal browser routes; verify direct entry, unknown/removed records, sanitized metadata and map-free low-bandwidth browsing. The dedicated `/ayudar/` route reads the public sync snapshot and center/need detail endpoints; legacy fragments redirect to it. A direct 404 or invalid prefix does not open another record or fetch. Rendering allowlists localized public text and validated contact fields. The landing still has no campaign, occurrence, or meal route. The additional campaign/occurrence/meal APIs are not wired into this page, so this broad task is not complete.
- [ ] 2.6 Open the landing help action into public discovery without an account; verify demonstration cards stay labeled, catalog failure does not present them as current needs, and the action does not submit or open `/kindness-center`. The help action and cause-card actions open `#descubrir` without an account, without a POST and without `/kindness-center`. Illustration cards stay labeled in `#necesidades`. A catalog failure keeps the search and does not present those cards as current needs. Browser fixtures are not a production round trip, so this task is not complete.
- [ ] 2.2 Add provider-backed browser entry with preserved intent and session/CSRF protection; verify cancelled login, embedded-browser fallback, expired session and no automatic submission.
- [ ] 2.3 Implement browser goods/time/event/meal/personal-help forms against existing endpoints; verify concurrent native/web capacity, stale availability, lost-response retry and gated monetary actions. The landing can prepare goods or time against a published, unexpired need and submits only with a verified session, CSRF, a UUID `Idempotency-Key` and `contributions.create`. Fixtures verified a goods lost-response retry and `capacity_exceeded`. They did not submit time, events, meals or personal help, and they did not race a native client. Money stays disabled. This task is not complete.
- [ ] 2.4 Add owned history/cancellation/inbox/preferences; verify account switching, back navigation, ES/EN browser defaults and no private cache leakage. A verified session can read `GET /api/v1/me/contributions` and cancel its own offered row with `POST /api/v1/contributions/{id}/transition` and `{ action: "cancel", version }`. An account switch clears owner-scoped drafts. `GET /api/v1/me/notifications` and `GET /api/v1/me/notification-preferences` exist. The landing does not read them, and account-switch, back-navigation, and private-cache coverage are absent, so this task is not complete.
- [ ] 2.5 Add optional PWA manifest/service worker with public-only caching; verify offline stale states, no acknowledged offline reservation, private-route exclusions and upgrade/rollback without losing forms; document browser limits.

## 3. Invitations and QR Entry

- [ ] 3.1 Implement canonical opportunity URLs, safe preview projections and bounded QR/text export; verify no recipient/household/roster data, no GET mutations and current cancelled-target resolution; document link ownership. The visible link is the current origin plus `#centro/{id}` or `#necesidad/{id}`. A text download names the record and states that it is not a scannable image and does not show remaining places. No QR image is generated, and the landing does not mint a server-owned short link. The API preview ignores `channel` and a cancelled occurrence stays cancelled with empty alternatives. The landing does not use that preview, so this task is not complete.
- [ ] 3.2 Add web share/copy/download controls and optional opaque channel attribution; verify cancelled sharing records no send/award, declined analytics leaves participation usable and redirects are allowlisted. Copy, share and download controls exist. Closing the share sheet records no send. The fixture link has no channel code. The API records no attribution when `channel` is present. Analytics and native share handling are absent, so this task is not complete.
- [ ] 3.3 Add shared route validation and Android app-link/share handling; verify matching destination, safe web fallback and no tokens/private fields in URLs.
- [ ] 3.4 Add equivalent SwiftUI universal-link/share behavior; verify installed/uninstalled entry, malformed links, ES/EN labels and unchanged central plus action.

## 4. Assisted Participation

- [ ] 4.1 Implement private participant references, scoped coordinator writes and consent metadata; verify no fabricated accounts, mandatory contact or privilege escalation. The API can store a center-scoped subject without creating a user or membership. Browser and mobile entry are absent, so this task is not complete.
- [ ] 4.2 Reuse inventory/cancellation transactions for assisted subjects; verify web-versus-assisted last-seat races, quantity limits, retries and offline pending state. Sequential shared capacity, duplicate enrollment, and one-time cancellation passed locally. No race or offline pending state was verified, so this task is not complete.
- [ ] 4.3 Implement verified association and conflict review; verify name-only claims fail, duplicate seats reconcile once and household/referral records never transfer; document no-contact recovery and retention. A name-only claim is rejected and approval copies no history. Review stays 503 while reviewer ids are empty. No-contact recovery and retention are not configured, so this task is not complete.
- [ ] 4.4 Build mobile-first coordinator entry/confirmation/correction and paper-reference output; verify phone keyboard/error flows, private identity checks and cancellation without an app.
- [ ] 4.5 Expose verified associated history through shared state and native account views; verify each platform preserves evidence identity and association itself creates no points, certificate or review eligibility. `GET /api/v1/me/enrollments` shows an assisted seat only after `claimed_user_id` matches, and approval awards nothing. Native views are absent, so this task is not complete.

## 5. Outcomes and Continuity

- [ ] 5.1 Implement requested/committed/reported/validated public projections and attributed update lifecycle; verify compatible units, corrections, withdrawal and sanitized beneficiary publication permissions. Need progress keeps `fulfilled` false and `reported` unknown. Outcome withdrawal clears the public body, and a private key or email is rejected. Compatible-unit and correction coverage is incomplete, so this task is not complete.
- [ ] 5.2 Add channel-specific subscriptions, verified email ownership and unsubscribe; verify duplicate prevention, unsupported push fallback, revocation and no implicit marketing consent. Email follow stays 503 and writes no row. Inbox and push follows record `sent: false`, and unsubscribe works while the create flag is off. Verified email ownership was not exercised, so this task is not complete.
- [ ] 5.3 Build mobile-first updates/progress and scoped aggregate metrics views; verify consent suppression, canonical completion deduplication and no claim that a share-sheet open is delivery; document metric definitions/limits. The operator funnel stays 503 while operator ids are empty. When a test fixture lists an operator, `shareSheetOpens` remains null. No mobile view was built, so this task is not complete.
- [ ] 5.4 Add shared update/subscription state and Android readable/shareable results; verify ES/EN units, explicit publication choice and corrected hosted links.
- [ ] 5.5 Add equivalent SwiftUI results and preferences; verify state parity, accessible summaries and account isolation.

## 6. Cross-System Acceptance

- [ ] 6.1 Exercise shared link -> browser participation -> center validation -> corrected shared outcome plus assisted claim across backend/web/Android/iOS; verify one capacity/evidence identity and no referral rewards.
- [ ] 6.2 Run Chromium/WebKit screenshots/overflow at 320/360/390/768/1280, real mobile keyboard and social-browser checks, ES/EN 200-percent text and private-cache inspection; record evidence and run strict OpenSpec validation without marking unverified work complete.

## 7. Focused directory and access — 2026-11-05

- [x] 7.1 Clean `/ayudar/` into compact account access, search/location, real map and separate center/need lists; hide the offer form until a need is opened.
- [x] 7.2 Reuse the bounded public snapshot for full-directory/address search, validated coordinate markers and local result pagination; preserve authoritative need detail, CSRF and idempotent write paths.
- [x] 7.3 Add opt-in location, timeout/denial/manual-search fallbacks, distance ordering and pin/list selection; test that visitor coordinates never enter API requests or browser storage.
- [x] 7.4 Keep hosted sign-in reachable when session checks fail; retain drafts and add an explicitly public-only loopback relay for live catalog preview.
- [x] 7.5 Verify Chromium/WebKit regression and focused map/access tests with intercepted tiles; manually inspect real OSM cartography and the public catalog locally.
- [ ] 7.6 Publish the routes, include OpenStreetMap in the hosted privacy provider disclosure, and verify real HTTPS login/return plus Safari/Chrome location permissions. The local read-only relay does not establish this acceptance.
