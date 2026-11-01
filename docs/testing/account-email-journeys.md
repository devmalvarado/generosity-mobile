# Account email journey verification

Date: 2026-11-01 (America/Monterrey). Target: `https://api.generosity.global`.

The published email/password backend was exercised through HTTPS with a cookie
jar, real hosted forms and their CSRF tokens, actual Hostinger SMTP messages read
from a controlled mailbox, and the scheduled queue worker. This is HTTP/form
acceptance, not a physical-device or screen-reader test.

An initially absent account was registered using the project's notification
mailbox. Only that temporary account was used for password changes and expiry
setup. No existing user's password was changed. Production application code and
hosting configuration were not deployed or modified by this review.

Cleanup completed: the temporary account, its bearer tokens, owned sessions,
reset record and pending handoffs were removed. Local test passwords, links and
cookie state were deleted. The production queue finished with **0 queued jobs
and 0 failed jobs**. Existing accounts and business records were preserved.

## Live cases

| ID | Journey / action | Observed result |
| --- | --- | --- |
| EMAIL-01 | Open hosted entry with `locale=es`, submit registration | Spanish form; HTTP 202; unverified account created |
| EMAIL-02 | Attempt password sign-in before verification | HTTP 403, `email_not_verified` |
| EMAIL-03 | Request verification resend from the hosted form | HTTP 202; both registration and resend messages arrived through cron |
| EMAIL-04 | Follow the actual verification email link | Correct production HTTPS host; signed link; HTTP 200, account verified |
| EMAIL-05 | Alter a verification URL or use an expired signed URL | Both rejected with HTTP 403; expiry test used a correctly signed URL generated for the temporary account only |
| EMAIL-06 | Reopen an unexpired verification link | HTTP 200; verification is intentionally idempotent |
| EMAIL-07 | Sign in after verification | Bearer token accesses `/api/v1/me`; hosted login establishes a session and redirects to the Generosity portal |
| EMAIL-08 | Request password recovery from hosted entry | HTTP 202; reset message delivered automatically |
| EMAIL-09 | Request recovery for an unknown address | Generic HTTP 202 `reset_requested`; no account disclosure in response |
| EMAIL-10 | Open the reset button's actual `href` | HTTP 200; token and correct email in form; `Cache-Control: no-store` and `Referrer-Policy: no-referrer` |
| EMAIL-11 | Submit a mismatching confirmation / invalid token | Mismatch redirects without changing password; invalid token returns HTTP 422 `invalid_reset_token` |
| EMAIL-12 | Submit the web reset without a CSRF cookie | HTTP 419 |
| EMAIL-13 | Submit the emailed token with a matching valid password and CSRF session | HTTP 200 `password_reset`; old bearer token rejected with 401; browser session becomes unauthenticated |
| EMAIL-14 | Sign in with old and new passwords | Old rejected with 401; new accepted with 200 |
| EMAIL-15 | Reuse the consumed reset link | HTTP 422 `invalid_reset_token`; current token continues to work |
| EMAIL-16 | Submit an expired reset token | HTTP 422; password remains unchanged |

For EMAIL-16, only the temporary account's reset row was backdated beyond the
configured 60-minute expiry. This tests server expiry enforcement without waiting
an hour; it is not evidence from an hour-long browser session. Expired signed
verification URLs were likewise generated explicitly for the test account.

Allow the scheduled worker to deliver mail before inspecting the mailbox. The
initial mailbox probe preceded delivery and was retried after the messages
arrived. Read the button's HTML `href`: a regex over multipart fallback text can
concatenate Markdown links and produce a false invalid-email result. That test
harness error was corrected before the successful reset. Do not retain signed
URLs, tokens, passwords or raw email bodies in reports or Git.

## Original production user-experience findings

These were observed on the previously published release. They are corrected in
the local release below; production still requires deployment and a fresh check.

1. **Spanish does not carry through to email and reset.** Spanish hosted registration
   and recovery produced English emails; the reset page is hard-coded in English.
   The queued notifications have no explicit locale, and their framework email
   strings are absent from `backend/lang/es.json`.
2. **Completion has no useful next step.** Verification and successful browser
   reset display raw JSON. Provide localized HTML confirmation and a sign-in action
   for browser links while preserving the API's JSON contract and native entry context.
   Invalid/expired browser links also need clear recovery/resend guidance.
