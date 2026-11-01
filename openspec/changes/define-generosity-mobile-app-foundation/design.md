## Context

This design records the foundation's intended architecture. The change was
reopened on 2026-10-25 because the runnable prototype does not yet satisfy
all requirements. See [implementation-status.md](implementation-status.md)
for evidence and [tasks.md](tasks.md) for remaining work.

Generosity is a cross-platform mobile product for iOS and Android. The app connects people with nearby Kindness Centers through an interactive map and helps users understand how to support each center through time, goods, or money. The initial mobile experience must support Spanish and English.

The original proposal excluded backend work. On 2026-10-25 the user explicitly
included backend, persistence/sync and center-authorized contribution actions.
Welcome alignment and device-default language were also requested. The new
planning scope supersedes that earlier exclusion; implementation is unchanged.
The user then included a mobile-first web portal for center needs, extending
the landing in the older PoC. Official verification and payment processing
remain future work. As of 2026-10-31 the repository has a separate PHP/Laravel
API and MySQL/MariaDB schema. The native and portal clients are not yet connected
to it; historical prototype and interface checks do not certify integration.

### Current Implementation

Android observes `GenerosityAppState`. As of 2026-10-30 SwiftUI discovery and
details also observe that state through the existing controller/Swift store;
the duplicate Swift sample models and discovery/expiry rules were removed.
Contribution/history and notification integration remain separate tasks. Repositories are
in-memory and the clock is fixed to a demo date. JSON is the documented
catalog format, not a wired file loader. iOS uses MapKit with a fixed initial
region. Android now has a Google Maps adapter, but live tiles remain unverified
without a configured key and Google-enabled device. Permission prompts and shared
notification eligibility exist, but scheduling and delivery do not.

Shared offers start as `offered`; only `confirmed` and `fulfilled` entries
count toward impact. Bounded goods offers reduce remaining quantity in the
local catalog, using its latest snapshot. This is not a server reservation,
payment confirmation, or evidence of delivery. No user-facing contribution
status transition workflow exists. The `hoursOffered` field currently counts
time contributions, not hours; the design's hour and currency totals must not
be populated from contribution counts.

The [visual reference](../../../docs/design-reference.md) defines the requested
welcome and visual alignment of existing native screens. The user explicitly
chose visual design only, keeping new functions separate. Navigation changes,
ratings, favorites, opening hours, events, levels, achievements and streaks
remain outside this visual work. Existing non-monetary points requirements
remain in effect; this does not invent a scoring rule or sample balances.

## Goals / Non-Goals

**Goals:**

- Establish the product name as "Generosity".
- Build a shared mobile foundation with Kotlin Multiplatform for domain logic, data models, validation, and use cases.
- Use native UI technologies: SwiftUI for iOS and Jetpack Compose/Kotlin Compose for Android.
- Support Spanish and English for user-facing mobile UI, domain labels, notification text, and imported content when translations are available.
- Support discovery of imported Kindness Centers through map, list, category filters, search, and detail screens.
- Complete durable, authenticated center-to-need-to-contribution-to-history behavior on both native apps.
- Add one authoritative backend service with transactional data and a delivery worker, not independently deployed microservices.
- Preserve offline browsing and explicit pending states without claiming offline server confirmation.
- Follow device language by default and expose a persistent language setting; align welcome with the supplied reference.
- Apply the same visual direction to existing native discovery, center details, current needs, profile/impact and settings without changing destinations or domain rules.
- Preserve the public landing and add a mobile-first center workspace backed by the same authoritative catalog and contribution API.

**Non-Goals:**

- Implementing general administration, public organization onboarding, media uploads or a moderation/review queue. The scoped center workspace is included.
- Implementing official Kindness Center verification or public staff self-enrollment. Controlled operator provisioning of center roles is included.
- Processing direct bank payments, storing payment details, or managing nonprofit bank accounts.
- Implementing tax receipts, donation compliance, fraud review, or moderation tooling.
- Harvesting third-party catalog data automatically; controlled validated file imports are included.
- Translating center-managed content that is not provided in the catalog or backend data.

## Decisions

### Use Kotlin Multiplatform for Shared Logic

The shared module will own domain models, repository interfaces, validation rules, filtering, sorting, and use cases. iOS and Android will use platform-native UI layers.

Alternative considered: build with Flutter, React Native, or a single shared UI framework. This was not chosen because the app needs a native map, notification, permission, and platform UX feel while still avoiding duplicated business logic.

### Keep Maps and Permissions Platform-Specific

