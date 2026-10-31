package org.generosity.app

import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.*
import org.generosity.app.ui.GenerosityApp
import org.generosity.app.ui.map.google.GoogleCenterMapRenderer
import org.generosity.data.SampleImportedCatalog
import org.generosity.domain.*
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertTrue
import java.io.File

/** Run with adb instrumentation so captures can be exported before uninstall. */
class VisualAlignmentTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @After fun dispose() = scope.cancel()

    @Test fun captureCurrentDeviceConfigurationAndAccessibleStates() {
        val args = InstrumentationRegistry.getArguments()
        val locale = SupportedLocale.fromCode(args.getString("visualLocale")) ?: SupportedLocale.ENGLISH
        val label = args.getString("visualLabel") ?: "default"
        val original = SampleImportedCatalog.centers().first()
        val center = original.copy(
            name = LocalizedText.of("Bright Futures Home — community support and learning", "Hogar Futuros Brillantes — apoyo comunitario y aprendizaje"),
            needs = NeedPriority.entries.map { priority -> original.needs.single().copy(
                id = "visual-" + priority.code, priority = priority,
                title = LocalizedText.of("School supplies for community learning", "Útiles escolares para aprendizaje comunitario")
            ) }
        )
        val app = GenerosityAppState(scope, catalog = listOf(center), initialLocalePreference = LocalePreference(selectedLocale = locale))
        composeRule.runOnUiThread {
            composeRule.activity.enableEdgeToEdge()
            composeRule.activity.window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
        composeRule.setContent { GenerosityApp(app, GoogleCenterMapRenderer) }
        composeRule.runOnIdle { app.start() }
        composeRule.waitUntil(5_000) { app.state.value.centers.isNotEmpty() }
        capture(label, "map-unavailable")
        composeRule.onNodeWithTag("tab-list").performClick()
        capture(label, "list")
        composeRule.onNodeWithTag("discovery-scroll").performScrollToNode(hasTestTag("center-" + center.id))
        composeRule.onNodeWithTag("center-" + center.id).performClick()
        composeRule.onNodeWithTag("discovery-scroll").performScrollToNode(hasTestTag("detail-" + center.id))
        composeRule.onNodeWithTag("detail-" + center.id).performScrollTo()
        capture(label, "detail")
        val priorities = if (locale == SupportedLocale.SPANISH) listOf("Urgente", "Alta", "Media", "Normal") else listOf("Urgent", "High", "Medium", "Normal")
        priorities.forEach { composeRule.onNodeWithText(it).performScrollTo().assertIsDisplayed() }
        capture(label, "need-priorities")
        composeRule.runOnIdle { app.offerHelp("visual-urgent", ContributionOffer(HelpMethod.GOODS, quantity = 5)) }
        val rejected = if (locale == SupportedLocale.SPANISH) "Inicia sesión para registrar una aportación. Puedes explorar sin cuenta." else "Sign in to record a contribution. You can browse without an account."
        composeRule.onNodeWithText(rejected).performScrollTo().assertIsDisplayed()
        capture(label, "rejected-offer")
        composeRule.onNodeWithTag("tab-profile").performClick()
        capture(label, "guest-profile")
        composeRule.runOnIdle {
            app.signIn("visual-user", "Test contributor")
            app.recordPersonalHelp(ContributionOffer(HelpMethod.GOODS, note = "Fixture activity"))
        }
        composeRule.waitUntil(5_000) { app.state.value.contributions.isNotEmpty() }
        val provenance = if (locale == SupportedLocale.SPANISH) "Ayuda declarada por ti" else "Self-reported help"
        composeRule.onNodeWithText(provenance).performScrollTo().assertIsDisplayed()
        capture(label, "local-history")
        composeRule.onNodeWithTag("tab-settings").performClick()
        composeRule.onNodeWithTag(if (locale == SupportedLocale.SPANISH) "language-es" else "language-en").assertIsSelected()
        capture(label, "settings")
        composeRule.onNodeWithTag("tab-list").performClick()
        composeRule.onNodeWithTag("discovery-scroll").performScrollToIndex(0)
        val placeholder = if (locale == SupportedLocale.SPANISH) "Buscar centros" else "Search centers"
        composeRule.onNodeWithText(placeholder).performClick().performTextInput("zzzz")
        capture(label, "keyboard-search")
        composeRule.onNodeWithText(placeholder).performImeAction()
        val empty = if (locale == SupportedLocale.SPANISH) "No se encontraron centros de bondad." else "No kindness centers found."
        composeRule.onNodeWithTag("discovery-scroll").performScrollToNode(hasText(empty))
        composeRule.onNodeWithText(empty).assertIsDisplayed()
        capture(label, "empty")
    }

    private fun capture(label: String, name: String) {
        composeRule.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val directory = File(instrumentation.targetContext.filesDir, "visual/" + label)
        directory.mkdirs()
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        File(directory, name + ".png").outputStream().use { assertTrue(bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)) }
        bitmap.recycle()
    }
}
