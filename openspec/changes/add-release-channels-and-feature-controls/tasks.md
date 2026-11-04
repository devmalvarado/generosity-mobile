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

- [ ] 4.1 Provision the separate HTTPS preview API/site, database/user, APP_KEY, storage and cron/queue; keep outgoing mail isolated.
- [ ] 4.2 Configure dedicated preview OAuth, Maps and Firebase/APNs credentials before testing those providers.
- [ ] 4.3 Deploy the migration/backend first, assign approved feature administrators and then release updated web/native clients.
- [ ] 4.4 Verify real accounts in both environments, live flag changes across platforms and physical-device coexistence.
- [ ] 4.5 Archive only after deployment and acceptance evidence is recorded; builds alone do not complete these gates.

Evidence: `docs/testing/release-controls.md`; operational setup: `docs/release-channels.md`.
