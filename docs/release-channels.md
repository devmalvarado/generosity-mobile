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
A center membership does not make someone a feature administrator. No real account
has been assigned this permission by this code change. The panel requires a reason,
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

The new preview URLs are targets to provision, not currently deployed endpoints.
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

1. Provision preview HTTPS API and web origins, a separate `*_preview` database and
   user, separate application storage, APP_KEY and cron/queue context. Start from
   `backend/.env.preview.example`; never copy production user or beneficiary data.
2. Run migrations on that database. Preview startup rejects a production database
   name/host, parent-domain cookies, production push project or external outgoing
   mail. Preview mail defaults to the private `log` mailer; verification/recovery
   links can be inspected in the preview application log. `array` (discarded) or
   a local SMTP sink are also allowed. This is deliberate isolation,
   not real external email delivery. Database users/network isolation remain hosting
   configuration responsibilities; the application cannot provision them.
3. Supply separately scoped OAuth/Maps/Firebase credentials as needed. Register
   provider callbacks against the preview API, not the production callback. Push
   remains off until an isolated project/keys are configured. No credential files
   belong in Git or public_html. No new provider credentials were created here.
4. For production release, migrate/deploy the backend before updated clients so
   flag endpoints exist. Publish clients after the backend checks pass. Configure
   approved administrators separately. Existing apps still receive write rejection
   when a flag is off even though their old UI cannot display the new flag state.
5. Verify the isolated/live journeys and record evidence before archiving the
   OpenSpec change. This increment does not authorize a hosting deployment.

## Native design exploration

The additional `preview.*` flags control ten groups of session-only native design views. All start off and do not grant real account or center permissions. See [native design preview](native-design-preview.md) for the 34-route inventory, activation and isolation rules.
