package org.generosity.app

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import kotlinx.coroutines.*
import org.generosity.app.ui.GenerosityApp
import org.generosity.app.ui.map.google.GoogleCenterMapRenderer
import org.generosity.data.*
import org.generosity.domain.*
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*

class ContributionFlowTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    @After fun close() { scope.cancel() }

    @Test fun enteredOfferIsDurableBeforeClosingAndHistoryComesFromServer() {
        val disk = mutableMapOf<String, String>()
        var accept = false
        var requestBody: String? = null
        var firstKey: String? = null
        val connection = object : AppConnection {
            override fun read(key: String) = disk[key].orEmpty()
            override fun write(key: String, value: String?) { if (value == null) disk.remove(key) else disk[key] = value }
            override fun newOperationId() = java.util.UUID.randomUUID().toString()
            override fun send(request: ApiRequest, completion: (ApiResponse) -> Unit) {
                val response = when {
                    request.path == "/api/v1/sync/catalog?category_version=2" -> ApiResponse(200, """{"catalog":${BundledKindnessCatalog.JSON},"updatedAt":"2026-11-01T00:00:00Z"}""")
                    request.method == "POST" -> {
                        requestBody = request.body
                        if (firstKey == null) firstKey = request.operationId else assertEquals(firstKey, request.operationId)
                        ApiResponse(if (accept) 201 else 0, "{}")
                    }
                    else -> ApiResponse(200, """{"user":{"id":1},"contributions":${if (accept) """[{"id":"offer-1","user_id":1,"title":{"en":"School supplies"},"method":"goods","quantity":3,"quantity_unit":"kits","status":"confirmed","center_id":"kc-children-001","need_id":"need-school-supplies","created_at":"2026-11-01T00:00:00Z","version":2}]""" else "[]"}}""")
                }
                completion(response)
            }
        }
        val app = GenerosityAppState(scope, backend = BackendStore(connection), clock = { "2026-11-01T00:00:00Z" })
        ui.setContent { GenerosityApp(app, GoogleCenterMapRenderer) }
        ui.runOnIdle { app.signIn("1", "María", remoteAccount = true) }
        ui.waitUntil(5000) { app.state.value.centers.isNotEmpty() }
        ui.onNodeWithTag("tab-list").performClick()
        ui.onNodeWithTag("discovery-scroll").performScrollToNode(hasTestTag("center-kc-children-001"))
        ui.onNodeWithTag("center-kc-children-001").performClick()
        ui.onNodeWithTag("discovery-scroll").performScrollToNode(hasText("Offer goods"))
        ui.onNodeWithText("Offer goods").performScrollTo().performClick()
        ui.onNodeWithText("Quantity").performTextInput("3")
        ui.onNodeWithText("Notes (optional)").performTextInput("Niñas: útiles escolares")
        ui.onNodeWithText("Save contribution").performClick()
        ui.waitUntil(5000) { app.state.value.savedOffer == 1 && !app.state.value.writing }
        assertEquals(3, JSONObject(requestBody!!).getInt("quantity"))
        assertEquals("Niñas: útiles escolares", JSONObject(requestBody!!).getString("notes"))
        assertEquals(1, BackendStore(connection).pending("1").size)
        assertTrue(app.state.value.contributions.isEmpty())
        ui.runOnIdle { accept = true; app.onResume() }
        ui.waitUntil(5000) { app.state.value.contributions.size == 1 }
        ui.onNodeWithTag("tab-profile").performClick()
        ui.onNodeWithText("School supplies").performScrollTo().assertIsDisplayed()
        assertTrue(app.state.value.pendingOperations.isEmpty())
        assertEquals(1, app.state.value.impact.centersSupported)
    }
}
