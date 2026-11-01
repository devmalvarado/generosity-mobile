package org.generosity.app

import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.*
import org.junit.Test
import org.junit.Assert.*

class BackendStorageTest {
    @Test fun encryptedStoreSurvivesAdapterRestartAndSeparatesAccountKeys() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        val key = "test-" + java.util.UUID.randomUUID()
        val first = AndroidConnection(context, scope)
        try {
            first.write(key, "Niñas: apoyo médico")
            val restarted = AndroidConnection(context, scope)
            assertEquals("Niñas: apoyo médico", restarted.read(key))
            assertEquals("", restarted.read(key + "-other"))
            restarted.write(key, null)
            assertEquals("", first.read(key))
        } finally { first.write(key, null); scope.cancel() }
    }
}
