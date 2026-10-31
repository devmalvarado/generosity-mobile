# Tasks

All tasks are pending. Dependencies are planned capabilities, not verified
services. Test with synthetic participants; production communication requires
separate channel and consent configuration.

## 1. Contracts and Prerequisites

- [ ] 1.1 Verify foundation identity/portal transactions and relevant event/meal/inbox prerequisites with their integration tests; document blockers and the browser route/authorization matrix.
- [ ] 1.2 Add shared public opportunity and assisted subject API contracts; verify old account-bound records remain compatible and actor/subject distinctions cannot bypass authorization.

## 2. Public Web Participation

- [ ] 2.1 Add public center/need/campaign/occurrence/meal browser routes; verify direct entry, unknown/removed records, sanitized metadata and map-free low-bandwidth browsing.
- [ ] 2.2 Add provider-backed browser entry with preserved intent and session/CSRF protection; verify cancelled login, embedded-browser fallback, expired session and no automatic submission.
- [ ] 2.3 Implement browser goods/time/event/meal/personal-help forms against existing endpoints; verify concurrent native/web capacity, stale availability, lost-response retry and gated monetary actions.
- [ ] 2.4 Add owned history/cancellation/inbox/preferences; verify account switching, back navigation, ES/EN browser defaults and no private cache leakage.
- [ ] 2.5 Add optional PWA manifest/service worker with public-only caching; verify offline stale states, no acknowledged offline reservation, private-route exclusions and upgrade/rollback without losing forms; document browser limits.

## 3. Invitations and QR Entry

- [ ] 3.1 Implement canonical opportunity URLs, safe preview projections and bounded QR/text export; verify no recipient/household/roster data, no GET mutations and current cancelled-target resolution; document link ownership.
- [ ] 3.2 Add web share/copy/download controls and optional opaque channel attribution; verify cancelled sharing records no send/award, declined analytics leaves participation usable and redirects are allowlisted.
- [ ] 3.3 Add shared route validation and Android app-link/share handling; verify matching destination, safe web fallback and no tokens/private fields in URLs.
- [ ] 3.4 Add equivalent SwiftUI universal-link/share behavior; verify installed/uninstalled entry, malformed links, ES/EN labels and unchanged central plus action.

## 4. Assisted Participation

- [ ] 4.1 Implement private participant references, scoped coordinator writes and consent metadata; verify no fabricated accounts, mandatory contact or privilege escalation.
- [ ] 4.2 Reuse inventory/cancellation transactions for assisted subjects; verify web-versus-assisted last-seat races, quantity limits, retries and offline pending state.
- [ ] 4.3 Implement verified association and conflict review; verify name-only claims fail, duplicate seats reconcile once and household/referral records never transfer; document no-contact recovery and retention.
- [ ] 4.4 Build mobile-first coordinator entry/confirmation/correction and paper-reference output; verify phone keyboard/error flows, private identity checks and cancellation without an app.
- [ ] 4.5 Expose verified associated history through shared state and native account views; verify each platform preserves evidence identity and association itself creates no points, certificate or review eligibility.

## 5. Outcomes and Continuity

- [ ] 5.1 Implement requested/committed/reported/validated public projections and attributed update lifecycle; verify compatible units, corrections, withdrawal and sanitized beneficiary publication permissions.
- [ ] 5.2 Add channel-specific subscriptions, verified email ownership and unsubscribe; verify duplicate prevention, unsupported push fallback, revocation and no implicit marketing consent.
- [ ] 5.3 Build mobile-first updates/progress and scoped aggregate metrics views; verify consent suppression, canonical completion deduplication and no claim that a share-sheet open is delivery; document metric definitions/limits.
- [ ] 5.4 Add shared update/subscription state and Android readable/shareable results; verify ES/EN units, explicit publication choice and corrected hosted links.
- [ ] 5.5 Add equivalent SwiftUI results and preferences; verify state parity, accessible summaries and account isolation.

## 6. Cross-System Acceptance

- [ ] 6.1 Exercise shared link -> browser participation -> center validation -> corrected shared outcome plus assisted claim across backend/web/Android/iOS; verify one capacity/evidence identity and no referral rewards.
- [ ] 6.2 Run Chromium/WebKit screenshots/overflow at 320/360/390/768/1280, real mobile keyboard and social-browser checks, ES/EN 200-percent text and private-cache inspection; record evidence and run strict OpenSpec validation without marking unverified work complete.
