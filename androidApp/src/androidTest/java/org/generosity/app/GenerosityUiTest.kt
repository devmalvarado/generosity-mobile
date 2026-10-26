package org.generosity.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
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
        composeRule.onNodeWithText("Lista").performClick()
        val list = composeRule.onNode(hasScrollAction())
        list.performScrollToNode(hasText("Hogar Futuros Brillantes"))
        composeRule.onNodeWithText("Hogar Futuros Brillantes").performClick()
        list.performScrollToNode(hasText("Necesidades actuales"))
        composeRule.onNodeWithText("Necesidades actuales").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Utiles escolares").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun profileShowsAnonymousEmptyState() {
        composeRule.onNodeWithText("Profile").performClick()
        composeRule.onNodeWithText("Anonymous browsing").assertIsDisplayed()
        composeRule.onNodeWithText("No contributions recorded yet.").assertIsDisplayed()
    }
}
