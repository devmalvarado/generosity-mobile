package org.generosity.app

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import kotlinx.coroutines.*
import org.generosity.app.ui.GenerosityApp
import org.generosity.app.ui.map.google.GoogleCenterMapRenderer
import org.generosity.data.*
import org.generosity.domain.SupportedLocale
import org.junit.*
import org.junit.Assert.*

class NotificationInboxTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    @After fun close() = scope.cancel()

    @Test fun spanishInboxOpensCurrentCenterWithoutPushPermission() = checkInbox(SupportedLocale.SPANISH)
    @Test fun englishInboxOpensCurrentCenterWithoutPushPermission() = checkInbox(SupportedLocale.ENGLISH)

    private fun checkInbox(locale: SupportedLocale) {
        val disk = mutableMapOf<String, String>()
        var read = false
        val connection = object : AppConnection {
            override fun read(key: String) = disk[key].orEmpty()
            override fun write(key: String, value: String?) { if (value == null) disk.remove(key) else disk[key] = value }
            override fun newOperationId() = java.util.UUID.randomUUID().toString()
            override fun send(request: ApiRequest, completion: (ApiResponse) -> Unit) {
                completion(when {
                    request.path.startsWith("/api/v1/sync/catalog") -> ApiResponse(200, """{"catalog":${BundledKindnessCatalog.JSON}}""")
                    request.path.endsWith("/read") -> { read = true; ApiResponse(200, "{}") }
                    else -> ApiResponse(200, """{"user":{"id":1},"contributions":[],"notices":{"items":[{"id":"notice-1","eventId":"event-1","centerId":"kc-children-001","needId":"need-school-supplies","title":{"es":"Aviso de útiles escolares","en":"School supplies notice"},"createdAt":"2026-11-02T00:00:00Z","read":$read}]}}""")
                })
            }
        }
        val app = GenerosityAppState(scope, backend = BackendStore(connection))
        ui.setContent { GenerosityApp(app, GoogleCenterMapRenderer) }
        ui.runOnIdle { app.setLocale(locale); app.signIn("1", "María", remoteAccount = true) }
        ui.waitUntil(5000) { !app.state.value.syncing && app.state.value.notices.size == 1 }
        val title = if (locale == SupportedLocale.SPANISH) "Avisos" else "Notifications"
        ui.onNodeWithTag("notification-inbox").assertTextContains("$title (1)").performClick()
        ui.onNodeWithText(if (locale == SupportedLocale.SPANISH) "• Aviso de útiles escolares" else "• School supplies notice").performClick()
        ui.waitUntil(5000) { read && app.state.value.selectedCenterId == "kc-children-001" }
        ui.onNodeWithTag("notification-inbox").assertTextContains("$title (0)")
        assertFalse(app.state.value.pushConfigured)
        assertTrue(app.state.value.notices.single().read)
        ui.runOnIdle { app.continueAnonymously() }
        ui.onNodeWithTag("notification-inbox").assertDoesNotExist()
    }
}
