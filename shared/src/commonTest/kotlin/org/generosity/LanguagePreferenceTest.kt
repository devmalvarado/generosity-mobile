package org.generosity

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.generosity.app.GenerosityAppState
import org.generosity.data.SampleImportedCatalog
import org.generosity.domain.CenterCategory
import org.generosity.domain.KindnessCenter
import org.generosity.domain.KindnessCenterRepository
import org.generosity.domain.LocalePreference
import org.generosity.domain.SupportedLocale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LanguagePreferenceTest {
    @Test
    fun systemUsesFirstSupportedLanguageIncludingRegionalAndUnderscoreTags() {
        listOf("es-MX", "es_ES", " ES-ar ", "es-Latn-MX").forEach { tag ->
            val preference = LocalePreference.fromDeviceLanguages(null, listOf("fr-FR", tag, "en-US"))
            assertNull(preference.selectedLocale)
            assertEquals(SupportedLocale.SPANISH, preference.activeLocale)
        }
        assertEquals(SupportedLocale.ENGLISH,
            LocalePreference.fromDeviceLanguages("system", listOf("en-GB", "es-MX")).activeLocale)
    }

    @Test
    fun unsupportedOrEmptyDeviceLanguagesFallBackToEnglish() {
        listOf(emptyList(), listOf("fr-FR", "de-DE"), listOf("", "esoteric")).forEach { languages ->
            assertEquals(SupportedLocale.ENGLISH,
                LocalePreference.fromDeviceLanguages(null, languages).activeLocale)
        }
    }

    @Test
    fun savedOverrideWinsAndInvalidSavedChoiceUsesDevice() {
        assertEquals(SupportedLocale.ENGLISH,
            LocalePreference.fromDeviceLanguages("en", listOf("es-MX")).activeLocale)
        assertEquals(SupportedLocale.SPANISH,
            LocalePreference.fromDeviceLanguages("unknown", listOf("es-MX")).activeLocale)
    }

    @Test
    fun initialStateResolvesBeforeStartingAndRefreshesSystemLanguage() = runTest {
        val app = GenerosityAppState(backgroundScope,
            initialLocalePreference = LocalePreference.fromDeviceLanguages(null, listOf("es-MX")))
        assertEquals(SupportedLocale.SPANISH, app.state.value.locale)
        app.setDeviceLanguages(listOf("en-US"))
        assertEquals(SupportedLocale.ENGLISH, app.state.value.locale)
        app.setLocale(SupportedLocale.SPANISH)
        app.setDeviceLanguages(listOf("fr-FR", "en-GB"))
        assertEquals(SupportedLocale.SPANISH, app.state.value.locale)
        app.setLocale(null)
        assertNull(app.state.value.localePreference.selectedLocale)
        assertEquals(SupportedLocale.ENGLISH, app.state.value.locale)
    }

    @Test
    fun changingLanguageRefreshesSearchWithoutClearingQueryOrCategories() = runTest {
        val app = GenerosityAppState(backgroundScope)
        app.start()
        app.toggleCategory(CenterCategory.CHILDREN_SUPPORT)
        app.setSearchText("Hogar")
        runCurrent()
        assertTrue(app.state.value.centers.isEmpty())
        app.setLocale(SupportedLocale.SPANISH)
        runCurrent()
        assertEquals("Hogar", app.state.value.searchText)
        assertEquals(setOf(CenterCategory.CHILDREN_SUPPORT), app.state.value.selectedCategories)
        assertEquals(listOf("kc-children-001"), app.state.value.centers.map { it.id })
        app.setLocale(SupportedLocale.ENGLISH)
        runCurrent()
        assertTrue(app.state.value.centers.isEmpty())
    }

    @Test
    fun localeRefreshSortsUsingCurrentTitlesAndPreservesSelection() = runTest {
        val app = GenerosityAppState(backgroundScope)
        app.start()
        runCurrent()
        val selected = app.state.value.centers.first().id
        app.selectCenter(selected)
        app.setLocale(SupportedLocale.SPANISH)
        runCurrent()
        val titles = app.state.value.centers.map { it.name.resolve(SupportedLocale.SPANISH) }
        assertEquals(titles.sorted(), titles)
        assertEquals(selected, app.state.value.selectedCenterId)
    }

    @Test
    fun accountChangesDoNotReplaceInstallationLanguage() = runTest {
        val app = GenerosityAppState(backgroundScope)
        app.setLocale(SupportedLocale.SPANISH)
        app.signIn("one", "One")
        runCurrent()
        app.continueAnonymously()
        runCurrent()
        app.signIn("two", "Two")
        runCurrent()
        assertEquals(SupportedLocale.SPANISH, app.state.value.localePreference.selectedLocale)
    }

    @Test
    fun slowerPreviousLanguageQueryCannotOverwriteCurrentResults() = runTest {
        val firstRead = CompletableDeferred<List<KindnessCenter>>()
        var reads = 0
        val catalog = SampleImportedCatalog.centers()
        val repository = object : KindnessCenterRepository {
            override suspend fun centers(): List<KindnessCenter> =
                if (reads++ == 0) firstRead.await() else catalog
            override suspend fun centerById(id: String): KindnessCenter? = catalog.firstOrNull { it.id == id }
        }
        val app = GenerosityAppState(backgroundScope, centersRepository = repository)
        app.setSearchText("Hogar")
        runCurrent()
        app.setLocale(SupportedLocale.SPANISH)
        runCurrent()
        val expected = app.state.value.centers
        assertTrue(expected.isNotEmpty())
        firstRead.complete(catalog)
        runCurrent()
        assertEquals(expected, app.state.value.centers)
    }
}
