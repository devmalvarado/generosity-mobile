# Tasks

All tasks are pending. Foundation auth/backend/catalog/portal and shared iOS
integration remain prerequisites. Use synthetic records, not the user's named
center or representative, in fixtures and acceptance screenshots.

## 1. Shared Directory Contracts

- [ ] 1.1 Add typed directory sections and explicit known/not-informed/not-applicable/withheld states while preserving center IDs and category codes; verify serialization, omitted legacy fields, invalid supplied values and exact proper/legal-name preservation with shared tests, and document the versioned public contract.
- [ ] 1.2 Add shared fee, beneficiary-measure and admission projections with service references, valid currencies/units/dates and configurable freshness; verify unknown versus zero/free, invalid ranges, undated counts, overlapping populations and stale availability in deterministic tests, and document each measure's meaning.
- [ ] 1.3 Add optional public-location discovery and complete English/Spanish labels for all sections/states; verify category OR filters, contact-only name searches, no hidden-coordinate distance/radius inference and localization coverage, and document map/list parity exceptions without introducing SDK types.

## 2. Backend Publication And Authority

- [ ] 2.1 Extend the existing profile endpoints with incomplete drafts, minimum publication validation, expected versions and idempotent saves; verify missing name/category/public contact blocks publication while optional unknowns do not, test cross-center and revoked-staff denial, and document draft/published/needs-review transitions.
- [ ] 2.2 Separate restricted location/contact storage from allowlisted public projections and search/cache outputs; verify anonymous API/export/preview/media/marker payloads exclude private fields and derived location, and document publication designation and cache invalidation responsibilities.
- [ ] 2.3 Persist dated fee, aggregate-beneficiary and admission declarations with source ownership and disclosure review; verify incompatible measures cannot be silently combined, expired admission is unconfirmed and intake-service state is not duplicated, and document review/freshness policy configuration.
- [ ] 2.4 Add bounded approved attributes and declared resource-source types, and connect read-only verification and permitted contribution actions; verify extra fields cannot override reserved fields, legacy verified assertions grant no claim and fees cannot open donation checkout, and document authority boundaries and plain-text review limits.
- [ ] 2.5 Implement withdrawal of public contact/location, last-contact publication review and claim-policy rechecks for legal-identity changes; verify stale writes cannot restore withdrawn data, controlled projections refresh and linked history survives, and document private support and unavoidable external-copy limits.

## 3. Catalog Migration

- [ ] 3.1 Extend the staged import schema and reconciliation report for directory sections; verify repeat imports preserve IDs/history/newer staff edits, missing mandatory data goes to review and invalid optional values fail validation without fabrication, and update imported-catalog-format documentation with synthetic valid/invalid fixtures.
- [ ] 3.2 Add version negotiation and rollback handling for legacy clients with mandatory coordinates/coarse verification; verify unsupported shapes return a safe supported response or upgrade requirement without private fallbacks, and document that rollback cannot republish withdrawn fields.

## 4. Android Compose

- [ ] 4.1 Render the new directory sections and explicit missing/freshness states from shared projections; verify minimally published, multi-category, fee/count/availability and scoped-verification cases with Compose tests in English/Spanish and large text, and update the Android detail acceptance checklist.
- [ ] 4.2 Add contact-only list/details and safe explicit phone/email/social/web actions using the neutral map boundary; verify protected/missing locations produce no pin, distance or route, invalid links are inactive and public filtering stays consistent, and document anonymous/offline behavior.

## 5. iOS SwiftUI

- [ ] 5.1 After the shared bridge prerequisite is satisfied, render the equivalent directory sections without independent sample-only rules; verify unknown/withheld, fee/count/availability, proper names and scoped verification with SwiftUI tests and VoiceOver/large-text checks, and update the iOS detail checklist.
- [ ] 5.2 Add equivalent contact-only discovery and explicit contact handoffs with public-only MapKit inputs; verify missing coordinates, stale refresh, safe links and language parity in UI tests, and document consistency with Android.

## 6. Canonical Mobile-First Portal

- [ ] 6.1 Extend the authorized profile editor with typed fields, optional unknown states, contact designation and separate public preview; verify field errors, unauthorized changes, lost-response reconciliation and last-public-contact withdrawal through API/browser tests, and document the staff publication procedure.
- [ ] 6.2 Extend anonymous center pages, print/share summaries and page metadata to consume only the public projection; verify a minimally published center is useful without login/install and protected fields cannot leak through previews, structured metadata or caches, and document safe contact-only presentation.
- [ ] 6.3 Verify new editor/public routes at 320/360/390/768/1280 CSS pixels with long Spanish/English text, 200-percent text, keyboard/screen-reader use and real iOS Safari/Android Chrome; include screenshots, overflow checks and session-loss/private-input clearing evidence in the portal acceptance checklist.

## 7. Cross-System Acceptance

- [ ] 7.1 Run synthetic draft -> minimally valid publication -> extended field edit -> native/web refresh -> disclosure withdrawal scenarios, including import conflicts and stale declarations; verify durable shared revisions, no private public payloads and no false fee/capacity/verification claims, and record integration evidence.
- [ ] 7.2 Exercise upgrade/rollback with older clients and retained contributions, then validate strict OpenSpec and the combined foundation/discovery requirements; verify the planned application/archive ordering preserves real-map behavior and contact-only privacy exceptions, and record readiness without marking unimplemented dependencies complete.
