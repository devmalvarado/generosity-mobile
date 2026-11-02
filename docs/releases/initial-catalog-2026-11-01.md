# Initial catalog and location intake publication

The owner explicitly approved publishing the reviewed catalog and landing form.
Released from main commit `8213725` on 2026-11-01 (America/Monterrey):

- [Explicit API release and import](https://github.com/devmalvarado/generosity-mobile/actions/runs/41367588487): succeeded, source `2b289a5`.
- [Portal publication](https://github.com/devmalvarado/generosity-mobile/actions/runs/41367713476): succeeded, source `8213725`.
- [Automatic API publication on main](https://github.com/devmalvarado/generosity-mobile/actions/runs/41367713484): succeeded; import disabled on push, so data was not loaded again.

The [research inventory](../../data/initial-catalog/README.md) retains 40 places.
Production contains 37 imported listings: 10 children's homes, 8 blood banks
(existing health category), 10 older-adult homes and 9 community kitchens. Three
location discrepancies remain held. Public requests verified every ID, full
coordinate pair, Google Maps URL and contact against the committed batch. There
are zero published needs in this initial production catalog and no fabricated
verification, staff membership or availability. The mobile-v1 snapshot omits
backend-only Maps URL metadata, retaining coordinate-based native map behavior.

The location form now exposes public address, Maps link and both coordinates,
with Spanish/English labels, pair/range validation and protected-location guidance.
Applications remain private, pending, verified-account-only and idempotent; blank
new fields preserve pre-update operation hashes. No schema migration was needed.
The landing preserves the current layout, example needs, wordmark and separate
legal pages. The script query version changed to avoid stale browser caches.

## Publication safeguards

`Publish API` has an explicit `import_initial_catalog` workflow-dispatch input,
default false. Ordinary main pushes never import the dataset. Before upload the
script requires exactly 37 distinct imported/no-needs records, places the dataset
outside `public_html` and records its SHA-256. The protected release step checks
the digest and refuses any existing batch ID before writing. It records batch IDs,
prior target rows (empty for this new batch), pre-import center/need counts and the
source digest in a mode-600 `before.json` inside a mode-700 directory beneath
`private/.deployments/initial-catalog-*`. Import and postconditions execute in a
database transaction, with an operator audit. It preserves unrelated records and
refuses an uncertain repeat instead of overwriting existing centers.

The deployment used the existing GitHub workflow from the authorized browser
session; no SSH credential, repository permission, DNS or provider account was
changed. API code shipped before the form. No account, email, contribution or
center membership was created during live acceptance.

For rollback, retain accepted applications and audit/history. The ordinary
importer is an upsert, not a removal operation. Do not automatically delete batch
IDs once production activity may reference them; review withdrawal and references
before any compensating data operation. This release does not finish general
source/staff-edit reconciliation or the full directory profile spec.

## Evidence and remaining checks

34 targeted PHP tests passed (690 assertions before the additional legacy-retry
assertion); the final intake/import rerun passed 9 tests / 436 assertions. Sixteen
Chromium/WebKit form cases passed again against the release based on main,
including 320–1280px, 200% text and ES/EN. Two publication-script tests and generated
PHP syntax validation passed. Both modified OpenSpec changes passed strict
validation.

Production health reports ready; the private dataset URLs return 404 and `.env`
returns 403. Published HTML, JS, CSS and four legal pages match source hashes. A
browser inspection confirms the new location fields and preserved legal links.
No new authenticated production application was submitted. Physical entrance
accuracy, phone reachability and current admission/fees require confirmation with
the organizations; Google pins are not field-surveyed coordinates.
