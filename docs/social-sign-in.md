# Generosity social sign-in

Decision: 2026-10-31. Google and **Sign in with Apple** are the initial implemented social providers. Google credentials were activated on 2026-11-02; see the live acceptance checkpoint below. Apple credentials remain pending. Apple is the user-facing name, not “iCloud login”. The user subsequently added Facebook to planned scope; Microsoft remains deferred. Identity is exclusively for Generosity web, Android and iOS; it is not SSO for the institute's other platforms.

## Planned Facebook and mobile contact extension

The [authentication spec](../openspec/changes/define-generosity-mobile-app-foundation/specs/user-authentication/spec.md)
and [profile spec](../openspec/changes/define-generosity-mobile-app-foundation/specs/user-contribution-profile/spec.md)
define requirements, not current endpoints or configuration:

- Facebook will reuse hosted entry, explicit linking and the existing web/native sessions, with a provider-specific OAuth adapter. Missing or unverified email requires Generosity email verification for new accounts; an existing verified account can explicitly link without Facebook email. No automatic email/phone-based merging.
- Private mobile contact will be collected progressively in settings or when an approved action needs it. International formatting and possession verification are separate; social login does not supply verified mobile contact automatically.
- The backend will enforce each approved action requirement. Failed/cancelled verification leaves unrelated features available and the draft unsent. Replacing/removing a verified number requires recent reauthentication; replacement verifies the new number before switching.
- Phone sharing requires a stated purpose/recipient and permission. It does not imply marketing or WhatsApp consent. Public discovery, basic account entry, assisted participation and referred beneficiaries retain their existing no-phone access.

Approved action matrix (2026-10-31): creating a group or an event requires a privately verified mobile number, in addition to earned organizer eligibility and other permissions. Browsing, basic account entry and receiving help do not. Open decisions: verification channel/provider, supported countries, abuse/cost limits and retention rules. No provider subscription, SMS
delivery, Facebook credentials or new API routes were added by this spec update.
Track implementation and acceptance in foundation tasks 18.7–18.10 and 19.1–19.7.
The following implemented boundary and setup instructions cover Google/Apple only.

## Implemented boundary

The Laravel account database owns the user ID and staff assignments. Google and Apple prove identity through authorization codes and signed ID tokens; they do not grant center roles. The same provider subject resolves to the same account across clients. Different providers are not assumed to represent the same person: sign in to the existing account on the hosted web page and explicitly link the other provider within five minutes of authentication. Equal emails never trigger an automatic merge. Apple private-relay addresses are accepted; the real address is not requested.

`/auth/sign-in?client=web&locale=es` is the shared hosted entry page. Android opens the system browser; iOS uses `ASWebAuthenticationSession`. Both providers use the backend's web client registration, so native provider SDKs/client secrets are not embedded. Existing verified-email/password login also works through the hosted page. Google/Apple entry creates an account only after verification. Provider buttons remain disabled until their configuration is complete.

Laravel HTTP requests use fixed provider endpoints. `firebase/php-jwt`, pinned in Composer, verifies RS256 signatures against provider JWKS and signs Apple's ES256 client secret. Issuer, audience/authorized party, subject, expiry, issue time and nonce are checked. Five-minute attempts have random state/nonce, an encrypted browser-binding cookie and atomic one-use consumption. Apple's cross-site form POST uses a separate Secure/HttpOnly/SameSite=None binding cookie; only that callback is exempted from Laravel CSRF, and it still requires state and browser binding.

## Client sessions

| Client | Credential and boundary |
| --- | --- |
| Web | Host-only Secure HttpOnly Laravel session. Explicit CORS/stateful origins, CSRF on writes; no bearer token in browser storage. `/auth/session` returns current user and CSRF token. `/auth/logout` invalidates the session. |
| Android | System browser returns to `org.generosity.app.android:/oauth/callback`. An encrypted pending verifier/state survives activity/process recreation. AES-GCM key lives in Android Keystore; token and pending flow are stored atomically in `noBackupFilesDir`. |
| iOS | System authentication session returns to `org.generosity.app.ios:/oauth/callback`. Keychain stores the token with `WhenUnlockedThisDeviceOnly`. A terminated in-flight browser flow must be restarted. |

