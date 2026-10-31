# Tasks

All implementation work is pending. Foundation authentication, backend storage,
native parity and the canonical portal are prerequisites, not completed work.
Use synthetic cases until the applicable policy and operational gates pass.

## 1. Service Participation And Public Discovery

- [ ] 1.1 Add versioned center intake-service storage and authorized participation endpoints, separating receiving, assessment, distribution and coordination roles; verify migration compatibility, cross-center denial and that existing catalog centers are not automatically opted in, and document the public/private API boundary.
- [ ] 1.2 Publish service hours/timezone, channels, allowed/excluded items, freshness and availability with server-side eligibility rechecks; verify closed, appointment-only, stale, paused and changed-version scenarios without deleting existing obligations, and document refresh/continuity policy.
- [ ] 1.3 Extend shared KMP discovery models, repositories and filters to match eligible services before distance using an explicit device/manual origin; verify map/list parity, unknown capacity, no matches and straight-line labeling with shared tests, and document compatibility with existing category discovery.

## 2. Private Heads-Up And Assessment Backend

- [ ] 2.1 Implement minimal authenticated heads-up submission and versioned recipient/purpose consent with independent contact, helper-progress and donor-disclosure permissions; verify decline creates no referral/refusal record and unsupported clinical fields/uploads are rejected, and document the consent payload and approved language gate.
- [ ] 2.2 Implement recipient-center intake permissions, minimal unassigned queues, auditable case claiming and assigned-case projections; verify guessed IDs, another center/helper/donor, revoked assignments and associated-resource/export access are denied, and document the role/projection matrix.
- [ ] 2.3 Generate non-enumerable readable references with authorized rate-limited lookup and a private lost-reference/representative support procedure; verify a copied reference grants no public status or handover authority and telephone contact works without a beneficiary account, personal phone or mandatory ID upload, and document staff handling.
- [ ] 2.4 Implement heads-up, acknowledgement, actual contact, assessment acceptance and neutral terminal outcomes with server time, expected versions and operation identities; verify contact/consent/service guards, no-show expiry, duplicate/lost responses and out-of-order transitions, and document the state diagram and correction/appeal route.
- [ ] 2.5 Connect durable referral events to the existing inbox/outbox and opt-in notification contracts; verify notice retries do not duplicate cases and no beneficiary/product content appears in previews or logs, and document why delivery acknowledgement differs from center acknowledgement.
- [ ] 2.6 Implement no-account correction/withdrawal support, scoped access revocation and configured retention/deletion with separate minimized financial records; verify new disclosure/funding stops, alternate-center referral requires fresh permission and existing obligations remain reconcilable, and document retention, downstream deletion and backup limitations.

## 3. Approved Requests And Goods Fulfillment

- [ ] 3.1 Add private assistance requests with a typed contribution target, accepted-case revision, permitted donor audience, expiry, approved lines and methods; verify pre-contact/pre-acceptance requests fail and guessed IDs never resolve as public needs or personal-help records, and document the request/projection contract.
- [ ] 3.2 Implement atomic goods allocations, versioned commitments and explicit center/partner receiving instructions; verify duplicate retries, concurrent last-unit requests, changed services and cancellation with goods in transit, and document reservation/release rules and the non-integrated external purchase/shipment boundary.
- [ ] 3.3 Implement attributable partial receipt, rejection and beneficiary handover events with separately authorized staff actions; verify carrier/donor reports cannot complete a request, partial quantities remain accounted for and corrections preserve history, and document the private no-app handover procedure without mandatory photos or clinical evidence.
- [ ] 3.4 Implement uncollected, changed-need, unsuitable-goods and withdrawal reconciliation with private donor/beneficiary support; verify capacity is released once only after obligations permit and returns/repurposing need the agreed policy and consent, and document ownership/storage/return responsibilities.
- [ ] 3.5 Connect case-specific completed handovers to owned history and the existing contribution/recognition ledger; verify independent validation, canonical-ID deduplication and correction reversals without awarding referrals/payments or exposing case data in public impact, and document the distinction from generic center-stock donations.

## 4. Case-Linked Hosted Funding

- [ ] 4.1 Extend the existing hosted-donation contract with approved case procurement quotes, bounded amounts/units, currency/expiry and the same transactional capacity ledger used by goods; verify mixed goods/money last-unit races and obsolete quotes cannot charge or double-allocate, and document donor reapproval and provider/recipient release gates.
- [ ] 4.2 Connect verified provider events using opaque financial correlation and separate paid, allocated, procured, received and handed-over facts; verify redirects, uploaded receipts and staff commands cannot mark paid, metadata excludes health/case details and no personal payout/escrow is introduced, and document sandbox evidence versus production approval.
- [ ] 4.3 Implement late-payment, expired-allocation, uncertain-checkout and refund reconciliation; verify verified late charges remain in the ledger without duplicate procurement, silent redirection or premature refund claims, and document support ownership and the reviewed center-purpose policy.

## 5. Medicine Service Safeguards

