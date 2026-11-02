package org.generosity.app

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.os.SystemClock
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.*
import org.generosity.app.ui.GenerosityApp
import org.generosity.app.ui.map.CenterMapMarker
import org.generosity.app.ui.map.CenterMapRenderer
import org.generosity.domain.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class CurrentLocationTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val app = GenerosityAppState(scope)
    private val source = Source()
    private val map = MapProbe()
    @After fun close() = scope.cancel()

    private fun launch() {
        app.setLocale(SupportedLocale.ENGLISH)
        ui.setContent { GenerosityApp(app, map, locationSource = source) }
        ui.runOnIdle { app.start() }
    }

    @Test fun automaticApproximateOriginFiltersBothTabsAndManualClearStopsResume() {
        // Use an actual fixture coordinate, never a device's private GPS in a test.
        source.point = org.generosity.data.BundledKindnessCatalog.centers().first().coordinates
        launch()
        ui.onNodeWithTag("discovery-origin-label").assertTextEquals("Centers within 10 km of your location")
        ui.onNodeWithText("Approximate location; distances may vary.").assertIsDisplayed()
        ui.runOnIdle {
            assertEquals(source.point, app.state.value.discoveryArea?.coordinates)
            assertEquals(source.point, map.origin)
            assertEquals("Your location", map.label)
            assertTrue(app.state.value.centers.all { it.coordinates.distanceMetersTo(source.point) <= 10_000 })
        }
        ui.onNodeWithTag("tab-list").performClick()
        ui.onNodeWithTag("discovery-origin-label").assertTextEquals("Centers within 10 km of your location")
        ui.onNodeWithTag("area-clear").performClick()
        val calls = source.calls
        ui.activityRule.scenario.moveToState(Lifecycle.State.STARTED)
        ui.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        ui.runOnIdle { assertNull(app.state.value.discoveryArea); assertEquals(calls, source.calls) }
        ui.onNodeWithTag("location-use").performClick()
        ui.onNodeWithTag("discovery-origin-label").assertTextEquals("Centers within 10 km of your location")
    }

    @Test fun deviceOffAndRevocationOfferCorrectSettingsAndReturnReacquires() {
        source.enabled = false
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        var opened: Intent? = null
        val monitor = object : Instrumentation.ActivityMonitor() {
            override fun onStartActivity(intent: Intent): Instrumentation.ActivityResult? {
                if (intent.action !in listOf(Settings.ACTION_LOCATION_SOURCE_SETTINGS, Settings.ACTION_APPLICATION_DETAILS_SETTINGS)) return null
                opened = intent
                return Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null)
            }
        }
        instrumentation.addMonitor(monitor)
        try {
            launch()
            ui.onNodeWithTag("location-status").assertTextContains("Device location is turned off.", substring = true)
            ui.onNodeWithTag("location-settings").performClick()
            ui.runOnIdle { assertEquals(Settings.ACTION_LOCATION_SOURCE_SETTINGS, opened?.action) }
            ui.activityRule.scenario.moveToState(Lifecycle.State.STARTED)
            source.enabled = true; source.access = LocationAccess.NONE
            ui.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
            ui.onNodeWithTag("location-status").assertTextContains("Location access is off.", substring = true)
            ui.onNodeWithTag("location-settings").performClick()
            ui.runOnIdle {
                assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, opened?.action)
                assertEquals("package:org.generosity.app", opened?.data.toString())
                assertNull(app.state.value.discoveryArea)
            }
            ui.activityRule.scenario.moveToState(Lifecycle.State.STARTED)
            source.access = LocationAccess.APPROXIMATE
            ui.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
            ui.onNodeWithTag("discovery-origin-label").assertTextEquals("Centers within 10 km of your location")
            ui.onNodeWithTag("location-status").assertDoesNotExist()
        } finally { instrumentation.removeMonitor(monitor) }
    }

    private class Source : DeviceLocationSource {
        override var access = LocationAccess.APPROXIMATE
        override var enabled = true
        var point = Coordinates(19.35, -99.16)
        var calls = 0
        override suspend fun currentFix(): DeviceLocationFix {
            calls++
            return DeviceLocationFix(point, 1_500f, SystemClock.elapsedRealtime())
        }
    }
    private class MapProbe : CenterMapRenderer {
        var origin: Coordinates? = null
        var label: String? = null
        @Composable override fun Render(markers: List<CenterMapMarker>, selectedCenterId: String?, locale: SupportedLocale,
            onCenterSelected: (String) -> Unit, modifier: Modifier, origin: Coordinates?, originLabel: String?) {
            SideEffect { this.origin = origin; label = originLabel }
            Box(modifier.testTag("location-map")) { Text("Map") }
        }
    }
}