3. **Reset validation errors are invisible.** Submitting mismatching passwords
   redirects to the same form, which does not render validation errors. Users need
   a visible, accessible explanation and a retry path without repopulating passwords.

Locations: `backend/resources/views/reset-password.blade.php`,
`backend/routes/web.php`, `backend/app/Http/Controllers/AuthController.php`,
`backend/app/Http/Middleware/ApiBoundary.php`, `backend/app/Models/User.php`,
`backend/app/Notifications/` and `backend/lang/es.json`.
Foundation task 7.17 now records the implemented local corrections below. Task
7.18 tracks deployment and production/physical-device acceptance.

## Repeatable automated regression checks

Run from `backend/`:

```sh
docker compose exec -T app composer test
```

Historical result from the original production review: **38 tests, 425 assertions passed**, using the isolated MariaDB test database.
The existing 35-test suite passed before adding these checks. New coverage adds:

- verification expiry and idempotent reopening, including unchanged verification timestamp;
- expired and wrong-account reset tokens preserving passwords and active credentials;
- equal generic responses for existing and missing accounts on recovery/resend;
- CSRF enforcement on hosted registration, recovery, resend and password reset.

Existing tests cover reset-token single use, native/browser session revocation,
pending mobile handoff revocation, signed-link tampering and Unicode passwords.
See `backend/tests/Feature/ApiTest.php` and `SocialAuthTest.php`.

## Remaining acceptance

This review does not establish physical iOS/Android return flows, Gmail/Outlook
rendering of every auth email, accessibility or delivery under provider failure,
bounce conditions or quota exhaustion. The earlier plain SMTP message reached
the user's Gmail Inbox; these auth messages were received in the controlled
Hostinger mailbox. See [integration evidence](backend-integration.md) and
[SMTP activation](../hostinger-backend.md#smtp-activation-evidence--2026-11-01).

## Local corrections and release candidate — 2026-11-01

- Notifications capture the request language before queuing and carry validated
  native context. Signed verification emails now use `/verify-email/{id}/{hash}`;
  the existing `/api/v1/auth/verify/...` JSON contract remains supported. Reset
  links carry locale/context and keep the same token and CSRF protections.
- Browser verification/reset success, invalid/expired links, expired sessions and
  throttling render localized HTML with explicit sign-in/resend/recovery actions.
  A verification link does not sign the user in. Native continuation remains
  PKCE-bound; opening mail elsewhere explains how to resume in the app.
- Registration/login/reset validation returns visible, escaped field errors in
  the selected language. Non-secret fields and context survive, passwords do not.
  Malformed email arrays no longer crash the account rate limiter.
- Security headers cover reset/verification pages, including no-store and
  no-referrer. Provider logos stay same-origin. No credentials or external
  provider configuration changed.

Current release regression: **52 tests, 633 assertions passed** against the isolated `generosity_test` database.
Reproduction from `backend/`: `docker compose exec -T app composer test`.
`AccountEmailJourneyTest` covers both language variants, serialized queued mail
processed under an English worker using the in-memory mail transport, generated
links, expiry/tampering/reuse, malformed input, safe validation, revocation,
CSRF/throttling pages and both native PKCE continuations. Nothing is mailed
externally by these tests.

From `portal/`, with the local Docker API running at `127.0.0.1:8088`:
`npm test -- --config=playwright.auth.config.js` passes **6 browser checks** in
Chromium/WebKit. They exercise actual local PHP forms with invalid requests only,
Spanish/English recovery, Spanish registration errors, 320/390-pixel layout and
200% text, without creating accounts or sending mail. Each run uses ten mutation
requests, matching the real IP rate limit; wait at least a minute between runs or
a previous run can correctly produce 429. Do not relax production rate limits to
make this suite pass.

The ordinary `npm test` portal suite is separate and uses API fixtures: **67 passed, 1 intentional duplicate-screenshot skip** across Chromium/WebKit. OpenSpec strict validation passed all 16 items. See the
[prepared publication sequence](../releases/account-portal-2026-11-01.md).
No new production-email delivery, physical-device return or deployment is claimed.
