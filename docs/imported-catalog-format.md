# Imported Kindness Center Catalog Format

The initial mobile foundation consumes imported Kindness Center catalog data before a live backend exists.

## Format

The first supported format is JSON with a top-level `centers` array. App-controlled labels such as categories, help methods, verification status, and notification types use stable codes from the shared Kotlin domain model. Human-readable center-managed content uses localized objects keyed by language code.

```json
{
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

