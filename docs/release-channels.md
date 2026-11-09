# Release channels and feature controls

One source tree builds Generosity and Generosity Pruebas. Feature flags are stored
in each environment's existing Laravel/MySQL backend; no Firebase Remote Config
or additional feature-management service is needed. Flags apply to implemented
code, not downloaded code or unfinished functionality.

## Current controls

| Key | What turning it off does | What remains available |
| --- | --- | --- |
| `contributions.create` | Stops new need offers and self-reported help | History, cancellations, center confirmation/fulfillment, exact accepted retries |
| `needs.publish` | Stops new publication and draft/closed → published transitions | Drafts, existing published edits, closure |
| `needs.progress` | Hides the progress component | Authoritative API totals, capacity rules and receipt accounting |

Modes: `all`, `testers` (listed verified user IDs only), `off`. Existing features
start as `all` when the migration is applied. New registry keys need a migration,
implementation and checks; adding a database row alone cannot enable new behavior.

Set `FEATURE_ADMIN_IDS` to a comma-separated deployment-approved user-ID allowlist,
refresh Laravel configuration, sign in and visit `/admin/features?locale=es`.
A center membership does not make someone a feature administrator. The preview
deployment assigned its synthetic tester as administrator (see below); production
administrator assignment remains a separate rollout step. The panel requires a reason,
uses optimistic versions and records `feature.changed` in `operator_audits`.

Clients read `/api/v1/features` (anonymous) or `/api/v1/me/features` (verified
account), refresh on entry/foreground/focus/manual refresh, and do not persist
permission grants. An offline/unavailable response disables new actions. The
backend still checks every new write even if an old app displays a button. A flag
change is therefore enforced at the next server write; visible UI updates at the
next refresh. It does not remotely kill the app or cancel accepted obligations.
Paused native outbox entries keep their original key/body and retry on later sync.

## Separate builds

| Configuration | Production | Preview |
| --- | --- | --- |
| Android application / iOS bundle | `org.generosity.app` | `org.generosity.app.preview` |
| Display name | Generosity | Generosity Pruebas (P badge) |
| API default | `https://api.generosity.global` | `https://api-preview.generosity.global` |
| Android auth client | `android` | `android-preview` |
| iOS auth client | `ios` | `ios-preview` |
| Native callback prefix | `org.generosity.app.{android,ios}` | `org.generosity.app.preview.{android,ios}` |

The preview origins were provisioned on 2026-11-06. The preview API and portal
were deployed the same day from revision `ffe94f8`. Production was not changed.
Distinct native IDs isolate preferences, caches, queued operations and iOS keychain
service. Production retains its existing ID/service, so its persisted data stays
in place. There is no in-app toggle to reuse a session against another environment.

Android (JDK/SDK configured):

```sh
./gradlew :androidApp:assembleProductionDebug :androidApp:assemblePreviewDebug
./gradlew :androidApp:compileProductionDebugAndroidTestKotlin :androidApp:compilePreviewDebugAndroidTestKotlin
```

Artifacts are `androidApp/build/outputs/apk/{production,preview}/debug/`.
`-PgenerosityPreviewApiUrl=https://another-preview.example` can set a separate HTTPS
origin. Preview Maps uses `MAPS_PREVIEW_API_KEY`; production keeps `MAPS_API_KEY`.
Preview Firebase reads only `androidApp/src/preview/google-services.json`, with
package `org.generosity.app.preview` in a separate Firebase project. Missing preview
Firebase config disables its processing and never falls back to the production
file. Restrict a preview Maps key to its package and signing certificate.

For iOS, generate the project and shared bridge as usual, then use scheme
`GenerosityPreview`, configuration `Preview`. Normal `Generosity`/Debug/Release
keeps production settings. `scripts/check-mobile-environment.py` validates identity
and API/Firebase separation before building. Preview Firebase reads only
`iosApp/GenerosityApp/GoogleService-Info.preview.plist`. That file is ignored by Git;
missing configuration does not inherit the production plist. Signing/provisioning
and APNs entitlements for the preview bundle are separate setup tasks.

```sh
xcodegen generate --spec iosApp/project.yml
./gradlew :shared:prepareSharedBridge
xcodebuild -project iosApp/Generosity.xcodeproj -scheme GenerosityPreview \
  -configuration Preview -destination 'generic/platform=iOS Simulator' \
  -derivedDataPath iosApp/build/PreviewDerivedData CODE_SIGNING_ALLOWED=NO build
python3 scripts/prepare-preview-portal.py
```

The last command writes `portal/build/preview` (ignored build output) with preview
API/channel metadata, the existing HTML/CSS/JS and assets. It does not deploy or
change the landing's main-branch workflow. Host the artifact on the separate preview
origin and configure that exact origin in Laravel CORS/Sanctum. Source `portal/`
continues to target production.

## Backend deployment sequence

### Hosting provisioned on 2026-11-06

- Separate sites: `api-preview.generosity.global` and `preview.generosity.global`.
  Their authoritative DNS records resolve to the hosting account, and HTTPS
  certificate validation passed for both origins. Existing production DNS records
  were not edited.
- PHP 8.4 is selected for the preview API site. The CLI binary
  `/opt/alt/php84/usr/bin/php` reports 8.4.27.
