## Why

Generosity needs a safe way to test upcoming behavior while keeping the public app
stable. The user approved side-by-side production and test apps and centrally
controlled feature availability, using the existing PHP/MySQL backend.

## What Changes

- Build Generosity and Generosity Pruebas from the same Android/iOS sources with
  distinct app identifiers, names, icons, login callbacks and private storage.
- Prepare a separate preview API, database, website and provider configuration;
  reject known production resources when preview configuration is selected.
- Add server-owned `off`, `testers` and `all` availability with an audited,
  administrator-only browser control panel.
- Integrate three implemented capabilities: creating contributions, publishing
  new needs and showing need progress. Existing history, cancellation, center
  confirmation/fulfillment and editing/closing published needs remain available.
- Refresh effective flags in shared KMP and the mobile-first portal. Unknown or
  unavailable configuration cannot enable new actions. Preserve queued operations.

## Capabilities

### New Capabilities

- `release-channels`: Side-by-side builds and isolated preview configuration.
- `feature-controls`: Audited feature availability and server enforcement.

### Modified Capabilities

None. These controls wrap existing foundation and contribution contracts without
changing their authorization, quantities, receipt accounting or idempotency.

## Impact

Backend migration, authenticated control page, two read-only API contracts,
shared/native state and UI, platform build configuration, portal configuration,
tests and deployment documentation. No additional runtime service or dependency.
Preview provisioning, credentials, administrator assignment and production
publication remain explicit deployment tasks, not accomplished by compilation.
