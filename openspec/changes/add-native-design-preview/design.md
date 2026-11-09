## Context

Source: `codex/visual-design-map` at `5a5bd63`, `design/preview/{model,app,pages}.mjs`. Its geometry, centers, identities, rankings and activity values are fictional. The requested platform decision is native SwiftUI/Compose, not an embedded website.

## Decisions

1. `PreviewSession` owns a typed route inventory and native presentation items. Shared code supplies localized content and session behavior; SwiftUI/Compose supply actual buttons, fields, menus, toggles, cards, progress bars and a native Canvas illustration. No HTML rendering or web runtime is used.
2. Reuse `GenPalette`/`MaterialTheme`, `GenTypography`, `GenSpace`, `GenRadius` and existing vector branding. Native appearance follows device light/dark settings, with text scaling, labeled controls and scrollable forms. Connected discovery retains its Google Maps/MapKit adapters; only the isolated fixture map uses Canvas geometry.
3. Each route belongs to one of ten feature groups. Missing, unknown, false, unavailable or other-account grants cannot expose a group. Cross-group links are omitted when their target is unavailable. Access updates clear the entire preview session and select an allowed route; account changes close/recreate it.
4. `PreviewSession` has no connection, account, repository, notification, payment or storage adapter. Unsupported fixture inputs, favorites, selection, offers, attendance and consent are in memory only and are discarded when leaving. Connected destinations instead use the host's real state and adapters. Sample staff status transitions cannot mutate real contributions or grant real staff privileges.
5. Preview payment checkout and tax issuance remain disabled even when their design group is enabled. Sample phone verification never sends SMS, account deletion discards sample data only, and certificate views have no validity.
6. A DEBUG-only iOS launch argument `--native-preview` enables the isolated native UI test harness without a server. It is compiled out of Release and uses the existing local-demo startup path. Android instrumentation constructs the preview directly. There is no production fallback enabling previews offline.
7. Per the 2026-11-04 visual revision, remove the repeated fictional-session banner and technical map footer. Use SwiftUI `TabView` / `NavigationStack` toolbars with no opaque bar override, and Material 3 `TopAppBar` / `NavigationBar` / `NavigationBarItem`. The shared session resolves allowed primary tabs and selected roots; groups without primary tabs retain the inventory. No custom glass imitation, navigation dependency or real-service change is needed. References: [Apple native design](https://developer.apple.com/videos/play/wwdc2025/323/), [Android navigation bar](https://developer.android.com/develop/ui/compose/components/navigation-bar).

## Tradeoffs and limits

- A single shared presentation model prevents drift between 34 pairs of screens. These are purpose-built preview models, not a general UI schema or replacement for real domain repositories.
- Fixture map geometry and distances are illustrative; its location selection is simulated and never requests GPS permission. Connected discovery reuses the actual app's location/permission behavior and cartography.
- The prototype's administrative and public-web views are included as native previews to satisfy the complete inventory, but remain outside real public-app navigation and authorization.
- Activation requires deployment of the migration/API and updated native builds, tracked in release-controls task 4.3. Flags are refreshed through existing account-aware refresh/resume behavior; these design flags are not security authorization. Executed deployments and remaining distribution gates are recorded below.

## Connected destinations (2026-11-04)

The user expanded the design-area scope to connect screens with existing mobile services. `NativePreview` now accepts a native destination renderer. `PreviewSession.connectedRoutes` selects supported destinations; the hosts render them from the existing `GenerosityAppState` or `GenerosityStore`. The session remains navigation/fixture state only and has no HTTP dependency. Unbound destinations retain their mock controls. Real map provider adapters, validated forms, account session, sync/errors, outbox and notifications are reused; no new HTTP client or credential store is introduced.

`discoveredNeeds` applies `CurrentNeedsUseCase` to visible catalog centers for both platforms. History/profile/impact reuse existing presentation with a section selector. Live and mock center IDs never translate implicitly; mock saved rows are disabled in connected exploration. Separate DEBUG-only connected launch entries support isolated navigation tests and read-only public catalog inspection; they grant no business or server permissions. Existing isolated mock entries remain available.

Normal entry requires effective feature grants. The availability of a web/API participation endpoint does not imply an implemented native integration for that route.

Map overlays retain provider attribution: Compose sends measured top/bottom `PaddingValues` through the rendering interface, without exposing Google types to shared business logic; MapKit keeps its attribution footer clear. Address lookup reuses the existing native search sheet with the current text and clears that catalog text when a new geographic origin is selected.

## Default connected design and production promotion (2026-11-08)

Both native channels enter the connected design after welcome and preserve it on
relaunch. Missing grants show loading/unavailable, retry and guest sign-in instead
of an old-layout or fictional-data fallback. Production constructs `PreviewSession`
with `connectedOnly`; `canOpen` intersects the shared connected-route list with
effective groups for inventory, direct navigation, auxiliary routes and tabs.
Preview keeps all 34 prototype routes when their own groups are enabled. Only
Preview labels its inventory destinations as connected/design.

Production enabled discovery, contributions, impact, communication and account
groups after the API/migration rollout. Other design groups and participation/
payments remain disabled. The hosted portal and both native simulator builds were
verified, including normal entry against the separate live APIs. Physical/store
distribution, a production feature administrator and provider/accessibility
acceptance remain open. See [integration evidence](../../../docs/releases/integration-2026-11-08.md).
