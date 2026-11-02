# Reviewed catalog expansion publication

The owner explicitly approved publishing the 120 eligible records and excluding
the 12 held records. Publication completed on 2026-11-01 (America/Monterrey), from
main commit `a8095b04bb878bb743eeb63a32dde5e93e329486`.

- [Manual catalog publication](https://github.com/devmalvarado/generosity-mobile/actions/runs/41371875124): succeeded. Logs confirm 120 inserted, 157 total centers, 37 previous centers unchanged, zero needs and zero memberships unchanged; temporary endpoint removed and dataset private.
- [Automatic API workflow](https://github.com/devmalvarado/generosity-mobile/actions/runs/41371811049): succeeded before the manual import, with catalog import disabled.
- The source batch SHA-256 is `9342270b1409f38a18d2bf4d69885efd0eef0519567d0781b959378dbb0dcfbf`.
- [Research, coverage and exclusions](../../data/catalog-expansion-2026-11-01/README.md) retain the sources for all 132 researched records across 33 zones and four categories.

## Publication safeguards

The manual `Publish reviewed catalog expansion` workflow requires explicit
`confirm_expansion=true`. It uses the existing API FTPS secrets. Before upload it
checks all 120 IDs against the reviewed records, coordinates, public address,
contact and Maps URL, and requires imported status and empty needs. It uploads
only the dataset outside the web root and a random, token-protected, temporary
release endpoint. The server verifies the SHA-256, rejects any existing batch ID,
and writes a private backup before running the existing audited importer in a
transaction. Postconditions preserve every previous center, need count and staff
membership count. The endpoint removes itself, with FTP cleanup and an HTTP 404
check afterward. The private dataset's web paths return 403/404.

The mode-600 `before.json` backup is under the mode-700 directory
`private/.deployments/catalog-expansion-<UTC timestamp>-<random suffix>/`. It
contains the prior center rows, batch IDs, counts and source digest. The audit
operator is `github-catalog-expansion-<source SHA-256>`. There were no changes to
credentials, DNS, repository access, accounts or provider configuration.

The push also matched the existing automatic API workflow because it added a
backend test. The application runtime, schema and dependencies are unchanged;
that workflow does not import this batch. The catalog import is a separate,
explicit manual action serialized with API publication by the same concurrency
group. The landing was not changed.

## Acceptance evidence

Independent HTTPS checks completed at `2026-11-02T01:20:19.757105+00:00`:

- All seven public pagination pages contain 157 distinct center IDs.
- All imported fields in the 120 new records match the approved source, including
  full coordinate pairs, addresses, Maps URLs and contacts; all have version 1.
- The prior 37 public records exactly match the pre-import snapshot, including
  version and update timestamp.
- All 12 held IDs are absent. There are zero published needs.
- The mobile-v1 snapshot contains the same 157 IDs, coordinates and supported
  fields, with no unsupported Maps/funding metadata and no fabricated needs.
- Health reports ready and payments remain disabled.

Local checks passed: four PHP tests with 3,375 assertions in isolated
`generosity_test`, three publisher guard tests, generated PHP syntax validation
and strict OpenSpec foundation validation. The publisher guard tests also passed
on the main-based release and in GitHub Actions. Development retains its 160
centers, six needs and three memberships; those development fixtures are not
production data.

Do not rerun this publication to update records: it intentionally refuses existing
IDs. For withdrawal, review downstream references before a compensating data
operation; do not delete centers automatically after user activity may reference
them. General source/staff-edit reconciliation remains open in task 11.5.

Physical entrances, phone reachability, operating hours, admission and current
funding mix have not been confirmed with organizations. Coordinates reproduce
public Maps place pins, not a field survey. Vatican-area results retain their
actual Roma, Italia postal addresses. This data-only release did not rerun native
simulators; it verified the live mobile response contract instead.
