# Center-Mediated Assistance

Planning date: 2026-10-26. This change specifies a consented path from an advance
notice to verified contact, center-approved support and documented handover.
All implementation tasks are pending. No live referral, medicine or payment
service is enabled by these documents.

## Coverage

| User need | Capability | Main task groups |
| --- | --- | --- |
| Find a nearby center that explicitly accepts the needed service | [center-intake-services](specs/center-intake-services/spec.md): opt-in roles, availability, suitable-center matching and public no-app directions. | 1, 7-9 |
| Warn the center that a person may arrive or call | [consented-center-referrals](specs/consented-center-referrals/spec.md): minimal consented heads-up, private reference, actual contact, assessment and scoped progress. | 2, 6-9 |
| Support an accepted need through the center and know what happened | [center-mediated-fulfillment](specs/center-mediated-fulfillment/spec.md): private approved request, bounded goods/funding commitments, receipts, handover and reconciliation. | 3-4, 6-9 |
| Handle medicine assistance without bypassing professional or privacy checks | [medicine-support-safeguards](specs/medicine-support-safeguards/spec.md): country/service authorization, qualified review, sourcing/handling and minimal disclosure. | 5, 7-9 |

The [proposal](proposal.md) defines scope, [design](design.md) defines state,
authority and privacy boundaries, and [tasks](tasks.md) separates implementation
and verification by layer. Group 10 verifies cross-system behavior.

## Approved Flow

1. A helper searches the public map/list by required service and a permitted
   device or manually selected origin. Suitable participation comes before
   proximity; a health category alone is not medicine authorization.
2. With the person's permission, the authenticated helper sends one selected
   center a minimal heads-up about a possible visit or call. It is not a booking,
   validated need, purchase, donation commitment or completed help.
3. The person receives readable public directions and an optional private
   reference. They can visit or call without an app, account or personal phone.
   Possessing the reference does not reveal private status or authorize handover.
4. Assigned center staff records actual contact, reconfirms consent and assesses
   the request. Only acceptance under current service policy permits a private
   assistance request. Medicine also requires the applicable qualified review.
5. An authorized donor explicitly chooses approved goods to buy/carry/send to the
   center or approved partner, or an eligible hosted center donation. Money stays
   disabled until provider, country and recipient gates pass. No direct cash or
   helper reimbursement is introduced.
6. The center records receipt and separately records partial/final handover to
   the person. Payment, shipment and center receipt are not beneficiary delivery.
   Owned history shows only permitted facts, with cancellation/support outcomes.

**User decision:** the heads-up can precede contact; case-linked purchases or
donations cannot precede center validation. The helper and donor may be the same
user, but neither role grants access to clinical records or authority to validate
their own completed contribution.

## Privacy And Operational Boundaries

- Public maps show centers, never beneficiary locations or private case pins.
  Google Maps, MapKit and a later MapLibre adapter receive no private case payload.
- The person controls separate notice, contact and progress/disclosure permissions.
  Declining notice still allows public directions; declining progress sharing
  must not leak arrival or approval through a donation button or error response.
- Medicine product names can reveal health information. Donor disclosure requires
  specific permission; otherwise eligible center/partner procurement can use a
  generic approved purpose. Prescriptions remain with the qualified service.
- No-contact, expiry or an unconfirmed need is not a fraud verdict or public score.
  A private correction/support route remains available without an account.
- Private intake requires current online authorization in this release. Public
  directions can remain available offline; private records are not cached as
  ordinary browser drafts or bundled into device backups.
- Case-specific recognition requires independently center-validated completed
  handover and deduplication with the canonical contribution. A notice, arrival,
  assessment or payment alone does not earn validated-help points.
- Traceable receipts reduce uncertainty; they do not guarantee honesty, medical
  benefit, delivery time or segregation of funds in a center's bank account.

## Dependencies And Delivery

1. Complete relevant [foundation](../define-generosity-mobile-app-foundation/spec-index.md)
   identity, backend, contribution authority, native parity and portal work.
   The current sample sign-in and in-memory data cannot authorize private intake.
2. Reuse [community private-case consent/access conventions](../add-community-campaigns-and-coverage/specs/household-needs-assessments/spec.md)
   without requiring an assessment campaign, household census, organizer status
   or the fieldwork flow's separate offline collection rights.
3. Reuse [reference-feature](../add-reference-product-features/spec-index.md)
   contribution recognition, inbox and gated hosted-donation contracts, plus
   [browser participation](../add-web-participation-and-sharing/spec-index.md)
   for no-install helper/donor parity. General organization verification never
   substitutes for the specific medicine-service authorization in this change.
4. Pilot reviewed non-medical intake and goods handover first. Enable medicine
   and hosted case funding only after their separate safety/provider gates pass.
   Staff operations use the same mobile-first portal, not a second backoffice.
5. Configure consent, service validity, retention, receiving/return support and
   independent rollback controls before live use. Outstanding obligations and
   rights requests survive a feature shutdown.

No main spec is synchronized or archived here. The change is ready for review
and a later explicit implementation request, not a claim of deployed behavior.
