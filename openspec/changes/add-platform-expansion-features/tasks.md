# Tasks

All tasks are pending. This change depends on the foundation, reference-product
and community changes; those services are not verified. Do not select a payment
seller, fiscal country/provider, geography source, privacy threshold, credential
template, retention period or moderation staffing value in these tasks. A missing
selection keeps the affected action disabled. Use synthetic records. Do not mark
a task complete from models, mocks or a successful build alone.

## 1. Prerequisites and Closed Gates

- [ ] 1.1 Verify foundation identity, catalog revisions, contribution ledger and portal authorization with their existing tests; record each missing prerequisite as a blocker instead of substituting a local demo service, and document the dependency matrix.
- [ ] 1.2 Add a server-side capability-gate record for verification claims, connectors, communications, credentials, rankings, fiscal issuance and paid admission; verify each gate is closed by default, that closing it preserves history/refunds/rights routes, and document that no task below may open a gate by choosing a provider.

## 2. Center Onboarding

- [ ] 2.1 Add private organization application and existing-center claim storage with contact verification, representation evidence and consent; verify submission grants no publish, payout or verification right and duplicate flags hide other applicants' evidence, and document the application states.
- [ ] 2.2 Add independent membership review with explicit center, account and role grants plus recipient acceptance; verify an applicant cannot approve their own request and membership stays distinct from verification, tax eligibility and provider onboarding.
- [ ] 2.3 Add expiring account-bound staff invitations and audited grant, revoke, transfer and recovery; verify email or domain possession cannot seize a center, revocation stops later protected writes and clears stale staff views, and document the role matrix.
- [ ] 2.4 Add duplicate and dispute resolution that preserves public center IDs, contributions and memberships; verify merges keep redirects and provenance, appeals stay private, and a report alone does not publish an allegation.
- [ ] 2.5 Add the mobile-first applicant tracker in Spanish and English; verify pending, changes-requested, approved, rejected and withdrawn states, private bounded evidence, and that withdrawal keeps the audit records required by the still-unselected retention policy.

## 3. Organization Verification

- [ ] 3.1 Add scoped verification claims with issuer, criteria version, scope, evidence reference, decision date and validity; verify visits, points, ratings, photos, memberships and payments cannot create a claim, and that a Generosity review is not labeled as government certification.
- [ ] 3.2 Add assigned evidence review with file type and size limits, private access and access audits; verify reviewers cannot decide their own organization and upload alone does not establish authenticity.
- [ ] 3.3 Add pending, active, rejected, expired, suspended and revoked transitions with dated reasons and appeals; verify expiry or revocation removes the public claim without erasing contribution history and an appeal does not restore status before approval.
- [ ] 3.4 Show only approved public claim metadata on native and web center details in Spanish and English; verify private documents and reviewer identities stay hidden and distinct claim types do not collapse into one badge implying safety, quality or tax deductibility.

## 4. Platform Operations

- [ ] 4.1 Add least-privilege operator sessions and explicit permissions for role provisioning, moderation, support, verification, fiscal/provider operations and privacy requests; verify no general administrator role can read every case or any payment credential, and that sensitive grants require reauthentication plus the configured independent approval.
- [ ] 4.2 Record actor, target, expected version, reason and outcome for administrative mutations; verify retries are idempotent, operators cannot rewrite point balances, paid states or terminal contribution evidence, and conflicts keep the newer work.
- [ ] 4.3 Add scoped fraud and abuse cases with evidence provenance, status and appeal; verify a signal alone is not a fraud finding, restrictions stay proportional, and public content omits allegations, reporter identity and investigative records.
- [ ] 4.4 Add versioned release controls, queue-failure handling and audited redacted exports; verify disabling new payments, publication or collection preserves reconciliation, refunds, cancellations, rights requests and history, and that secrets stay server-side.
- [ ] 4.5 Add mobile-first operations queues from 320 CSS pixels in Spanish and English with keyboard and screen-reader access; verify each queue has an assigned handling policy and a missing staffing selection keeps its release gate closed.
- [ ] 4.6 Keep the operations workspace off `/kindness-center` and the helper flow; verify a center assignment and an operator permission do not grant each other.
- [ ] 4.7 Add defined usage aggregates with period, definition, and coverage; verify an unmeasured figure stays unavailable, no beneficiary or payment detail appears, and center staff cannot read platform-wide statistics.
- [ ] 4.8 Add test-origin award void with reason and recompute; verify it rejects a target balance, leaves real validated evidence in place, and stays unavailable when test-origin marking is absent.

## 5. Authorized Data Connectors

