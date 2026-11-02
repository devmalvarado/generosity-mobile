package org.generosity

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.generosity.app.GenerosityAppState
import org.generosity.data.SampleImportedCatalog
import org.generosity.domain.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class DiscoveryAreaTest {
    private val template = SampleImportedCatalog.centers().first()
    private val catalog = listOf(
        template.copy(id = "far", coordinates = Coordinates(0.10, 0.0)),
        template.copy(id = "edge", coordinates = Coordinates(0.0899, 0.0)),
        template.copy(id = "near", coordinates = Coordinates(0.01, 0.0))
    )
    private val area = DiscoveryArea("Test neighborhood", Coordinates(0.0, 0.0))

    @Test fun radiusOrdersNearbyCentersAndClearKeepsOtherFilters() = runTest {
        val app = GenerosityAppState(backgroundScope, catalog = catalog)
        app.start(); app.setSearchText(template.name.resolve(SupportedLocale.ENGLISH))
        app.toggleCategory(template.categories.first()); runCurrent()
        app.selectCenter("far"); app.setDiscoveryArea(area); runCurrent()
        assertNull(app.state.value.selectedCenterId)
        assertEquals(listOf("near", "edge"), app.state.value.centers.map { it.id })
        assertEquals(1.112, area.distanceKilometersTo(Coordinates(0.01, 0.0)), .001)
        app.setDiscoveryArea(DiscoveryArea("Elsewhere", Coordinates(50.0, 50.0))); runCurrent()
        assertTrue(app.state.value.centers.isEmpty())
        app.setDiscoveryArea(null); runCurrent()
        assertEquals(3, app.state.value.centers.size)
        assertEquals(template.name.resolve(SupportedLocale.ENGLISH), app.state.value.searchText)
        assertEquals(setOf(template.categories.first()), app.state.value.selectedCategories)
    }

    @Test fun invalidNativeCoordinatesAndEmptyLabelsAreRejected() {
        for (point in listOf(Coordinates(Double.NaN, 0.0), Coordinates(0.0, Double.POSITIVE_INFINITY), Coordinates(91.0, 0.0), Coordinates(0.0, -181.0))) {
            assertFailsWith<IllegalArgumentException> { DiscoveryArea("Place", point) }
        }
        assertFailsWith<IllegalArgumentException> { DiscoveryArea(" ", Coordinates(0.0, 0.0)) }
    }

    @Test fun gpsRefreshKeepsOpenDetailEvenWhenOriginExpiresOrMoves() = runTest {
        val app = GenerosityAppState(backgroundScope, catalog = catalog)
        app.start(); app.setDiscoveryArea(area); runCurrent()
        app.selectCenter("near")
        val selected = app.state.value.selectedCenter
        val currentNeeds = app.state.value.currentNeeds
        // Foreground return can clear an expired fix before publishing a new one.
        app.setDiscoveryArea(null, preserveSelection = true); runCurrent()
        assertEquals(selected, app.state.value.selectedCenter)
        app.setDiscoveryArea(DiscoveryArea("New GPS fix", Coordinates(50.0, 50.0)), preserveSelection = true)
        runCurrent()
        assertTrue(app.state.value.centers.isEmpty())
        assertEquals(selected, app.state.value.selectedCenter)
        assertEquals(currentNeeds, app.state.value.currentNeeds)
        // Choosing a different area explicitly still clears the selection.
        app.setDiscoveryArea(area); runCurrent()
        assertNull(app.state.value.selectedCenter)
        assertNull(app.state.value.selectedCenterDetails)
    }

    @Test fun gpsRefreshDoesNotKeepDetailsOfARemovedCatalogCenter() = runTest {
        var available = catalog
        val app = GenerosityAppState(backgroundScope, catalog = catalog, centersRepository = object : KindnessCenterRepository {
            override suspend fun centers() = available
        })
        app.start(); app.setDiscoveryArea(area); runCurrent()
        app.selectCenter("near")
        assertNotNull(app.state.value.selectedCenter)
        available = emptyList()
        app.setDiscoveryArea(area, preserveSelection = true); runCurrent()
        assertNull(app.state.value.selectedCenter)
        assertNull(app.state.value.selectedCenterDetails)
    }

    @Test fun staleLookupCannotReplaceTheSelectedArea() = runTest {
        val delayed = CompletableDeferred<Unit>()
        var calls = 0
        val app = GenerosityAppState(backgroundScope, catalog = catalog, centersRepository = object : KindnessCenterRepository {
            override suspend fun centers(): List<KindnessCenter> {
                if (++calls == 1) delayed.await()
                return catalog
            }
            override suspend fun centerById(id: String) = catalog.firstOrNull { it.id == id }
        })
        app.setDiscoveryArea(area); runCurrent()
        app.setDiscoveryArea(DiscoveryArea("Remote", Coordinates(50.0, 50.0))); runCurrent()
        delayed.complete(Unit); runCurrent()
        assertTrue(app.state.value.centers.isEmpty())
        assertEquals("Remote", app.state.value.discoveryArea?.label)
    }
}