Map rendering, location permissions, notification permissions, and platform lifecycle behavior will live in the iOS and Android app layers. The shared module will expose platform-neutral inputs and outputs, such as location coordinates, search criteria, notification preferences, and center results.

Alternative considered: abstract every platform service behind shared interfaces immediately. This was not chosen because over-abstracting early would add complexity before the first native screens exist.

On 2026-10-26 the user selected Google Maps for Android and retained Apple
MapKit for iOS, explicitly requiring a future Android MapLibre replacement
without changes to business logic. Android now has a small `CenterMapRenderer`
presentation contract using catalog IDs, localized titles and shared coordinates.
The concrete adapter alone owns Google camera/marker objects and native loading
behavior; `MainActivity` selects it. Shared state still owns filters and selection.
MapLibre is not installed and there is no provider switch in product settings.
See [native maps](../../../docs/native-maps.md) for configuration, version
compatibility, architecture guard, migration steps and remaining real-device
acceptance. GPS, place search and iOS shared-state wiring remain separate work.

### Start with Imported Center Data Behind a Repository Boundary

The app consumes validated bundled data, a last-known-good cache and backend
revisions through one repository contract. Backend imports validate a complete
revision before publication and preserve the contribution ledger. Fixtures
remain development data, never a source of real identities or verification.

Alternative considered: require a live server for every read. Rejected because
first-launch bundled discovery and cached browsing must work offline.

### Use Stable Domain Codes with Platform-Native Localization

The shared module will expose stable domain codes for categories, help methods, verification states, notification types, and other app-controlled labels. iOS and Android will localize those labels through their native localization resource systems. Imported center content can include localized text values where available and must have deterministic fallback behavior.

Alternative considered: store all localized strings in the shared Kotlin module. This was not chosen because each native UI layer needs idiomatic integration with platform localization, accessibility, previews, and app-store language behavior.

### Treat Needs as Read-Only Mobile Data for This Change

The consumer app displays published needs and submits offers, not center
publishing controls. Authorized operators can publish catalog imports.
Assigned staff use the new web workspace to publish and maintain their center's
needs and confirm/fulfill contributions through the same API. API contracts
remain independently testable without requiring a browser.

Alternative considered: allow centers to manage needs from inside the consumer mobile app. This was not chosen because center access, verification, permissions, and moderation require a separate operational surface.

### Separate Visit Signals from Official Center Verification

The mobile app can collect explicit user visit signals that help establish that an imported Kindness Center has been visited. A visit signal does not make a center officially verified. Official verification belongs to backend and backoffice systems.

Alternative considered: automatically verify a center after one or more visits. This was not chosen because location signals are not enough to prove organizational legitimacy or authorization.

### Prepare for Payments Without Processing Them

The mobile foundation may model "donate money" as a help method, but it will not process bank payments or store payment details. Direct bank/payment processing through the nonprofit organization's account must be handled by a future backend and payment provider integration.

Alternative considered: integrate payments directly in the mobile foundation. This was not chosen because payment processing introduces legal, fiscal, security, and operational responsibilities that should be designed separately.

## Risks / Trade-offs

- Concurrent/offline offers can overbook a need -> Backend transactions own capacity; local projections are explicitly provisional and rejected offers retain editable input.
- Account switching can leak or misattribute work -> Account-scoped stores/outboxes, protected credentials and suspended queues until the original account authenticates.
- Expanding backend scope changes completion criteria -> New service, sync and lifecycle tasks remain unchecked; planning is not implementation.

- Location privacy risk -> Require explicit permission, provide useful manual discovery without location access, and avoid passive visit submission.
- Imported data can become stale -> Track import metadata and expose enough data-state information for the UI to communicate unavailable or outdated content.
- Future backend contracts may differ from the imported model -> Keep repository interfaces domain-focused and avoid binding UI directly to import file structure.
- Visit signals can be abused or misunderstood -> Require explicit user action and label them as signals, not official verification.
- Notifications can become intrusive -> Require opt-in preferences, category controls, and frequency limits.
- KMP/native UI increases integration work -> Keep shared code focused on stable business logic and let platform UI own platform-specific behavior.
- Payment expectations can leak into the MVP -> Keep payment processing out of scope and only enable money donation flows when a backend-provided payment session or external payment handoff exists.
- Localization drift can create inconsistent UX across platforms -> Keep domain labels keyed by shared stable codes and require Spanish and English resource coverage in both native apps.
- Imported content may be available in only one language -> Provide deterministic fallback to available content and avoid machine-translation assumptions in the mobile app.

## Migration Plan

The original foundation had no production data to migrate. Reopening its
tracking artifacts does not change application data or roll back the
already-synchronized main specs.

