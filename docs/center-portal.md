# Center web portal

The canonical source exists in `portal/`: `index.html`, `styles.css`, `portal.js`
and `assets/`. The original PoC is preserved. The public cards describe causes;
calls to action lead to the location intake and center sign-in. The former dummy
search, donation and publication forms, fictional centers and sample figures are
removed. The authenticated workspace at `#center-workspace` uses the PHP API.

`#contacto` identifies the institute, RFC and fiscal address and names support,
legal and privacy requests handled by institute staff at the owner-approved temporary
contact `notificaciones@generosity.global`. Separate mailbox creation remains
deferred. Intake and footer links point to the full ES/EN backend legal pages.

## Initial location applications

The landing's `#registrar-sede` form is prepared locally for organizations to
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
data on account changes. Existing-center claims, documentary evidence, independent
review, approval and withdrawal are still pending. See
[intake verification](testing/center-applications.md).

## Implemented flow

1. Open the hosted account page from the landing. Sign in with email, or a configured provider.
2. The browser uses the API's host-only HttpOnly session cookie. Requests include credentials, exact-origin CORS and CSRF for mutations; no bearer token is stored in the landing.
3. `/api/v1/staff/centers` returns only assigned centers. No membership means no editor. Membership is provisioned by the existing operator command, never inferred from a social login.
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

Still pending: full preview/field-specific error UX, audit/urgent-event integration,
independent real-device keyboard/screen-reader acceptance, live identity and
cross-device acceptance. No foundation change is ready to archive.

## Deployment boundary

See the [prepared account/portal release](releases/account-portal-2026-11-01.md)
for artifacts, API-first sequence, prerequisites and rollback. The institute
corrected its receiving bank to BBVA; no payments are enabled here.

The selective main-branch workflow publishes only `index.html`, `styles.css`,
`portal.js` and `assets/` when those files reach `main`. This source change does
not deploy itself. The API sync migrations are already applied in production; do
not rerun them as a fresh migration. Publishing these portal files is a separate
landing release. Do not upload the repository or server secrets to the public web root.

The new location intake is not deployed. Its PHP controller, routes and
`2026_11_01_000003_create_center_applications.php` migration must be released
before the landing advertises submission. No other site's files are involved.

See [integration status](testing/backend-integration.md),
[authentication](social-sign-in.md), and
[foundation tasks](../openspec/changes/define-generosity-mobile-app-foundation/tasks.md).
