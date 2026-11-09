## Why

The `codex/visual-design-map` branch contains a web-only design prototype with 34 routes. The user explicitly requested rebuilding all views in SwiftUI and Compose, including simulated behavior for features whose services do not yet exist, behind backend feature controls.

## What Changes

- Port all 34 routes and their auxiliary forms into native UI using the existing design system.
- Share the route inventory, bilingual copy, fictional fixtures and session-only interactions in Kotlin Multiplatform.
- Add ten independently controlled `preview.*` feature groups, off by default, using the existing audited administrator controls.
- Connect supported mobile destinations to existing shared state, sessions, repositories, outbox and native maps; keep unsupported fixture inputs isolated.
- Make the connected design the default after welcome in production and Preview, with no optional exploration button or exit to the old layout.
- Restrict production inventory and direct navigation to connected routes. Preview retains the complete prototype and connected-versus-design inventory labels.
- Preserve feature checks, error/cache behavior and authorization in both channels; unavailable groups cannot restore fixtures or the old layout.

## Capabilities

### New Capabilities
- `native-design-preview`: Native, server-gated design exploration with supported service bindings on iOS and Android.

### Modified Capabilities
None. This does not complete or replace the backend requirements of the source roadmap changes.

## Impact

Shared KMP models, SwiftUI and Compose entry points/renderers, backend feature registry/migration/localization, native and shared tests, documentation. No new runtime dependency or WebView. Connected destinations perform real account operations only through existing validated flows when explicitly submitted by the user. Owner-authorized activation and distribution are tracked in release controls: Preview was activated on November 6 and the production API/portal on November 8. Integrated physical-device/store distribution and provider acceptance remain separate gates.
