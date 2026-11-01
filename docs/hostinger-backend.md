# Hostinger PHP backend deployment and recovery

Prepared 2026-10-31. The API was deployed that day at `https://api.generosity.global`. Payments stay disabled, mail uses the log driver, remote MySQL stays closed, and cron is not scheduled. These instructions remain the recovery and activation procedure for that site; existing websites must keep their current document roots and data.

## Required deployment inputs

- A chosen Generosity API HTTPS hostname and its separate document root.
- PHP 8.4 for this site's web requests, with PDO MySQL, mbstring, OpenSSL, ctype, curl, DOM/XML, fileinfo, filter, hash, pcre, session and tokenizer. CLI PHP 8.4 was inspected at `/opt/alt/php84/usr/bin/php`; confirm the web runtime independently. Do not leave a public `phpinfo()` page.
- A new database and least-privilege user from hPanel, plus the actual internal host/version. The screenshots show the account database-creation facility; no Generosity database or password has been configured. Do not enable “any host” remote MySQL. Native apps connect to the HTTPS API, never directly to MySQL.
- SMTP sender/domain configuration and a real reviewed catalog. The local Mailpit addresses and demo records must not be used as production configuration.

The server's Composer 2.9.8 was previously found outdated. Build dependencies locally with the pinned supported Composer instead of invoking that server binary. If PHP 8.4 cannot be selected for a dedicated site without affecting the other sites, resolve that with Hostinger before deploying; do not change the whole plan's runtime blindly.

## Release layout

Build a release from the committed `backend/` directory with `composer install --no-dev --prefer-dist --optimize-autoloader --no-interaction`, under PHP 8.4 and the same extensions. Keep `composer.lock`. Audit the locked dependencies and run tests before producing a release. `vendor/` is a build artifact, never committed. Do not package local `.env`, tests, database dumps or development logs into the public directory.

Place application code and vendor outside `public_html`. Configure the dedicated site to serve **only `backend/public/`**. If the provider requires a fixed `public_html`, put only the contents of `public/` there and adjust the three relative paths in its `index.php` to the private application location (`storage/framework/maintenance.php`, `vendor/autoload.php`, `bootstrap/app.php`). Keep the supplied `public/.htaccess` rewrite rules. Git deployment must not expose the monorepo root.

Store production `.env` beside `artisan`, outside the document root, readable only by the account. Configure:

```dotenv
APP_ENV=production
APP_DEBUG=false
APP_URL=https://YOUR-API-HOST
APP_KEY=GENERATE-ONCE-AND-KEEP-PRIVATE
DB_CONNECTION=mysql
DB_HOST=VALUE-FROM-HPANEL
DB_DATABASE=YOUR-NEW-DATABASE
DB_USERNAME=YOUR-NEW-DATABASE-USER
DB_PASSWORD=PRIVATE-VALUE
SESSION_SECURE_COOKIE=true
SESSION_ENCRYPT=true
CACHE_STORE=database
QUEUE_CONNECTION=database
MAIL_MAILER=smtp
MAIL_HOST=YOUR-SMTP-HOST
MAIL_PORT=587
MAIL_USERNAME=PRIVATE-VALUE
MAIL_PASSWORD=PRIVATE-VALUE
MAIL_FROM_ADDRESS=YOUR-VERIFIED-SENDER
CORS_ALLOWED_ORIGINS=https://YOUR-PORTAL-HOST
LOG_CHANNEL=single
LOG_LEVEL=warning
```

Follow the SMTP provider's scheme/TLS settings; do not disable TLS verification. Preserve `APP_KEY` across releases and include it in the protected recovery plan. Framework mail jobs/reset links and database backups contain sensitive account material; do not publish them or retain them indefinitely.

Run with the verified PHP binary from the private release directory:

```sh
/opt/alt/php84/usr/bin/php artisan migrate --force
/opt/alt/php84/usr/bin/php artisan config:cache
/opt/alt/php84/usr/bin/php artisan route:cache
/opt/alt/php84/usr/bin/php artisan view:cache
```

Only `storage/` and `bootstrap/cache/` need runtime write access; do not use world-writable permissions. HTTPS is enforced in production. Configure trusted proxies only if the actual host topology requires them, never by accepting arbitrary forwarded headers. Check `/api/v1/health`, absence of public `.env`/vendor/storage files, failed authentication, CORS and one real email before enabling clients. Omit or redact request bodies, verification/reset paths and query strings in web-server access logs: provider email links carry temporary secrets. Framework redaction cannot protect external access logs.

## Bounded cron jobs

Use hPanel Cron Jobs, since shell `crontab` was not available in the account audit. Every minute, drain the database queue with the account's `flock` binary and an absolute private application path:

```sh
/usr/bin/flock -n /PRIVATE/APP/storage/queue.lock /opt/alt/php84/usr/bin/php /PRIVATE/APP/artisan queue:work database --stop-when-empty --max-time=45 --timeout=20 --tries=3
```

Also run `/opt/alt/php84/usr/bin/php /PRIVATE/APP/artisan schedule:run` every minute for expired-token and reset-token cleanup. Confirm the panel's actual minimum interval and process limits. Mail may wait until the next cron run. Monitor failed jobs privately and retry only after fixing delivery configuration. This does not implement push delivery or domain reminders. No resident daemon or supervisor is required.

## Backup, restore and rollback

1. Before migrations, create a transaction-consistent SQL dump (`mariadb-dump --single-transaction`) using a private client credentials file or an interactive password prompt, not a password in shared command history. Include the schema, all business tables, migrations, queues and operator audits. Keep `APP_KEY` and release hash in protected backup metadata.
2. Encrypt and restrict backups outside the document root; apply a retention policy. Shared weekly hosting backups alone are not proof of recovery.
3. Restore into a new isolated database with its own credentials. Compare identities, membership, needs/committed quantities, contributions/events and stored operation responses. Prevent restored queue jobs from sending real email during verification. The local `scripts/smoke.py` demonstrates this comparison without production credentials.
4. Deploy compatible code using the preserved schema. Take a maintenance window for incompatible migrations; don't run `migrate:fresh` or reset migrations against production.
5. A code rollback is safe only if the previous release supports the current schema. This first schema's `down()` is destructive. If a data restore is necessary, stop writes and workers, restore the coordinated backup and reconcile all writes after its timestamp before reopening. Never silently discard acknowledged offers.

No automatic promotion, remote DB allowlist change, plan upgrade or production email has been performed by this implementation.

References: [Laravel deployment](https://laravel.com/framework/docs/13.x/deployment), [Sanctum](https://laravel.com/framework/docs/13.x/sanctum), [Hostinger cron](https://www.hostinger.com/support/1583465-how-to-set-up-a-cron-job-at-hostinger/), [Hostinger PHP settings](https://www.hostinger.com/support/1575755-how-to-change-the-php-version-of-your-hostinger-hosting-plan/).

## Google/Apple identity activation

The intended API origin is `https://api.generosity.global`. Configure Google/Apple
only after HTTPS, the database and server-only secrets are ready. See the
[provider callbacks, session/CORS settings and acceptance checklist](social-sign-in.md).
The existing landing publish workflow does not deploy this backend.
