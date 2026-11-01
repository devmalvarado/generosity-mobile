# Hostinger PHP backend deployment and recovery

Updated 2026-11-01. The API is published at `https://api.generosity.global`. The catalog/account sync increment and its two migrations were published with `1adf57a`; a public catalog read returned no centers. Hostinger SMTP and both minute-based cron jobs are now active; see the activation evidence below. Payments stay disabled and remote MySQL stays closed. The portal center workspace is not part of that API release. These instructions remain the recovery and activation procedure for that site; existing websites must keep their current document roots and data.

## Required deployment inputs

- A chosen Generosity API HTTPS hostname and its separate document root.
- PHP 8.4 for this site's web requests, with PDO MySQL, mbstring, OpenSSL, ctype, curl, DOM/XML, fileinfo, filter, hash, pcre, session and tokenizer. CLI PHP 8.4 was inspected at `/opt/alt/php84/usr/bin/php`; confirm the web runtime independently. Do not leave a public `phpinfo()` page.
- The existing Generosity database and least-privilege user, plus the actual internal host/version. Preserve the deployed configuration and data; provision a new database only for a separate environment or isolated restore. Do not enable “any host” remote MySQL. Native apps connect to the HTTPS API, never directly to MySQL.
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
MAIL_SCHEME=smtps
MAIL_HOST=smtp.hostinger.com
MAIL_PORT=465
MAIL_USERNAME=notificaciones@generosity.global
MAIL_PASSWORD=PRIVATE-HOSTINGER-APP-PASSWORD
MAIL_FROM_ADDRESS=notificaciones@generosity.global
MAIL_FROM_NAME=Generosity
MAIL_EHLO_DOMAIN=api.generosity.global
CORS_ALLOWED_ORIGINS=https://YOUR-PORTAL-HOST
LOG_CHANNEL=single
LOG_LEVEL=warning
```

Hostinger uses implicit TLS (`MAIL_SCHEME=smtps`) on port 465. Use a mailbox app password named `Generosity API SMTP`, not the hosting-account password. Keep `MAIL_URL` unset so it cannot override these settings. Do not disable TLS verification or fall back to the log mailer in production: a logged message has not been delivered. Preserve `APP_KEY` across releases and include it in the protected recovery plan. Framework mail jobs/reset links and database backups contain sensitive account material; do not publish them or retain them indefinitely.

Run with the verified PHP binary from the private release directory:

```sh
/opt/alt/php84/usr/bin/php artisan migrate --force
/opt/alt/php84/usr/bin/php artisan config:cache
/opt/alt/php84/usr/bin/php artisan route:cache
/opt/alt/php84/usr/bin/php artisan view:cache
```

Only `storage/` and `bootstrap/cache/` need runtime write access; do not use world-writable permissions. HTTPS is enforced in production. Configure trusted proxies only if the actual host topology requires them, never by accepting arbitrary forwarded headers. Check `/api/v1/health`, absence of public `.env`/vendor/storage files, failed authentication, CORS and one real email before enabling clients. Omit or redact request bodies, verification/reset paths and query strings in web-server access logs: provider email links carry temporary secrets. Framework redaction cannot protect external access logs.

## Bounded cron jobs

Use hPanel Cron Jobs, since shell `crontab` is unavailable in this account. Select **Custom** and schedule both commands as `* * * * *` (every minute):

```sh
/bin/sh /PRIVATE/APP/scripts/hostinger-mail-queue.sh
/bin/sh /PRIVATE/APP/scripts/hostinger-scheduler.sh
```

The deployed private application path is `/home/u643005321/domains/api.generosity.global/private`. Keep both scripts in future releases. hPanel does not accept output-redirection characters directly, so the scripts contain the commands and redirect to private `storage/logs/queue-cron.log` and `scheduler-cron.log`. They use PHP 8.4, `umask 077` and independent `flock` locks; a competing invocation exits successfully without starting a second worker. The queue drains with `--stop-when-empty --max-time=45 --timeout=20 --tries=3 --backoff=60`. The scheduler runs existing hourly/daily auth cleanup at its defined time, not every task every minute.

Mail may wait until the next cron run. Monitor mailbox quotas, failed jobs and private log growth; retry failed jobs only after fixing delivery configuration. An SMTP acceptance does not establish inbox placement. This does not implement push delivery or domain reminders. No resident daemon or supervisor is required.

## SMTP activation evidence — 2026-11-01

- hPanel activated the included Free Business Email plan for `generosity.global`: 100 mailbox slots, 1 GB per mailbox, linked to the hosting renewal shown as 2029-04-02. No paid upgrade was purchased. The mailbox count is not a sending quota.
- Created `notificaciones@generosity.global` and its separate `Generosity API SMTP` app credential. hPanel reports MX/SPF/DKIM/DMARC configured; public DNS also confirms Hostinger MX, Hostinger SPF and monitoring-only DMARC (`p=none`). No unrelated DNS records were changed.
- Authenticated from the production PHP runtime against `smtp.hostinger.com:465` with certificate verification. Updated only mail keys in the private `.env`, preserved a mode-600 pre-SMTP backup, and rebuilt Laravel's protected config cache. Credentials are not in Git or this document.
- Both cron entries are present in hPanel at one-minute intervals. A queued framework `Mailable` to the owner's approved Gmail address was automatically processed at 16:10 UTC, without manually running the worker. The queue and failed-job counts were both zero afterward. The scheduler log confirms automatic runs at 16:11 and 16:12 UTC.
- Hostinger's outgoing delivery log marks that message **Delivered** at 16:10:03 UTC (10:10:03 America/Monterrey). On 2026-11-01, the recipient explicitly confirmed that the test arrived in Gmail's **Inbox**, not Spam. This is recipient-confirmed evidence; no Gmail inbox contents were inspected by the agent.
- Public health still returns HTTP 200 / `ready`; `/.env` returns 403 and `/scripts/hostinger-mail-queue.sh` returns 404. The production environment/cache files are owner-only.
- This verifies SMTP transport and automatic processing. A subsequent [live account-email review](testing/account-email-journeys.md) verified registration, resend, verification/reset links and session revocation using the scheduled queue. It found three user-experience gaps now corrected locally; the [release candidate](releases/account-portal-2026-11-01.md) is prepared but not published. Outlook delivery, bounce handling and behavior at the account's actual sending quota remain separate acceptance checks.

## Account entry UI deployment — 2026-11-01

After explicit owner approval, published the hosted sign-in/registration view,
`BrowserAuthBoundary` CSP update and five files under public `auth-assets/`.
No migrations, account changes, environment changes or provider credentials were
included. PHP lint and Blade cache compilation passed. Public Spanish/English
registration pages return HTTP 200 with the selected form, same-origin image/font
CSP and matching image/font hashes; health remains `ready`. The iOS simulator
opens the published registration form with the provider logos.

The previous view and middleware are preserved outside the document root at
`/home/u643005321/domains/api.generosity.global/private/.deployments/auth-ui-20261101T170756Z/backup/`.
For rollback, restore these two files to their original private application paths
and rebuild the Blade cache with PHP 8.4. Evidence and test results:
[account entry visibility](social-sign-in.md#account-entry-visibility--2026-11-01).

## Backup, restore and rollback

1. Before migrations, create a transaction-consistent SQL dump (`mariadb-dump --single-transaction`) using a private client credentials file or an interactive password prompt, not a password in shared command history. Include the schema, all business tables, migrations, queues and operator audits. Keep `APP_KEY` and release hash in protected backup metadata.
2. Encrypt and restrict backups outside the document root; apply a retention policy. Shared weekly hosting backups alone are not proof of recovery.
3. Restore into a new isolated database with its own credentials. Compare identities, membership, needs/committed quantities, contributions/events and stored operation responses. Prevent restored queue jobs from sending real email during verification. The local `scripts/smoke.py` demonstrates this comparison without production credentials.
4. Deploy compatible code using the preserved schema. Take a maintenance window for incompatible migrations; don't run `migrate:fresh` or reset migrations against production.
5. A code rollback is safe only if the previous release supports the current schema. This first schema's `down()` is destructive. If a data restore is necessary, stop writes and workers, restore the coordinated backup and reconcile all writes after its timestamp before reopening. Never silently discard acknowledged offers.

The SMTP activation above was explicitly requested. It did not promote unrelated application changes, change the remote DB allowlist or upgrade the paid plan.

References: [Laravel deployment](https://laravel.com/framework/docs/13.x/deployment), [Sanctum](https://laravel.com/framework/docs/13.x/sanctum), [Hostinger SMTP](https://www.hostinger.com/support/1575756-how-to-get-email-account-configuration-details-for-hostinger-email/), [Hostinger cron](https://www.hostinger.com/support/1583465-how-to-set-up-a-cron-job-at-hostinger/), [cron special characters](https://www.hostinger.com/support/5646919-how-to-set-up-a-cron-job-with-special-characters-at-hostinger/), [Hostinger PHP settings](https://www.hostinger.com/support/1575755-how-to-change-the-php-version-of-your-hostinger-hosting-plan/).

## Google/Apple identity activation

The intended API origin is `https://api.generosity.global`. Configure Google/Apple
only after HTTPS, the database and server-only secrets are ready. See the
[provider callbacks, session/CORS settings and acceptance checklist](social-sign-in.md).
The existing landing publish workflow does not deploy this backend.
