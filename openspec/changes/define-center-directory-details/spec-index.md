# Center Directory Details

Planning date: 2026-10-26. The user confirmed that highlighted fields are
prominent, not all mandatory. This change is specified only; implementation,
real-center publication and archive remain pending.

## Publication Rule

Required: a stable center identity, nonblank public name, one or more supported
categories and at least one designated public contact route. A private contact
does not qualify. Incomplete records can remain private drafts.

Optional fields distinguish known, not informed, not applicable and withheld.
Missing information is not zero, false, free, closed, full or officially verified.
Invalid supplied values must be corrected rather than quietly discarded.

## Coverage

| Field group | Defined behavior |
| --- | --- |
| Identity and categories | Public/legal names remain separate; multiple existing categories; no implied ownership or service authorization. |
| Activities and hours | Localized activity descriptions and authoritative general/service schedules, with unknown hours allowed. |
| Recovery fees | Service, fee type, amount/range when known, currency, charge basis, conditions and effective/review dates; no fee collection. |
| Address and contacts | Public location and designated name/role, phone, email, social and website; protected data stays outside public projections. |
| Current beneficiaries | Dated aggregate quantity, unit, population/service scope and provenance; no individual registry or inferred impact. |
| New-beneficiary availability | Dated service-specific declaration and conditions, not a guaranteed place or an occupancy calculation. |
| Ways to help and resources | Descriptive support methods linked to eligible needs/actions; resource sources are declarations, not financial audits. |
| Verification | Read-only issuer/scope/validity claims or unconfirmed legacy assertions, never a staff-controlled verified checkbox. |
| Featured/additional attributes | Approved typed values and explicit unknowns; featured affects presentation, not requiredness or verification. |

- [center-directory-details](specs/center-directory-details/spec.md) defines the
  complete field contract, publication, disclosure, maintenance and native/web parity.
- [kindness-center-discovery](specs/kindness-center-discovery/spec.md) updates two
  existing requirements for detailed profiles and contact-only list entries.
- [Design and field dictionary](design.md) records representation, migration,
  privacy boundaries and assumptions; [tasks](tasks.md) separates shared, backend,
  imports, Android, iOS, portal and cross-system verification.
- [Proposal](proposal.md) records motivation and the explicit user decision.

## Example Handling

An undated source saying 31 girls does not establish a current count, maximum
capacity or vacancies. A supplied representative name does not prove consent to
publish personal contact details. Missing legal name and fees stay not informed.
The named source example has not been imported, verified or published; use
synthetic equivalent data for testing.

## Dependencies And Limits

This extends the [reference public-profile plan](../add-reference-product-features/specs/center-public-profile/spec.md)
and [foundation catalog/portal](../define-generosity-mobile-app-foundation/spec-index.md),
reusing [scoped organization verification](../add-platform-expansion-features/specs/organization-verification/spec.md)
and [intake-service availability](../add-center-mediated-assistance/specs/center-intake-services/spec.md).
These dependencies are not assumed implemented. The prior discussion of broader
category taxonomy and food-surplus rescue is not included in this field update.

Contact-only centers remain searchable by public name/category without fabricated
coordinates. The modified discovery requirements must follow/reconcile the
foundation's pending delta before any future archive. No main specs are synced
or archived as part of this planning change.
