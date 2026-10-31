# Decision: PHP/MySQL authoritative backend

Date: 2026-10-31. Status: selected and implemented locally; production deployment pending.

## Decision and preservation

The user explicitly requested committing the current state, retaining the Kotlin service on a mockup branch, and replacing that service with PHP using the already-paid Hostinger Premium database capability. The checkpoint is `36eab89`; `codex/mockup-backend-kotlin` preserves the full Kotlin prototype and its directory journal/tests. The PHP work is on `codex/backend-php-mysql`. Native/shared Kotlin remains in place; `:service` is removed only from this branch.

Use one Laravel 13 application under `backend/`, PHP 8.4 and MySQL-compatible relational storage. Local verification uses MariaDB 11.8.9, PHP 8.4.27, Composer 2.11.1 and Mailpit. Exact PHP dependencies are committed in `composer.lock`. Docker is a development tool, not a requirement on Hostinger. The production database server version is still to be verified; the previously inspected client version does not establish it.

Laravel supplies routing, request validation, password hashing, email verification/recovery, queues, database migrations, Sanctum token storage/revocation and transactions. This avoids maintaining custom authentication/cryptography or a process supervisor on shared hosting. One database owns identities, memberships, public catalog, reservations, contribution events and idempotent outcomes. Row locks and transactions make offer acceptance and cancellation atomic; deadlocks receive bounded transaction retries.

## Identity decision

The earlier unspecified external identity adapter is superseded for this implementation by Laravel's first-party database-backed user provider, framework password broker and Sanctum. This is a deliberate architecture choice under the requested PHP backend, not a claim that Firebase, Keycloak or OIDC has been configured. Password hashes remain exclusively on the server. Email verification is required before issuing usable account tokens. Recovery uses time-limited framework tokens; reset revokes existing API tokens.

Seven-day bearer sessions are the implemented API boundary. Cookie-only requests are denied; browser origins are an explicit allowlist, not authorization. Native clients must use Keychain/Keystore. The center portal must add its own reviewed session/token lifecycle before production use; cookie/BFF integration, silent renewal, external SSO and MFA have not been implemented. Operators grant or revoke staff via audited CLI and need access to the backend host; public clients have no role-management endpoint.

## Hosting fit and limits

Hostinger screenshots establish that this account can create MySQL databases/users. A prior read-only audit found PHP 8.4 CLI and required extensions. Neither proves that a selected web document root executes PHP 8.4, that a production database is configured, or that email delivery works. Verify these gates before deployment. Do not change the account-wide PHP selector without checking the other hosted sites.

Use ordinary PHP HTTP requests plus a database queue drained by short cron invocations. No JVM, Docker daemon, resident PHP worker, Node service, Redis server or new paid plan is required for this initial design. The development server is not a production web server. Notifications to devices, scheduled domain reminders and large imports/analytics remain future work; shared-hosting capacity must be measured before scale claims.

## Data and recovery

There is no automatic migration from the Kotlin prototype's local journal. Its development data remains recoverable on the checkpoint branch. PHP starts with an empty migrated schema and only explicitly imported sample data. Imports are atomic upserts that preserve reservations and publication state, not a full source-revision/deletion protocol. An isolated SQL restore is exercised by the local smoke script. See [deployment and recovery procedure](hostinger-backend.md).

## Evidence and completion boundary

See `backend/tests/Feature/ApiTest.php` and `backend/scripts/smoke.py` for executable authorization, rate-limit, Unicode, lifecycle, real MariaDB concurrency, restart and backup/restore checks. `backend/openapi.json` documents every versioned route. OpenSpec remains active: native/portal integration, provider delivery, complete source reconciliation and account synchronization are not certified by backend tests.
