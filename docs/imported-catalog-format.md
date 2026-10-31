# Imported Kindness Center Catalog Format

The initial mobile foundation consumes a validated bundled Kindness Center
catalog before a live backend exists. As of 2026-10-31, both apps load schema
version 1 through the shared repository. The bundled document is the only labeled demonstration fixture.
`SampleImportedCatalog` reads that same document. It is not a
published catalog and does not establish that any center is a verified real
organization. `data/sample-kindness-centers.json` is a separate labeled sample
file, not the document the apps load.

## Format

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
- `addiction_recovery`
- `community_kitchen`
- `education`
- `vulnerable_families`
- `organ_tissue_donation`

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

