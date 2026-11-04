## Why

The `codex/visual-design-map` branch contains a web-only design prototype with 34 routes. The user explicitly requested rebuilding all views in SwiftUI and Compose, including simulated behavior for features whose services do not yet exist, behind backend feature controls.

## What Changes

- Port all 34 routes and their auxiliary forms into native UI using the existing design system.
- Share the route inventory, bilingual copy, fictional fixtures and session-only interactions in Kotlin Multiplatform.
- Add ten independently controlled `preview.*` feature groups, off by default, using the existing audited administrator controls.
- Connect supported mobile destinations to existing shared state, sessions, repositories, outbox and native maps; keep unsupported fixture inputs isolated.
- Preserve feature checks, error/cache behavior, authorization, and real-versus-design labels in the inventory.

## Capabilities

### New Capabilities
- `native-design-preview`: Native, server-gated design exploration with supported service bindings on iOS and Android.

### Modified Capabilities
None. This does not complete or replace the backend requirements of the source roadmap changes.

## Impact

Shared KMP models, SwiftUI and Compose entry points/renderers, backend feature registry/migration/localization, native and shared tests, documentation. No new runtime dependency, WebView, provider credential or deployment. Connected destinations perform real account operations only through existing validated flows when explicitly submitted by the user.
