# Center Web Portal

## Scope and Source

On 2026-10-25 the user supplied the existing public landing at
`/Users/manuel.alvarado/Documents/Development/PoCs/generosity/portal` and
requested that Kindness Centers publish their needs there, with a mobile-first
experience. This supersedes the previous portal exclusion. The canonical plan
remains in this repository, alongside the native apps and planned backend.

Source reviewed: `index.html`, `styles.css`, `portal.js` and the asset listing.
The directory is a static site with no package manifest or operational API
integration. This was a source inspection, not a browser rendering or security
test. The original source has not been edited, moved or copied by this update.
Implementation will consolidate reviewed source/assets into canonical `portal/`
without deleting the original PoC; that destination does not exist yet.

See [center-web-portal](../openspec/changes/define-generosity-mobile-app-foundation/specs/center-web-portal/spec.md),
[architecture](../openspec/changes/define-generosity-mobile-app-foundation/design.md)
and [tasks, section 16](../openspec/changes/define-generosity-mobile-app-foundation/tasks.md).

## Source Findings

| Area | Evidence in the supplied directory | Required correction |
| --- | --- | --- |
| Need submission | `portal.js:71` handles submit locally, reports receipt/review and resets the form without a request or durable write. | Authenticate assigned staff; preserve failed input; acknowledge only durable server outcomes. |
| Donation form | `portal.js:59` constructs success/payment text locally. | Remove fake receipts and payment claims; link to supported app behavior or show unavailability. Web donor transactions are not part of this addition. |
| Mobile layout | `styles.css:114` starts with a two-column hero; `styles.css:351` collapses some layouts below 900 px. `index.html:5` provides a viewport tag. | A viewport tag and breakpoint do not prove mobile-first usability. Start with single-column phone constraints and verify every operational screen. |
| Narrow forms | `styles.css:130` keeps the city input/button in a row; `styles.css:140` prevents button text wrapping; `styles.css:305` keeps paired fields in two columns. | Verify shrink/wrap behavior and keyboard access at 320 px with long Spanish labels. Overflow is a risk identified in source, not a measured browser result. |
| Hidden state | `portal.js` toggles `hidden` for the menu/cards while `.drawer` and `.need` set `display: flex`. | Ensure author CSS respects hidden state and test visibility/focus of the closed menu and filtered items. |
| Data and authority | `index.html:45` hard-codes active-center counts; the publication form accepts a center name instead of an authorized center ID. | Use labeled samples or real catalog data; enforce backend assignments, never trust typed center names as ownership. |
| Review and payments | `index.html:248` promises review; surrounding copy describes verification and a payment provider. | Do not infer a moderation service or working payment integration from prototype copy. |

## Target Flow

1. Public visitors can view the landing and choose center sign-in without installing an app.
2. Provider-backed sign-in resolves operator-provisioned center assignments. No assignment means no publishing access.
3. Staff selects an assigned center, enters a goods/time need, previews it and saves a private draft or explicitly publishes.
4. The backend validates authorization, fields, capacity and expected version, then returns a durable result.
5. Landing and native apps consume the same published catalog. Drafts remain private and do not trigger alerts.
6. Staff edits or closes needs and confirms/fulfills permitted contributions. Donors retain their approved cancellation rights.

Initial publication is direct for authorized staff after validation, not a new
moderation queue. Center membership does not equal official verification.
Existing contributions survive closure. Imports must not erase portal-created
needs or reset commitments. Public signup, center profile/media management,
general administration and payment processing are not added here.

Subsequently, [add-reference-product-features](../openspec/changes/add-reference-product-features/spec-index.md)
scoped center profile/photo/hours editing, event management, a separate
moderator role/queue and provider-hosted payment contracts. These are follow-up
capabilities, not existing landing behavior or completed portal features.

The later [community campaign plan](../openspec/changes/add-community-campaigns-and-coverage/spec-index.md)
extends the same portal to registered users who earn organizing eligibility,
meal coordination, approved fieldwork/case assignments and scoped coverage
analysis. Organizer access does not create center membership or permission to
read household records. These routes inherit the mobile-first checks below;
no second portal or desktop-only analyst interface is planned.

The [center-mediated assistance plan](../openspec/changes/add-center-mediated-assistance/spec-index.md)
adds service participation settings, a restricted heads-up/intake queue, actual
phone/on-site contact and assessment, and separate goods receipt/beneficiary
handover records. Center membership alone does not grant private-case access;
assigned intake staff must validate the need before a donor can act. The person
seeking help needs no app, account or personal phone. Medicine services require
separate reviewed authorization and handling policies, not a general health tag.
Private intake is online-authorized and memory-bound, with no sensitive offline
draft/service-worker cache; public center directions remain available separately.
Browser helper/donor participation uses the same authoritative backend and gated
payment contracts. These are planned routes, not functionality of the old landing.

The [directory field extension](../openspec/changes/define-center-directory-details/spec-index.md)
defines the profile editor's publication minimums: identity, categories and a
designated public contact route. Highlighted fields are prominent, not all
mandatory. Optional unknown values remain explicit, with separate fee terms,
dated aggregate beneficiary reports, service admission declarations and declared
resource sources. The public preview excludes private contacts/locations and
consumes verification claims read-only. This extends the existing profile plan,
not a second editor, beneficiary registry or fee-collection service.

## Mobile-First Acceptance

- All public and staff routes work from 320 CSS pixels upward without horizontal page scrolling or overlapping content.
- Single-column phone forms, wrapping labels/errors, primary targets of at least 44 by 44 CSS pixels and no hover-only actions.
- Browser screenshots and overflow checks at 320, 360, 390, 768 and 1280 CSS pixels, in Chromium and WebKit.
- Real iOS Safari and Android Chrome checks with the virtual keyboard, portrait/landscape and 200-percent text; simulated viewports alone are insufficient.
- Keyboard focus, screen-reader names/errors, closed-menu focus exclusion and non-color status cues.
- Spanish/English interface, ordered browser-language default and a persistent browser-local system/en/es override.
- Slow/offline/expired-session/conflicting-write states preserve the current account's input without fake success; sign-out clears private work from the browser.
- Cross-center, CSRF, unsafe-content and public-data tests exercise the backend, not only disabled UI buttons.

These are acceptance criteria, not completed checks. All new implementation
tasks remain unchecked; no portal or native acceptance tests ran in this
documentation update.
