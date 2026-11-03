package org.generosity.app

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.*
import org.generosity.app.ui.GenerosityApp
import org.generosity.app.ui.map.google.GoogleCenterMapRenderer
import org.generosity.data.*
import org.generosity.domain.*
import org.json.JSONArray
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*

class NeedCatalogSyncTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    @After fun close() = scope.cancel()

    @Test fun publishedNeedsRefreshInSpanish() = checkLifecycle(SupportedLocale.SPANISH)
    @Test fun publishedNeedsRefreshInEnglish() = checkLifecycle(SupportedLocale.ENGLISH)

    @Test fun publishedDemoUsesAndroidHttpsAndStorage() {
        // Explicit opt-in keeps the regression suite independent of live demo data/network.
        Assume.assumeTrue(InstrumentationRegistry.getArguments().getString("liveCatalog") == "true")
        val connection = AndroidConnection(InstrumentationRegistry.getInstrumentation().targetContext, scope)
        val app = GenerosityAppState(scope, backend = BackendStore(connection))
        ui.setContent { GenerosityApp(app, GoogleCenterMapRenderer) }
        ui.runOnIdle { app.start(); app.setSearchText("DEMO") }
        ui.waitUntil(30000) { !app.state.value.syncing && app.state.value.centers.isNotEmpty() }
        assertNull(app.state.value.syncError)
        ui.onNodeWithTag("tab-list").performClick()
        ui.onNodeWithTag("discovery-scroll").performScrollToNode(hasTestTag("center-demo-generosity-20261102"))
        ui.onNodeWithTag("center-demo-generosity-20261102").performClick()
        val need = app.state.value.currentNeeds.single { it.id == "ca6be1ba-7d4e-45fe-8db6-1db5bacbd985" }
        ui.onNodeWithText(need.title.resolve(app.state.value.locale)).performScrollTo().assertIsDisplayed()
        ui.onNodeWithText("${need.quantityRemaining} items still needed").performScrollTo().assertIsDisplayed()
        assertEquals(need, runBlocking { BackendStore(connection).needById(need.id) })
    }

    private fun checkLifecycle(locale: SupportedLocale) {
        val spanish = locale == SupportedLocale.SPANISH
        val empty = if (spanish) "No hay necesidades actuales publicadas." else "No current needs are listed."
        val title = if (spanish) "Útiles escolares" else "School supplies"
        val disk = mutableMapOf<String, String>()
        val catalog = JSONObject(BundledKindnessCatalog.JSON)
        val center = catalog.getJSONArray("centers").getJSONObject(0)
        val need = center.getJSONArray("needs").getJSONObject(0)
            .put("quantityCommitted", 0).put("quantityRequired", 10).put("urgent", false)
        catalog.put("centers", JSONArray().put(center))
        var published = false
        var offline = false
        val connection = object : AppConnection {
            override fun read(key: String) = disk[key].orEmpty()
            override fun write(key: String, value: String?) { if (value == null) disk.remove(key) else disk[key] = value }
            override fun newOperationId() = java.util.UUID.randomUUID().toString()
            override fun send(request: ApiRequest, completion: (ApiResponse) -> Unit) {
                assertEquals("GET", request.method)
                assertEquals("/api/v1/sync/catalog?category_version=2&progress_version=1", request.path)
                center.put("needs", if (published) JSONArray().put(need) else JSONArray())
                completion(if (offline) ApiResponse(0, "") else ApiResponse(200,
                    JSONObject().put("catalog", catalog).put("updatedAt", "2026-11-02T12:00:00Z").toString()))
            }
        }
        val app = GenerosityAppState(scope, backend = BackendStore(connection),
            initialLocalePreference = LocalePreference(selectedLocale = locale), clock = { "2026-11-02T12:00:00Z" })
        ui.setContent { GenerosityApp(app, GoogleCenterMapRenderer) }
        ui.runOnIdle { app.start() }
        ui.waitUntil(5000) { app.state.value.centers.size == 1 && !app.state.value.syncing }
        ui.onNodeWithTag("tab-list").performClick()
        ui.onNodeWithTag("discovery-scroll").performScrollToNode(hasTestTag("center-kc-children-001"))
        ui.onNodeWithTag("center-kc-children-001").performClick()
        ui.onNodeWithText(empty).performScrollTo().assertIsDisplayed()

        ui.runOnIdle { published = true }
        ui.onNodeWithTag("detail-back").performClick()
        ui.onNodeWithTag("discovery-scroll").performScrollToIndex(0)
        ui.onNodeWithText(if (spanish) "Actualizar" else "Refresh").performClick()
        ui.waitUntil(5000) { app.state.value.currentNeeds.singleOrNull()?.quantityRemaining == 10 }
        ui.onNodeWithTag("discovery-scroll").performScrollToNode(hasTestTag("center-kc-children-001"))
        ui.onNodeWithTag("center-kc-children-001").performClick()
        ui.onNodeWithText(title).performScrollTo().assertIsDisplayed()
        ui.onNodeWithText(if (spanish) "Faltan 10 unidades" else "10 items still needed").performScrollTo().assertIsDisplayed()
        ui.onNodeWithText("Normal").performScrollTo().assertIsDisplayed()

        // Resume must refresh the currently open detail, not only the discovery cards.
        ui.runOnIdle { need.put("quantityRequired", 20).put("urgent", true); app.onResume() }
        ui.waitUntil(5000) { app.state.value.currentNeeds.singleOrNull()?.quantityRemaining == 20 }
        ui.onNodeWithText(if (spanish) "Faltan 20 unidades" else "20 items still needed").performScrollTo().assertIsDisplayed()
        ui.onNodeWithText(if (spanish) "Urgente" else "Urgent").performScrollTo().assertIsDisplayed()
        ui.onNodeWithText(if (spanish) "Faltan 10 unidades" else "10 items still needed").assertDoesNotExist()

        ui.runOnIdle { offline = true; app.onResume() }
        ui.waitUntil(5000) { app.state.value.syncError != null && !app.state.value.syncing }
        ui.onNodeWithText(title).performScrollTo().assertIsDisplayed()
        assertEquals(20, runBlocking { BackendStore(connection).needById("need-school-supplies") }?.quantityRemaining)

        ui.runOnIdle { offline = false; published = false; app.onResume() }
        ui.waitUntil(5000) { app.state.value.currentNeeds.isEmpty() && !app.state.value.syncing && app.state.value.syncError == null }
        ui.onNodeWithText(empty).performScrollTo().assertIsDisplayed()
        ui.onNodeWithText(title).assertDoesNotExist()
        assertNull(runBlocking { BackendStore(connection).needById("need-school-supplies") })
    }
}
