# Generosity social sign-in

Decision: 2026-10-31. Google and **Sign in with Apple** are the initial implemented social providers, pending live credentials. Apple is the user-facing name, not “iCloud login”. The user subsequently added Facebook to planned scope; Microsoft remains deferred. Identity is exclusively for Generosity web, Android and iOS; it is not SSO for the institute's other platforms.

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

1. The API is deployed at `https://api.generosity.global`, with only the public front controller exposed. The catalog/account sync routes and their migrations are included. Adding Google/Apple credentials still needs only the private `.env` and a configuration refresh, not another code deploy. The portal center workspace is not on the live landing until its files are published from `main`. Add provider values only to the existing private `.env`, then refresh cached configuration. Provider sign-in refuses plain HTTP. Local fixture tests do not require provider credentials.
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
3. In a Generosity Google Cloud project, configure the OAuth consent screen, production domain and required privacy/terms URLs. Create a **Web application** OAuth client. Register exactly `https://api.generosity.global/auth/google/callback`. Supply its ID/secret to the backend; scopes are `openid email profile`. While the consent configuration is in testing, authorize the intended test users.
4. In Apple Developer, enable Sign in with Apple on the primary app ID and create/group the **Services ID** used by this hosted browser flow. Configure `api.generosity.global` and return URL `https://api.generosity.global/auth/apple/callback`. Create the signing key, keep its `.p8` outside the web root, and configure Team ID, Key ID and Services ID. Configure approved email sender domains for Apple's private email relay before relying on recovery mail. Live Apple acceptance is required on both mobile platforms and web.
5. Native builds default to `https://api.generosity.global`. Android accepts an HTTPS-only `-PgenerosityApiUrl=https://...` build property. iOS uses `GENEROSITY_API_URL` from `project.yml`/Xcode build settings. No provider secrets belong in either app.
6. Redact authorization callback query strings, form bodies and handoff codes in hosting/CDN access logs. Application exception redaction cannot remove data already logged by infrastructure. Confirm signing keys, `.env` and Composer sources are not publicly readable.

The already-existing `origin/main:.github/workflows/publish-portal.yml` remains separate: it stages only the landing HTML/CSS/JS and three explicit public asset files for FTPS. This change modifies the existing portal files, not that workflow, and does not publish the API or the repository. No push/merge/deployment was performed for this integration.

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

**Not certified:** real Google/Apple consent screens, private relay delivery, physical-device browser return and secure-storage behavior, API/HTTPS/CORS deployment, real end-to-end identity across all three clients, account deletion/revocation lifecycle, App Store acceptance. These remain open tasks; do not archive the foundation or advertise live provider sign-in based only on local tests.

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
