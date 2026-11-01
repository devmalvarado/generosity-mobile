# PHP backend verification — 2026-10-31

Historical PHP replacement checkpoint; newer client integration evidence is in
[backend-integration.md](backend-integration.md).

Branch: `codex/backend-php-mysql`. Kotlin checkpoint: `36eab89` on `codex/mockup-backend-kotlin`.

## Environment

- PHP 8.4.27 CLI in Docker; Laravel 13.39.0, Sanctum 4.3.3, PHPUnit 12.5.42.
- MariaDB 11.8.9 with InnoDB transactions/row locks; isolated `generosity_test` database.
- Composer 2.11.1; PHP dependencies fixed by `backend/composer.lock`.
- Local Mailpit, no external email or production credentials.

## Results

- `composer test`: **18 tests, 182 assertions passed**. Includes real independent-process last-unit offer and cancellation/fulfillment races, cross-user/cross-center denial, revoked-role replay denial, terminal states, exact idempotent replay, capacity release, personal provenance, expiry, invalid import rollback, password verification/reset/revocation, Unicode byte limits, rate limiting and redacted error logging.
- `python3 scripts/smoke.py`: passed using actual HTTP routes. Registered a labeled local account, delivered its verification through the database queue to Mailpit, logged in, created/reserved a bounded goods need and completed personal help. Restarted the API, replayed an acknowledged operation, verified current state and compared a SQL dump with its restore into a new temporary database. Identities/tokens, staff membership, reservations, history/events and operation outcomes were preserved. Only the temporary restore database was deleted; labeled fixtures remain locally.
- OpenAPI coverage: all implemented versioned routes and required core need response fields checked. This is not native/portal contract acceptance or exhaustive JSON Schema validation.
- Composer audit: no known vulnerability advisories found at the time checked. Composer manifest validation passed.
- Container build with pinned PHP patch, route cache and Blade view cache passed.
- `:shared:jvmTest --offline`: **57 tests passed**, with no failures/errors/skips after removing the standalone JVM service module.
- Strict OpenSpec validation: **14 items passed**, no failures. Foundation remains active.

## Not verified or delivered

Production Hostinger web PHP/runtime configuration, actual database server version/credentials, SMTP sender delivery, deployment, real-user load, native/portal HTTP integration, refresh/session UX, end-to-end client contract tests, sync cursors/tombstones, push, reminders and payments. No mobile UI code changed, and emulator/device UI suites were not repeated for this backend-only replacement. The local built-in PHP server is not a production deployment.