- [ ] 5.1 Add connector configuration for a reviewed interface or supplied dataset, license scope, allowed fields, owner and secret storage; verify a named service without that interface stays disabled and the system does not invent a successful sync, and document the activation checklist.
- [ ] 5.2 Stage bounded imports with schema, coordinate and timezone validation before replacing current records; verify retries are idempotent, a malformed run does not wipe a usable catalog, and imported content cannot grant authorization.
- [ ] 5.3 Preserve field provenance and local ownership across connector updates; verify staff edits, commitments, verified status and private records are not overwritten, deletions require reconciliation, and imported schedules stay drafts that do not auto-enroll anyone.
- [ ] 5.4 Restrict each connector to its approved fields and enforce timeouts, rate limits and bounded parsing; verify household identifiers, contacts, dietary details, payment credentials and private rosters never enter the general catalog feed.
- [ ] 5.5 Add operator run status, pause, credential rotation and revocation; verify revocation stops new access without erasing unrelated local history and stale data is labeled in native and web views.

## 6. Event Series and Waitlists

- [ ] 6.1 Add timezone-aware recurrence with an explicit end or count, a generation limit and a date preview that shows daylight-saving handling; verify each occurrence keeps a stable ID and independent capacity, repeated generation does not duplicate occurrences, and organizer eligibility is rechecked before new publication.
- [ ] 6.2 Add single-occurrence and future-uncommitted series edits; verify completed history stays unchanged, committed time or venue changes require explicit participant acceptance, and cancelling a series reconciles each enrollment, reminder and paid-ticket obligation.
- [ ] 6.3 Add explicit waitlist join and leave; verify one participant has at most one active enrollment, offer or waitlist entry per occurrence, ordering uses server acceptance time, and payment, points or invitations cannot buy priority.
- [ ] 6.4 Add atomic expiring seat offers to the next eligible waitlisted participant; verify acceptance rechecks eligibility, a declined or expired offer releases once, and late payment follows ticket reconciliation instead of overbooking.
- [ ] 6.5 Show owned waitlist and offer status, deadline and accept or decline actions on web, Android and iOS in Spanish and English; verify other participants stay hidden, notifications obey channel consent, and assisted participants have a coordinator or paper-contact route.

## 7. Participant Communications

- [ ] 7.1 Add contextual conversations for an authorized need, contribution, event or meal; verify users cannot address arbitrary accounts by guessing an identifier, access is rechecked on every read and write, and announcements do not disclose the recipient list.
- [ ] 7.2 Send bounded inert text with safe links and idempotent delivery states; verify pending, sent and failed reflect server acknowledgment, retries do not duplicate messages, and private household fields, payment credentials and meal instructions are not embedded automatically.
- [ ] 7.3 Apply channel preferences, permissions, rate limits and quiet time to inbox, email and push delivery; verify lock-screen previews omit private content by default, marketing requires a separate opt-in, and read receipts are not public evidence of participation.
- [ ] 7.4 Add block and private report with scoped moderator review; verify a report does not automatically prove misconduct, a blocked user cannot bypass the block with another thread in the same context, and support and rights routes remain available.
- [ ] 7.5 Disclose retention and restricted access on web, Android and iOS; verify the client does not claim end-to-end encryption while moderators can inspect reported content, departed participants lose new access, and an account switch clears private conversation state.

## 8. Native Center Workspace

- [ ] 8.1 Add a separate Android staff workspace with an explicit active-center selector fed only by current server-assigned roles; verify consumer discovery stays read-only, cached or organizer roles cannot authorize staff writes, and switching center or account isolates drafts.
- [ ] 8.2 Add the equivalent SwiftUI staff workspace and selector; verify the same authorization failures and that native code does not keep an independent role cache as the source of truth.
- [ ] 8.3 Connect permitted need, profile, hours, media, occurrence and contribution confirmation actions to the same portal validation and audit records; verify staff cannot set verification, point balances or provider-paid status and cannot validate their own help.
- [ ] 8.4 Distinguish local drafts and pending requests from accepted publication on both platforms; verify protected writes reauthorize online, stale cached permissions fail offline, and revocation stops queued mutations and clears private staff views.
- [ ] 8.5 Label complex onboarding, verification, fiscal and operations reviews as web-only where they are not native, in Spanish and English with enlarged text; verify those links do not carry credentials or grant another role.

## 9. Recognition Credentials

- [ ] 9.1 Issue certificates and badges only from center-validated completed help and versioned criteria; verify self-reported work, sharing, purchases and attendance reservations do not qualify, and issuance does not spend points or grant cash, credit, tax status or professional certification.
- [ ] 9.2 Issue at most one active credential per owner, criterion, evidence scope and policy version; verify replacement links stay controlled, assisted claims keep canonical evidence, and the owner previews the display name before download.
- [ ] 9.3 Produce a Spanish/English PDF plus an equivalent accessible HTML view and badge; verify issuer, criterion, period, units, issue date and verification reference are present, private beneficiary data is absent, and long names are not truncated below readability.
- [ ] 9.4 Keep credentials private until the owner selects public fields for a shareable verification page or QR; verify identifiers are non-enumerable, withdrawn sharing removes public personal details, and verification does not leak email, contact or case records.
- [ ] 9.5 Update status when evidence is invalidated or issuance was erroneous; verify verification shows the current state, a client-edited PDF cannot change the server claim, and dependent achievements are reconciled without duplicate points.