- Database: `u643005321_gen_preview`; exclusive user: `u643005321_genpreview`.
  The owner created and saved its password privately. hPanel shows no remote
  MySQL access rules; `Cualquier host` is not enabled. Configure `DB_HOST=localhost`.
  Application authentication succeeded on 2026-11-06. The password is not recorded here.
- SSH key `generosity-preview-deploy-2026-11-06` was added and tested against
  `u643005321@145.223.77.153:65002`, with strict host-key verification. Its public
  fingerprint is `SHA256:k2fykIcGcv6K+pwJ9KqRAxoIkeLlM0PGtF18Ba+W9Jk`.
  The durable local key is `~/.ssh/id_ed25519_generosity_preview`, outside Git.
  Hostinger SSH access belongs to the whole hosting account, including production;
  the owner explicitly authorized that scope. This is not a preview-only OS user.

Preview application deployment on 2026-11-06 used revision `ffe94f8`:

- Private tree `domains/api-preview.generosity.global/private`, web root
  `public_html` only. `APP_KEY` was generated once and is not recorded here.
- Database login as `u643005321_genpreview` on `u643005321_gen_preview` at
  `localhost` succeeded. Migrations ran with `migrate --force`; none are pending.
  `migrate:fresh` was not used.
- Cached config: channel `preview`, session domain null, mail `log`,
  `LOG_LEVEL=debug`, push off. `auth:check-portal`, route cache and view cache passed.
- Both preview cron scripts run every minute. After `.env` existed they drained an
  empty queue and found no due scheduled commands. They do not send external mail.
  Their private logs are mode 600 and capped at 200k on this server only.
- At initial provisioning, synthetic tester `preview.tester@generosity.global` (user id 1) was the only
  feature administrator. Center `preview-center` and need `preview-need-kits`
  are preview-only. The ten `preview.*` groups are `testers` for that id.
  `contributions.create`, `needs.publish` and `needs.progress` stay `all`.
  Checkout and fiscal issuance stay disabled.
- No production data, SMTP settings or provider credentials were copied during
  initial provisioning. The later explicitly authorized public catalog copy is
  recorded below; production accounts and private activity were not copied.

### Reconciled preview status — 2026-11-08

- The one-time public catalog copy on November 6 added 171 production centers and
  one need, leaving **172 centers and three needs** in preview. Existing test rows,
  users, contributions and other tables were preserved. Backup and verification:
  [catalog merge evidence](testing/release-controls.md#public-production-catalog-merged-into-preview--2026-11-06).
- Public probes on November 8 returned 200 for preview features/campaigns/
  occurrences/meals. All ten design groups and the returned participation flags
  were effective for that anonymous preview request; this supersedes the initial
  tester-only group snapshot above. It grants neither account/staff permissions nor
  missing service/provider prerequisites. The participation lists were empty.
- A dedicated **Generosity Android Preview Maps** key is configured for the
  preview package and tested debug signer. Live Google cartography passed in the
  emulator. Preview OAuth and Firebase/APNs remain unconfigured; push is off.
- The connected design is the default after welcome on both Preview platforms.
  Updated builds reached the physical iPhone 14 Pro/17 and iPad; Android Preview
  passed nine emulator checks and live map/detail/progress inspection. iPad launch
  was user-confirmed; independent tablet layout acceptance is still open. See
  [native distribution and signing limits](testing/native-design-preview.md).
- Equivalent production feature/participation reads still returned 404 on
  November 8. Preview deployment does not establish production rollout.

Update by publishing a new `public/` plus `private/` tree, keeping this `APP_KEY`
and database, then `migrate --force` and rebuilding the config/route/view caches.
Roll back by restoring the previous private tree and `public_html`, not by
regenerating `APP_KEY` or running `migrate:fresh`. Preview Google/Apple sign-in and
Firebase/APNs remain unconfigured. A preview physical-device sign-in was not run.

### Remaining deployment steps

1. Done on 2026-11-06 for preview. Later deploys still start from
   `backend/.env.preview.example` and must not copy production user or beneficiary data.
2. Done on the preview database. Preview startup still rejects a production database
   name/host, parent-domain cookies, production push project or external outgoing
   mail. Verification and recovery links stay in the private log. This is isolation,
   not real email delivery.
3. Supply separately scoped OAuth/Firebase/APNs credentials as needed; the tested
   Android Preview Maps key is already configured. Register
   provider callbacks against the preview API, not the production callback. Push
   remains off until an isolated project/keys are configured. No credential files
   belong in Git or public_html. Additional Maps signing certificates require
   their own approved restrictions.
4. For production release, migrate/deploy the backend before updated clients so
   flag endpoints exist. Publish clients after the backend checks pass. Configure
   approved administrators separately. Existing apps still receive write rejection
   when a flag is off even though their old UI cannot display the new flag state.
5. Preview API journeys and native distribution were checked on 2026-11-06.
   Remaining provider setup, a preview physical-device
   sign-in and production publication are still open. Do not archive the change yet.

## Native design exploration

The additional `preview.*` flags control ten groups of session-only native design views. All start off and do not grant real account or center permissions. See [native design preview](native-design-preview.md) for the 34-route inventory, activation and isolation rules.