1. Connect iOS to shared state; extend repository contracts for durable data, units, synchronization state and real time without changing native UI ownership.
2. Add local persistence and the backend/API schema, validating identity, role isolation, catalog imports and concurrent/idempotent writes before client integration.
3. Complete forms, center-authorized lifecycle, account synchronization and truthful history/impact; test the full flow across two users and both platforms.
4. Deliver device-language settings and welcome as small native slices, followed by real maps/contact actions and explicit visit queues.
5. Enable reminders and alerts only after permissions, durable events, opt-out handling and delivery tests pass.
6. Consolidate the landing into the canonical repository without deleting the PoC source; deliver the center workspace against the established backend and complete browser-to-native acceptance before treating publishing as operational.

Keep sample data in explicit development fixtures. Do not migrate sample Ana
or fixed-date offers into real accounts. Local schema migrations must retain
durable unsent work once introduced. Back up server data before migrations;
verify restore and schema compatibility before rollback. Do not roll back a
client by deleting queues or replaying acknowledged operations under new IDs.

## Expanded Design Decisions

### Shared Presentation State, Authoritative Server Writes

SwiftUI will consume the existing Kotlin controller/Swift store; remove
sample-only filtering and give state subscriptions a bounded lifecycle.
Compose keeps its shared-state path. The backend revalidates every write,
using a durable relational store and transactions for contribution/capacity
updates. Mobile validation improves feedback but is never trusted authorization.
Alternative: trust client validation or share mutable in-memory state across
devices. Rejected because concurrent users and retries require server authority.

### Local Storage and Sync Are Separate from Identity

Store public catalog cache separately from account-owned private rows and
outboxes. Persist operation IDs before sending; reconcile server IDs/versions
instead of appending duplicate records. Credential storage remains native and
protected. Signed-out queues stop, not silently transfer to the next account.
Language mode belongs to the installation; account notification interests sync
through the backend, while OS grants and delivery locale belong to each device.
Alternative: save one global user object. Rejected because it cannot isolate
accounts, protect credentials or recover partial network operations safely.

### Device Language and Welcome

Represent language mode explicitly as system, en or es. System mode resolves
the OS ordered language preferences to the first supported language, then en.
Resolve the saved mode/session before rendering welcome to avoid a language
flash. User settings remain available in anonymous mode. A manual choice stays
on this installation across sign-in/sign-out and can be cleared to system mode.
Refresh shared search/sorting and notification content when locale changes.
Alternative: a toolbar-only switch or account-global language. Rejected because
the requested setting must persist and each device should retain its own default.

Welcome uses native layouts and approved logo/illustration assets, not a crop of
the phone mockup. The guest action leads to existing discovery. Returning guests
or valid sessions bypass welcome after restoration. Sign-out returns to welcome.
Actual Terms/Privacy destinations and production assets are release prerequisites;
the [Spanish legal draft](../../../docs/legal/terms-and-conditions.es.md) added on
2026-11-01 is not an approved or published legal destination. Identity/contact,
retention and age-policy fields still require review; asset licensing remains
a separate requirement.

### Permanent Account Closure and Data Removal

The user requires a deleted account to remain inaccessible. Commit deletion
acceptance and the authorization block atomically; purging may finish later but
cannot roll back access closure. Every login, recovery, provider callback,
handoff and protected API path must reject the deleted identity. No soft-delete
restoration is offered. A permitted future signup is a separate explicit account.

Account settings on all three clients and a public ownership-verified web route
share that backend operation. A scoped deletion receipt/status route must not
provide a normal account session. Revoke provider credentials and retry failures;
clear client caches/outboxes when clients receive the deletion state. Server
authorization is immediate, while disconnected device cleanup cannot be promised.

Approve a purpose-specific retention matrix before implementing purge jobs. Keep
only minimum justified evidence with restricted access and expiry; a full profile
with deleted_at is insufficient. Irreversibly anonymize shared records where
appropriate, suppress deliveries and apply deletion records before serving a
restored backup. This is not implemented by this documentation change. See the
[source and release decisions](../../../docs/legal/store-and-donation-decisions.md)
and tasks 20.1–20.8; unlinking a provider remains distinct from deleting an account.

### Visual Alignment Without New Product Flows

The new `mobile-visual-alignment` capability complements `mobile-welcome`.
The 2026-10-25 source inspection found Android using an uncustomized Material theme, text
tabs, green placeholder markers, nested detail surfaces and an unlabeled
impact number string. SwiftUI uses text-only tabs, green markers and a
260-point map inside scrolling discovery. These are source findings, not
new screenshot-test results. The 2026-10-26 Google adapter replaces the Android
placeholder but does not implement this broader visual alignment. Keep native Compose/SwiftUI rendering and KMP
state ownership; do not introduce another UI framework or fork domain logic.

