package org.generosity

import kotlinx.coroutines.test.runTest
import org.generosity.data.InMemoryImportedCatalogDataSource
import org.generosity.data.ImportedCatalogRepository
import org.generosity.data.NotificationCopy
import org.generosity.data.SampleImportedCatalog
import org.generosity.domain.LocalizedText
import org.generosity.domain.NotificationType
import org.generosity.domain.SupportedLocale
import org.generosity.usecase.DiscoverKindnessCentersUseCase
import org.generosity.usecase.DiscoveryQuery
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UnicodeLocalizationTest {
    @Test
    fun registeredLanguagesResolveWithoutLanguageSpecificBranches() {
        SupportedLocale.entries.forEach { locale ->
            assertEquals(locale, SupportedLocale.fromCode(locale.code.uppercase()))
            assertEquals(locale, SupportedLocale.fromCode("${locale.code}_MX"))
            assertTrue(locale.nativeName.isNotBlank())
        }
    }

    @Test
    fun contentPreservesSpanishAndAdditionalLanguageCharacters() {
        val content = "¡Qué alegría! ¿Niñez, pingüinos y educación?"
        val text = LocalizedText(mapOf("es" to content, "fr" to "Français — bénévolat"))
        assertEquals(content, text.resolve(SupportedLocale.SPANISH))
        assertEquals("Français — bénévolat", text.values["fr"])
        assertEquals(content, text.resolve(SupportedLocale.ENGLISH))
    }

    @Test
    fun searchAcceptsAccentsEnyeAndDecomposedUnicodeWithoutChangingContent() = runTest {
        val centers = SampleImportedCatalog.centers()
        val discover = DiscoverKindnessCentersUseCase(ImportedCatalogRepository(InMemoryImportedCatalogDataSource(centers)))
        for (query in listOf("años", "ANOS", "an\u0303os")) {
            val found = discover(DiscoveryQuery(searchText = query, locale = SupportedLocale.SPANISH))
            assertEquals(listOf("kc-seniors-001"), found.map { it.id })
            assertEquals("Comedor Comunitario Años Dorados", found.single().name.resolve(SupportedLocale.SPANISH))
        }
        for (query in listOf("niños", "ninos", "NIN\u0303OS")) {
            assertEquals(listOf("kc-children-001"), discover(DiscoveryQuery(searchText = query, locale = SupportedLocale.SPANISH)).map { it.id })
        }
        assertEquals(3, discover(DiscoveryQuery(searchText = "Mexico", locale = SupportedLocale.SPANISH)).size)
    }

    @Test
    fun notificationCopyCoversEveryEnabledLanguageAndType() {
        for (locale in SupportedLocale.entries) {
            for (type in NotificationType.entries) {
                assertTrue(!NotificationCopy.titles.getValue(type).values[locale.code].isNullOrBlank())
                assertTrue(!NotificationCopy.bodies.getValue(type).values[locale.code].isNullOrBlank())
            }
        }
    }
}
