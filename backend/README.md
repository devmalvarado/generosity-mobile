# Generosity PHP backend

One Laravel 13 / PHP 8.4 HTTP API with MySQL/MariaDB storage. The native apps remain Kotlin Multiplatform + SwiftUI/Compose; replacing the service does not replace the mobile apps. See [architecture decision](../docs/backend-php-mysql.md), [OpenAPI 3.1 contract](openapi.json) and [Hostinger deployment](../docs/hostinger-backend.md).

## Local startup

Requires Docker Desktop/Compose. Run these commands from `backend/`:

```sh
cp .env.example .env
docker compose build app
docker compose run --rm --no-deps app composer install --no-interaction
docker compose run --rm --no-deps app php artisan key:generate
docker compose up -d
docker compose exec app php artisan migrate --force
# Explicit demonstration data; never a production source.
docker compose exec app php artisan catalog:import /catalog/sample-kindness-centers.json --operator=local-development
```

API: http://localhost:8088/api/v1/health. Local email inbox: http://localhost:8028.
Both ports bind only to loopback. The database has no host port. Compose passwords are local fixtures, not production credentials. The API starts empty; there are no seeded login identities. `docker compose down` preserves its database volume; `down -v` deletes local data.

Register through `POST /api/v1/auth/register`, then deliver the queued verification email:

```sh
docker compose exec app php artisan queue:work --stop-when-empty --max-time=45 --tries=3
```

Open the verification link from Mailpit and log in. API tokens expire after seven days; log in again on expiry. Logout revokes the current token; password reset revokes all account tokens, web sessions and pending handoffs. Registration and recovery return generic accepted responses. An accepted response means a request was queued, not that an email has reached an inbox.

## API behavior

- Public paginated centers and published needs use localized language-code dictionaries; all data is UTF-8. Page size is 25.
- Need `status` describes publication (`draft`, `published`, `closed`). A published need can be expired or fully reserved; clients derive availability from its inclusive UTC expiry date and quantity. Native DTO adaptation is still required.
- Native protected requests require an email-verified account and `Authorization: Bearer ...`. Configured first-party Generosity origins may alternatively use a CSRF-protected HttpOnly session. Set exact `CORS_ALLOWED_ORIGINS` and `SANCTUM_STATEFUL_DOMAINS`; never use wildcard origins or persist bearer tokens in browser storage. See [Google/Apple entry and session configuration](../docs/social-sign-in.md).
- Goods/time offers accept user-entered quantities, activity, minutes, availability and notes. Money is rejected; its presence in catalog help methods does not enable payments.
- Every contribution write and staff need write requires a UUID `Idempotency-Key`. Keys are scoped to the account, retained durably, and cannot be reused with different validated content/path/method. A retry replays the original successful response after checking current permission, even if later transitions changed the record. Fetch current state separately.
- Center offers go `offered → confirmed → fulfilled`. Only another verified account assigned to that center may confirm/fulfill; donors may cancel offered/confirmed records. Terminal states cannot change. Transitions require the current `version`.
- Goods reservations are transactional. Offered, confirmed and fulfilled goods consume capacity; cancellation releases once. Closed/expired needs do not reopen on cancellation. Changing units with outstanding commitments is rejected.
- Personal help has `self_reported` provenance, can be completed only by its owner and cannot be reassigned to a center. Impact separates self-reported and center-offer records; goods with different units are not summed.
- Errors use `error.code` and `error.details`; expected errors are not logged. Unexpected diagnostics contain only exception type, not SQL, request content or credentials. Development limits are 120 API requests/minute/IP, with tighter account/IP limits for authentication; production tuning requires measurements.

## Operators and catalog imports

Only a trusted shell operator can provision center staff; users cannot self-assign. The operator label and process file-owner UID are audited, and membership does not change official verification.

```sh
docker compose exec app php artisan center:staff center-id staff@example.org --operator=operator-name
docker compose exec app php artisan center:staff center-id staff@example.org --revoke --operator=operator-name
docker compose exec app php artisan catalog:import /catalog/reviewed.json --operator=operator-name
```

Imports accept the documented schemaVersion 1 public catalog fields and supported shared category codes; malformed/duplicate IDs, cross-center need reassignment and nonzero imported commitments are rejected. Commitments must originate in accepted API offers. Entire imports are transactional. Existing commitments and staff publication states survive updates; missing records are retained. This is an **upsert**, not a deletion/synchronization mechanism. A reviewed import can overwrite descriptive fields; source ownership, stale-source checks and tombstones remain pending. The fixture's verification labels are demonstration data only.

## Verification

Create the isolated test database once (these credentials are local Compose fixtures):

```sh
docker compose exec db mariadb -uroot -plocal-test-root-only -e "CREATE DATABASE IF NOT EXISTS generosity_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; GRANT ALL ON generosity_test.* TO 'generosity'@'%';"
docker compose exec app composer test
docker compose exec app vendor/bin/pint --test
docker compose exec app composer audit --locked
python3 scripts/smoke.py
```

PHPUnit refuses to run migrations unless the selected database is `generosity_test`. Tests use real MariaDB, including separate PHP processes racing for the last unit and racing cancellation against fulfillment. No SQLite substitution is used. OpenAPI route coverage and core response fields are checked; client contract tests remain open.

The smoke script is fixed to local Compose/loopback. It adds clearly labeled development accounts, staff membership, needs and contributions; sends mail only to Mailpit; restarts the API; verifies replay and durable reservations; dumps and restores business tables into a newly named temporary database; and compares the restored bytes before deleting only that temporary database. It must not be adapted to production as a destructive test.

## Remaining scope

The API is published at https://api.generosity.global and the portal is published from `main`. Google and Apple stay disabled until server configuration exists. Mail uses the log driver, so registration mail is not delivered. Payments stay disabled. Catalog and contribution client adapters, incremental sync, profile updates, visits, reminders, device registration and push delivery are not in the published slice. The password-reset page is a minimal English operational page; the hosted sign-in page and native/portal account controls have Spanish/English resources.