Preserve Map/List/Profile/Settings, their platform navigation placement and
existing back behavior. Profile retains impact/history and center details
retain current needs. The reference informs composition, not a new global
needs feed or an extra Impact destination. Do not add the central plus, hearts,
share or notification icons without a separately specified working action.

Use a small native theme/component vocabulary rather than restyling each
screen independently:

| Role | Visual implementation direction |
| --- | --- |
| Brand and primary actions | Approved lowercase wordmark/logo; orange accent with a contrast-tested filled-action variant. Orange is an accent, not a full-screen tint. |
| Surfaces and type | White/light-neutral surfaces, near-black primary text, readable secondary text and native sans-serif roles. Preserve any supported dark-mode legibility without claiming the reference defines a dark palette. |
| Type hierarchy | Body around 16 logical units, section headings around 20 and screen headings around 24 at default text size; use native text scaling, not viewport-based font scaling or negative tracking. |
| Spacing and shape | A small 4/8/12/16/24 spacing scale, typically 16-unit content gutters and 8-unit item corners. Keep sections unframed; do not nest cards. |
| Icons and categories | Reuse native icon facilities with matching visual weight. Category color is supplemented by icon/label; red/amber/green priority cues always include text. |
| Controls | Search as an input, categories as selection controls, binary preferences as switches, locale as a system/en/es choice. Use named icon controls for tools and text for actual commands. |

These are design starting values, not measurements extracted exactly from the
raster. Record finalized tokens and contrast evidence in task 17.1. Do not
expand this into a separate general-purpose design-system package.

For discovery, let the real map occupy the main available area under compact
brand/search/category controls. Present selection in a compact preview, with
adequate map padding for native attribution and existing controls. When large
text cannot coexist with a map overlay, reflow the preview into normal layout
or a scrollable sheet instead of covering essential content. Preserve list
discovery and its filters. This visual task cannot make the Android placeholder
pass the separate real-map contract.

For details, use identity/metadata, description, current needs and help actions
in a clear vertical hierarchy. Reuse the existing offer actions and authority
rules; do not add a second, competing submit flow solely to copy the reference's
button. Restyle current needs as aligned icon/title/priority rows, not a new
cross-center feed. Show a center photo only when the data already supplies an
approved real asset; otherwise use the category/identity treatment with no
large blank image slot. No photo source/schema or upload feature is added here.

For Profile, arrange real account/guest identity, compact labeled impact and
history. Metric units/provenance remain owned by the existing impact contract;
the visual work must not reinterpret current entry counts as hours. Use an
orange-accent points treatment only with a supported value or unavailable
state. Omit sample avatars, levels, streak flames, badges and currency totals
without an actual data contract. Settings and existing forms inherit the same
visual rules while retaining their controls, validation and account semantics.

Verify against the five-phone reference by content hierarchy, color roles,
spacing and action prominence, not exact pixel identity across native OSes.
Use explicit test fixtures for reproducible screenshots, never production
sample balances. Capture small/large phones, Spanish/English, default/enlarged
text and loaded/empty/error states. Compare each native baseline separately;
document intentional omissions from the concept. Do not mask controls or
content to hide layout regressions. See the visual acceptance matrix in
[mobile-ui-checks.md](../../../docs/testing/mobile-ui-checks.md).

### Shared Design System Implementation — 2026-11-01

The implemented design system is a build-time resource pipeline shared by
Android, iOS, the public/center portal and Laravel's hosted account/legal pages.
It extends the existing native theme helpers and web CSS components; it adds
no UI framework, runtime configuration endpoint or dependency to the KMP domain
module. The initial visual-direction values above are historical guidance;
`design/tokens.json` is the authoritative implemented configuration.

#### Sources and token contract

| Source / section | Contract |
| --- | --- |
| `design/tokens.json` → `colors` | Semantic primary-action, text, surface, outline, error and category roles. Each role contains exactly two six-digit sRGB colors, ordered light/dark. |
| `brand` | Logo color, light/dark wordmark lettering and opaque app-icon background. Logo orange and accessible action orange are deliberately separate roles. |
| `web` | Landing editorial neutrals, map illustration colors, hover/focus, disabled and field states. These coexist with the common action/error roles. |
| `typography` | Android body/heading system families and type scale; iOS body/heading family names and system font design; web body/heading/account stacks and optional landing font stylesheet. |
| `spacing`, `radii`, `sizes` | Common spacing, control/card corners, wordmark/symbol bounds and minimum interaction sizes. Values map to Android dp, iOS points and CSS pixels; enlarging text does not double fixed padding. |
| `assets/brand/master/symbol.svg`, `wordmark.svg` | Editable outlined logos. The viewBox starts at `0 0`; paths have `data-color="brand"` or `data-color="text"`. Flat paths and optional `translate(x y) scale(x y)` are supported; unsupported elements/transforms fail explicitly. |

