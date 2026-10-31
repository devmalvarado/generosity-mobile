package org.generosity.app

import androidx.test.platform.app.InstrumentationRegistry
import org.generosity.app.ui.localizedResources
import org.generosity.domain.SupportedLocale
import org.junit.Assert.assertEquals
import org.junit.Test

class LocalizationResourcesTest {
    @Test
    fun nativeResourcesPreserveAccentsAndSelectSingularAndPlural() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val spanish = context.localizedResources(SupportedLocale.SPANISH)
        val english = context.localizedResources(SupportedLocale.ENGLISH)
        assertEquals("Visité este centro", spanish.getString(R.string.visit_signal))
        assertEquals("Donación de órganos y tejidos", spanish.getString(R.string.category_organ_tissue_donation))
        for ((count, expected) in listOf(0 to "Faltan 0 unidades", 1 to "Falta 1 unidad", 15 to "Faltan 15 unidades")) {
            assertEquals(expected, spanish.getQuantityString(R.plurals.remaining_units, count, count))
        }
        assertEquals("1 item still needed", english.getQuantityString(R.plurals.remaining_units, 1, 1))
        assertEquals("15 items still needed", english.getQuantityString(R.plurals.remaining_units, 15, 15))
        assertEquals("Puedo llevar 1 artículo", spanish.getQuantityString(R.plurals.offer_goods, 1, 1))
        assertEquals("Puedo llevar 5 artículos", spanish.getQuantityString(R.plurals.offer_goods, 5, 5))
    }
}
