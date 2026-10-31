package org.generosity.app.ui.map

import org.generosity.data.SampleImportedCatalog
import org.generosity.domain.Coordinates
import org.generosity.domain.LocalizedText
import org.generosity.domain.SupportedLocale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CenterMapMarkersTest {
    private val center = SampleImportedCatalog.centers().first()

    @Test
    fun projectionPreservesCatalogIdentityAndCoordinates() {
        val centers = SampleImportedCatalog.centers()
        val markers = centers.toMapMarkers(SupportedLocale.ENGLISH)
        assertEquals(centers.map { it.id }, markers.map { it.centerId })
        assertEquals(centers.map { it.coordinates }, markers.map { it.coordinates })
        assertEquals(centers.map { it.name.resolve(SupportedLocale.ENGLISH) }, markers.map { it.title })
    }

    @Test
    fun projectionUsesSharedLocalizationAndFallback() {
        val translated = center.copy(name = LocalizedText.of("Center", "Centro"))
        val fallback = center.copy(name = LocalizedText.englishOnly("Fallback"))
        assertEquals(listOf("Centro", "Fallback"), listOf(translated, fallback)
            .toMapMarkers(SupportedLocale.SPANISH).map { it.title })
    }

    @Test
    fun invalidCoordinatesAreNotClampedIntoInventedLocations() {
        val invalid = listOf(
            Coordinates(Double.NaN, 0.0), Coordinates(0.0, Double.NaN),
            Coordinates(Double.POSITIVE_INFINITY, 0.0), Coordinates(0.0, Double.NEGATIVE_INFINITY),
            Coordinates(90.1, 0.0), Coordinates(-90.1, 0.0),
            Coordinates(0.0, 180.1), Coordinates(0.0, -180.1)
        ).map { center.copy(coordinates = it) }
        assertTrue(invalid.toMapMarkers(SupportedLocale.ENGLISH).isEmpty())
        val edges = listOf(Coordinates(90.0, 180.0), Coordinates(-90.0, -180.0))
        assertEquals(edges, edges.map { center.copy(coordinates = it) }
            .toMapMarkers(SupportedLocale.ENGLISH).map { it.coordinates })
    }

    @Test
    fun emptyFilteredResultsDoNotProduceSampleMarkers() {
        assertTrue(emptyList<org.generosity.domain.KindnessCenter>()
            .toMapMarkers(SupportedLocale.ENGLISH).isEmpty())
    }
}
