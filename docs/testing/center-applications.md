# Initial organization/location intake

Initial intake was prepared on 2026-11-01 on `codex/backend-php-mysql` and
deployed that day. See the release notes below. The 2026-11-02 review routes and additive migration `2026_11_02_000002`
were subsequently deployed with the [critical-foundation release](../releases/critical-foundation-2026-11-02.md).
Review decisions remain unavailable because no reviewer IDs have been appointed.

## Contract

- Landing entry: `#registrar-sede`, available from the desktop action and mobile menu.
- One application per location. Organization/location name, activities, country,
  city, representative name/role and both declarations are required. Legal name,
  region, postal code, public service address, phone and website are optional.
- Verified Generosity email is required to submit. The server takes the contact
  email from the account; Google/Apple credentials are not required for email login.
- `POST /api/v1/center-applications` requires session/CSRF or Sanctum authentication,
  verified email and a UUID `Idempotency-Key`; limited to five attempts per minute
  per account. It returns `application` with a private pending reference.
- `GET /api/v1/me/center-applications?page=1` returns only the owner's summaries,
  paginated by ten. Private contact/form fields are not returned in that history.
- Existing transaction/operation handling and a per-account payload fingerprint
  prevent duplicate applications on retry. The fingerprint is not a cross-account
  duplicate or organization-identity decision.
- The request stores declarations and consent version `site-application-v1`, not
  documentary proof. It creates no public catalog record, staff grant or verified
  status. No automatic review message is sent.

OpenAPI describes both routes, request limits and receipt schemas in
`backend/openapi.json`.

## Reproducible checks

From `backend/`: `docker compose exec -T app composer test`.
The complete backend suite passed **44 tests / 502 assertions**, against isolated
MySQL database `generosity_test`. The new `CenterApplicationTest` verifies:

- guest/unverified denial, verified token and browser-session submission;
- required consent, invalid values and attempted authority/identity injection;
- same-key replay, conflicting reuse and identical-body deduplication;
- owner-only history, privacy and no catalog/membership side effects;
- missing browser CSRF and submission rate limits.

From `portal/`: `npm test -- --workers=2`.
The complete portal suite passed **67 tests**, with one intentional skip of the
duplicate WebKit screenshot capture. Both engines ran all seven intake scenarios.
`tests/center-applications.spec.js` exercises Chromium and WebKit with intercepted
API fixtures: guest preparation, verified-email gate, bilingual labels, required
field focus, CSRF/operation headers, server validation, lost-response reload/retry,
storage failure, safe text rendering and account-switch isolation. Layout checks
cover 320, 390, 768 and 1280 CSS pixels and 200% text. The existing portal suite
also checks account and staff flows. Browser fixtures are not proof of a deployed
HTTPS/CORS integration.

`openspec validate add-platform-expansion-features --strict`, JavaScript syntax
validation and `git diff --check` also passed. The existing representative captures
in `portal/screenshots/` were regenerated; the desktop form was inspected in the
local browser. These checks do not replace real-device accessibility acceptance.

## Remaining release and workflow work

### Location intake update — 2026-11-01

Address is now visible beside the optional Google Maps URL and coordinate pair.
Unknown/protected location values can remain absent. Both coordinates are required
when either is supplied, with numeric latitude -90..90 and longitude -180..180.
The API validates HTTPS Maps hosts/paths without fetching or resolving submitted
URLs. No geocoding or short-link expansion is performed. Accepted values remain
private JSON details and do not grant public listing or verification rights.
Blank new fields are omitted from canonicalization so pre-update pending requests
retain their operation hashes and duplicate fingerprints. Numeric representations
of the same pair are normalized without rounding geographic precision.

`CenterApplicationTest` covers persistence, canonical retries, old hashes, range,
pair and host/credential rejection. `center-applications.spec.js` covers visible
fields, client validation, exact decimal payloads, language changes and the
existing 320/390/768/1280px, 200%-text checks in Chromium and WebKit. The location
update passed 16 browser checks on both the working and release branches and the
targeted backend suite. No additional database migration was needed for these
JSON details. The published form was visually inspected; its bytes match the
release. No new authenticated production application was submitted.

### Release procedure

The original intake and its additive migration were published with the
[account/portal release](../releases/account-portal-2026-11-01.md). The location
field increment was published backend-first on 2026-11-01; see the
[initial catalog release](../releases/initial-catalog-2026-11-01.md).
No new migration is needed. The selective landing workflow does not deploy PHP.
Preserve accepted applications; do not roll back their table destructively.

Before accepting real applications, finalize the privacy/retention notice and the
process/person responsible for reviewing and responding. No retention duration or
review turnaround was invented.

### Review routes — 2026-11-02

`POST /api/v1/center-applications` accepts optional `claimed_center_id`. The
center must already exist and is not modified. Submission still creates no
membership and no verification.

`GET /api/v1/center-applications/{application}` is limited to reviewer account
IDs in `CENTER_APPLICATION_REVIEWER_IDS`. An empty value returns 503
`review_unavailable` and does not reveal the application. The applicant cannot
read or decide their own case. The payload omits reviewer identity and other
applicants' evidence.

`POST /api/v1/center-applications/{application}/decision` records
`request_changes`, `reject` or `approve` with rationale. Approval must name an
existing center, a verified account and the staff role. It does not create
membership, publish a center, or grant verification, tax eligibility or provider
onboarding. Staff of the named center cannot decide it.

`POST /api/v1/center-applications/{application}/acceptance` is only for the
named account. Acceptance creates that staff membership and does not change the
center. `POST /api/v1/center-applications/{application}/withdrawal` is only for
the applicant, retains the application and decision rows, and does not revoke an
accepted membership. No review email is sent. No retention period is selected,
so these rows are not purged. Do not drop `center_application_decisions` in
production.

Evidence uploads, dispute merges and staff invitations remain open in tasks
2.1–2.5. The landing tracker, published on 2026-11-02, names the five states
in Spanish and English, renders rationale as text, and calls withdrawal or
acceptance only when the receipt has a version and the signed-in account is
allowed to act. It does not upload evidence, send a notice, or state a retention
period. A named recipient who is not the applicant still has no list. Browser
checks fixture the API; they are not a production round trip. Reviewer IDs were
not selected, so production decisions stay unavailable until that configuration
exists outside git. The Node contract branch does not include these routes.

Current regression: **77 backend tests / 5,194 assertions** and **77 portal tests
passed**, with one intentional duplicate WebKit screenshot skip. Production Google
session reads now work; no synthetic application was submitted. The real pilot
submission/decision/acceptance round trip, screen reader and keyboard acceptance
remain unverified.
