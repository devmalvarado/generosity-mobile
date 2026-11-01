## 1. Foundation Dependencies and Contracts

- [ ] 1.1 Record evidence that required foundation shared/native parity, auth, persistence, catalog, center roles, lifecycle and portal contracts work before enabling dependent features; link their existing task results rather than duplicating that implementation.
- [ ] 1.2 Extend versioned backend schemas and API definitions for these feature domains; test ownership, migration/rollback and compatibility with the foundation's supported clients, and document contract errors.
- [ ] 1.3 Add shared models/repository boundaries and localization codes incrementally with each feature; run serialization, missing-field, account-isolation and English/Spanish key-coverage tests without reintroducing platform-only business logic.

## 2. Center Public Profiles

- [ ] 2.1 Implement versioned center-owned profile/hours updates and ownership-aware import reconciliation; test cross-center denial, protected-field rejection, stale writes and import collisions, and document the public schema.
- [ ] 2.2 Add bounded cover-image ingestion and public derivatives; verify format/size rejection, metadata removal, rights/alt-text fields and no public raw originals or arbitrary remote fetching.
- [ ] 2.3 Add mobile-first portal profile/photo/hours editing; browser-test narrow layouts, validation, failed-upload preservation and conflicts without false publication.
- [ ] 2.4 Add shared timezone-based hours evaluation and Android photo/open-status rendering; test unknown/closed/holiday/overnight/offset changes, long content and absent photos.
- [ ] 2.5 Add equivalent iOS public-profile rendering using shared hours state; verify refresh/resume boundaries and identical public metadata without sample substitutions.

## 3. Global Needs Feed

- [ ] 3.1 Implement the public feed endpoint with mode/filter/origin/radius/cursor contracts; test publication visibility, priority/distance ordering, page stability and redacted location logs, and document query semantics.
- [ ] 3.2 Add shared feed state/cache/query cancellation; test filter changes with late responses, duplicate pages, missing origin, stale data and backend availability conflicts.
- [ ] 3.3 Implement Android All/Urgent/Nearby feed and need entry; UI-test manual-location fallback, category/search composition, center-scoped View all and preserved contribution input.
- [ ] 3.4 Implement equivalent iOS feed using shared state; verify parity for empty/offline/location-denied and concurrent-capacity scenarios.

## 4. Saved Centers

- [ ] 4.1 Implement private backend favorites with versioned desired-state writes; test ownership, idempotency and concurrent devices, and document conflict outcomes.
- [ ] 4.2 Implement separate shared guest/account stores and explicit import; test restart recovery, declined import, deduplication and account-switch queue isolation.
- [ ] 4.3 Add Android favorite controls and saved list; test consistent map/detail/list state, pending changes and removed centers.
- [ ] 4.4 Add equivalent iOS controls/list; test lost-response retries and cross-device reconciliation without reversing saved state.

## 5. Public Sharing

- [ ] 5.1 Configure the chosen public link domain and mobile-first center fallback pages; verify anonymous public reads, removed-center responses, allowlisted routes and documented associated-link ownership without deploying unapproved production infrastructure.
- [ ] 5.2 Add Android native sharing and incoming-link resolution; test installed/uninstalled destinations, cancelled sharing and malicious/unsupported link parameters.
- [ ] 5.3 Add equivalent iOS sharing/associated links; verify account-safe routing, no private payloads and honest behavior when the domain is unavailable.

## 6. Reviews and Moderation

- [ ] 6.1 Implement server-side completed-help eligibility, one review per user/center and versioned revisions; test ineligible direct requests, bounds, alias consent, owner editing and withdrawal.
- [ ] 6.2 Add moderator roles, visibility transitions, report deduplication and atomic aggregates; test own-center moderation denial, hidden-versus-withdrawn restoration and concurrent edit/report decisions, and document states.
- [ ] 6.3 Build the mobile-first moderator queue and publish content/reporting/appeal procedures with an assigned operator; browser-test pending approval, rejection reason, audit visibility and unavailable moderation handling.
- [ ] 6.4 Add Android rating/list/editor/report views; test pending edits, no-review states, inert text and exclusion of private donor metadata.
- [ ] 6.5 Add equivalent iOS review flows; verify public rating/count parity after approval, hide and withdrawal.

## 7. Volunteer Events

- [ ] 7.1 Implement documented event, enrollment and roster APIs with transactional seat allocation; test final-seat races, duplicate enrollment, start-time cutoff and cross-center roster denial.
- [ ] 7.2 Add portal draft/publication/cancellation and capacity controls; browser-test invalid intervals, enrolled-event rescheduling rejection and no automatic replacement enrollment.
- [ ] 7.3 Implement staff-validated attendance and one linked completed time contribution; test early/self-validation rejection, bounded duration, retries and deduplicated eligibility/impact.
- [ ] 7.4 Add shared event/enrollment state and cancellation/reminder reconciliation; test offline requests, account changes, cancelled targets and zero impact from enrollment alone.
- [ ] 7.5 Add Android event browsing/enrollment and owned activities; UI-test authentication intent, full/cancelled events and preference-gated event notifications.
- [ ] 7.6 Add equivalent iOS event routes and activities; verify matching IDs, seat state and attendance history across devices, and document cancellation rules in user-facing copy.

## 8. Gamification

