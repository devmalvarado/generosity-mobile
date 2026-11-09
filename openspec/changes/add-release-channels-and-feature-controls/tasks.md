## 1. Backend

- [x] 1.1 Add the three known flag keys, safe defaults and per-account effective API responses.
- [x] 1.2 Add an administrator allowlist, CSRF forms, optimistic updates and transactional audit.
- [x] 1.3 Enforce new contribution/publication gates after idempotent replay; preserve existing resolution.
- [x] 1.4 Separate preview native auth clients and reject mismatched request channels.
- [x] 1.5 Validate preview configuration and provide a secret-free environment template.
- [x] 1.6 Verify permissions, CSRF, stale edits, isolated configuration and existing backend regressions.

## 2. Shared and native apps

- [x] 2.1 Fetch effective flags in shared state, clear on account change and fail closed on refresh failure.
- [x] 2.2 Preserve and retry paused outbox operations with their original idempotency key.
- [x] 2.3 Apply Compose and SwiftUI creation/progress controls with Spanish/English messages.
- [x] 2.4 Add Android production/preview flavors with isolated IDs, API, callback, storage and Firebase configuration.
- [x] 2.5 Add iOS Preview configuration/scheme and isolated ID, API, callback, keychain service and Firebase configuration.
- [x] 2.6 Generate identifiable preview icons from the existing design-system source.
- [x] 2.7 Verify shared flag/outbox tests, both Android builds and instrumentation compilation.
- [x] 2.8 Verify iOS production tests and preview build; inspect both installed simulator identities.

## 3. Web and documentation

- [x] 3.1 Apply effective controls to the center workspace and public contribution flow without blocking resolution.
- [x] 3.2 Package preview metadata/assets separately without changing the landing deployment workflow.
- [x] 3.3 Document configuration, administrator access, refresh behavior, defaults and boundaries.
- [x] 3.4 Verify browser behavior, narrow layouts, brand/localization checks and strict OpenSpec validation.

## 4. Deployment and acceptance

- [x] 4.1 Provision the separate HTTPS preview API/site, database/user, APP_KEY, storage and cron/queue; keep outgoing mail isolated.
  - [x] 4.1.1 Create both preview sites/DNS with valid HTTPS, PHP 8.4 on the API site and an exclusive `*_preview` database/user without remote MySQL access; verify the explicitly authorized account-wide SSH key (2026-11-06).
  - [x] 4.1.2 Configure the private preview environment, separate APP_KEY, application storage and cron/queue, verify database login and retain isolated outgoing mail before deploying the application (2026-11-06).
- [ ] 4.2 Complete dedicated preview OAuth and Firebase/APNs configuration and provider acceptance. Android Preview Maps is configured for its package/development certificate and live cartography passed on 2026-11-06; other signers need authorized restrictions. The remaining providers are not completed by Maps setup.
- [ ] 4.3 Complete the production backend-first rollout, approved administrator assignment and updated web/native release. See `docs/releases/integration-2026-11-08.md` for the main/preview integration and preserved production records.
  - [x] 4.3.1 Back up production, publish the API/migrations, verify unchanged private configuration and existing business records, and activate only the five connected design groups (2026-11-08). Feature reads now return 200; payments and participation remain disabled.
  - [x] 4.3.2 Publish the portal after the API gate and verify dedicated routes, live map/catalog, hosted sign-in, standalone ES/EN legal pages and the interactive tour without production writes (2026-11-08).
  - [ ] 4.3.3 Assign an explicitly approved production feature administrator. The allowlist is currently empty; preview administrator privileges were not copied.
  - [ ] 4.3.4 Distribute the integrated native release to physical devices/stores. Builds and simulator acceptance are recorded separately; earlier Preview phone installations do not include this integration.
- [ ] 4.4 Verify real accounts in both environments, live flag changes across platforms and physical-device coexistence. Preview API account/lifecycle and an administrator flag toggle passed; native direct startup and preserved separate app data are recorded. The complete account-switch/provider/coexistence matrix, physical Android and cross-client live flag acceptance remain open.
- [ ] 4.5 Archive only after deployment and acceptance evidence is recorded; builds alone do not complete these gates.

Evidence: `docs/testing/release-controls.md`; operational setup: `docs/release-channels.md`.
