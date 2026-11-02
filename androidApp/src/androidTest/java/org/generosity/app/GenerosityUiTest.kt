package org.generosity.app

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasAnyAncestor
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
import org.junit.Assert.*

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
        composeRule.onNodeWithText("Sign in").assertIsDisplayed()
        composeRule.onNodeWithText("Create account").assertIsDisplayed()
        composeRule.onNodeWithText("No contributions recorded yet.").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun profileOpensTheSelectedHostedAccountMode() {
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
            composeRule.onNodeWithTag("tab-profile").performClick()
            for ((label, screen) in listOf("Create account" to "register", "Sign in" to null)) {
                composeRule.runOnIdle { opened = null }
                composeRule.onNodeWithText(label).performScrollTo().performClick()
                composeRule.runOnIdle {
                    val uri = requireNotNull(opened) { "Account button did not open the browser" }
                    assertEquals(BuildConfig.API_BASE_URL + "/auth/sign-in", uri.toString().substringBefore('?'))
                    assertEquals(screen, uri.getQueryParameter("screen"))
                    assertEquals("android", uri.getQueryParameter("client"))
                    assertEquals("en", uri.getQueryParameter("locale"))
                    assertEquals(43, uri.getQueryParameter("state")?.length)
                    assertEquals(43, uri.getQueryParameter("code_challenge")?.length)
                    assertFalse(uri.queryParameterNames.contains("code_verifier"))
                }
            }
        } finally {
            instrumentation.removeMonitor(monitor)
        }
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
    fun onlyVerifiedCentersShowAnAccessibleCheckmark() {
        for ((language, label) in listOf("en" to "Verified", "es" to "Verificado")) {
            composeRule.onNodeWithTag("tab-settings").performClick()
            composeRule.onNodeWithTag("language-$language").performClick()
            composeRule.onNodeWithTag("tab-list").performClick()
            for (id in listOf("kc-children-001", "kc-seniors-001", "kc-health-001")) {
                val card = "center-$id"
                composeRule.onNodeWithTag("discovery-scroll").performScrollToNode(hasTestTag(card))
                val badge = composeRule.onNode(hasContentDescription(label) and hasAnyAncestor(hasTestTag(card)), useUnmergedTree = true)
                if (id == "kc-health-001") badge.assertIsDisplayed() else badge.assertDoesNotExist()
                for (oldLabel in listOf("Imported listing", "Ficha importada", "Visit reported by a user", "Visita reportada por un usuario")) {
                    composeRule.onNodeWithText(oldLabel).assertDoesNotExist()
                }
            }
        }
    }

    @Test
    fun bloodDonationFilterIsSeparateFromOrganDonation() {
        composeRule.onNodeWithTag("tab-list").performClick()
        composeRule.onNodeWithTag("category-blood_donation").performScrollTo().performClick().assertIsSelected()
        composeRule.onNodeWithText("Bright Futures Home").assertDoesNotExist()
        composeRule.onNodeWithText("Open Health Bridge").assertDoesNotExist()
        composeRule.onNodeWithTag("tab-map").performClick()
        composeRule.onNodeWithTag("category-blood_donation").performScrollTo().assertIsSelected()
        composeRule.onNodeWithTag("tab-settings").performClick()
        composeRule.onNodeWithTag("language-es").performClick()
        composeRule.onNodeWithTag("tab-list").performClick()
        composeRule.onNodeWithText("Donación de sangre").performScrollTo().assertIsDisplayed()
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
