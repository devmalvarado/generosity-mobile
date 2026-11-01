# Initial organization/location intake

Prepared locally on 2026-11-01, on `codex/backend-php-mysql`. Not published and no
production application or database migration was performed.

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

Deploy the PHP code and new additive `create_center_applications` migration before
the three static landing files. The current selective main-branch workflow only
publishes landing files/assets; it never applies PHP migrations. Publishing this
intake has not been requested. Preserve existing production data; do not use the
migration's destructive rollback after accepting real applications.

Before accepting real applications, finalize the privacy/retention notice and the
process/person responsible for reviewing and responding. No retention duration or
review turnaround was invented. Full claim/review/approval/rejection/withdrawal,
evidence handling, duplicate disputes and invitations remain in OpenSpec tasks
2.1–2.5. Only the smaller initial-intake task 2.6 is implemented locally.

Still unverified: production browser round trip, real-device screen reader and
keyboard acceptance. No synthetic request was sent to the live API.
