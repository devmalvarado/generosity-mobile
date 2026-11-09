## Decisions

Use one source tree and the existing Laravel/MySQL service. Android has production
and preview product flavors; iOS has Debug/Release and Preview configurations with
Generosity/GenerosityPreview schemes. The preview bundle/application identifier is
`org.generosity.app.preview`; its display name is Generosity Pruebas and its icon
adds a P badge generated from the central brand source. Production keeps its
existing identifier, signing and storage namespace.

Preview defaults to `https://api-preview.generosity.global`; its web origin is
`https://preview.generosity.global`. Both sites, the isolated database, APP_KEY,
storage and cron/queue were provisioned on November 6. Separate OAuth clients and
Firebase/APNs configuration remain pending. Preview
boot validation requires a `*_preview` database, separate HTTPS origins, host-only
cookies and a local mail sink. Preview push is off until its own project and
credentials exist. Native builds reject known production preview Firebase/API
configuration. Production data is never automatically cloned or imported.

Existing `android`/`ios` PKCE clients retain their callbacks. Preview has only
`android-preview` and `ios-preview`, with `.preview` in their callback schemes.
The API and hosted auth endpoints reject a supplied mismatched
`X-Generosity-Channel`. Legacy clients without that header remain compatible.
Distinct app IDs isolate Android preferences and iOS defaults/keychain service;
there is no runtime environment switch that can reuse a signed-in session.

The `feature_flags` table has a closed set of keys, mode, verified tester IDs and
an optimistic version. Initial migration enables the three existing capabilities
for everyone, preserving behavior until an administrator changes it. Missing keys
are disabled. No arbitrary feature can be switched on without implementation.

`FEATURE_ADMIN_IDS` is a deployment allowlist of active, email-verified accounts.
The `/admin/features` session/CSRF-protected page checks this permission on reads
and writes. A transaction locks the actor and flag, checks the expected version,
and stores the prior/new state, actor and reason in `operator_audits`. Ordinary
users and center staff cannot assign themselves control access. Tester membership
is per feature and never grants a center role or bypasses existing validation.

GET `/api/v1/features` always resolves anonymously. GET `/api/v1/me/features`
requires a verified session/token. Both return effective booleans and channel,
never tester IDs. Public responses do not depend on a browser's ambient session.
Responses are not shared-cacheable. Native and web refresh on start/resume/focus
and explicit refresh; there is no live remote socket or offline permission grant.

`contributions.create` and `needs.publish` are enforced inside existing mutation
transactions, after acknowledged idempotency replay. An administrator turning off
a feature serializes with new writes through the flag row lock. A previously
accepted operation still returns its receipt. `feature_disabled` is a 409 response;
shared outboxes retain its original body/key and retry on later synchronization.
Disabling creation never cancels an accepted offer, wipes history or prevents
cancellation/center fulfillment. Already-published needs may be edited or closed;
republishing a draft/closed need is a new publication and is gated.

`needs.progress` is presentation-only: quantities remain authoritative and visible
in API data. Hiding the bar is not a privacy mechanism. These three foundation
controls do not enable money, Facebook, campaigns or other unfinished capabilities.
Additional registry keys require their own implementation, server boundary and
tests; enabled design routes do not imply a completed real feature.

## Production publication

The API publisher validates its public authentication inventory before changing
the server. Generated CSS, PNG and WOFF2 assets and TXT/MD notices are allowed;
unexpected files such as PHP are rejected. Private release archives exclude `.env`,
runtime provider credentials and backups. Initial catalog import requires explicit
opt-in; ordinary code publication does not import preview records or reset a database.

After extracting the release, remove the old Laravel configuration cache before
bootstrapping its new providers. Run incremental migrations, rebuild configuration,
validate portal authentication configuration, and rebuild route/view caches. The
portal workflow waits for the feature API plus account/intake/legal prerequisites
before staging/uploading its public files. This avoids publishing clients against
missing API routes; it is not an atomic rollback mechanism across both sites.

The November 8 promotion retained a private application/database recovery point
and compared production business-record and `.env` fingerprints afterward.
Production enabled only five connected design groups. Its other design and
participation/payment groups remain off, and the feature-admin allowlist is empty.
Preview keeps its own grants, users, credentials and data. See
[integration evidence](../../../docs/releases/integration-2026-11-08.md).

## Validation and release

See `docs/testing/release-controls.md` for executed checks and
`docs/release-channels.md` for setup and build commands. Infrastructure and API/
portal publication are evidenced; keep this change active until the approved
production administrator, integrated physical/store distribution and remaining
account/provider/flag/coexistence acceptance are completed.
