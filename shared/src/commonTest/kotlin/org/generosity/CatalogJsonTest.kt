package org.generosity

import kotlinx.coroutines.test.runTest
import org.generosity.data.BundledKindnessCatalog
import org.generosity.data.CatalogImport
import org.generosity.data.CatalogJson
import org.generosity.data.ImportedCatalogRepository
import org.generosity.data.SampleImportedCatalog
import org.generosity.data.ValidatingCatalogDataSource
import org.generosity.domain.SupportedLocale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CatalogJsonTest {
    @Test
    fun bundledCatalogIsTheOnlyLabeledSample() {
        val centers = BundledKindnessCatalog.centers()
        val school = centers.flatMap { it.needs }.first { it.id == "need-school-supplies" }
        assertEquals(centers, SampleImportedCatalog.centers())
        assertEquals("Útiles escolares", school.title.resolve(SupportedLocale.SPANISH))
        assertEquals(20, school.quantityRequired)
        assertEquals(5, school.quantityCommitted)
    }

    @Test
    fun repositoryExposesTheBundledCatalog() = runTest {
        val repository = ImportedCatalogRepository(
            ValidatingCatalogDataSource(readText = { BundledKindnessCatalog.JSON })
        )

        assertEquals(SampleImportedCatalog.centers(), repository.centers())
    }

    @Test
    fun spanishOnlyTextFallsBackForEnglish() {
        val result = CatalogJson.parse(catalog("""
            {
              "id": "kc-1",
              "name": {"es": "Solo español"},
              "description": {"en": "Described"},
              "categories": ["education"],
              "coordinates": {"latitude": 0, "longitude": 0},
              "address": {"en": "Address"},
              "verificationStatus": "imported"
            }
        """))

        val accepted = assertIs<CatalogImport.Accepted>(result)
        assertEquals("Solo español", accepted.centers.single().name.resolve(SupportedLocale.ENGLISH))
    }

    @Test
    fun malformedJsonIsRejected() {
        val result = CatalogJson.parse("{")

        assertIs<CatalogImport.Rejected>(result)
    }

    @Test
    fun missingFileKeepsThePreviousCatalog() {
        var text: String? = BundledKindnessCatalog.JSON
        val source = ValidatingCatalogDataSource(readText = { text })
        val published = source.loadCenters()

        text = null
        val retained = source.loadCenters()

        assertEquals(published, retained)
        assertEquals("Catalog file is missing.", source.lastDiagnostic)
    }

    @Test
    fun firstMissingFileStaysEmpty() {
        val source = ValidatingCatalogDataSource(readText = { null })

        assertTrue(source.loadCenters().isEmpty())
        assertEquals("Catalog file is missing.", source.lastDiagnostic)
    }

    @Test
    fun invalidImportDoesNotReplaceThePublishedCatalog() {
        var text: String? = BundledKindnessCatalog.JSON
        val source = ValidatingCatalogDataSource(readText = { text })
        val published = source.loadCenters()

        text = """{"schemaVersion": 1, "centers": [{"id": "dup"}, {"id": "dup"}]}"""

        assertEquals(published, source.loadCenters())
        assertTrue(source.lastDiagnostic?.contains("rejected") == true)
    }

    @Test
    fun duplicateCenterIdsAreRejected() {
        val result = CatalogJson.parse(catalog("""
            ${center("same")},
            ${center("same")}
        """))

        val rejected = assertIs<CatalogImport.Rejected>(result)
        assertTrue(rejected.diagnostic.contains("duplicate center id"))
    }

    @Test
    fun duplicateNeedIdsAreRejected() {
        val result = CatalogJson.parse(catalog("""
            ${center("kc-1", needId = "same-need")},
            ${center("kc-2", needId = "same-need")}
        """))

        val rejected = assertIs<CatalogImport.Rejected>(result)
        assertTrue(rejected.diagnostic.contains("duplicate need id"))
    }

    @Test
    fun unsupportedVersionAndUnknownPropertyAreRejected() {
        assertIs<CatalogImport.Rejected>(CatalogJson.parse("""{"schemaVersion": 2, "centers": []}"""))
        assertIs<CatalogImport.Rejected>(
            CatalogJson.parse(catalog(center("kc-1")).replace("\"centers\"", "\"directoryExtension\": true, \"centers\""))
        )
    }

    @Test
    fun repeatedLoadReplacesRatherThanAppends() {
        val source = ValidatingCatalogDataSource(readText = { BundledKindnessCatalog.JSON })

        assertEquals(3, source.loadCenters().size)
        assertEquals(3, source.loadCenters().size)
        assertNull(source.lastDiagnostic)
    }

    private fun catalog(centers: String): String = """{"schemaVersion": 1, "centers": [$centers]}"""

    private fun center(id: String, needId: String? = null): String {
        val needs = if (needId == null) "[]" else """
            [{
              "id": "$needId",
              "title": {"en": "Need"},
              "description": {"en": "Need"},
              "helpMethods": ["time"]
            }]
        """
        return """
            {
              "id": "$id",
              "name": {"en": "Name"},
              "description": {"en": "Description"},
              "categories": ["education"],
              "coordinates": {"latitude": 1, "longitude": 2},
              "address": {"en": "Address"},
              "verificationStatus": "imported",
              "needs": $needs
            }
        """
    }
}
