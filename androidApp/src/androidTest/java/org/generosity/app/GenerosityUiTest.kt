package org.generosity.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class GenerosityUiTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun anonymousDiscoverySupportsLanguageSwitchingAndCenterDetails() {
        composeRule.onNodeWithText("Generosity").assertIsDisplayed()
        composeRule.onNodeWithText("Spanish").performClick()
        composeRule.onNodeWithText("Centros de bondad cercanos").assertIsDisplayed()
        composeRule.onNodeWithText("Hogar Futuros Brillantes").performClick()
        composeRule.onNodeWithText("Necesidades actuales").assertIsDisplayed()
        composeRule.onNodeWithText("Utiles escolares").assertIsDisplayed()
    }

    @Test
    fun profileShowsAnonymousEmptyState() {
        composeRule.onNodeWithText("Profile").performClick()
        composeRule.onNodeWithText("Anonymous browsing").assertIsDisplayed()
        composeRule.onNodeWithText("No contributions recorded yet.").assertIsDisplayed()
    }
}

