## 1. Shared native preview
- [x] 1.1 Inventory all 34 source routes and auxiliary forms; retain separate real-feature roadmap status.
- [x] 1.2 Implement isolated bilingual fixtures, shared navigation, filters, favorites and mock interactions.
- [x] 1.3 Guard route/actions by effective groups; clear session state after access or account changes.

## 2. Backend controls
- [x] 2.1 Register ten independent native preview flags, off by default, with reversible migration.
- [x] 2.2 Reuse administrator permissions, tester targeting, version checks, auditing and bilingual labels.
- [x] 2.3 Parse only known effective preview flags in the mobile bridge.

## 3. Native applications
- [x] 3.1 Implement SwiftUI counterparts for every route and auxiliary form with existing design tokens.
- [x] 3.2 Implement Compose counterparts for every route and auxiliary form with existing design tokens.
- [x] 3.3 Integrate gated entry, account isolation and native illustrative map/list/detail navigation.

## 4. Validation and handoff
- [x] 4.1 Run shared inventory, state, validation and access-revocation tests.
- [x] 4.2 Run isolated backend feature tests and regression suite.
- [x] 4.3 Build both Android variants and iOS; run available native UI checks and inspect captures.
- [x] 4.4 Record validation evidence and activation instructions.
- [x] 4.5 Remove repeated session banners, adopt system navigation bars and validate tab selection on both platforms (2026-11-04).
- [x] 4.6 Align the map header with the supplied references: centered brand, left list/filter actions, right notifications/inventory, floating search and no close X; validate both native platforms (2026-11-04).

## 5. Preview activation and remaining acceptance
- [x] 5.1 Deploy migration/API after a concrete deployment request, choose tester IDs, enable selected groups, and distribute updated native builds. Completed for preview on 2026-11-06: API/migrations and tester administrator provisioned, effective groups enabled, updated iPhone 14 Pro/17/iPad builds installed and Android Preview installed/checked in the emulator. See `docs/testing/release-controls.md` and `docs/testing/native-design-preview.md`; this does not certify production rollout or full device/provider acceptance.
- [ ] 5.2 Obtain full physical-device/assistive-technology visual acceptance and connect real services under their separate feature specs.

## 6. Bind supported native destinations to existing services

- [x] 6.1 Reuse live shared state and map adapters for map, list, filters, center details and current needs.
- [x] 6.2 Reuse validated offer/personal forms, durable outbox and authoritative history/impact; keep feature and account checks.
- [x] 6.3 Bind profile, welcome, settings and notification inbox to the current session and persisted preferences.
- [x] 6.4 Preserve fixture isolation for unsupported screens and distinguish destinations in the inventory; add DEBUG-only connected inspection entries.
- [x] 6.5 Validate connected partial offers/offline retry, flag revocation, native navigation, localization and both platform builds; record evidence and deployment limits.
- [x] 6.6 Make the connected design the default in iOS Preview after welcome; remove its optional entry/exit while retaining effective groups and account isolation.
- [x] 6.7 Validate direct startup/relaunch and missing-group behavior, then install the updated default design on the requested iPhone 14 Pro.

- [x] 6.8 Apply default connected entry to Android Preview, with no exit to the old layout and unavailable/retry/sign-in when groups are absent.
- [x] 6.9 Update the physical iPhone 17 and verify its direct connected-map startup.
- [x] 6.10 Validate Android Preview startup/recreation, connected interactions, map availability and visual layout; record device and credential limits (9 emulator tests passed; restricted Preview Maps key configured and live tiles/detail/needs inspected; physical Android and push remain untested).
- [x] 6.11 Install Preview on the physical iPad (6th generation) / iPadOS 17.7.5 with an authorized development profile; record successful installation and user-confirmed launch after trusting the developer. Independent iPad visual/layout acceptance remains pending.

- [x] 6.12 Make connected navigation the default in production, restrict inventory and direct navigation to supported routes even when every group is granted, and preserve preview fixtures. Shared production route/revocation tests pass; hosted rollout and device distribution are tracked in release controls 4.3.
