package org.generosity.app

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import org.generosity.domain.DiscoveryArea
import org.generosity.domain.Coordinates
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.generosity.app.ui.GenerosityApp
import org.generosity.app.ui.map.CenterMapMarker
import org.generosity.app.ui.map.CenterMapRenderer
import org.generosity.app.ui.map.MapLoadFeedback
import org.generosity.app.ui.map.MapUnavailable
import org.generosity.app.ui.map.toMapMarkers
import org.generosity.domain.SupportedLocale
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MapDiscoveryTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val app = GenerosityAppState(scope)
    private val probe = ProbeMapRenderer()

    @After
    fun tearDown() = scope.cancel()

    @Test
    fun selectedDetailOpensItsOwnCoordinatesInBothMapChoices() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        var opened: Uri? = null
        val monitor = object : Instrumentation.ActivityMonitor() {
            override fun onStartActivity(intent: Intent): Instrumentation.ActivityResult? {
                if (intent.action != Intent.ACTION_VIEW) return null
                opened = intent.data
                return Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null)
            }
        }
        instrumentation.addMonitor(monitor)
        try {
            launch()
            val centers = app.state.value.centers
            for (locale in listOf(SupportedLocale.ENGLISH, SupportedLocale.SPANISH)) {
                composeRule.runOnIdle { app.setLocale(locale) }
                composeRule.onNodeWithTag("tab-list").performClick()
                for (center in listOf(centers.first(), centers.last())) {
                    composeRule.onNodeWithTag("discovery-scroll").performScrollToNode(hasTestTag("center-${center.id}"))
                    composeRule.onNodeWithTag("center-${center.id}").performClick()
                    for (choice in listOf("google", "device")) {
                        composeRule.runOnIdle { opened = null }
                        composeRule.onNodeWithTag("map-open-$choice").performScrollTo().performClick()
                        composeRule.runOnIdle {
                            val uri = requireNotNull(opened)
                            val point = "${center.coordinates.latitude},${center.coordinates.longitude}"
                            if (choice == "google") assertEquals(point, uri.getQueryParameter("query"))
                            else assertEquals("$point(${center.name.resolve(locale)})", Uri.decode(uri.toString().substringAfter("?q=")))
                        }
                    }
                    composeRule.onNodeWithTag("detail-back").performClick()
                }
            }
        } finally { instrumentation.removeMonitor(monitor) }
    }

    @Test
    fun everyCardSelectsItsPinAndOpensItsOwnDetail() {
        launch()
        val ids = app.state.value.centers.map { it.id }
        val mapBounds = composeRule.onNodeWithTag("renderer-probe").getUnclippedBoundsInRoot()
        for (id in ids.reversed()) {
            composeRule.onNodeWithTag("discovery-scroll").performScrollToNode(hasTestTag("center-$id"))
            composeRule.onNodeWithTag("center-$id").performClick()
            composeRule.onNodeWithTag("renderer-probe").assertIsDisplayed()
            composeRule.runOnIdle { assertEquals(id, probe.selectedCenterId) }
            assertEquals(mapBounds, composeRule.onNodeWithTag("renderer-probe").getUnclippedBoundsInRoot())
            composeRule.onNodeWithTag("discovery-scroll").performScrollToNode(hasTestTag("details-$id"))
            composeRule.onNodeWithTag("details-$id").performClick()
            val name = app.state.value.selectedCenter!!.name.resolve(SupportedLocale.ENGLISH)
            composeRule.onNode(hasText(name) and hasAnyAncestor(hasTestTag("detail-$id"))).assertIsDisplayed()
            composeRule.onNodeWithTag("detail-back").performClick()
        }
        composeRule.onNodeWithTag("tab-list").performClick()
        val id = ids.last()
        composeRule.onNodeWithTag("discovery-scroll").performScrollToNode(hasTestTag("center-$id"))
        composeRule.onNodeWithTag("center-$id").performClick()
        composeRule.onNodeWithTag("detail-$id").assertIsDisplayed()
        composeRule.onNodeWithTag("detail-back").performClick()
        composeRule.onNodeWithTag("tab-map").performClick()
        composeRule.onNodeWithTag("renderer-probe").assertIsDisplayed()
        composeRule.runOnIdle { assertEquals(id, probe.selectedCenterId) }
        val other = ids.first()
        composeRule.onNodeWithTag("marker-$other").performClick()
        composeRule.onNodeWithTag("discovery-scroll").performScrollToNode(hasTestTag("details-$other"))
        composeRule.onNodeWithTag("details-$other").performClick()
        composeRule.onNodeWithTag("detail-$other").assertIsDisplayed()
    }

    @Test
    fun replacementRendererUsesSharedFiltersAndSelectionWithoutProviderTypes() {
        launch()
        composeRule.onNodeWithText("Health", useUnmergedTree = true).performScrollTo().performClick()
        showMap()
        composeRule.runOnIdle {
            assertEquals(app.state.value.centers.toMapMarkers(app.state.value.locale), probe.markers)
            assertEquals(1, probe.markers.size)
        }
        val marker = probe.markers.single()
        composeRule.onNodeWithTag("marker-${marker.centerId}").performClick()
        composeRule.runOnIdle { assertEquals(marker.centerId, app.state.value.selectedCenterId) }
        composeRule.onNodeWithText("List").performClick()
        composeRule.onNodeWithTag("discovery-scroll").performScrollToNode(hasText(marker.title))
        composeRule.onNodeWithTag("center-${marker.centerId}").performClick()
        composeRule.onNodeWithText("Current needs").performScrollTo()
        composeRule.onNodeWithText("Current needs").assertIsDisplayed()
        composeRule.onNodeWithTag("detail-back").performClick()
        composeRule.onNodeWithText("Map").performClick()
        showMap()
        composeRule.runOnIdle {
            assertEquals(listOf(marker), probe.markers)
            assertEquals(marker.centerId, probe.selectedCenterId)
        }
    }

    @Test
    fun renderingStateSurvivesTabsAndLocaleChangesWithoutResettingFilters() {
        launch()
        composeRule.onNodeWithText("Health", useUnmergedTree = true).performScrollTo().performClick()
        showMap()
        composeRule.onNodeWithText("Test camera: 0").performClick()
        composeRule.onNodeWithText("List").performClick()
        composeRule.onNodeWithTag("tab-settings").performClick()
        composeRule.onNodeWithTag("language-es").performClick()
        composeRule.onNodeWithText("Mapa").performClick()
        showMap()
        composeRule.onNodeWithText("Test camera: 1").assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals(app.state.value.centers.toMapMarkers(SupportedLocale.SPANISH), probe.markers)
            assertEquals(1, probe.markers.size)
        }
    }

    @Test
    fun emptySearchRemovesMarkersAndPreservesAnonymousBrowsing() {
        launch()
        composeRule.onNodeWithText("Search centers").performTextInput("no-such-center-123")
        composeRule.onNodeWithText("Search centers").performImeAction()
        showMap()
        composeRule.runOnIdle { assertTrue(probe.markers.isEmpty()) }
        composeRule.onNodeWithText("List").performClick()
        composeRule.onNodeWithTag("discovery-scroll").performScrollToNode(hasText("No kindness centers found."))
        composeRule.onNodeWithText("No kindness centers found.").assertIsDisplayed()
    }

    @Test
    fun unavailableRendererLeavesListAndDetailsUsable() {
        launch(unavailable = true)
        showMap()
        composeRule.onNodeWithTag("map-unavailable").assertIsDisplayed()
        composeRule.onNodeWithText("List").performClick()
        val title = app.state.value.centers.first().name.resolve(SupportedLocale.ENGLISH)
        composeRule.onNodeWithTag("discovery-scroll").performScrollToNode(hasText(title))
        composeRule.onNodeWithText(title).performClick()
        composeRule.onNodeWithText("Current needs").performScrollTo()
        composeRule.onNodeWithText("Current needs").assertIsDisplayed()
    }

    @Test
    fun delayedLoadingOffersRetryAndLateSuccessClearsFeedback() {
        val loaded = mutableStateOf(false)
        var retries = 0
        composeRule.setContent {
            MaterialTheme {
                MapLoadFeedback(loaded.value, SupportedLocale.SPANISH, onRetry = { retries++ })
            }
        }
        composeRule.onNodeWithText("Cargando mapa…").assertIsDisplayed()
        composeRule.mainClock.advanceTimeBy(15_100)
        composeRule.onNodeWithText("El mapa está tardando en cargar. Puedes seguir consultando la lista de centros.").assertIsDisplayed()
        composeRule.onNodeWithText("Reintentar").performClick()
        composeRule.runOnIdle { assertEquals(1, retries); loaded.value = true }
        composeRule.onNodeWithText("Reintentar").assertDoesNotExist()
    }

    @Test
    fun placeSearchRequiresSelectionPreservesAreaOnFailureAndCanClear() {
        var fail = false
        var queryReceived = ""
        val place = DiscoveryArea("Coyoacán, México", Coordinates(19.35, -99.16))
        composeRule.setContent { GenerosityApp(app, probe, placeSearch = { query, _ ->
            queryReceived = query
            if (fail) error("offline")
            if (query == "No results") emptyList() else listOf(place)
        }) }
        composeRule.runOnIdle { app.start() }
        composeRule.onNodeWithTag("area-open").performClick()
        composeRule.onNodeWithTag("area-query").performTextInput("Coyoacán México")
        composeRule.onNodeWithTag("area-submit").performClick()
        composeRule.onNodeWithTag("area-result-0").assertIsDisplayed()
        composeRule.runOnIdle { assertEquals("Coyoacán México", queryReceived); assertEquals(null, app.state.value.discoveryArea) }
        composeRule.onNodeWithTag("area-result-0").performClick()
        composeRule.runOnIdle { assertEquals(place, app.state.value.discoveryArea); assertEquals(place.coordinates, probe.origin); fail = true }
        composeRule.onNodeWithTag("area-open").performClick()
        composeRule.onNodeWithTag("area-query").performTextInput("Unknown")
        composeRule.onNodeWithTag("area-submit").performClick()
        composeRule.onNodeWithText("Place search is unavailable. Check your connection and try again.").assertIsDisplayed()
        composeRule.onNodeWithText("Back").performClick()
        composeRule.runOnIdle { assertEquals(place, app.state.value.discoveryArea); fail = false }
        composeRule.onNodeWithTag("area-open").performClick()
        composeRule.onNodeWithTag("area-query").performTextInput("No results")
        composeRule.onNodeWithTag("area-submit").performClick()
        composeRule.onNodeWithText("No places found. Try adding the city or country.").assertIsDisplayed()
        composeRule.onNodeWithText("Back").performClick()
        composeRule.runOnIdle { assertEquals(place, app.state.value.discoveryArea) }
        composeRule.onNodeWithTag("discovery-scroll").performScrollToNode(hasTestTag("area-clear"))
        composeRule.onNodeWithTag("area-clear").performClick()
        composeRule.runOnIdle { assertEquals(null, app.state.value.discoveryArea); assertEquals(3, app.state.value.centers.size) }
    }

    private fun launch(unavailable: Boolean = false) {
        probe.unavailable = unavailable
        composeRule.setContent { GenerosityApp(app, probe) }
        composeRule.runOnIdle { app.start() }
    }

    private fun showMap() {
        composeRule.onNodeWithTag("renderer-probe").assertIsDisplayed()
    }
}

private class ProbeMapRenderer : CenterMapRenderer {
    var markers = emptyList<CenterMapMarker>()
    var selectedCenterId: String? = null
    var unavailable = false
    var origin: Coordinates? = null

    @Composable
    override fun Render(
        markers: List<CenterMapMarker>,
        selectedCenterId: String?,
        locale: SupportedLocale,
        onCenterSelected: (String) -> Unit,
        modifier: Modifier,
        origin: org.generosity.domain.Coordinates?,
        originLabel: String?
    ) {
        var cameraEdits by rememberSaveable { mutableIntStateOf(0) }
        SideEffect { this.markers = markers; this.selectedCenterId = selectedCenterId; this.origin = origin }
        Column(modifier.testTag("renderer-probe")) {
            if (unavailable) {
                MapUnavailable(locale)
            } else {
                TextButton(onClick = { cameraEdits++ }) { Text("Test camera: $cameraEdits") }
                markers.forEach { marker ->
                    TextButton(
                        onClick = { onCenterSelected(marker.centerId) },
                        modifier = Modifier.testTag("marker-${marker.centerId}")
                    ) { Text("Marker: ${marker.title}") }
                }
            }
        }
    }
}
