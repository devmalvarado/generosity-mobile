# Web Participation and Sharing

Planning date: 2026-10-26. Four new specs; all implementation tasks remain pending.

**2026-10-31 consolidation:** The proposed helper-web change was folded into this spec and into [center-web-portal](../define-generosity-mobile-app-foundation/specs/center-web-portal/spec.md). Landing help without an account is a requirement here. Staff entry is `/kindness-center` there. This change remains the owner of campaigns, meals, invitations, assisted participation, installable offline mode, and outcome sharing. It is not a second citizen site.

| Experience | Contract | Tasks |
| --- | --- | --- |
| Help without installing | [public-web-participation](specs/public-web-participation/spec.md): anonymous discovery, real browser helping/history, optional PWA and safe offline states. | 1, 2 |
| Invite to a concrete opportunity | [opportunity-invitations](specs/opportunity-invitations/spec.md): links, previews, QR, browser/native destinations and voluntary sharing. | 3 |
| Participate without a personal digital account | [assisted-participation](specs/assisted-participation/spec.md): consented coordinator entry, common capacity and verified later association. | 4 |
| Share actual results and return | [shared-impact-updates](specs/shared-impact-updates/spec.md): attributed outcomes, opt-in updates and aggregate completed-help measurement. | 5 |

See [proposal](proposal.md), [design](design.md) and [implementation tasks](tasks.md).
Group 6 verifies whole flows. This extends the existing canonical portal, not a
second backend or a requirement to abandon native KMP apps.

Public browsing remains anonymous. Self-service protected writes use real
authentication; assisted participants use a separately authorized actor/subject
flow, not fake accounts. Browser installation, push and precise location are
optional. Sharing/downloads never earn helping points. App-store links are not
substitutes for a usable browser destination.

Dependencies: [foundation](../define-generosity-mobile-app-foundation/spec-index.md),
[reference features](../add-reference-product-features/spec-index.md) and the
relevant [community flows](../add-community-campaigns-and-coverage/spec-index.md).
The [platform expansion](../add-platform-expansion-features/spec-index.md) adds
credentials, territorial comparisons and formerly deferred operations separately.
The [scope review](../../../docs/scope-review.md) reconciles earlier exclusions.

No application code, hosted service, email campaign or real enrollment is
created by these specs. Production providers/domains and privacy configuration
remain release prerequisites. No change is archived until implemented/verified.