Mobile callbacks contain only a sixty-second, single-use opaque code plus client state. `/api/v1/auth/exchange` requires the corresponding PKCE S256 verifier and matching client, then returns a seven-day Sanctum bearer token over HTTPS. Fixed callbacks prevent arbitrary redirects; PKCE prevents an app that intercepts a custom-scheme callback from redeeming it. Provider access/refresh tokens are not retained. Native API requests refuse redirects. Restore calls `/api/v1/me`; 401 removes the stored credential, while connectivity failure preserves the cached account identity/history and public discovery. Expired sessions require interactive sign-in again; cached identity is not server authorization.

Logout revokes only that Generosity session, not the Google/Apple browser account or another device's session. Failed logout is shown as failure and retains the credential for retry. Password reset revokes native tokens, web sessions and pending native handoffs/link attempts. Scheduled hourly cleanup removes expired attempts/handoffs; daily Sanctum pruning remains configured.

The landing opens account entry in another tab so its unsent demo form stays intact. Refocusing the landing refreshes account status. It does not automatically submit a need or grant staff access. Native release entry now uses the PHP catalog/account snapshots, protected cache and account-bound outbox. Explicit debug fixtures keep the old sample catalog for tests. The landing has an assigned-center workspace with CSRF-protected writes; public demonstration forms still never submit. Hosted email registration, recovery and verification resend are available. Live credential tests, silent renewal, account deletion/provider-revocation handling and MFA remain unfinished. See [integration status](testing/backend-integration.md).

## Configuration before activation

### Account entry visibility — 2026-11-01

The iOS and Android guest Profile now offer both **Sign in** and **Create account**.
Welcome already offered both. Registration opens the existing hosted entry with
`screen=register`; the hosted page now places both account-mode links above the
provider buttons and shows the selected email form directly. Switching modes
preserves the validated client, locale, state and PKCE challenge.

Provider buttons use the official Google mark and Apple logo, a white Google
button and black Apple button, with visible localized text and disabled states
when credentials are missing. Images and the Google Sans Latin font are hosted
locally; CSP permits only same-origin images/fonts, retaining the existing
script, form and framing restrictions. Asset sources/license are recorded in
[`backend/public/auth-assets/README.md`](../backend/public/auth-assets/README.md).

Local verification: 39 backend tests / 452 assertions; two iOS simulator UI tests
cover welcome in both languages and the guest Profile actions. Browser checks
cover Chromium/WebKit at 320/390 CSS pixels, English/Spanish, 200% text, loaded
logos/font and preserved PKCE context. Captures:
[iOS Profile](testing/screenshots/account-entry/ios-profile-es.png),
[hosted sign-in](testing/screenshots/account-entry/hosted-sign-in-es.png),
[hosted registration](testing/screenshots/account-entry/hosted-register-es.png).

