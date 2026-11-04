package org.generosity.app

import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import org.generosity.app.ui.GenerosityTheme
import org.generosity.app.ui.NativePreview
import org.generosity.preview.PreviewSession
import org.junit.Rule
import org.junit.Test

class NativePreviewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    @Test fun everyRouteUsesNativeControls() {
        rule.setContent { GenerosityTheme { NativePreview(PreviewSession.groupNames,"es","fixture") {} } }
        for(route in PreviewSession.routes) {
            rule.onNodeWithTag("preview-screens").performClick()
            rule.onNodeWithTag("preview-route-${route.id}").performScrollTo().performClick()
            if (route.id == "map") rule.onNodeWithTag("brand-wordmark").assertIsDisplayed()
            else rule.onNodeWithTag("preview-title").assertTextEquals(route.title("es"))
        }
    }

    @Test fun nativeMapSelectionDetailsAndFlagRevocation() {
        var groups by mutableStateOf(PreviewSession.groupNames)
        var closed = false
        rule.setContent { GenerosityTheme { NativePreview(groups,"es","fixture") { closed=true } } }
        rule.onNodeWithTag("preview-pin-c2").performClick()
        rule.onNodeWithTag("preview-details").performClick()
        rule.onNodeWithText("Centro de Salud Horizonte", substring = true).assertIsDisplayed()
        rule.onNodeWithTag("preview-save").performScrollTo().performClick()
        rule.onNodeWithTag("preview-feedback").assertIsDisplayed()
        rule.runOnIdle { groups=emptyList() }
        rule.waitUntil(5000) { closed }
    }

    @Test fun nativeNavigationSelectsEachTabWithoutSessionBanner() {
        rule.setContent { GenerosityTheme { NativePreview(PreviewSession.groupNames,"es","fixture") {} } }
        rule.onNodeWithText("Diseño de prueba · Datos ficticios · Solo esta sesión").assertDoesNotExist()
        for ((route, title) in listOf("needs" to "Necesidades", "help" to "¿Cómo quieres ayudar?", "impact" to "Mi impacto", "profile" to "Perfil", "map" to "Mapa")) {
            rule.onNodeWithTag("preview-tab-$route").performClick().assertIsSelected()
            if (route == "map") rule.onNodeWithTag("brand-wordmark").assertIsDisplayed()
            else rule.onNodeWithTag("preview-title").assertTextEquals(title)
        }
        rule.onNodeWithTag("preview-screens").performClick()
        rule.onNodeWithTag("preview-route-offer").performScrollTo().performClick()
        rule.onNodeWithTag("preview-tab-help").assertIsSelected()
    }

    @Test fun floatingMapSearchAndToolbarActionsRespectFlags() {
        var groups by mutableStateOf(PreviewSession.groupNames)
        rule.setContent { GenerosityTheme { NativePreview(groups,"es","fixture") {} } }
        rule.onNodeWithTag("brand-wordmark").assertIsDisplayed()
        rule.onNodeWithTag("preview-title").assertDoesNotExist()
        rule.onNodeWithText("Salir").assertDoesNotExist()
        rule.onNodeWithTag("preview-list").performClick()
        rule.onNodeWithTag("preview-list-c2").performClick()
        rule.onNodeWithText("Centro de Salud Horizonte", substring = true).assertIsDisplayed()
        rule.onNodeWithTag("preview-filters").performClick()
        rule.onNodeWithTag("preview-funding").assertExists()
        rule.onNodeWithText("Cerrar").performClick()
        rule.onNodeWithTag("preview-query").performTextInput("Encuentro")
        rule.onNodeWithTag("preview-query").performImeAction()
        rule.onNodeWithTag("preview-pin-c4").assertIsDisplayed()
        rule.onNodeWithTag("preview-pin-c2").assertDoesNotExist()
        rule.onNodeWithTag("preview-locate").performClick()
        rule.onNodeWithTag("preview-origin").assertExists()
        rule.onNodeWithTag("preview-notifications").performClick()
        rule.onNodeWithTag("preview-title").assertTextEquals("Notificaciones")
        rule.onNodeWithText("Atrás").performClick()
        rule.onNodeWithTag("preview-screens").performClick()
        rule.onNodeWithTag("preview-route-map").assertIsDisplayed()
        rule.onNodeWithText("Cerrar").performClick()
        rule.runOnIdle { groups = listOf("discovery") }
        rule.onNodeWithTag("preview-notifications").assertDoesNotExist()
    }
}
