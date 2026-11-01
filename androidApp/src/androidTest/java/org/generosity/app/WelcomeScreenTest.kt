package org.generosity.app

import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import org.generosity.app.ui.WelcomeScreen
import org.generosity.domain.SupportedLocale
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals

class WelcomeScreenTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()

    @Test fun largeTextKeepsAllEntryActionsReachableInBothLanguages() {
        val locale = mutableStateOf(SupportedLocale.ENGLISH)
        var action = ""
        ui.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 2f)) {
                WelcomeScreen(locale.value, false, false, { action = "guest" }, { action = "signIn" }, { action = "create" })
            }
        }
        for ((language, labels) in listOf(
            SupportedLocale.ENGLISH to listOf("Continue as a guest", "Sign in", "Create account"),
            SupportedLocale.SPANISH to listOf("Continuar como invitado", "Iniciar sesión", "Crear cuenta")
        )) {
            ui.runOnIdle { locale.value = language }
            labels.zip(listOf("guest", "signIn", "create")).forEach { (label, expected) ->
                ui.onNodeWithText(label).performScrollTo().assertIsDisplayed().performClick()
                ui.runOnIdle { assertEquals(expected, action) }
            }
        }
    }
}