The updated iOS build is installed on the iPhone 16 Pro simulator. With explicit
owner approval, the hosted view, CSP middleware and five asset/license files were
published to `api.generosity.global` on 2026-11-01. Public checks passed for
Spanish/English registration, asset hashes, CSP and HTTP 200 / `ready`. Opening
**Create account** from iOS also displays the published form and provider logos:
[live iOS registration](testing/screenshots/account-entry/ios-register-live-es.png).
The private rollback copy is documented in [the hosting runbook](hostinger-backend.md#account-entry-ui-deployment--2026-11-01).
This does not activate Google/Apple credentials or resolve the separate account-email
completion/localization/validation findings in foundation task 7.17.

Android parity verification on 2026-11-01 passed five JVM unit tests and four
instrumented tests on `Generosity_API_36` (Android 16). Welcome/Profile actions
remain reachable in English/Spanish at 200% text; busy actions are disabled and
registration is hidden after sign-in. The actual Activity → AccountSession flow
is checked by intercepting the browser intent: registration selects
`screen=register`, sign-in omits it, both preserve Android client/locale/state and
PKCE challenge, and neither exposes the verifier. Public HTTP checks for both
languages confirm the Android-context registration form, provider logo references
and context-preserving mode links. The published hosted UI is shared, so this
Android change requires a rebuilt APK, not another backend deployment.
The rebuilt APK is installed and opens on the emulator. After the suite, Android
System UI stalled; a cold start with `-no-snapshot -gpu software` restored normal
startup without wiping app data.

Reproduce the focused Android UI checks with the README JDK/SDK environment:

```sh
./gradlew :androidApp:testDebugUnitTest :androidApp:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class='org.generosity.app.AccountEntryTest,org.generosity.app.WelcomeScreenTest,org.generosity.app.GenerosityUiTest#profileShowsAnonymousEmptyState,org.generosity.app.GenerosityUiTest#profileOpensTheSelectedHostedAccountMode' --offline
```

No new accounts or provider logins were submitted. A physical-device browser and
real Google/Apple authorization remain separate acceptance checks.

### Provider configuration

No Google or Apple credentials were available during implementation. Do not put secrets in the repository, app resources, portal JavaScript or chat.

1. The API is deployed at `https://api.generosity.global`, with only the public front controller exposed. The catalog/account sync routes and their migrations are included. Adding provider credentials needs only the private `.env` and a configuration refresh, not another code deploy. The portal workspace is also published; the November 2 follow-up corrected its Sanctum session configuration. Add provider values only to the existing private `.env`, then refresh cached configuration. Provider sign-in refuses plain HTTP. Local fixture tests do not require provider credentials.
2. Set server-only configuration:

   ```dotenv
   APP_URL=https://api.generosity.global
   APP_DEBUG=false
   SESSION_DRIVER=database
   SESSION_ENCRYPT=true
   SESSION_SECURE_COOKIE=true
   SESSION_HTTP_ONLY=true
   SESSION_SAME_SITE=lax
   SESSION_DOMAIN=
   GENEROSITY_PORTAL_URL="https://generosity.global/#acceso"
   CORS_ALLOWED_ORIGINS=https://generosity.global,https://www.generosity.global
   SANCTUM_STATEFUL_DOMAINS=generosity.global,www.generosity.global
   GOOGLE_CLIENT_ID=
   GOOGLE_CLIENT_SECRET=
   APPLE_SERVICE_ID=
   APPLE_TEAM_ID=
   APPLE_KEY_ID=
   APPLE_PRIVATE_KEY_PATH=/absolute/private/path/SignInWithApple.p8
   ```

   Keep the session domain unset: the API cookie must not be shared with unrelated sites. Add development origins explicitly only in a development environment. Run the existing scheduler cron; no resident PHP worker is needed for this flow.
3. In a Generosity Google Cloud project, configure the OAuth consent screen, production domain and required privacy/terms URLs. Create a **Web application** OAuth client. Register exactly `https://api.generosity.global/auth/google/callback`. Supply its ID/secret to the backend; scopes are `openid email profile`. Google exempts requests limited to these basic identity scopes from the testing-user allowlist and seven-day authorization expiry. Testing status is therefore not an application-level access restriction. Reassess this if scopes change; see [Google's audience rules](https://support.google.com/cloud/answer/15549945?hl=en).
4. In Apple Developer, enable Sign in with Apple on the primary app ID and create/group the **Services ID** used by this hosted browser flow. Configure `api.generosity.global` and return URL `https://api.generosity.global/auth/apple/callback`. Create the signing key, keep its `.p8` outside the web root, and configure Team ID, Key ID and Services ID. Configure approved email sender domains for Apple's private email relay before relying on recovery mail. Live Apple acceptance is required on both mobile platforms and web.
5. Native builds default to `https://api.generosity.global`. Android accepts an HTTPS-only `-PgenerosityApiUrl=https://...` build property. iOS uses `GENEROSITY_API_URL` from `project.yml`/Xcode build settings. No provider secrets belong in either app.
6. Redact authorization callback query strings, form bodies and handoff codes in hosting/CDN access logs. Application exception redaction cannot remove data already logged by infrastructure. Confirm signing keys, `.env` and Composer sources are not publicly readable.

The existing `.github/workflows/publish-portal.yml` remains separate: it stages
only landing HTML/CSS/JS and explicit public assets, including generated design
tokens, for FTPS. It does not publish the API or repository. The November 2
release published the scoped portal/API changes directly over SSH.

## Google activation checkpoint — 2026-11-02

The existing `generosity-v01` Google Cloud project already hosts the Android Maps
SDK configuration. Its Google Auth Platform was not configured when inspected.
After explicit owner approval, an Owner-role invitation was sent to the
institute-designated Google account. The owner completed the required two-step
verification and accepted the invitation. Access to `generosity-v01` and OAuth
configuration with that institutional account were verified. The existing
personal owner and service-account grants were preserved.

Branding was configured while signed in with the institutional account: Google's
support-email selector accepts the active account or a Google Group that account
manages, not an arbitrary contact address. Following explicit approval of Google's
API Services User Data Policy and client creation, these values were saved:

| Field | Value |
| --- | --- |
| App name | `Generosity` |
| Audience | External; Testing |
| Support/developer contact | Institute-designated Google account |
| Homepage | `https://generosity.global/` |
| Privacy policy | `https://api.generosity.global/legal/privacy?locale=es` |
| Terms | `https://api.generosity.global/legal/terms?locale=es` |
| Authorized domain | `generosity.global` |
| Existing logo asset | `assets/brand/web/icon-512.png` |
| Client type | Web application, used only by the backend |
| Client name | `Generosity backend` |
| Authorized redirect URI | `https://api.generosity.global/auth/google/callback` |
| Requested scopes | `openid email profile`; equivalent `userinfo.email` / `userinfo.profile` entries saved in Data Access, with no sensitive or restricted scopes |
| Authorized JavaScript origins | None required for this server authorization-code flow |

The legal endpoints returned HTTP 200 and API health returned `ready` during
preparation. Google's [branding requirements](https://support.google.com/cloud/answer/15549049?hl=en)
also require domain verification and verification of displayed branding where
applicable; uploading the logo does not establish approval.

The `Generosity backend` web client was created and its credential download was
transferred directly over SSH to the institute's private API application. Only
`GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET` were changed in the server environment;
Laravel configuration was refreshed and compared with the uploaded credential
without printing secret values. The environment and configuration cache are
owner-only. The temporary local download was deleted after successful server
verification. No provider secrets were added to the repository or native clients.

Rollback: the previous environment is preserved on the server at
`private/.google-oauth.p5SVFK/env.before-google`, relative to the API domain directory.
Restore it with owner-only permissions and rebuild Laravel's configuration cache
to disable this activation. Do not download or publish that backup.

Verified after activation: API health is HTTP 200 / `ready`, `/.env` is HTTP 403,
the private credential path is HTTP 404, and the hosted Spanish account entry
enables **Continue with Google** while Apple remains disabled. Google Console
still reports External / Testing. The basic identity-scope exception described
above means Testing is not a closed pilot access control.

The first live attempt exposed a CSP restriction: Chrome blocked the form POST's
redirect because `form-action` allowed only the API origin. The correction adds
only `https://accounts.google.com` and `https://appleid.apple.com`; scripts,
framing, base URLs, cookies and CSRF protections remain unchanged. See
[MDN's redirect behavior note](https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy/form-action).
The regression assertion failed before the change and passed afterward. The
complete local backend suite passed: **75 tests / 5,187 assertions**. Local tests
use isolated MariaDB and simulated provider responses, never live credentials.

Only that `form-action` replacement was published, with original-file hash
verification, PHP syntax validation and atomic replacement. The prior production
`style-src 'unsafe-inline'` was preserved; local source already additionally
permits same-origin styles from the separate design-system work. Public HTTP
checks confirmed the new policy and API `ready`. The private original is
`private/.google-oauth.p5SVFK/BrowserAuthBoundary.before-csp.php`; restoring that
file rolls back this code correction independently of the provider environment.

After the correction, Chrome reached Google's account chooser. The owner
completed Google's passkey challenge personally, then the approved basic-profile
consent returned to the landing, which displayed the signed-in account. Signing
out returned the landing to guest mode; a second Google sign-in restored the same
account. A read-only, account-scoped backend check confirmed one verified user,
one Google identity and zero center memberships. No password, passkey, provider
token or authorization code was copied into the evidence.

The landing initially failed to load applications and the center workspace.
The 2026-11-02 follow-up found an empty `SANCTUM_STATEFUL_DOMAINS` in production.
After setting the exact `generosity.global` origin and rebuilding config, real
Google sign-in displayed the correct empty applications and unassigned-center
states. The deployment command `auth:check-portal` now rejects this missing
session/CORS configuration. Assigned-center pilot acceptance is still separate.

Physical iPhone acceptance used the installed `org.generosity.app` build 0.1.0 (1)
on an iPhone 14 Pro with iOS 27.0.1, in dark mode. After the owner unlocked it,
Profile → Sign in opened the system authentication session and reached Google's
account chooser. The owner completed the separate passkey challenge on the
phone. The browser returned to the native app and Profile displayed the same
Google account. Terminating and relaunching the app preserved that signed-in
profile. Signing out returned to Welcome; an account-scoped, read-only backend
check found one user and zero remaining native sessions. Cancelling a subsequent
iOS system sign-in permission dialog returned to Welcome without signing in.
The phone was left at Welcome after these checks.

The same Google return, process-restart restoration, logout and system-dialog
cancellation passed on the physical iPhone 17 with iOS 27.2 on November 2.
Profile showed the same account after restart. Guest Profile exposes both Sign
in and Create account; the hosted registration and expandable password recovery
forms were visibly reachable. These screen checks did not submit a personal
password reset. Actual mail-link submission was checked separately through the
controlled live HTTP account journey. Device Hub captures are retained locally
under `build/acceptance-2026-11-02/`; they are not public website assets.
A scoped server check after logout confirmed zero remaining native tokens and
zero center memberships. Four existing Android instrumented entry tests also
passed freshly on the Google API 34 emulator; they intercept browser launch and
do not establish real Android Google authorization.

This tests cancellation before opening the hosted browser, not Google's own
consent-denial callback. Live provider cancellation, explicit existing-account
linking and token expiry remain unverified, as do Android and Apple acceptance.
No Android device was connected. Foundation task 18.5 remains open; Apple
credentials also remain pending.

## Acceptance and test cases

| Cases | Evidence |
| --- | --- |
| Disabled provider; valid Google identity; no automatic staff membership; replay, wrong browser, wrong provider, expired/cancelled attempt | `backend/tests/Feature/SocialAuthTest.php` |
| Invalid signature/issuer/audience/expiry/nonce/subject/issued-at; unverified email; explicit recent linking; identity cannot move accounts | Same file, using ephemeral RSA keys and mocked provider HTTP, not a production test bypass |
| Apple POST/private-relay claim, ES256 client-secret generation; email changes preserve subject identity | Same file |
| PKCE mismatch, wrong native client, expired/reused handoff, fixed redirect, token logout, password-reset revocation, CSRF and origin controls | Same file and existing `ApiTest.php` |
| S256 RFC 7636 vector; exact iOS callback scheme/path/state and duplicate-parameter rejection | Android `AccountSessionTest.kt`; iOS `AccountSessionTests.swift` |
| Real account cannot create demo contributions; guest mode after logout | `shared/src/commonTest/kotlin/org/generosity/GenerosityAppStateTest.kt` |
| Guest/session/unavailable/failed logout, escaped display name, CSRF request, locale and draft preservation | `portal/tests/account.spec.js`, Chromium and WebKit |

Validated locally on 2026-10-31: backend suite; 58 shared JVM tests; 5 Android unit tests and debug/instrumentation-test compilation; iOS simulator build and 4 unit tests; 39 portal tests passed with one existing WebKit screenshot-only skip; English/Spanish localization consistency; strict OpenSpec validation. Generated portal screenshots were refreshed by its existing tests. Exact backend totals are recorded in the foundation implementation audit.

**Remaining live acceptance:** Apple credentials/consent and private relay delivery; Google consent denial and explicit linking; Android browser return/restart/logout and real token expiry; complete identity behavior across all three clients; account deletion/provider-revocation lifecycle and App Store acceptance. Google web consent/sign-in/logout/re-entry, iPhone return/restart/logout, iOS entry cancellation and the published API health/security checks have evidence in the activation checkpoint. Do not archive the foundation or generalize those results to untested clients or providers.

## Primary references

- [Google OpenID Connect](https://developers.google.com/identity/openid-connect/openid-connect)
- [Sign in with Apple REST API](https://developer.apple.com/documentation/signinwithapplerestapi)
- [Apple login-services review guideline](https://developer.apple.com/app-store/review/guidelines/#login-services)
- [Laravel Sanctum](https://laravel.com/framework/docs/13.x/sanctum)
- [PHP JWT verifier](https://github.com/googleapis/php-jwt)

## SMS options reviewed on 2026-10-31

- [Identity Platform pricing](https://cloud.google.com/identity-platform/pricing): first ten SMS per day are not billed; Mexico is listed at USD 0.04 per additional SMS. [Firebase limits](https://firebase.google.com/docs/auth/limits) require Blaze for real verification SMS. Web phone auth needs client-side abuse checks; PHP must validate the resulting proof rather than accepting a client-supplied number as verified.
- [Twilio Verify](https://www.twilio.com/en-us/verify/pricing): USD 0.05 per successful verification plus destination/channel delivery fees; PHP can call its API. [Twilio.org Impact Access](https://www.twilio.org/en-us/support-and-resources/impact-access-program) lists a USD 100 credit for eligible organizations. Eligibility and supported country must be confirmed for the institute; credit is not permanent free SMS.

No SMS provider has been selected, subscribed to or configured. Real SMS remains
disabled; a trial or mock code must not authorize production group/event creation.