`assets/brand/source/` and `geometry.json` retain the original supplied images
and historical tracing geometry. Ordinary regeneration never retraces the PNG
or overwrites the master vectors. Explicit `generate-brand-assets.py
--trace-source` is an authoring import operation, not a build prerequisite.

#### Generation and platform consumption

Run `scripts/generate-design-system.py` with the documented brand-authoring
Python environment. It validates tokens, renders platform declarations and,
when brand inputs or exported files change, invokes the existing brand exporter.
The exporter uses the already adopted Pillow/resvg authoring dependencies;
Potrace is required only for explicit PNG re-import. Runtime apps gain no new
dependency.

| Generated output | Consumer and behavior |
| --- | --- |
| `androidApp/src/main/java/org/generosity/app/ui/DesignTokens.kt` | `GenerosityTheme.kt` maps `GenColors` to Material light/dark schemes and configures Material typography/shapes. Existing Compose controls consume the theme; screen spacing and logo bounds use generated dimensions. The Google adapter derives marker hue from theme colors without exposing SDK types to shared logic. |
| `iosApp/GenerosityApp/DesignTokens.swift` | `GenerosityTheme.swift` supplies adaptive `GenPalette` colors and the `GenTypography` helper. SwiftUI screens use semantic text roles and generated dimensions; system-owned UI keeps native rendering. |
| `portal/assets/design-tokens.css` | Imported by `portal/styles.css`; existing button, card, field and illustration styles consume custom properties. This stylesheet may load the configured landing font service. |
| `backend/public/auth-assets/design-tokens.css` | Loaded by `auth-styles.blade.php` for sign-in, registration, recovery, result and legal views. The generated file omits remote font imports. |
| Native/web logo exports | Vector drawables, iOS SVG image sets, opaque launcher/store icons, SVG/PNG/ICO exports, the brand ZIP and the portal's logo/favicon/touch-icon are generated from the same masters. |

The Kotlin and Swift layers remain separate native implementations; reuse is
through configuration and platform components, not shared rendered UI. Layout
breakpoints, map sizing constraints and domain/data state stay in their
respective layers. Google/Apple sign-in controls retain provider-owned colors,
logos and fonts. Map tiles, keyboards and OS dialogs are not recolored by this
pipeline; the Google marker API accepts hue rather than exact RGB.

#### Typography, appearance and accessibility

Android copies Material's semantic text styles with configured body/heading
families and scales both font size and line height, preserving system font
scaling. iOS `system` uses native text styles with default/rounded/serif/monospaced
design. Custom iOS fonts use an unscaled base size and SwiftUI `relativeTo`
scaling so Dynamic Type is applied once. A new bundled font still requires its
licensed file and platform registration; a configuration edit cannot install
an absent font.

The portal retains its editorial font stacks. Hosted account pages default to
system fonts and use only same-origin font resources; their CSP allows
`style-src 'self' 'unsafe-inline'` and `font-src 'self'`. No script or external
font permission is added to the account boundary.

Validation rejects malformed colors, unsupported iOS system designs, unsafe
font configuration, Android type scales outside 0.8–2 and targets below 48dp
Android / 44pt iOS / 44px web. Both appearances must meet 4.5:1 for the configured
on-accent/accent, text/surface, text/background, secondary/surface and
on-error/error pairs. This checks those text pairs, not full accessibility or
every possible composed background. The current action pair is white on
`#C6401D` (5.06:1); the source logo remains `#F1562C`.

#### Drift prevention and release behavior

`design/generated.json` records SHA-256 hashes of brand inputs and generated
assets. Token source code/CSS is compared with freshly rendered declarations;
brand inputs and outputs are compared with the manifest. The read-only
`python3 scripts/generate-design-system.py --check` needs only Python's standard
library and exits nonzero with a regeneration instruction when resources drift.

The check is wired into Gradle's Android pre-build, Xcode's pre-build phase,
the portal's pre-test script, the design-resource CI job and both publication
workflows. Generated resources and their sources belong in the same commit.
The portal deployment explicitly includes the generated CSS in its public-file
allowlist; the API deployment publishes its own auth asset. Existing separate
deployment targets and authorization boundaries remain intact.

