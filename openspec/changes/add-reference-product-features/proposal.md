## Why

The supplied Generosity concept includes functional controls and information
that neither the prototype nor the visual-only spec implements. The user now
requests specifications for those missing functions, separately from styling.

## What Changes

- Add Map, Needs, Impact and Profile navigation with a central helping action; retain center list and settings access inside those destinations.
- Add an anonymous cross-center needs feed with All/Urgent/Nearby modes.
- Add saved centers, safe public sharing links, actual center photos and opening hours.
- Add eligible donor reviews with moderation, public rating/count and user reporting.
- Add informational points, levels, achievements, a meaningful points chart and day-based streaks based only on center-validated completed help.
- Add a bell destination with an owned/readable notification inbox, without treating push permission as required to read it.
- Add center-managed volunteering events with enrollment, capacity, cancellation and validated attendance.
- Specify provider-hosted monetary donations and truthful currency totals, gated on provider selection and approved recipient setup; no native card/bank collection.
- Preserve Spanish/English, device-default language, native accessibility, anonymous discovery and server authority.

## Capabilities

### New Capabilities

- `mobile-product-navigation`: Reference destinations, central helping choice, account summary and migration of existing routes.
- `global-needs-feed`: Cross-center current needs, mode/filter semantics, origin handling and stale-data behavior.
- `saved-centers`: Favorite controls, durable ownership, guest storage and explicit account import.
- `center-sharing`: Safe public links, native sharing and installed/uninstalled app destinations.
- `center-public-profile`: Authorized public profile/photo/hour management, source metadata and truthful open status.
- `center-reviews`: Completed-help eligibility, review lifecycle, moderation, aggregates and reporting.
- `impact-gamification`: Audited non-monetary awards, levels, achievements, chart and local-day streaks.
- `notification-inbox`: Bell destination, read state, account isolation, safe navigation and notification preferences.
- `volunteer-events`: Center event publication, capacity-bound enrollment, cancellation, attendance and impact integration.
- `external-donations`: Provider-hosted checkout, verified outcomes, refunds/disputes, currency-aware history and release gates.

### Modified Capabilities

None of the seven synchronized baseline capabilities changes in this proposal.
These are additive contracts that build on the active foundation. Its
visual-only navigation is an intermediate stage, not the final navigation
of this follow-up. Existing published-needs read-only access in native apps,
non-monetary points and no native payment-data collection remain intact.

## Impact

- Depends on [define-generosity-mobile-app-foundation](../define-generosity-mobile-app-foundation/spec-index.md): shared/native parity, durable storage, authentication, catalog, center roles, contribution lifecycle, portal and notification infrastructure. These are not implemented prerequisites yet.
- Extends shared KMP contracts/state, both native interfaces, backend schemas/endpoints/workers and the mobile-first web portal. No second backend is introduced.
- Adds authenticated moderator operations distinct from center staff, validated media handling, associated public links and an external payment-provider adapter.
- Existing menu locations change only when the corresponding functional routes are delivered; old list/settings/history access must migrate without data loss.
- New strings, statuses, public-content fallback, dates, levels and accessibility names require English/Spanish coverage.
- All artifacts describe planned behavior, not implemented functionality. Sample names, ratings, point totals and balances in the image remain illustrative.

## Confirmed Decisions and Limits

Confirmed on 2026-10-25: the plus action offers a need or personal help;
only center-validated completed help earns gamification; reviews require
completed help and moderation; external payments get a spec subject to
provider selection. The center portal remains the publishing surface.

Scoring thresholds, event defaults and inbox retention are explicit initial
product proposals in design.md, not values inferred from the image or approved
individually by the user. Provider, recipient eligibility, operating regions,
currencies and production readiness require a separate release decision.
No production charges, organization self-enrollment, official verification
workflow, tax receipts, redeemable rewards, rankings, paid events, chat or
general administration is authorized by this spec-writing request.
