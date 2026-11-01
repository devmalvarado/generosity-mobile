# Account fixes and center portal release candidate

Prepared 2026-11-01 on `codex/backend-php-mysql`. This is a local release
candidate, not a production deployment or a completed public launch. No catalog
entries, accounts or bank connections were created in production.

## Release contents

- Spanish/English queued verification and recovery mail; signed browser
  verification, localized reset/error/success pages and explicit next actions.
- Visible registration/login/reset validation without retaining passwords,
  existing API JSON compatibility, session revocation and native PKCE context.
- Initial private location applications and the existing authorized-center
  workspace: assigned-center selection, need editing/publication and contribution
  confirmation/fulfillment.
- Public ES/EN landing copy with institute identity, RFC, fiscal address and
  support/legal/privacy contacts. Editorial cause cards replace fictional center
  data; working intake/access links replace disconnected demonstration forms.
  The owner-approved temporary contact is notificaciones@generosity.global.
  ES/EN full terms/privacy are served by the API and linked before registration.
- Additive migrations `2026_11_01_000003` (location applications) and
  `2026_11_01_000004` (adult-policy receipts and permanent access block).
- New password/social accounts require explicit adult declaration and current
  terms/privacy acknowledgement. Operator-assisted deactivation revokes access
  atomically without claiming erasure or complete store deletion.
- Preserved main-branch deployment workflows, with route/view cache refresh and
  public auth assets added to API deployment. The portal checks API prerequisites
  before uploading its six existing allowlisted files.

The institute's bank is **BBVA**, corrected by the owner. Payment specs and legal
planning notes now say BBVA. Payments, deductible receipts and bank integration
remain disabled/unconfigured.

## Release source

Previously generated ignored ZIPs in `build/release-prep/2026-11-01/` are superseded
by the committed source and GitHub workflow build. Do not deploy those older ZIPs.
The API workflow installs locked dependencies; portal publication stages exactly
six allowlisted public files. Neither release uploads private configuration.

## Local verification

- Backend: 57 tests / 710 assertions plus the expanded six-case account-policy suite (81 assertions), isolated `generosity_test` database.
- Portal: 67 browser cases passed; one duplicate screenshot case intentionally
  skipped in WebKit. Auth forms: ten additional Chromium/WebKit cases passed.
- OpenSpec: all 16 items passed strict validation.
- Laravel routes and Blade templates cache successfully. The route-cache check
  used a temporary cache path and removed it afterward.
- Workflow YAML and deployment Python parse. Offline assertions checked private
  archive exclusions, private bootstrap paths, pending-only migrations, route/view
  cache commands and all five auth asset uploads with new/existing directories.
  The FTPS transport was simulated; this does not validate server permissions.

## Publication order

1. Complete the legal/operator inputs below. Back up production database,
   application files and private configuration using the existing recovery
   procedure; keep backups outside `public_html`.
2. Publish the API first using the prepared `Publish API` workflow. Preserve
   `.env`, `APP_KEY`, SMTP and both cron scripts. Build dependencies with PHP 8.4,
   apply pending migrations only, refresh config/routes/views and upload the five
   `auth-assets` files. No `migrate:fresh`, sample seeder or catalog import.
3. Check health, localized registration/reset pages, auth assets and signed email
   journeys. An unauthenticated `/api/v1/me/center-applications` must return 401,
   not 404. Verify existing sessions/data and the queue scheduler survive.
4. Publish the six portal files. If workflows start together, the portal waits for
   the intake route, new Spanish reset UI and current privacy version. Missing prerequisites stop it before
   upload; after fixing/releasing the API, rerun the portal workflow. Its preflight
   is read-only and sends no email or donation.
5. Check a controlled real signup → verification → web sign-in → location-request
   receipt, then recovery → new sign-in. An account without a center must see no
   editor. With an assigned pilot account and reviewed catalog data, check an
   actual center need/offer lifecycle.
6. Repeat approved mail links and return-to-app flows on physical Android/iOS.
   Record task 7.18 evidence. Recheck production HTTPS, CSRF/CORS, revocation,
   private request isolation, pending retry and mobile layout.

The public upload allowlist and no-clean-slate behavior are preserved. Preparation
changes no domain, DNS or other website. The branch includes the relevant existing
`origin/main` workflows; preserve their histories when integrating the changes.

## Inputs before accepting public location requests

- The owner supplied fiscal identity/address, approved adult-only accounts and
  temporary use of the working notificaciones@generosity.global mailbox. Institute
  staff handle legal/privacy/support requests. Separate mailboxes remain deferred;
  independent legal review and full store deletion remain separate follow-up work.
- A named operator and a process to review/respond to location requests. This
  release stores private pending requests; approval/rejection/withdrawal and
  automatic center/staff provisioning are not implemented.
- Reviewed catalog entries and authorized staff for operational pilot centers.
  An empty catalog is honest; do not publish examples to manufacture a working
  pilot. Intake can be tested without a full catalog.

Google/Apple credentials remain absent; email/password is the available entry.
The dedicated `/kindness-center` route remains task 16.13; the prepared workspace
uses `#acceso`/`#center-workspace`. Account deletion implementation and full store
publication are separate gates, not completed by these registration fixes.

## Rollback and verification boundaries

Restore previous compatible API/portal files and rebuild caches for code rollback.
Retain the additive intake table and accepted requests; do not run its destructive
`down()` after submissions. Preserve mail jobs and users; coordinated data restores
must follow the established recovery procedure.

Backend regression uses the isolated `generosity_test` database. Portal browser
checks use fixtures; auth browser checks submit only invalid forms to local Docker.
Serialized mail tests use an in-memory transport. These checks do not prove a new
Hostinger deployment, inbox delivery or physical-device acceptance. See
[email cases](../testing/account-email-journeys.md),
[intake cases](../testing/center-applications.md) and
[hosting recovery](../hostinger-backend.md).