Design changes require regenerated resources, rebuilt/distributed mobile apps
and ordinary web/API publication. They do not remotely modify installed mobile
versions. Reverting the source and regenerated resources together restores the
prior design without changing accounts or business data.

#### Executed validation and remaining acceptance

| Environment | Executed result |
| --- | --- |
| `Generosity_API_36` Android 16 emulator | Four UI tests passed: bilingual welcome and profile entry with enlarged text, guest profile, and discovery/language/detail navigation. Debug APK, instrumentation compilation and five JVM tests also passed. |
| iPhone 16 Pro simulator, iOS 18.6 | Three UI tests passed: bilingual welcome, guest profile and Spanish visual tour through map/list/detail/needs/profile/settings/keyboard/empty state. Xcode simulator build passed. |
| Chromium and WebKit | 69 portal tests passed, with one existing duplicate screenshot case intentionally skipped. Hosted account/legal rendering and CSP were additionally checked in ES/EN at 320px with account text at 200%. |
| Generation/backend/specs | Five generator checks passed, including a temporary recolored brand export and deterministic regeneration. PHP social-auth tests passed (14 tests / 201 assertions). Strict foundation OpenSpec validation passed. |

The generator tests distinguish config propagation from real rendering checks;
local auth tests do not certify live Google/Apple authorization. Physical
devices, complete screen-reader acceptance, newly installed font files, live
Google map tiles and production cache behavior remain outside this validation.
Tasks 17.11–17.13 are complete; task 17.10 remains open and the foundation is not
ready for archival. Reproduction commands and limitations are in
[design verification](../../../docs/testing/design-system.md) and the
[maintainer guide](../../../docs/design-system.md).

### Contribution Authority and Units

User-approved policy: users submit/cancel their own offers; assigned center
staff confirm and fulfill center-linked contributions through the backend.
Personal activities are explicitly self-reported and completed by their owner.
Use offered -> confirmed -> fulfilled, with cancellation allowed from offered
or confirmed and terminal fulfilled/cancelled states. Cancellation and capacity
release share one transaction. A conflicting expected version returns current
state; it never silently overwrites a concurrent action.

Track planned and actual duration explicitly and store goods units. Confirmed
participation is not completed delivery; self-reported help is not center
verification. Monetary totals remain unavailable without payment integration.
Keep supplied informational points display-only; no scoring engine is added.
Alternative: let users mark center donations verified. Rejected by the approved
authority rule. Existing append-only contribution storage must become an
identifier/version-aware record plus event history before lifecycle integration.

### Backend Contract and Operational Surface

Add the backend as a separate runnable service within this repository during
implementation, with one database and a durable notification-event worker.
The user selected PHP and the existing Hostinger database capability on
2026-10-31. [The architecture decision](../../../docs/backend-php-mysql.md)
selects Laravel 13/PHP 8.4, MySQL/MariaDB and the framework first-party user
provider/password broker with Sanctum. This supersedes the earlier unspecified
external identity adapter: framework hashing/recovery stays on the server, with
verified-email bearer sessions and audited CLI staff provisioning. Google/Apple OIDC entry now uses a maintained JWT verifier; no custom
cryptography, Firebase or Keycloak integration is introduced. Hostinger web
runtime, database, SMTP and deployment acceptance remain separate gates.
The Kotlin directory-journal service remains on `codex/mockup-backend-kotlin`.

The API schema will cover these contract families, using stable domain codes:

| Family | Operations and boundaries |
| --- | --- |
| Public catalog | Read/version/page centers and needs anonymously; controlled atomic import outside consumer UI. |
| Center workspace | Read assigned centers and private drafts; create, edit, publish and close needs with versions/idempotency; read only that center's contribution details. |
| Current account | Resolve validated identity, read/update profile and notification interests without client-selected ownership. |
| Contributions | Submit, page/detail history and request versioned transitions with idempotency; enforce owner/center roles. |
| Reminders and visits | Owned reminder CRUD, explicit visit receipts, durable queue acknowledgements. |
| Installations | Register/rotate/remove delivery tokens, locale and permission state; invalidate private delivery at sign-out. |
| Synchronization | Versioned private changes/tombstones and cursor reset independently of public catalog revisions. |
| Operations | Health/readiness, migrations, backup/restore, controlled center-role provisioning and import diagnostics. |

Role provisioning is not official organizational verification. Staff use the
scoped web workspace; authenticated API tooling remains useful for tests.
Contract tests must
include unauthenticated reads, cross-account access, cross-center access, stale
versions, concurrent capacity and replay of operation IDs with changed payloads.

### Mobile-First Public Landing and Center Workspace

