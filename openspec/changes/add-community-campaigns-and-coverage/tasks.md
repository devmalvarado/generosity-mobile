# Tasks

All tasks are pending. Use synthetic participants/households until the relevant
release gates are approved. Planning readiness is not permission to collect
real data or a claim that prerequisite backend features already work.

## 1. Prerequisites and Contracts

- [ ] 1.1 Verify foundation identity/sync/center authority and reference events/awards/inbox prerequisites with their integration tests; record missing dependencies before enabling this change.
- [ ] 1.2 Define versioned eligibility, fieldwork, retention, offline lease, meal-access and disclosure configuration with no permissive defaults; verify missing configuration disables the corresponding writes and document approval ownership.
- [ ] 1.3 Add additive campaign ownership and scoped-role migrations/contracts; verify existing center events, enrollments and contribution IDs retain their original meaning through upgrade and rollback fixtures.

## 2. Organizer Eligibility

- [ ] 2.1 Implement authoritative eligibility from published level/points/distinct validated-help criteria; verify threshold boundaries, duplicated evidence, missing policy, personal help and payments in backend tests.
- [ ] 2.2 Implement reassessment, protected-write checks, activity holds and accepted handover; verify stale offline publications, points corrections, policy changes, suspension and continued cancellation access.
- [ ] 2.3 Implement scoped safeguarding report/appeal endpoints and audit events; verify cross-account denial, non-automatic sanctions and absence of unrelated case data in responses/logs.
- [ ] 2.4 Add mobile-first organizer progress and safeguarding/handover views; verify ES/EN phone flows, eligible-assignee acceptance and document the role/appeal procedure.
- [ ] 2.5 Add shared KMP eligibility models, repository contract and state; verify unknown/stale/error states and that client point fields cannot authorize writes.
- [ ] 2.6 Add Android Profile organizer/progress entry; verify below-threshold, eligible, held and signed-out flows with Compose UI tests.
- [ ] 2.7 Add the equivalent SwiftUI entry backed by shared state; verify the same scenarios, account switching and enlarged ES/EN text in iOS UI tests.

## 3. Community Campaigns

- [ ] 3.1 Implement campaign drafts, type-specific plans, partner acceptance and scoped publication; verify missing planting maintenance, unsafe/incomplete plans, pending partnership and unapproved assessment protocol are rejected.
- [ ] 3.2 Extend the existing event engine for community ownership and material commitments; verify last-seat/quantity races, idempotent retries, replacement scheduling and owner/center isolation.
- [ ] 3.3 Implement pause/cancel/resume/completion and inbox/reminder reconciliation; verify existing commitments remain traceable, resumed changes require acceptance and notifications contain no private recipient data.
- [ ] 3.4 Add attributed outputs, later observations and independent partner validation; verify planted-versus-surviving trees, collected-versus-disposed waste, dual-role self-validation rejection and one eligibility source per activity.
- [ ] 3.5 Add shared campaign state and Android discovery/detail/participation/history; verify units, offline pending writes, guest entry and no organizer-threshold requirement for volunteering.
- [ ] 3.6 Add equivalent SwiftUI campaign flows using shared state; verify restored navigation, cancellations, account isolation and ES/EN accessibility in iOS UI tests.
- [ ] 3.7 Build mobile-first campaign/roster/outcome management in canonical portal; verify small-screen form errors and all type-specific plans, and document reported versus validated outcomes and safety responsibilities.

## 4. Coordinated Meals

- [ ] 4.1 Implement recipient/representative approval and private plan storage/access; verify absent approval, per-recipient disclosure permissions, pause/withdrawal and the no-app private support channel; document how consent is evidenced.
- [ ] 4.2 Implement dated portion slots and idempotent reservation/change/cancel commands; verify concurrent final portions, timezone boundaries, late cancellation, dietary re-acknowledgment and replacement acceptance.
- [ ] 4.3 Implement assignment-limited delivery instructions, recipient/coordinator donor approval, receipt provenance and retention jobs; verify unapproved reservations disclose nothing, unauthorized link access, expiry, repeated receipt and no automatic points/review eligibility.
- [ ] 4.4 Add KMP meal state and Android slot/private delivery/history screens; verify portions, offline pending state, declined restrictions and notification redaction in UI/contract tests.
- [ ] 4.5 Add equivalent SwiftUI meal flows; verify shared-state parity, account cleanup, instructions acknowledgment and accessible local dates in ES/EN.
- [ ] 4.6 Add mobile-first plan/slot coordination and recipient-rights operations; verify phone cancellation/replacement workflows and document reservation, delivery claim and confirmed receipt distinctions.

## 5. Household Assessment and Follow-Up