## 10. Regional Help Rankings

- [ ] 10.1 Attribute rankings only to authoritative activity or center geography with namespaced, versioned country, neighborhood and postal identifiers; verify inferred home address, device tracking and a self-entered postal code are not accepted as proof, and missing geography stays unattributed.
- [ ] 10.2 Publish comparisons with geography level, period, evidence, metric, unit, coverage and update time; verify participants, activity counts, hours and compatible goods units stay distinct, and money, purchases, certificates and invitation volume create no ranking credit.
- [ ] 10.3 Add a separately labeled per-population view that requires a licensed denominator matching the geography version; verify a missing denominator yields an unavailable rate rather than zero or an estimate, and document that the denominator source is still unselected.
- [ ] 10.4 Enforce participation permission, distinct-person suppression and sensitive-area suppression before publication; verify a missing privacy threshold disables publication, a suppressed area is not ranked last as zero, and no participant list or household observation is exposed.
- [ ] 10.5 Recompute projections after corrections, consent changes and boundary revisions; verify duplicate evidence across event, contribution and assisted records counts once, method changes keep a version label, and suspicious activity enters review instead of a public accusation.
- [ ] 10.6 Add country, neighborhood or postal, period and metric controls on web and native Impact in Spanish and English; verify unknown, stale and suppressed states stay distinct and a place's position cannot close a center, deny household aid or change organizer permissions.

## 11. Donation Tax Documents

- [ ] 11.1 Add the jurisdiction and issuer gate so a request without an approved country policy, eligible issuer, provider integration and rule version is unavailable; verify interface language, nationality and a Generosity badge do not create eligibility and the client fabricates no fiscal PDF, identifier or deductible status.
- [ ] 11.2 Collect only fields required by a selected reviewed policy, with purpose, recipients and retention disclosed; verify tax identifiers stay out of public profiles, analytics and notification previews, and issuer secrets never reach the client. Leave this disabled until a policy is recorded.
- [ ] 11.3 Bind issuance to an authoritative donation, eligible issuer and policy-required evidence; verify offers, unverified payment returns, event tickets and volunteer hours are not treated as deductible donations and the app does not invent goods value.
- [ ] 11.4 Track requested, processing, issued, failed, cancelled and replaced states with issuer correlation; verify retries do not issue duplicates and a financial refund is not recorded as the same event as fiscal cancellation.
- [ ] 11.5 Restrict retrieval to authorized donors and issuer staff, including an accessible representation where the policy requires one; verify knowing an ID does not expose the document and a recognition certificate is not presented as fiscal evidence.
- [ ] 11.6 Require dated fiscal review, sandbox evidence and separate production approval before enabling a jurisdiction; verify test documents are labeled non-fiscal, isolated from live ledgers, and disabling new issuance still allows private retrieval of existing records.

## 12. Paid Event Ticketing

- [ ] 12.1 Keep paid admission disabled until approved seller onboarding, region, currency, displayed price and fees, cancellation policy and support ownership exist; verify donation-provider readiness does not enable ticket sales and the client never collects card or bank credentials.
- [ ] 12.2 Add atomic seat holds and idempotent provider-hosted checkout sessions once that gate can be configured; verify holds, free enrollments, waitlist offers and paid tickets share inventory, retries do not duplicate charges, and a provider return URL does not itself mark a ticket paid.
- [ ] 12.3 Issue a ticket only after reconciled provider success matches intent, seller, amount and currency and capacity is still valid; verify late success after cancellation enters refund reconciliation, and a failed refund stays visible as unresolved.
- [ ] 12.4 Add non-enumerable ticket references and an authorized check-in route on web and native clients; verify check-in is idempotent, a copied QR does not create another admission, and offline state is not presented as validation.
- [ ] 12.5 Stop sales and start the applicable provider refund process when an event is cancelled; verify a refund request is not shown as a completed refund and disabling new sales retains refund handling and owned history.
- [ ] 12.6 Keep tickets separate from donations, fiscal evidence, volunteer credit and rankings; verify paying or attending does not award helping points, organizer eligibility, recognition credentials or ranking credit.

## 13. Cross-System Acceptance

- [ ] 13.1 Run synthetic onboarding, verification, connector, communication, credential and ranking scenarios across the backend, portal, Android and iOS; verify closed gates stay closed, private fields stay out of public payloads, and no task records a provider, threshold or jurisdiction that was not supplied.
- [ ] 13.2 Run fiscal and ticketing scenarios only in sandbox with unmistakably non-production documents and no live charge; verify production emission and charging remain disabled, then rerun strict OpenSpec validation. Do not archive this change while a prerequisite change or a required release selection is still open.

## Workflow follow-up

- Archive only after foundation, reference-product and community prerequisites are implemented and this change's tasks have verification evidence.
- Sync these delta specs into main specs as part of that archive, not before the behavior exists.
- Record any later provider, jurisdiction, threshold or retention choice in the change's design before enabling the matching gate.
