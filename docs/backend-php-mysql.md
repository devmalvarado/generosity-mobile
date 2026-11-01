# Decision: PHP/MySQL authoritative backend

Date: 2026-10-31. Status: selected, implemented, and published at `https://api.generosity.global`. SMTP delivery, payment processing, and Google/Apple credentials remain unconfigured.

## Decision and preservation

The user explicitly requested committing the current state, retaining the Kotlin service on a mockup branch, and replacing that service with PHP using the already-paid Hostinger Premium database capability. The checkpoint is `36eab89`; `codex/mockup-backend-kotlin` preserves the full Kotlin prototype and its directory journal/tests. The PHP work is on `codex/backend-php-mysql`. Native/shared Kotlin remains in place; `:service` is removed only from this branch.

Use one Laravel 13 application under `backend/`, PHP 8.4 and MySQL-compatible relational storage. Local verification uses MariaDB 11.8.9, PHP 8.4.27, Composer 2.11.1 and Mailpit. Exact PHP dependencies are committed in `composer.lock`. Docker is a development tool, not a requirement on Hostinger. The production database server version is still to be verified; the previously inspected client version does not establish it.

Laravel supplies routing, request validation, password hashing, email verification/recovery, queues, database migrations, Sanctum token storage/revocation and transactions. This avoids maintaining custom authentication/cryptography or a process supervisor on shared hosting. One database owns identities, memberships, public catalog, reservations, contribution events and idempotent outcomes. Row locks and transactions make offer acceptance and cancellation atomic; deadlocks receive bounded transaction retries.

## Identity decision

Laravel's database-backed account provider, password broker and Sanctum remain the account/session authority. Google and Apple OIDC sign-in now feed the same account store through a shared hosted authorization-code flow. This is exclusively for Generosity web/Android/iOS, not federation for other institute platforms. Provider credentials and live acceptance remain pending. See [social sign-in configuration and tests](social-sign-in.md).

Native clients use seven-day bearer tokens in Keychain/Keystore; browser clients use CSRF-protected HttpOnly sessions from explicitly configured first-party origins. Password reset revokes both session types and pending native handoffs. Operators still grant/revoke staff via audited CLI; public clients have no role-management endpoint. Silent renewal, MFA and account deletion/provider revocation remain unfinished.

## Hosting fit and limits

The dedicated API site now serves PHP 8.4 and a local database. Email delivery is not enabled: production mail uses the log driver. Do not change the account-wide PHP selector without checking the other hosted sites.

Use ordinary PHP HTTP requests plus a database queue drained by short cron invocations. No JVM, Docker daemon, resident PHP worker, Node service, Redis server or new paid plan is required for this initial design. The development server is not a production web server. Notifications to devices, scheduled domain reminders and large imports/analytics remain future work; shared-hosting capacity must be measured before scale claims.

## Data and recovery

There is no automatic migration from the Kotlin prototype's local journal. Its development data remains recoverable on the checkpoint branch. PHP starts with an empty migrated schema and only explicitly imported sample data. Imports are atomic upserts that preserve reservations and publication state, not a full source-revision/deletion protocol. An isolated SQL restore is exercised by the local smoke script. See [deployment and recovery procedure](hostinger-backend.md).

## Evidence and completion boundary

See `backend/tests/Feature/ApiTest.php` and `backend/scripts/smoke.py` for executable authorization, rate-limit, Unicode, lifecycle, real MariaDB concurrency, restart and backup/restore checks. `backend/openapi.json` documents every versioned route. OpenSpec remains active: native/portal integration, provider delivery, complete source reconciliation and account synchronization are not certified by backend tests.