- [ ] 8.1 Record the accepted version of configurable points/level/badge/streak rules and timezone policy; deliver localized rule descriptions and example fixtures distinguishing the proposed values from approved release configuration.
- [ ] 8.2 Implement authoritative eligibility and award/correction ledger; test duplicate sources, daily caps, personal/payment exclusion, late evidence and controlled invalidation without rewriting terminal contribution states.
- [ ] 8.3 Add shared points/level/achievement/chart/streak projections; test threshold equality, timezone changes, offset boundaries, missed days and invalidated progress with deterministic clocks.
- [ ] 8.4 Add Android Impact progress and View all achievements; UI-test locked/earned/corrected/offline states and an accessible chart summary without invented balances.
- [ ] 8.5 Add equivalent iOS Impact progress and profile level; verify both views share one source, match Android outcomes and expose no global personal leaderboard or redeemable-value claims. Dedicated organization/team and opt-in campaign-only individual standings belong to add-organization-participation-and-sustainability and do not multiply personal awards.
- [ ] 8.6 Record operator-attested external activity under a published rule; verify the engine applies that rule's amount, a typed balance is rejected, missing rule configuration keeps the action unavailable, and self-reported help without attestation still earns nothing.

## 9. Notification Inbox

- [ ] 9.1 Add owned durable inbox/read-state APIs and retention; test duplicate domain events, expired rows, read retries and separate push permission, and document event categories and retention.
- [ ] 9.2 Add shared local/remote notice reconciliation; test local-only proximity notices, guest/account separation, pending reads and private-state clearing at sign-out.
- [ ] 9.3 Implement Android bell/list/unread/target actions; UI-test denied push permission, offline badges and removed/cancelled targets.
- [ ] 9.4 Implement equivalent iOS inbox; verify cross-device read state, safe private targets and no implicit push/location opt-in.

## 10. External Donations

- [ ] 10.1 Select a provider meeting the hosted-checkout contract and record region/currency/recipient, fees, refund/dispute and operational prerequisites; verify sandbox access and keep production explicitly disabled until the separate release decision.
- [ ] 10.2 Add durable donation intents, controlled recipient mappings and idempotent session creation; test tampered amounts/currency/recipients, closed needs and repeated requests, and document financial API states.
- [ ] 10.3 Add authenticated provider-event ingestion and reconciliation; test signatures, replay, lost/late/out-of-order events, amount/recipient mismatch and durable recovery before acknowledging accepted events.
- [ ] 10.4 Add payment/adjustment ledger and currency-aware net impact; test partial/full refunds, disputed amounts, overlapping adjustments, eventual dispute resolution and no double-counting with time/goods history.
- [ ] 10.5 Implement Android review/hosted-checkout/return/history UI; sandbox-test failed, cancelled, pending and paid flows without collecting card data or trusting redirect success.
- [ ] 10.6 Implement equivalent iOS hosted flow; sandbox-test interrupted browser handoff, restored account ownership and matching totals/receipts.
- [ ] 10.7 Add owned support/receipt access, disable-new-intents control and redacted diagnostics; verify private receipt isolation, continuing reconciliation while disabled and no credentials in logs, URLs or app bundles.
- [ ] 10.8 Deliver the production-readiness checklist with accountable approval for provider/recipient eligibility, applicable compliance/store review, currency/fee presentation and refund/dispute support; record that sandbox tests are not permission to perform real charges.
- [ ] 10.9 Review and obtain acceptance of the institute-collects-then-distributes model with Mexican legal/accounting advisers and the selected provider; confirm BBVA ownership/settlement compatibility, beneficiary agreements and per-platform/territory fundraising eligibility. Record absent/pending SAT authorization without deductible-receipt claims; test all unresolved gates keep actions and links disabled.
- [ ] 10.10 Implement private allocation/distribution evidence and scoped audited operations separate from provider collection/settlement; test partial distributions, duplicate evidence, unauthorized recipient changes, unavailable centers, fee/net presentation and no center-received claim or help award from collection alone. Do not add automated bank transfers or a consumer wallet.
- [ ] 10.11 Publish reviewed delivery, unallocated-fund, campaign-failure and refund/dispute rules before collection; test donor-approved purpose handling and post-deletion ownership-verified support with minimum retained evidence, without restoring deleted accounts or relabeling ordinary receipts as deductible donations.

## 11. Functional Native Navigation

- [ ] 11.1 Implement shared destination/helping-intent state and account-summary metadata; test old-route migration, private-state clearing and no duplicate writes/subscriptions across tabs.
- [ ] 11.2 Add Android Map/Needs/Impact/Profile shell and helping chooser with map/list, settings/history/favorites/activity access; UI-test guest routes, contextual center/method help, sign-in intent, back navigation and long translated labels.
- [ ] 11.3 Add equivalent iOS shell and account summaries; verify safe-area/enlarged-text layout, actual avatar/join-date fallback and preserved map/feed state.
- [ ] 11.4 Document and test phased activation from the foundation's old routes; verify all new destinations have working or honest empty/error states before removing the old primary tabs.

## 12. Cross-System Acceptance

- [ ] 12.1 Run portal publication -> mobile feed -> contribution/event completion -> review eligibility -> points/inbox scenarios with multiple accounts and centers; verify shared IDs, authority, no duplicate impact and the personal-help exclusion.
- [ ] 12.2 Capture reference comparisons on both native platforms and mobile portal views in Spanish/English, small/large screens and enlarged text; verify accessibility plus public-link fallback, while distinguishing implemented functions from fixtures.
- [ ] 12.3 Run restart/offline/concurrency/security suites and sandbox payment reconciliation end to end; reconcile stage-specific spec boundaries, run strict OpenSpec validation and record evidence before any archive decision.
