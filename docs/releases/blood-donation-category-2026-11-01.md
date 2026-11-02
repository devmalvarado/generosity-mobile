# Blood donation classification and filtering

The 39 researched blood-service listings were originally imported only as
`health`. Native apps offered organ/tissue donation, a different category, so
that selection could not find the blood services. Discovery's shared OR-filter
was correct; the classification and category vocabulary were incomplete.

The fix adds `blood_donation` with “Donación de sangre” / “Blood donation” labels
to the shared domain and native resources. Blood services retain `health` for
broader discovery; organ/tissue donation remains separate. Both committed
catalog batches are corrected. A targeted migration adds only this category to
the 39 reviewed IDs, records prior categories in the operator audit and advances
changed center versions. It preserves all other details and linked records, is
idempotent, and rollback uses audited changed IDs so already-correct records are
not relabeled. Unknown health centers are not inferred to accept blood.

The public directory returns stored categories. Updated apps request
`/api/v1/sync/catalog?category_version=2`; omitted/1 projects blood donation to
health for existing strict native parsers. The JSON schema stays at version 1,
unknown values are still rejected, and invalid vocabulary versions return 422.
The new filter requires the updated native build; refreshing an old binary alone
does not add a new button.

## Publication and acceptance

[API publication](https://github.com/devmalvarado/generosity-mobile/actions/runs/41372997294)
succeeded from main `8d22e0b020835e5c8987418856fb5bcc21a366aa`. Independent HTTPS
verification at `2026-11-02T01:33:23.297603+00:00` confirmed:

- 157 centers remain; exactly 39 have both blood donation and health.
- Only categories, version and update timestamp changed in those 39 records.
- The other 118 public records exactly match the pre-release snapshot.
- Legacy and updated mobile snapshots both contain 157 centers; only the updated
  vocabulary exposes blood donation. Coordinates remain unchanged.
- Zero published needs and ready API health.

Verification passed: 70 shared JVM tests; 31 targeted PHP cases / 4,515 assertions
before the additional rollback-preservation case (the final category suite passed
three tests / 884 assertions); three publication guard tests;
localization and strict OpenSpec checks. Android emulator tests covered the new
filter, ES label, selection across tabs and the existing contribution flow (two
tests). The iOS 18.6 / iPhone 16 Pro UI test covered the separate filter and EN/ES
labels. An initial iOS test needed an explicit horizontal gesture before tapping
an offscreen chip; its corrected rerun passed. Main's Android Kotlin compilation
also passed after preserving its existing styling during integration.

The updated Android app was reinstalled after instrumentation, and the iOS build
remains installed in the simulator. A live iOS refresh displayed the newly
classified services with the blood-drop symbol. Native deterministic tests use
the demo fixture; positive blood matching, persisted refresh, broad-health and
OR-filter behavior are exercised in shared tests and the real published snapshot.

No store release or physical-device test was performed. Service hours, donor
eligibility and physical entrances still require confirmation with each center.
The wider category taxonomy and food rescue work remain open.
