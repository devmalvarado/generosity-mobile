# Decision: PHP/MySQL authoritative backend

Reconciled: 2026-11-08. PHP/MySQL is published at `https://api.generosity.global`,
including catalog/account synchronization. The dedicated portal routes were
published on November 5; their remaining authenticated-write gates are recorded
in the [portal release](releases/portal-map-2026-11-05.md). Hostinger SMTP and the
queue/scheduler cron are active. Google production sign-in has web/physical-iOS
evidence; Apple and payments remain inactive. The isolated preview API/portal,
database and feature migrations were deployed on November 6. Preview mail is
isolated and push is off. See [release channels](release-channels.md) for current
environment/provider differences; preview deployment is not production rollout.

## Decision and preservation

The user explicitly requested committing the current state, retaining the Kotlin service on a mockup branch, and replacing that service with PHP using the already-paid Hostinger Premium database capability. The checkpoint is `36eab89`; `codex/mockup-backend-kotlin` preserves the full Kotlin prototype and its directory journal/tests. The PHP work is on `codex/backend-php-mysql`. Native/shared Kotlin remains in place; `:service` is removed only from this branch.

Use one Laravel 13 application under `backend/`, PHP 8.4 and MySQL-compatible relational storage. Local verification uses MariaDB 11.8.9, PHP 8.4.27, Composer 2.11.1 and Mailpit. Exact PHP dependencies are committed in `composer.lock`. Docker is a development tool, not a requirement on Hostinger. The production database server version is still to be verified; the previously inspected client version does not establish it.

Laravel supplies routing, request validation, password hashing, email verification/recovery, queues, database migrations, Sanctum token storage/revocation and transactions. This avoids maintaining custom authentication/cryptography or a process supervisor on shared hosting. One database owns identities, memberships, public catalog, reservations, contribution events and idempotent outcomes. Row locks and transactions make offer acceptance and cancellation atomic; deadlocks receive bounded transaction retries.

## Identity decision

Laravel's database-backed account provider, password broker and Sanctum remain the account/session authority. Google and Apple OIDC sign-in use the same account store through a shared hosted authorization-code flow. This is exclusively for Generosity web/Android/iOS, not federation for other institute platforms. Production Google is configured in Testing mode with web and physical-iOS checkpoints; remaining provider cases, Android acceptance and Apple activation stay open. Preview OAuth is separate and unconfigured. See [social sign-in configuration and tests](social-sign-in.md).

Native clients use seven-day bearer tokens in Keychain/Keystore; browser clients use CSRF-protected HttpOnly sessions from explicitly configured first-party origins. Password reset revokes both session types and pending native handoffs. Operators still grant/revoke staff via audited CLI; public clients have no role-management endpoint. Silent renewal, MFA and account deletion/provider revocation remain unfinished.

## Hosting fit and limits

The dedicated API site serves PHP 8.4 and a local database. Production mail uses Hostinger SMTP, not the log driver. The queue and scheduler cron jobs are active. [Live registration, resend and password-reset checks](testing/account-email-journeys.md) and the subsequent localized completion/error fixes have evidence; physical mail-app-to-native completion remains open in foundation task 7.18b. Do not change the account-wide PHP selector without checking the other hosted sites.

Use ordinary PHP HTTP requests plus a database queue drained by short cron invocations. No JVM, Docker daemon, resident PHP worker, Node service, Redis server or new paid plan is required for this initial design. The development server is not a production web server. Urgent FCM delivery has Android emulator evidence; physical delivery, iOS APNs, nearby alerts and scheduled domain reminders remain open. Shared-hosting capacity must be measured before scale claims.

## Data and recovery

There is no automatic migration from the Kotlin prototype's local journal. Its development data remains recoverable on the checkpoint branch. Subsequent reviewed imports populated the public PHP catalog. The November 6 copy verified 171 production centers and merged their public data into preview, preserving its test records for a total of 172 centers and three needs. Production accounts/private activity were not copied. Imports are atomic upserts that preserve reservations and publication state, not a full source-revision/deletion protocol. An isolated SQL restore is exercised by the local smoke script. See [deployment and recovery procedure](hostinger-backend.md) and [catalog copy evidence](testing/release-controls.md).

## Evidence and completion boundary

See `backend/tests/Feature/ApiTest.php` and `backend/scripts/smoke.py` for executable authorization, rate-limit, Unicode, lifecycle, real MariaDB concurrency, restart and backup/restore checks. `backend/openapi.json` documents every versioned route. The [client integration verification](testing/backend-integration.md) records protected native cache/outbox, forms/history and the authenticated center workspace. OpenSpec remains active: live cross-system acceptance, provider delivery and cursor/tombstone reconciliation are still pending.
