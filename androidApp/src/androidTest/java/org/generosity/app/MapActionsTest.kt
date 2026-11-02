package org.generosity.app

import android.content.ActivityNotFoundException
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import org.generosity.app.ui.googleMapsUri
import org.generosity.app.ui.openCenterMap
import org.generosity.data.BundledKindnessCatalog
import org.generosity.domain.Coordinates
import org.generosity.domain.LocalizedText
import org.generosity.domain.SupportedLocale
import org.junit.Assert.*
import org.junit.Test

class MapActionsTest {
    @Test fun coordinatesStayExactAndInvalidLocationsCannotOpen() {
        for ((point, expected) in listOf(Coordinates(19.4326, -99.1332) to "19.4326,-99.1332",
            Coordinates(-90.0, 180.0) to "-90.0,180.0", Coordinates(0.0, 0.0) to "0.0,0.0")) {
            val uri = requireNotNull(point.googleMapsUri())
            assertEquals("https", uri.scheme)
            assertEquals("www.google.com", uri.host)
            assertEquals("1", uri.getQueryParameter("api"))
            assertEquals(expected, uri.getQueryParameter("query"))
            assertEquals(setOf("api", "query"), uri.queryParameterNames)
        }
        val context = RecordingContext()
        for (point in listOf(Coordinates(Double.NaN, 0.0), Coordinates(91.0, 0.0),
            Coordinates(0.0, Double.POSITIVE_INFINITY), Coordinates(0.0, -181.0))) {
            assertNull(point.googleMapsUri())
            assertFalse(openCenterMap(context, BundledKindnessCatalog.centers()[0].copy(coordinates = point), SupportedLocale.ENGLISH, true))
        }
        assertTrue(context.opened.isEmpty())
    }

    @Test fun nativeMapEncodesAccentsAndDoesNotInjectQueryParameters() {
        val context = RecordingContext()
        val name = "Niñez & Salud #1 + México"
        val center = BundledKindnessCatalog.centers()[1].copy(name = LocalizedText(mapOf("es" to name)))
        assertTrue(openCenterMap(context, center, SupportedLocale.SPANISH, true))
        val uri = context.opened.single()
        assertEquals("geo", uri.scheme)
        assertEquals("${center.coordinates.latitude},${center.coordinates.longitude}($name)", Uri.decode(uri.toString().substringAfter("?q=")))
        assertFalse(uri.toString().contains("&"))
        assertNull(uri.fragment)
    }

    @Test fun missingMapsAppFallsBackToWebAndMissingHandlersReturnFailure() {
        val context = RecordingContext()
        val center = BundledKindnessCatalog.centers()[0]
        context.missing = setOf("geo")
        assertTrue(openCenterMap(context, center, SupportedLocale.ENGLISH, true))
        assertEquals(listOf("geo", "https"), context.opened.map { it.scheme })
        context.opened.clear()
        context.missing = setOf("geo", "https")
        assertFalse(openCenterMap(context, center, SupportedLocale.ENGLISH, true))
        context.opened.clear()
        assertFalse(openCenterMap(context, center, SupportedLocale.ENGLISH, false))
        assertEquals(1, context.opened.size)
    }
}

private class RecordingContext : ContextWrapper(InstrumentationRegistry.getInstrumentation().targetContext) {
    val opened = mutableListOf<Uri>()
    var missing = emptySet<String>()
    override fun startActivity(intent: Intent) {
        assertEquals(Intent.ACTION_VIEW, intent.action)
        val uri = requireNotNull(intent.data)
        opened += uri
        if (uri.scheme in missing) throw ActivityNotFoundException()
    }
}
