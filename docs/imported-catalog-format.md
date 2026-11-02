# Imported Kindness Center Catalog Format

The mobile foundation consumes schema version 1 through the shared repository,
with backend refresh and a validated bundled fallback. The bundled document is
the only labeled demonstration fixture. Real initial data is imported into the
backend separately, as documented below.
`SampleImportedCatalog` reads that same document. It is not a
published catalog and does not establish that any center is a verified real
organization. `data/sample-kindness-centers.json` is a separate labeled sample
file, not the document the apps load.

## Format

### Initial real-directory batch and backend source metadata

The [initial researched catalog](../data/initial-catalog/README.md) contains 40
public-source candidates and a 37-center backend import. Three location
discrepancies remain in its research inventory only. All imported records have
`verificationStatus: imported` and empty needs; source listings do not establish
official verification, free services or admission availability.

The [33-zone expansion](../data/catalog-expansion-2026-11-01/README.md) adds a
separate 132-record research inventory and a reviewed 120-center batch, with
12 exclusions. It covers Mexico, the United States and European search areas,
including explicit Rome alternatives for Vatican searches. Stable IDs derive
from observed Google feature IDs; missing place IDs are not fabricated. Funding
origin, service distinctions and sources stay in the research inventory until
the full directory contract is implemented. The batch retains the same import
schema; public office and food-pantry descriptions do
not promise residential admission or prepared meals.

The backend importer accepts optional `googleMapsUrl` metadata alongside the
mobile-v1 fields. It must be an HTTPS Google Maps place/share URL (google.com/maps,
www.google.com/maps, maps.google.com/maps, maps.app.goo.gl or goo.gl/maps), without
embedded credentials. The backend stores it in center details and returns it
through `/api/v1/centers`. `/api/v1/sync/catalog` intentionally omits this backend
extension: existing mobile parsers reject unknown fields. The batch file is for
the backend importer, not the bundled mobile reader. This keeps provider-specific
source metadata out of the native business/map boundary.

A contactless backend import is allowed only for an imported discovery-only
listing with this Maps reference and zero needs. A stronger verification status
or any need still requires public contact. Missing fields are not synthesized.
Coordinates retain the source place-pin precision and do not prove that an
entrance was checked in person. Research notes and secondary-source schedules
stay in the separate inventory; they are not published as current intake rules.

### Mobile schema

Schema version 1 is a JSON object with integer `schemaVersion` equal to `1`
and a top-level `centers` array. Any other version, a missing version, or an
unknown property is rejected so a later directory contract cannot be partially
applied. App-controlled labels such as categories, help methods, verification
status, and notification types use stable codes from the shared Kotlin domain
model. Human-readable center-managed content uses localized objects keyed by
language code.

```json
{
  "schemaVersion": 1,
  "centers": [
    {
      "id": "kc-children-001",
      "name": {
        "en": "Bright Futures Home",
        "es": "Hogar Futuros Brillantes"
      },
      "description": {
        "en": "Support center for children and families.",
        "es": "Centro de apoyo para ninos y familias."
      },
      "categories": ["children_support", "education"],
      "coordinates": {
        "latitude": 19.4326,
        "longitude": -99.1332
      },
      "address": {
        "en": "Historic Center, Mexico City",
        "es": "Centro Historico, Ciudad de Mexico"
      },
      "contact": {
        "phone": "+52 55 0000 0001",
        "email": null,
        "website": "https://example.org/bright-futures"
      },
      "verificationStatus": "imported",
      "needs": [
        {
          "id": "need-school-supplies",
          "title": {
            "en": "School supplies",
            "es": "Utiles escolares"
          },
          "description": {
            "en": "Notebooks, pencils, backpacks, and art supplies.",
            "es": "Cuadernos, lapices, mochilas y material de arte."
          },
          "helpMethods": ["goods", "money"],
          "urgent": true,
          "expiresOnIsoDate": "2026-12-31"
        }
      ],
      "importedAtIsoDate": "2026-05-13"
    }
  ]
}
```

## Version 1 Validation

The shared reader is `org.generosity.data.CatalogJson`. It accepts only schema
version 1. A missing file, malformed JSON, an unsupported version, an unknown
property, an unknown code, a duplicate center or need ID, a need linked to a
different center, an invalid coordinate or date, or an inconsistent quantity
rejects the whole document. The previous published catalog stays intact.
The first failed load stays empty and does not substitute invented centers.
Repeating a successful load replaces the previous list rather than appending.
A localized field may omit `en` or `es`; display falls back as described below.
Omitted `centerId` means the parent center ID. `urgent` and `priority` must
agree when both are present.

## Localization Rules

- `en` and `es` are the initial supported text keys.
- The mobile app displays the active locale value when available.
- If the active locale value is missing, the mobile app falls back to the configured fallback locale.
- If the fallback locale is missing, the mobile app displays the first non-empty available value.
- The mobile app does not generate automatic translations.

## Stable Codes

Categories:

- `children_support`
- `older_adults`
- `disability_support`
- `health`
- `blood_donation`
- `addiction_recovery`
- `community_kitchen`
- `education`
- `vulnerable_families`
- `organ_tissue_donation`

`blood_donation` is distinct from organ/tissue donation. The 39 reviewed blood
service listings also retain `health` for broad health discovery. This is an
explicit classification, never inferred from a center name or all health sites.
The initial publication used only `health`; a targeted audited migration adds
the specific category and increments changed center versions without replacing
their other data. The batch JSON files now contain the corrected categories;
historical release digests still identify the original published commits.

Updated native apps request `/api/v1/sync/catalog?category_version=2` to receive
`blood_donation`. Omitted/1 projects that code to `health` (deduplicated) so old
strict readers continue to accept the entire snapshot. Invalid versions return
422. This negotiates the category vocabulary; `schemaVersion` remains 1 and
unknown properties/codes are still rejected by the reader. Directory endpoints
return the stored categories. New builds are required for the new filter.

Help methods:

- `time`
- `goods`
- `money`

Verification status:

- `imported`
- `visit_signaled`
- `officially_verified`

## Planned Directory Extension

The [center directory field plan](../openspec/changes/define-center-directory-details/spec-index.md)
adds legal name, activities, service fees, designated responsible/public contacts,
social links, dated beneficiary reports, service admission declarations, resource
sources and structured featured/additional attributes. Its
[field dictionary and migration design](../openspec/changes/define-center-directory-details/design.md)
define a later versioned contract; the JSON example above is not extended or
implemented by this documentation update.

New publication will require stable identity, a public name, supported categories
and a designated public contact route. Optional missing data stays not informed;
unsupported categories or invalid supplied values require review, not guessed
mappings. Incomplete legacy entries retain IDs/history privately for correction.

Public and private addresses/contacts must be separated before generating a
public catalog. Protected/missing coordinates must not be replaced with fabricated
positions. A legacy verification enum/checkbox is source provenance, not enough
to establish an active scoped verification claim. The existing category codes
remain unchanged; the separate taxonomy discussion is not implemented here.

