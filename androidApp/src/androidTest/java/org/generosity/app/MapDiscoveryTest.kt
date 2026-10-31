package org.generosity.app

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
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
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
        composeRule.onNode(hasText(marker.title) and hasClickAction()).assertIsDisplayed()
        composeRule.onNodeWithTag("discovery-scroll").performScrollToNode(hasText("Current needs"))
        composeRule.onNodeWithText("Current needs").assertIsDisplayed()
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
        composeRule.onNodeWithTag("discovery-scroll").performScrollToNode(hasText("Current needs"))
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

    private fun launch(unavailable: Boolean = false) {
        probe.unavailable = unavailable
        composeRule.setContent { GenerosityApp(app, probe) }
        composeRule.runOnIdle { app.start() }
    }

    private fun showMap() {
        composeRule.onNodeWithTag("discovery-scroll").performScrollToNode(hasTestTag("renderer-probe"))
    }
}

private class ProbeMapRenderer : CenterMapRenderer {
    var markers = emptyList<CenterMapMarker>()
    var selectedCenterId: String? = null
    var unavailable = false

    @Composable
    override fun Render(
        markers: List<CenterMapMarker>,
        selectedCenterId: String?,
        locale: SupportedLocale,
        onCenterSelected: (String) -> Unit,
        modifier: Modifier
    ) {
        var cameraEdits by rememberSaveable { mutableIntStateOf(0) }
        SideEffect { this.markers = markers; this.selectedCenterId = selectedCenterId }
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
