package org.generosity.data

import org.generosity.domain.KindnessCenter

/**
 * Labeled demonstration catalog bundled with both apps. It is not a published
 * or verified real-center source. Version 1 matches [SampleImportedCatalog].
 */
object BundledKindnessCatalog {
    const val JSON: String = """
        {
          "schemaVersion": 1,
          "centers": [
            {
              "id": "kc-children-001",
              "name": {"en": "Bright Futures Home", "es": "Hogar Futuros Brillantes"},
              "description": {
                "en": "Support center for children and families.",
                "es": "Centro de apoyo para niños y familias."
              },
              "categories": ["children_support", "education"],
              "coordinates": {"latitude": 19.4326, "longitude": -99.1332},
              "address": {
                "en": "Historic Center, Mexico City",
                "es": "Centro Histórico, Ciudad de México"
              },
              "contact": {"phone": "+52 55 0000 0001", "website": "https://example.org/bright-futures"},
              "verificationStatus": "imported",
              "needs": [
                {
                  "id": "need-school-supplies",
                  "centerId": "kc-children-001",
                  "title": {"en": "School supplies", "es": "Útiles escolares"},
                  "description": {
                    "en": "Notebooks, pencils, backpacks, and art supplies.",
                    "es": "Cuadernos, lápices, mochilas y materiales de arte."
                  },
                  "helpMethods": ["goods", "money"],
                  "urgent": true,
                  "expiresOnIsoDate": "2026-12-31",
                  "category": "education",
                  "quantityRequired": 20,
                  "quantityCommitted": 5,
                  "publishedOnIsoDate": "2026-05-01"
                }
              ],
              "importedAtIsoDate": "2026-05-13"
            },
            {
              "id": "kc-seniors-001",
              "name": {"en": "Golden Years Community Kitchen", "es": "Comedor Comunitario Años Dorados"},
              "description": {
                "en": "Meals and social support for older adults.",
                "es": "Alimentos y apoyo social para adultos mayores."
              },
              "categories": ["older_adults", "community_kitchen"],
              "coordinates": {"latitude": 19.4285, "longitude": -99.1277},
              "address": {"en": "Doctores, Mexico City", "es": "Doctores, Ciudad de México"},
              "contact": {"phone": "+52 55 0000 0002"},
              "verificationStatus": "visit_signaled",
              "needs": [
                {
                  "id": "need-volunteers-lunch",
                  "centerId": "kc-seniors-001",
                  "title": {"en": "Mealtime volunteers", "es": "Voluntariado para servir comidas"},
                  "description": {
                    "en": "Two-hour shifts to serve lunch on weekdays.",
                    "es": "Turnos de dos horas para servir comidas entre semana."
                  },
                  "helpMethods": ["time"],
                  "urgent": false
                }
              ],
              "importedAtIsoDate": "2026-05-13"
            },
            {
              "id": "kc-health-001",
              "name": {"en": "Open Health Bridge", "es": "Puente de Salud Abierta"},
              "description": {
                "en": "Health services and support for people with disabilities, people recovering from addiction, and families in need.",
                "es": "Servicios de salud y apoyo para personas con discapacidad, personas en recuperación de adicciones y familias en situación vulnerable."
              },
              "categories": [
                "health",
                "disability_support",
                "addiction_recovery",
                "vulnerable_families",
                "organ_tissue_donation"
              ],
              "coordinates": {"latitude": 19.4371, "longitude": -99.1448},
              "address": {"en": "Juárez, Mexico City", "es": "Juárez, Ciudad de México"},
              "contact": {"email": "contact@example.org"},
              "verificationStatus": "officially_verified",
              "needs": [
                {
                  "id": "need-health-kits",
                  "centerId": "kc-health-001",
                  "title": {"en": "Health kits", "es": "Kits de salud"},
                  "description": {
                    "en": "Basic hygiene and first-aid kits for families.",
                    "es": "Kits básicos de higiene y primeros auxilios para familias."
                  },
                  "helpMethods": ["goods", "money"],
                  "urgent": true,
                  "expiresOnIsoDate": "2026-08-31"
                }
              ],
              "importedAtIsoDate": "2026-05-13"
            }
          ]
        }
    """

    fun centers(): List<KindnessCenter> = when (val result = CatalogJson.parse(JSON)) {
        is CatalogImport.Accepted -> result.centers
        is CatalogImport.Rejected -> emptyList()
    }
}