The provided source is a small HTML/CSS/JavaScript site, assessed in
[center-portal.md](../../../docs/center-portal.md). During implementation,
bring reviewed source/assets into `portal/` in this canonical repository,
preserving the external PoC. Do not introduce a framework solely to render a
landing or forms; select additional tooling only where authentication/routing
and automated tests need it. The portal is a browser client, not a new backend
or another catalog. KMP remains the native shared layer; the web client uses
the documented API rather than requiring Kotlin/JS or duplicating authority.

Keep public presentation and the authenticated work area distinct. Staff entry
leads directly to a compact needs list with center selection, draft/published
status and create/edit/close actions; contribution details expose permitted
confirmation/fulfillment. Use a one-column phone layout first, then wider
grid/list arrangements. The work area uses restrained headings, unframed
sections and labeled forms, not a marketing hero or nested cards. Preserve
the existing brand assets subject to provenance review; brand/content reuse
does not require preserving desktop-first CSS or the decorative fake map.

Start CSS with narrow-screen constraints and add min-width enhancements.
Inputs and grid children need shrinkable widths; long content wraps; controls
must not require hover. Cover 320, 360, 390, 768 and 1280 CSS-pixel widths in
browser screenshots and horizontal-overflow assertions. Test 200 percent
text, keyboard/screen-reader navigation, portrait/landscape, and real iOS
Safari/Android Chrome with the virtual keyboard. An emulated screenshot alone
does not certify keyboard behavior. Keep hidden menus/items out of both layout
and focus; the source's explicit display rules require regression tests.

Use the established identity provider and server-scoped center assignments.
Public sign-up does not grant staff access. Reuse controlled operator
provisioning; a general staff-administration UI is not part of this addition.
Document browser sessions and CSRF protection before enabling writes; do not
put credentials in URLs or public assets. Scope private drafts to account and
center and clear them at explicit sign-out. Retain unsaved input on transient
failure/session renewal without letting another account inherit it.

New needs can be private drafts; only explicit, acknowledged publication adds
them to public discovery. For published needs, preview and explicitly submit
changes rather than publicly autosaving each keystroke. Record author, version
and timestamps. Closing maps to the existing cancelled need state and blocks
new offers without cancelling existing contribution records. Covered and
expired states continue to follow quantity/time rules; reopening is not needed
for this initial workspace. Reject edits that invalidate existing commitments.

Publication and catalog/alert events commit together. Imports operate on their
declared source ownership; absence from an import is not deletion of a
portal-created need. Reject stale collisions with staff-managed revisions for
explicit operator reconciliation. Native consumers remain read-only and learn
changes through existing catalog sync. No polling interval is advertised as
instant delivery, and drafts do not trigger alerts.

The original form promises review, but no moderation process was requested.
Initial publishing is direct for provisioned staff after server validation;
remove review promises rather than inventing a review queue. Sample centers,
counts, coordinates and payments remain clearly illustrative or unavailable.
The original donor form must not fake receipts: guide visitors to the supported
app flow or explain unavailability until a separately scoped web donor flow
exists. This addition does not implement web donations or payment processing.

Support Spanish/English browser resources and browser-local system/en/es mode,
using ordered browser languages and the same fallback policy as mobile. Keep
untranslated center text intact; do not add automatic translation. Date/time
entry shows its zone and uses the existing real-clock/expiry policy.

### Time and Notification Delivery

Use server instants for accepted events and expiry enforcement; retain offline
client timestamps only as context. Date-only legacy expiry ends at the next UTC
midnight. Reminder input displays its timezone, resolves to an instant and asks
for clarification for DST gaps/folds; travel changes display, not the instant.

Schedule reminders locally per installation and sync their records; deduplicate
logical deliveries per installation. Nearby checks use permitted device location
without uploading a continuous location trail. Initial configurable defaults are
500 meters and one nearby alert per center per 24 hours; document them and test
boundary values. Background evaluation is enabled only where supported and
authorized, otherwise settings describe foreground-only behavior.

Urgent alerts originate in backend catalog events, matching selected categories
or an explicit All choice. A durable event outbox survives delivery failures.
Recheck eligibility on retry, remove invalid tokens and avoid private notes in
lock-screen text. Offline preference updates take effect locally immediately,
while the UI shows remote synchronization pending. Already-in-flight pushes are
not claimed to be retractable. No provider-specific delivery guarantee is promised.

## Implementation Selections

