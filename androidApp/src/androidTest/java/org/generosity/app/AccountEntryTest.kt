package org.generosity.app

import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import kotlinx.coroutines.*
import org.generosity.app.ui.GenerosityApp
import org.generosity.app.ui.map.google.GoogleCenterMapRenderer
import org.generosity.domain.SupportedLocale
import org.junit.*
import org.junit.Assert.assertEquals

class AccountEntryTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    @After fun close() = scope.cancel()

    @Test fun profileAccountActionsWorkInBothLanguagesWithLargeText() {
        val app = GenerosityAppState(scope)
        val busy = mutableStateOf(false)
        var action = ""
        ui.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 2f)) {
                GenerosityApp(app, GoogleCenterMapRenderer,
                    onSignIn = { action = "signIn" }, onSignOut = { action = "signOut" },
                    onCreate = { action = "create" }, authBusy = busy.value)
            }
        }
        ui.onNodeWithTag("tab-profile").performClick()
        for ((locale, labels) in listOf(
            SupportedLocale.ENGLISH to listOf("Sign in", "Create account"),
            SupportedLocale.SPANISH to listOf("Iniciar sesión", "Crear cuenta")
        )) {
            ui.runOnIdle { app.setLocale(locale) }
            labels.zip(listOf("signIn", "create")).forEach { (label, expected) ->
                ui.onNodeWithText(label).performScrollTo().assertIsDisplayed().performClick()
                ui.runOnIdle { assertEquals(expected, action) }
            }
            ui.runOnIdle { busy.value = true }
            labels.forEach { ui.onNodeWithText(it).assertIsNotEnabled() }
            ui.runOnIdle { busy.value = false }
        }
        ui.runOnIdle { app.signIn("fixture-user", "María") }
        ui.onNodeWithText("Crear cuenta").assertDoesNotExist()
        ui.onNodeWithText("Iniciar sesión").assertDoesNotExist()
        ui.onNodeWithText("Cerrar sesión").performScrollTo().assertIsDisplayed().performClick()
        ui.runOnIdle { assertEquals("signOut", action) }
    }
}