- [ ] 5.1 Add reviewed country/center/partner role configuration, authority validity and independently gated service activation; verify an ordinary health category, center opt-in or general verification cannot activate medicine intake and expiry suspends new work while preserving obligations, and document required review evidence and gate owners.
- [ ] 5.2 Implement qualified-review attestations and versioned approved product/quantity or bounded procurement requests without generic prescription/clinical uploads; verify purchasing/substitution cannot bypass review and medicine-name disclosure requires separate permission, and document the qualified-service record boundary and generic-purpose funding alternative.
- [ ] 5.3 Enforce approved source, packaging, shelf-life, transport/storage and professional receipt/handover policy; verify opened, patient-returned, expired, untraceable, mismatched and unsupported special-handling items cannot fulfill requests, and document quarantine/rejection/recall and authorized continuity procedures.
- [ ] 5.4 Add reviewed localized urgent-care and safeguarding routes with supported consent/representation gates; verify missing policies block sensitive collection, no automated diagnosis/dose advice is generated and sponsorship is not presented as emergency care, and document country/service review requirements.

## 6. Shared Client Contracts And Privacy

- [ ] 6.1 Add KMP referral, private request, contribution-target and safe history repositories/projections backed by the foundation authenticated API; verify contract fixtures for allowed/denied transitions and server-authoritative errors without sample-user authorization, and document Android/iOS bridge mappings.
- [ ] 6.2 Add memory-bound private client state, opaque operation reconciliation and explicit permission/session invalidation; verify offline private writes fail honestly, sign-out/assignment loss clears sensitive state and public offline directions remain available, and document excluded durable caches, backups, URLs and analytics fields.
- [ ] 6.3 Add stable status/category/error codes and complete Spanish/English consent, availability and fulfillment strings under the existing device/manual-language contract; verify key coverage, fallback and date/timezone rendering in shared/platform tests, and document how approved additional-language consent is selected or blocked.

## 7. Android Compose

- [ ] 7.1 Add service/channel filters, suitability details and public directions to Android map/list discovery through the existing neutral renderer; verify permission-denied/manual-origin, missing Google key and no-match UI tests plus the SDK boundary guard, and update the Android discovery acceptance checklist.
- [ ] 7.2 Add authenticated minimal heads-up, consent, readable reference and permitted progress screens; verify unsent/received/acknowledged/contact states, offline/session loss and refusal-of-progress-sharing do not leak approval through actions/errors, and document accessible Spanish/English phone checks.
- [ ] 7.3 Add approved goods/funding review, explicit commitment and separate receipt/handover history; verify tampered/stale requests, money-disabled and partial/cancelled outcomes with Compose tests, and document external receiving/payment handoffs without private map payloads.

## 8. iOS SwiftUI

- [ ] 8.1 After the foundation shared-state bridge is operational, add equivalent service/channel discovery and public directions using MapKit only for public centers; verify shared filter parity, denied location/manual origin and map fallback in SwiftUI tests, and update the iOS discovery acceptance checklist.
- [ ] 8.2 Add shared-backed consent, heads-up, readable reference and permitted progress without a second Swift-only case model; verify phone/no-account beneficiary, offline/session expiry, VoiceOver and long Spanish/English states in UI tests, and document the same disclosure rules as Android.
- [ ] 8.3 Add approved donor commitment and separate financial/receipt/handover history with native external handoffs; verify stale approval, unavailable payment, partial fulfillment and retry reconciliation in UI tests, and document parity and private-state clearing on sign-out.

## 9. Mobile-First Web And Center Workspace

- [ ] 9.1 Extend the canonical portal with role-scoped service participation/settings and public service directions/print summaries; verify service changes are backend-authorized and printable/public URLs contain no case reference or beneficiary data, and document staff setup without implying the older static landing is operational.
- [ ] 9.2 Add browser helper/donor heads-up, consent, private reference, approved commitment and owned progress routes using the same backend; verify no-install participation, copied-reference privacy, disabled funding before acceptance and failed writes without fake success, and document separation of public share links from private authenticated views.
- [ ] 9.3 Add minimal staff intake queue, case claiming, actual contact, consent reconfirmation, assessment and no-account support; verify role/assignment loss, cross-center denial, unsupported representation and neutral no-show outcomes through API and browser tests, and document the staff contact/assessment procedure.
- [ ] 9.4 Add staff goods/procurement receipt, partial handover, discrepancy and reconciliation views; verify recorded evidence and independent validation match the backend without clinical uploads or manual paid flags, and document the receiving/handover/support workflow.
- [ ] 9.5 Apply and verify 320/360/390/768/1280-pixel layouts, 200-percent text, keyboard/screen-reader use and Spanish/English content for the new routes; include Chromium/WebKit screenshots and real iOS Safari/Android Chrome keyboard checks, test private cache/service-worker exclusion and sign-out clearing, and update the portal acceptance checklist.

## 10. Cross-System Acceptance And Controlled Release

- [ ] 10.1 Run synthetic end-to-end phone and in-person cases from helper heads-up through center validation, goods commitment, partial receipt and final handover across Android, iOS and web; verify the beneficiary needs no app/account/phone and only permitted status is disclosed, and record linked test evidence for all four capabilities.
- [ ] 10.2 Run integrated consent-withdrawal, revoked-assignment, guessed-reference, duplicate-event, no-contact, service-pause and in-flight cancellation drills; verify no private map/notification/cache leakage, false fulfillment or premature capacity release and record outcomes in the acceptance report.
- [ ] 10.3 Exercise gated medicine and mixed goods/payment paths with approved synthetic policy fixtures and provider sandbox events, including late payment, unsafe receipt and recall; verify disabled production gates, independent handover recognition and rollback that preserves outstanding obligations, and record readiness separately from any later live-service approval.
