package org.generosity.data

import org.generosity.domain.CenterCategory
import org.generosity.domain.CenterNeed
import org.generosity.domain.ContactInfo
import org.generosity.domain.Coordinates
import org.generosity.domain.HelpMethod
import org.generosity.domain.KindnessCenter
import org.generosity.domain.LocalizedText
import org.generosity.domain.VerificationStatus

object SampleImportedCatalog {
    fun centers(): List<KindnessCenter> = listOf(
        KindnessCenter(
            id = "kc-children-001",
            name = LocalizedText.of("Bright Futures Home", "Hogar Futuros Brillantes"),
            description = LocalizedText.of(
                "Support center for children and families.",
                "Centro de apoyo para ninos y familias."
            ),
            categories = setOf(CenterCategory.CHILDREN_SUPPORT, CenterCategory.EDUCATION),
            coordinates = Coordinates(19.4326, -99.1332),
            address = LocalizedText.of("Historic Center, Mexico City", "Centro Historico, Ciudad de Mexico"),
            contactInfo = ContactInfo(phone = "+52 55 0000 0001", website = "https://example.org/bright-futures"),
            verificationStatus = VerificationStatus.IMPORTED,
            needs = listOf(
                CenterNeed(
                    id = "need-school-supplies",
                    centerId = "kc-children-001",
                    title = LocalizedText.of("School supplies", "Utiles escolares"),
                    description = LocalizedText.of(
                        "Notebooks, pencils, backpacks, and art supplies.",
                        "Cuadernos, lapices, mochilas y material de arte."
                    ),
                    helpMethods = setOf(HelpMethod.GOODS, HelpMethod.MONEY),
                    urgent = true,
                    expiresOnIsoDate = "2026-12-31"
                )
            ),
            importedAtIsoDate = "2026-05-13"
        ),
        KindnessCenter(
            id = "kc-seniors-001",
            name = LocalizedText.of("Golden Years Community Kitchen", "Comedor Comunitario Anos Dorados"),
            description = LocalizedText.of(
                "Meals and social support for older adults.",
                "Alimentos y apoyo social para adultos mayores."
            ),
            categories = setOf(CenterCategory.OLDER_ADULTS, CenterCategory.COMMUNITY_KITCHEN),
            coordinates = Coordinates(19.4285, -99.1277),
            address = LocalizedText.of("Doctores, Mexico City", "Doctores, Ciudad de Mexico"),
            contactInfo = ContactInfo(phone = "+52 55 0000 0002"),
            verificationStatus = VerificationStatus.VISIT_SIGNALED,
            needs = listOf(
                CenterNeed(
                    id = "need-volunteers-lunch",
                    centerId = "kc-seniors-001",
                    title = LocalizedText.of("Lunch volunteers", "Voluntarios para comida"),
                    description = LocalizedText.of(
                        "Two-hour shifts to serve lunch during weekdays.",
                        "Turnos de dos horas para servir comida entre semana."
                    ),
                    helpMethods = setOf(HelpMethod.TIME),
                    urgent = false
                )
            ),
            importedAtIsoDate = "2026-05-13"
        ),
        KindnessCenter(
            id = "kc-health-001",
            name = LocalizedText.of("Open Health Bridge", "Puente de Salud Abierta"),
            description = LocalizedText.of(
                "Health, disability, recovery, and vulnerable-family support.",
                "Apoyo de salud, discapacidad, recuperacion y familias vulnerables."
            ),
            categories = setOf(
                CenterCategory.HEALTH,
                CenterCategory.DISABILITY_SUPPORT,
                CenterCategory.ADDICTION_RECOVERY,
                CenterCategory.VULNERABLE_FAMILIES,
                CenterCategory.ORGAN_TISSUE_DONATION
            ),
            coordinates = Coordinates(19.4371, -99.1448),
            address = LocalizedText.of("Juarez, Mexico City", "Juarez, Ciudad de Mexico"),
            contactInfo = ContactInfo(email = "contact@example.org"),
            verificationStatus = VerificationStatus.OFFICIALLY_VERIFIED,
            needs = listOf(
                CenterNeed(
                    id = "need-health-kits",
                    centerId = "kc-health-001",
                    title = LocalizedText.of("Health kits", "Kits de salud"),
                    description = LocalizedText.of(
                        "Basic hygiene and first-aid kits for families.",
                        "Kits basicos de higiene y primeros auxilios para familias."
                    ),
                    helpMethods = setOf(HelpMethod.GOODS, HelpMethod.MONEY),
                    urgent = true,
                    expiresOnIsoDate = "2026-08-31"
                )
            ),
            importedAtIsoDate = "2026-05-13"
        )
    )
}

