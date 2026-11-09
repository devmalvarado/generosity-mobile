# Center web portal

The canonical source exists in `portal/`: `index.html`, `styles.css`, `portal.js`
and `assets/`. The original PoC is preserved. The public cards describe causes;
calls to action lead to the location intake and center sign-in. The former dummy
search, donation and publication forms, fictional centers and sample figures are
removed. The authenticated workspace at `/kindness-center/` uses the PHP API.

The institute credit is a small footer line. Detailed identity/contact information
lives in the standalone ES/EN legal pages on `generosity.global`; the footer and
intake link to them in the current language. The temporary staff-handled contact
is `notificaciones@generosity.global`. The API legal URLs remain available.

## Initial location applications

The dedicated `/registrar-sede/` form allows organizations to
request one location at a time. It accepts organization/location identity,
activities, country/city and a representative, with explicit representation and
contact consent. Legal name, region, postal code, public service address, phone
and website are optional. It does not request beneficiary data or protected
addresses. The verified account supplies the contact email.

`POST /api/v1/center-applications` accepts verified accounts using the existing
session, CSRF and idempotency boundary. `GET /api/v1/me/center-applications` lists
only the current applicant's receipt summaries, ten per page. Requests remain
private and pending; submission never creates a published center or membership.
The browser preserves an uncertain submission across reloads and clears private
data on account changes. An optional existing-center id is sent only when filled
and is described as recorded only when the receipt echoes it. The applicant list
names pending, changes-requested, approved, rejected and withdrawn in Spanish and
English, shows rationale as text, and can withdraw or accept when the receipt
includes a version and the current account is the one allowed to act. It does not
upload evidence, send a notice, or state a response time or retention period. A
named recipient who is not the applicant has no tracker. Review stays unavailable
until reviewer account IDs are configured outside git. Documentary evidence and
dispute resolution remain pending. See [intake verification](testing/center-applications.md).

## Implemented flow

1. Follow the landing link to `/kindness-center/`, then open the hosted account page. Sign in with email, or a configured provider.
2. The browser uses the API's host-only HttpOnly session cookie. Requests include credentials, exact-origin CORS and CSRF for mutations; no bearer token is stored in the landing.
3. `/api/v1/staff/centers` returns only assigned centers. No membership means no editor. Membership requires an operator grant or approved review followed by acceptance from the named account; it is never inferred from a social login.
4. Select a center, browse paginated needs, create a draft, publish, edit or close a need. Spanish/English content is preserved independently of interface language; other existing translations are retained on edit.
5. Browse contributions and confirm/fulfill another person's contribution. Time completion accepts an explicit actual duration; planned duration is never silently counted as performed work.
6. Mutations carry UUID idempotency keys and version checks. The browser saves one uncertain submission in account-bound session storage before sending, and retries the exact body/key after a lost response or tab reload. Rejected changes retain the editor input. Changing accounts/signing out clears private drafts and pending browser data.

The browser is not the authority: PHP rechecks the current membership before an
idempotent replay, locks reservations and rejects cross-center writes. User text
is rendered with `textContent`. A stale version requires review and a new edit;
it is never silently merged. The workspace does not grant official verification,
process payments, show beneficiary records or pretend notification delivery works.

## Verification and limits

`portal/tests/center-workspace.spec.js` covers both browser engines, translated
edits, session/CSRF headers, pending retry after reload, version conflict, no
membership, escaped content, account changes, staff transitions and page overflow
at 320/360/390/768/1280 pixels with 200% text. Backend feature tests independently
exercise ownership, membership revocation, transactions and browser session writes.
Mocked browser tests are not a production HTTPS/CORS or live provider acceptance.

Still pending: full preview/field-specific error UX, need-mutation audit coverage,
independent real-device keyboard/screen-reader acceptance, remaining provider
cases and cross-device acceptance. Google web access and urgent-need publication
have recorded live evidence; they do not complete that broader matrix.
No foundation change is ready to archive.

## Deployment boundary

See the [prepared account/portal release](releases/account-portal-2026-11-01.md)
for artifacts, API-first sequence, prerequisites and rollback. The institute
corrected its receiving bank to BBVA; no payments are enabled here.

The selective main-branch workflow publishes only the allowlisted files in
`portal/public-files.txt` when the corresponding routes/assets reach `main`. This source change does
not deploy itself. The API sync migrations are already applied in production; do
not rerun them as a fresh migration. Publishing these portal files is a separate
landing release. Do not upload the repository or server secrets to the public web root.

The initial location intake and its additive `2026_11_01_000003` migration
were deployed on 2026-11-01 in `bd5b82e`, before the portal upload. The
unauthenticated private listing returns 401. A controlled real applicant/pilot
acceptance remains pending; no other site's files were changed.

See [integration status](testing/backend-integration.md),
[authentication](social-sign-in.md), and
[foundation tasks](../openspec/changes/define-generosity-mobile-app-foundation/tasks.md).

### Session and review release — 2026-11-02

Published the additive review migration and API routes before the three changed
portal files. No credentials or existing business rows were changed. The empty
reviewer allowlist keeps decisions disabled until an authorized reviewer is named.
At that release checkpoint production contained 157 centers, no needs and no
staff memberships. Later owner-authorized DEMO publication added a need and scoped
staff access; the reviewed Aguascalientes import brought the recorded catalog to
171 centers. See [the latest catalog checkpoint](../data/aguascalientes-directory-2026-11-02/README.md).
A real-center pilot still needs its responsible account and actual need.

Corrected `SANCTUM_STATEFUL_DOMAINS` to `generosity.global`, preserving host-only
cookies and exact credentialed CORS. Real Google web access now loads the empty
application history and no-assigned-center workspace. `php artisan auth:check-portal`
checks the configured portal against Sanctum and CORS during API deployment.
See [release and rollback](releases/critical-foundation-2026-11-02.md).

### Dedicated-route preparation — 2026-11-05

The landing now contains presentation and entry links only. Registration,
center operations and helping live at `/registrar-sede/`, `/kindness-center/`
and `/ayudar/`. The independent `/app/` tour presents 34 screens without technical
specification links or API mutations. Old fragments redirect to the corresponding
operational page, preserving their record and intended destination. Shared code
loads only the page's components; membership and CSRF checks are unchanged.
The new paths, cache versions, browser tests and static deployment inventory are
prepared locally; production verification remains pending the next portal release.