PHP/Laravel, MySQL/MariaDB and the first-party identity provider are selected
in the backend decision. The intended hosting is Hostinger Premium; production
web runtime/database/SMTP acceptance, live social-provider acceptance and platform
delivery configuration still require implementation evidence.
The Android map provider is now selected as Google Maps; key configuration and
real-provider acceptance are pending, with MapLibre retained as a future adapter.
Production assets, legal URLs and catalog source/rights must be supplied before
release. Official verification policy and payment providers belong to separate
future changes; they do not block the time/goods contribution contract.

### Generosity social identity decision — 2026-10-31

Add Google and Apple authorization-code sign-in to the Laravel account provider.
Use one hosted `/auth/sign-in` page across web and both mobile apps. Verify signed
OIDC identity tokens with the maintained `firebase/php-jwt` package and Laravel's
HTTP client; do not add native provider SDKs or an OAuth authorization server.
Sanctum remains the Generosity session issuer, not an OIDC federation service.
This explicitly excludes SSO for other institute platforms. The subsequent user
decision below adds Facebook to planned scope; Microsoft remains deferred.

Web uses host-only HttpOnly cookies, exact CORS/stateful origins and CSRF.
Apple's cross-site POST uses a separate five-minute encrypted browser-binding
cookie with Secure/HttpOnly/SameSite=None. Google/Apple state and nonce are random,
server-stored and consumed transactionally before token exchange. Native clients
use a separate one-use handoff with PKCE S256 and fixed callbacks; bearer tokens
are delivered only in an HTTPS exchange response, then stored using Keychain or
Keystore. Password resets revoke API tokens, web sessions and pending handoffs.
Do not auto-link equal emails or infer center roles. Link identities only after
explicit consent in a recently authenticated Generosity web session.

See [implementation/configuration/evidence](../../../docs/social-sign-in.md).
Provider credentials and live acceptance are still pending. Native account entry
is connected; catalog/history/contribution synchronization remains separate work.

### Facebook and progressive mobile contact decision — 2026-10-31

The user requested a Facebook specification and mobile numbers for selected
features. Reuse the hosted browser entry, account store, explicit linking and
web/native session boundaries. Add a Facebook-specific OAuth adapter rather than
assuming Google/Apple ID-token semantics. Native provider SDKs are not needed for
this hosted approach. Request only necessary identity/profile/email permissions;
do not request friends, posting permissions or a phone number from Facebook.
Treat absent/unverified email as an email-completion step for new accounts;
an existing verified account can explicitly link Facebook without provider email.
Never equate Facebook account verification with email or mobile verification.

The earlier estimate was 1–2 development/test days for the Facebook extension,
assuming the current session flow is reusable. It excludes mobile verification,
Meta setup/review and live acceptance; it is not a delivery commitment. Before
activation, configure a Generosity Meta app, fixed HTTPS callback and server-only
credentials, and verify current privacy/data-deletion and publication requirements.
Keep Facebook unavailable until configuration and acceptance pass. No Facebook
adapter or Meta credentials have been added by this planning update.

Mobile contact belongs to the Generosity account, separately from social identity,
email proof and public center contacts. Store a normalized international number
with server verification state/time; a pending replacement cannot overwrite the
current verified number. Do not import mobile numbers from social providers as
verified. Shared KMP models expose the server state and stable error codes; native
settings and mobile-first web forms render the same policy in Spanish/English.
No contacts permission, native SMS-reading permission or phone-only login is added.

Require a number progressively at an approved action, not during basic account
entry. The backend must enforce the requirement even if UI checks are bypassed.
Challenges are short-lived, one-use, account/number/purpose-bound, with bounded
verification attempts, resend cooldowns, send quotas and delivery timeouts. Choose
explicit limits and a delivery provider before implementation; missing service
configuration must show unavailability and preserve the unsent action safely.
Use recent reauthentication for replacement/removal and atomically revoke the old
proof on successful replacement. A shared/recycled number is neither a unique
person identifier nor authority to merge/recover another account.

Open decisions before enabling phone-gated actions:
- Approve a named action/purpose/recipient matrix. Event coordination, pickups and
  center follow-up are candidates only, not newly imposed requirements. Preserve
  existing no-phone assisted participation and beneficiary/referral routes.
- Select delivery channel/provider, supported countries, abuse/cost caps, challenge
  lifetime/retry limits and service-unavailable handling; no SMS/WhatsApp purchase
  or sending is authorized by this specification.
- Define per-purpose contact sharing and retention/removal rules. Number collection
  is not marketing consent; private numbers and codes stay out of public payloads,
  analytics and logs. Only scoped operations receive a number with explicit permission.

See tasks 18.7–18.10 and 19.1–19.7. Existing Google/Apple evidence does not certify
Facebook or phone verification, and this foundation remains active.
