package org.generosity.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import org.junit.Rule
import org.junit.Before
import org.junit.Test

class GenerosityUiTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<FixtureActivity>()

    @Before
    fun useEnglish() {
        composeRule.onNodeWithTag("tab-settings").performClick()
        composeRule.onNodeWithTag("language-en").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodes(hasText("Use device language")).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("tab-map").performClick()
    }

    @Test
    fun anonymousDiscoverySupportsLanguageSwitchingAndCenterDetails() {
        composeRule.onNodeWithTag("brand-wordmark").assertIsDisplayed()
        composeRule.onNodeWithTag("tab-settings").performClick()
        composeRule.onNodeWithTag("language-es").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodes(hasText("Usar idioma del dispositivo")).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("tab-map").performClick()
        composeRule.onNodeWithText("Centros de bondad cercanos").assertIsDisplayed()
        composeRule.onNodeWithText("Lista").performClick()
        val list = composeRule.onNodeWithTag("discovery-scroll")
        list.performScrollToNode(hasText("Hogar Futuros Brillantes"))
        composeRule.onNodeWithText("Hogar Futuros Brillantes").performClick()
        list.performScrollToNode(hasText("Necesidades actuales"))
        composeRule.onNodeWithText("Necesidades actuales").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Útiles escolares").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun profileShowsAnonymousEmptyState() {
        composeRule.onNodeWithText("Profile").performClick()
        composeRule.onNodeWithText("Browsing as a guest").assertIsDisplayed()
        composeRule.onNodeWithText("No contributions recorded yet.").assertIsDisplayed()
    }

    @Test
    fun languageOverrideSurvivesActivityRestartAndCanReturnToDevice() {
        composeRule.onNodeWithTag("tab-settings").performClick()
        composeRule.onNodeWithTag("language-es").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodes(hasText("Usar idioma del dispositivo")).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.activityRule.scenario.recreate()
        composeRule.onNodeWithTag("tab-settings").performClick()
        composeRule.onNodeWithTag("language-es").assertIsSelected()
        composeRule.onNodeWithText("Usar idioma del dispositivo").assertIsDisplayed()
        composeRule.onNodeWithTag("language-system").performClick()
        composeRule.waitUntil(5_000) {
            AndroidLanguageSettings(composeRule.activity).load().selectedLocale == null
        }
        composeRule.activityRule.scenario.recreate()
        composeRule.onNodeWithTag("tab-settings").performClick()
        composeRule.onNodeWithTag("language-system").assertIsSelected()
    }

    @Test
    fun languageChangeKeepsSearchAndRefreshesLocalizedResults() {
        composeRule.onNodeWithTag("tab-list").performClick()
        composeRule.onNodeWithText("Search centers").performTextInput("Hogar")
        composeRule.onNodeWithText("Search centers").performImeAction()
        // IME dismissal updates the navigation semantics after Compose becomes idle.
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodes(hasTestTag("tab-settings")).fetchSemanticsNodes().size == 1
        }
        composeRule.onNodeWithTag("tab-settings").performClick()
        composeRule.onNodeWithTag("language-es").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodes(hasText("Usar idioma del dispositivo")).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("tab-list").performClick()
        composeRule.onNodeWithText("Hogar").assertIsDisplayed()
        composeRule.onNodeWithTag("discovery-scroll").performScrollToNode(hasText("Hogar Futuros Brillantes"))
        composeRule.onNodeWithText("Hogar Futuros Brillantes").assertIsDisplayed()
    }

    @Test
    fun sharedCategoryAndNeedDetailSurviveRepeatedNavigation() {
        composeRule.onNodeWithTag("tab-list").performClick()
        composeRule.onNodeWithText("Support for children").performClick()
        composeRule.onNodeWithText("Golden Years Community Kitchen").assertDoesNotExist()
        composeRule.onNodeWithText("Bright Futures Home").performClick()
        repeat(3) {
            composeRule.onNodeWithTag("tab-map").performClick()
            composeRule.onNodeWithTag("tab-settings").performClick()
            composeRule.onNodeWithTag("tab-list").performClick()
            composeRule.onNodeWithTag("discovery-scroll").performScrollToNode(hasText("15 items still needed"))
            composeRule.onNodeWithText("School supplies").assertIsDisplayed()
            composeRule.onNodeWithText("15 items still needed").assertIsDisplayed()
            composeRule.onNodeWithText("Urgent").assertIsDisplayed()
        }
    }
}
