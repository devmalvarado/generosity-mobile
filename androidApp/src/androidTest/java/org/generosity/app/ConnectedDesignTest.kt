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

class ConnectedDesignTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    @After fun close() { scope.cancel() }

    @Test fun connectedOfferUsesServerIdAndSharedOutboxHistory() {
        val disk = mutableMapOf<String, String>()
        var accept = false
        var offline = false
        var contributionAccess = true
        var previewAccess = true
        var requestBody: String? = null
        var firstKey: String? = null
        val connection = object : AppConnection {
            override fun read(key: String) = disk[key].orEmpty()
            override fun write(key: String, value: String?) { if (value == null) disk.remove(key) else disk[key] = value }
            override fun newOperationId() = java.util.UUID.randomUUID().toString()
            override fun send(request: ApiRequest, completion: (ApiResponse) -> Unit) {
                val response = when {
                    request.path.endsWith("/features") -> ApiResponse(200, JSONObject().put("features", JSONObject().apply {
                        put("contributions.create", contributionAccess); put("needs.progress", true)
                        org.generosity.preview.PreviewSession.flagNames.forEach { put(it, previewAccess) }
                    }).toString())
                    request.path == "/api/v1/sync/catalog?category_version=2&progress_version=1" -> if (offline) ApiResponse(0, "") else ApiResponse(200, """{"catalog":${BundledKindnessCatalog.JSON},"updatedAt":"2026-11-01T00:00:00Z"}""")
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
        ui.setContent { GenerosityApp(app, GoogleCenterMapRenderer, placeSearch = { query, _ ->
            assertEquals("Neighborhood", query)
            listOf(DiscoveryArea("Neighborhood", BundledKindnessCatalog.centers().first { it.id == "kc-children-001" }.coordinates))
        }) }
        ui.runOnIdle { app.signIn("1", "María", remoteAccount = true) }
        ui.waitUntil(5000) { app.state.value.centers.isNotEmpty() }
        ui.onNodeWithTag("open-native-preview").performClick()
        ui.onNodeWithTag("preview-pin-c1").assertDoesNotExist()
        ui.onNodeWithTag("preview-list").performClick()
        ui.onNodeWithTag("connected-center-kc-children-001").performScrollTo().performClick()
        ui.onNodeWithTag("detail-kc-children-001").assertExists()
        ui.runOnIdle { offline = true; app.onResume() }
        ui.waitUntil(5000) { app.state.value.syncError != null && !app.state.value.syncing }
        ui.onNodeWithText("School supplies").performScrollTo().assertIsDisplayed()
        ui.runOnIdle { offline = false; app.onResume() }
        ui.waitUntil(5000) { !app.state.value.syncing && app.state.value.syncError == null }
        ui.onNodeWithText("Offer goods").performScrollTo()
        ui.onNodeWithText("Offer goods").performScrollTo().performClick()
        ui.onNodeWithText("Quantity").performTextInput("3")
        ui.onNodeWithText("Notes (optional)").performTextInput("Niñas: útiles escolares")
        ui.onNodeWithText("Save contribution").performClick()
        ui.waitUntil(5000) { app.state.value.savedOffer == 1 && !app.state.value.writing }
        assertEquals("need-school-supplies", JSONObject(requestBody!!).getString("need_id"))
        assertEquals(3, JSONObject(requestBody!!).getInt("quantity"))
        assertEquals("Niñas: útiles escolares", JSONObject(requestBody!!).getString("notes"))
        assertEquals(1, BackendStore(connection).pending("1").size)
        assertTrue(app.state.value.contributions.isEmpty())
        ui.runOnIdle { accept = true; app.onResume() }
        ui.waitUntil(5000) { app.state.value.contributions.size == 1 }
        ui.onNodeWithTag("preview-screens").performClick()
        ui.onNodeWithTag("preview-route-history").performScrollTo().performClick()
        ui.onNodeWithText("School supplies").performScrollTo().assertIsDisplayed()
        assertTrue(app.state.value.pendingOperations.isEmpty())
        assertEquals(1, app.state.value.impact.centersSupported)
        ui.onNodeWithTag("preview-tab-map").performClick()
        ui.onNodeWithTag("connected-query").performTextInput("Neighborhood")
        ui.onNodeWithTag("connected-query").performImeAction()
        ui.onNodeWithTag("area-query").assertTextContains("Neighborhood")
        ui.onNodeWithTag("area-submit").performClick()
        ui.onNodeWithTag("area-result-0").performClick()
        ui.waitUntil(5000) { app.state.value.discoveryArea?.label == "Neighborhood" && app.state.value.searchText.isEmpty() }
        ui.onNodeWithTag("preview-tab-help").performClick()
        ui.runOnIdle { contributionAccess = false; app.onResume() }
        ui.waitUntil(5000) { !app.state.value.syncing && !app.state.value.canRecordContributions }
        ui.onNodeWithText("Offer goods").performScrollTo().assertIsNotEnabled()
        ui.runOnIdle { previewAccess = false; app.onResume() }
        ui.waitUntil(5000) { app.state.value.features.previewGroups.isEmpty() }
        ui.onNodeWithTag("preview-screens").assertDoesNotExist()
        ui.onNodeWithTag("tab-map").assertExists()
    }
}
