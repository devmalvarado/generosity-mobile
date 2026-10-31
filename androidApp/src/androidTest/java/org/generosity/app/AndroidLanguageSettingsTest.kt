package org.generosity.app

import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.generosity.domain.SupportedLocale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidLanguageSettingsTest {
    @Test
    fun nativeStoreRestoresOverrideAndDeviceModeAcrossNewInstances() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val store = AndroidLanguageSettings(context)
        val previous = store.load().selectedLocale
        try {
            assertTrue(store.save(null))
            assertEquals(SupportedLocale.SPANISH, store.load(listOf("fr-FR", "es-MX", "en-US")).activeLocale)
            assertTrue(store.save(SupportedLocale.ENGLISH))
            val restored = AndroidLanguageSettings(context).load(listOf("es-MX"))
            assertEquals(SupportedLocale.ENGLISH, restored.selectedLocale)
            assertEquals(SupportedLocale.ENGLISH, restored.activeLocale)
            assertTrue(store.save(null))
            val automatic = AndroidLanguageSettings(context).load(listOf("es-MX"))
            assertNull(automatic.selectedLocale)
            assertEquals(SupportedLocale.SPANISH, automatic.activeLocale)
        } finally {
            store.save(previous)
        }
    }
}
