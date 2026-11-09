package org.generosity.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

class PreviewEntryTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()

    @Test fun launchAndRecreationKeepConnectedDesign() {
        // Read-only acceptance against the configured API; discovery must be enabled for guests.
        val guest = hasText("Continue as a guest") or hasText("Continuar como invitado")
        ui.waitUntil(30_000) {
            ui.onAllNodes(guest).fetchSemanticsNodes().isNotEmpty() ||
                ui.onAllNodesWithTag("connected-query").fetchSemanticsNodes().isNotEmpty()
        }
        if (ui.onAllNodes(guest).fetchSemanticsNodes().isNotEmpty()) ui.onNode(guest).performScrollTo().performClick()
        repeat(2) { pass ->
            ui.waitUntil(30_000) { ui.onAllNodesWithTag("connected-query").fetchSemanticsNodes().isNotEmpty() }
            ui.onNodeWithTag("connected-query").assertIsDisplayed()
            ui.onNodeWithTag("open-native-preview").assertDoesNotExist()
            ui.onNodeWithTag("preview-pin-c1").assertDoesNotExist()
            ui.onNodeWithTag("preview-screens").performClick()
            ui.onNodeWithText("Exit design preview").assertDoesNotExist()
            if (BuildConfig.CHANNEL != "preview") {
                listOf("saved", "reviews", "achievements", "events", "donate").forEach {
                    ui.onNodeWithTag("preview-route-$it").assertDoesNotExist()
                }
            }
            ui.onNodeWithText("Salir del diseño").assertDoesNotExist()
            ui.onNodeWithTag("preview-route-map").performScrollTo().performClick()
            if (pass == 0) ui.activityRule.scenario.recreate()
        }
    }
}