- [ ] 5.1 Add versioned approved questionnaires/protocols and collector assignments; verify organizer points cannot substitute for training/assignment and unsupported language or unapproved fields block collection.
- [ ] 5.2 Implement separate consent receipts and non-account correction/withdrawal channels; verify anonymous aggregate-only participation, refusal without an address record, optional location/follow-up and lost-receipt limits.
- [ ] 5.3 Implement protected case identifiers, assigned-worker access and per-center referral consent; verify cross-campaign/center denial, minimal disclosure, provenance on resolution and revoked referral access.
- [ ] 5.4 Add KMP questionnaire validation and pending-response state; verify unknown versus zero, schema revisions, retries, explicit revisit replacement and no survey-volume reward events.
- [ ] 5.5 Implement Android protected draft storage and bounded leases; verify encrypted restart, expiry, clock rollback, reconnect revocation, account isolation and backup/log exclusions.
- [ ] 5.6 Implement the equivalent iOS protected storage/lease adapter; verify the same security scenarios and record device-level cache/backup inspection evidence.
- [ ] 5.7 Add Android assigned field forms and readable consent; verify manual area selection, denied GPS, partial interview exit, no-contact participation and offline pending upload.
- [ ] 5.8 Add equivalent SwiftUI field forms; verify ES/EN consent/unknown values, shared validation, keyboard accessibility and interrupted-session recovery within the lease.
- [ ] 5.9 Add online-only mobile-first case, referral and protocol/assignment queues; verify role separation, responsive layouts and absence of sensitive content in browser caches/telemetry.
- [ ] 5.10 Implement retention/deletion, access audit, downstream revocation and restore protections; verify withdrawal invalidates derivatives and expired backups cannot resurrect data; document the rights/incident procedure and offline revocation limit.

## 6. Privacy-Safe Aggregate Releases

- [ ] 6.1 Implement server aggregation of consented current responses with quality/version provenance; verify linked revisits count once, unknown quantities stay unknown and unprovable distinct-household cohorts are suppressed/coarsened.
- [ ] 6.2 Implement approved release combinations, complementary suppression and output filtering; verify small cells, narrow/overlapping filters, totals, temporal differencing and export reconstruction fixtures.
- [ ] 6.3 Implement release approval, source withdrawal invalidation and refresh; verify no stale unauthorized map/download remains accessible and document disclosure-review and irreversible-release limits.
- [ ] 6.4 Add shared release/legend models and API contract fixtures; verify public/analyst payloads exclude raw coordinates, identifiers, private narratives and suppressed values, including errors and debug paths.

## 7. Territorial Coverage and Scenarios

- [ ] 7.1 Add scoped center service-capacity records and mobile-first editing; verify compatible units/periods, available versus gross capacity, shared pools, source ownership and unknown capacity; document the service dictionary.
- [ ] 7.2 Implement a bounded routing adapter over aggregate origins/public centers; verify inaccessible routes, mode/time assumptions, licensing configuration, proximity-only fallback and absence of private coordinates in requests.
- [ ] 7.3 Select/pin an established compatible solver and implement bounded allocation; verify hand-solved fixtures for two areas sharing ten units, incompatible units, pooled constraints and integer services; document method and timeout status.
- [ ] 7.4 Implement reproducible baseline/candidate runs, relocation, sensitivity and optional costs; verify no source-capacity duplication, gained/lost coverage, unknown cost handling, stale-input invalidation and derived-output suppression.
- [ ] 7.5 Add scoped scenario review/decision records; verify accept/defer/reject requires rationale and cannot update live center locations, grant household services or award points; document human-review limitations.
- [ ] 7.6 Build mobile-first analyst map/list comparison views and approved result publication; verify legends, assumptions, unmeasured areas and phone-readable gains/losses without color-only meaning or overflow.
- [ ] 7.7 Add shared published-coverage state and Android Map/list layer; verify center discovery is unchanged and unknown/suppressed/stale values, ES/EN units and accessibility render correctly.
- [ ] 7.8 Add equivalent SwiftUI published-coverage views; verify shared-source parity, map/list consistency and no household pin/network disclosure in iOS tests.

## 8. Cross-System Acceptance

- [ ] 8.1 Exercise earned eligibility -> community campaign -> independent center validation -> one award across backend, portal, Android and iOS; also verify a non-partner campaign stays unvalidated and the plus action remains unchanged.
- [ ] 8.2 Exercise consented field capture -> optional referral -> approved aggregate -> candidate comparison -> withdrawal using synthetic households; verify rights enforcement, no duplicate demand and no private-data leakage end to end.
- [ ] 8.3 Run Chromium/WebKit screenshots and overflow checks at 320/360/390/768/1280, real Safari/Chrome keyboard checks and native ES/EN enlarged-text flows; record evidence, run strict OpenSpec validation and retain unchecked tasks where gates or verification remain incomplete.

## 9. Verified organizer contact

- [ ] 9.1 Enforce foundation verified-phone proof for group/event creation, alongside earned eligibility; test direct API bypass, proof revocation, missing eligibility and preservation of unsent drafts on web/Android/iOS.
- [ ] 9.2 Verify that phone requirements do not expand to ordinary donors, participants receiving help or beneficiaries; document the selected SMS provider and approved sending/cost limits before activation.
