# Critical foundation acceptance — 2026-11-02

Scope: portal sessions, center application review, contribution integration,
email-account journeys and available native sign-in checks. Account deletion and
retention (review item 6) were explicitly excluded by the owner.

## Published

- Corrected the previously empty production `SANCTUM_STATEFUL_DOMAINS` to the
  exact `generosity.global` host. Real Google web login now loads private
  application history and the unassigned-center state. CORS still permits only
  the Generosity origin with credentials; cookies remain host-only.
- Added `auth:check-portal` to check the configured portal URL against Sanctum
  stateful origins and credentialed CORS. The API publication script runs this
  command after building config. Regression tests failed before implementation
  and pass for exact hosts/ports, missing config, disabled credentials and a
  deceptive host suffix.
- Published eight private backend files: application-review service/controller,
  reviewer config, provider rate limiter, API/console routes, OpenAPI and additive
  migration `2026_11_02_000002`. Then published only the landing's `index.html`,
  `styles.css` and `portal.js`. Public bytes match the tested local files.
- Approval records the center, named verified account, staff role and rationale;
  the recipient must explicitly accept before membership is granted. Submission
  does not verify or publish a center. The tracker exposes states, rationale,
  acceptance and withdrawal only when the signed-in applicant can act.
- Fixed a deployment cache mismatch: Chrome reused an older `portal.js`, which
  stopped on a missing element before session initialization. The HTML now
  references CSS/JS by content hash; a browser test checks both references.
  The scoped HTML update verifies deployed asset hashes before replacement.
- The live visual check found the generated design-token stylesheet missing
  (404). Published the existing generated file and versioned its CSS import,
  then the outer stylesheet URL. The regression checks the actual body color,
  not only the presence of a stylesheet tag. Twelve distinct files are now in
  the publication manifest, including this required public asset.

No reviewer IDs were invented. The allowlist is empty, so review decisions remain
unavailable pending appointment. The production catalog remains **157 centers,
0 needs, 0 staff memberships**. Activating the pilot still requires its center,
responsible email and a real need with quantity/unit and date where relevant.
The owner's “go ahead” did not supply these facts. No synthetic public need was
created and no unrelated hosting site was changed.

## Validation

| Check | Result and scope |
| --- | --- |
| PHP/MariaDB regression | 77 tests / 5,194 assertions passed in isolated `generosity_test` |
| Portal Chromium/WebKit | Full suite: 79 passed, one intentional duplicate screenshot skip. After token-import fix: all 33 affected layout/version checks passed again, same skip. API fixtures; request assertions wait for completion |
| Local real HTTP journey | Two accounts, hosted cookies/CSRF, assigned-center access, published need, offer replay, staff confirmation/fulfillment, matching history/unit, capacity and cancellation passed |
| Persistence | Local HTTP smoke passed server restart, visit/outbox replay and an isolated SQL restore with matching business tables |
| Production email | Actual Hostinger verification/reset mail, Spanish HTML/errors, CSRF, invalid/tampered/expired links, reset, session revocation and consumed-token rejection passed |
| Production cleanup | Only the newly created controlled email-test account and its auth artifacts were removed; queue and failed jobs both zero |
| Public release | Health 200; unauthenticated private application/staff endpoints 401; `/.env` 403; migration present; portal configuration guard passed |
| Shared/Android unit tasks | Gradle succeeded; tasks were up-to-date, not a fresh device run |
| Android instrumented entry | Four tests freshly passed on Google API 34 emulator: welcome/profile access, registration intent/PKCE and enlarged text; no real provider authorization |
| Physical iPhone 17, iOS 27.2 | Google returned to native Profile; same identity restored after process restart; logout revoked native tokens; cancelling system sign-in stayed signed out |
| Physical iOS email entry | Create account and expandable recovery forms reachable; mail-app-to-native completion remains open |

See [email cases](../testing/account-email-journeys.md),
[HTTP integration](../testing/backend-integration.md) and
[review boundaries](../testing/center-applications.md). HTTP/server and browser
fixture results do not close native offline/account-isolation or the real pilot
publication → native offer → staff fulfillment acceptance matrix.

Apple remains blocked because the institute's Apple Developer membership is not
active. No Apple capability, key or production sign-in was claimed.

## iPhone 14 Pro update — 2026-11-02

Rebuilt the current shared XCFramework and signed iOS app as **0.1.0 (2)** using
the existing personal development team. Both the Xcode project and its YAML source
now use build 2. Strict code-signature verification passed with macOS keychain
access; the sandbox-only check could not establish certificate trust.

Installed over the existing app on the physical iPhone 14 Pro / iOS 27.0.1,
without uninstalling or clearing its data. The device reports version 0.1.0,
bundle version 2. Launch showed the dark-mode MapKit map, nearby pins and saved
location configuration. Guest Profile displayed both Sign in and Create account;
the app was left on the map. This is an installation/startup check, not a new
Google authorization or full contribution journey. The private local capture is
`build/acceptance-2026-11-02/iphone14-build2-profile.png`.

A fresh read-only production check still found 157 centers, zero needs, zero
staff memberships and no configured reviewers. Mail queue and failed-job counts
were both zero. The remaining pilot and native acceptance gates above still apply.

## Recovery

The exact pre-release private backup is
`/home/u643005321/domains/api.generosity.global/.deployments/portal-review-20261102T191137Z`.
It contains a consistent SQL dump, application archive, changed-file originals
and published SHA-256 manifest. Credentials were never emitted or uploaded to the
public root; `.env` was unchanged by the code release. The earlier stateful-origin
correction has its own private backup under
`private/.deployments/portal-session-20261102-5ea317bf`.

For a code rollback, restore only the changed-file originals and rebuild config
and routes. Preserve the additive review table and migration record; do not drop
applications/decisions or restore an older database over newer user activity.
Keep the repaired stateful origin unless independently diagnosing its replacement.
The production backup was created and hashed; its restoration was not exercised
against production. The restore test above used an isolated local database.
