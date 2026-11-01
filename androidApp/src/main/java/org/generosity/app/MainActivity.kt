package org.generosity.app

import android.os.Bundle
import android.content.Intent
import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import org.generosity.app.ui.WelcomeScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import org.generosity.domain.SupportedLocale
import org.generosity.app.ui.GenerosityApp
import org.generosity.app.ui.map.google.GoogleCenterMapRenderer

open class MainActivity : ComponentActivity() {
    protected open val usesDemoCatalog = false
    private val languageSettings by lazy { AndroidLanguageSettings(this) }
    private val connection by lazy { AndroidConnection(this, lifecycleScope) }
    private val appState by lazy {
        GenerosityAppState(lifecycleScope, backend = if (usesDemoCatalog) null else org.generosity.data.BackendStore(connection), initialLocalePreference = languageSettings.load())
    }
    private val account by lazy { AccountSession(this, lifecycleScope, appState) }
    private var savingLanguage by mutableStateOf(false)
    private var languageSaveFailed by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        connection.credential = account::credentialFor
        appState.start()
        account.callback(intent.data)
        setContent {
            val state by appState.state.collectAsState()
            val settings = remember { getSharedPreferences("entry", MODE_PRIVATE) }
            var entered by remember { mutableStateOf(usesDemoCatalog || settings.getBoolean("entered", false)) }
            var wasRegistered by remember { mutableStateOf(false) }
            var entryError by remember { mutableStateOf(false) }
            LaunchedEffect(state.profile.isRegistered) {
                if (state.profile.isRegistered || wasRegistered) {
                    entered = state.profile.isRegistered
                    settings.edit().putBoolean("entered", entered).apply()
                }
                wasRegistered = state.profile.isRegistered
            }
            if (!entered && !state.profile.isRegistered) WelcomeScreen(state.locale, account.busy, account.failed || entryError,
                onGuest = {
                    entryError = !settings.edit().putBoolean("entered", true).commit()
                    if (!entryError) entered = true
                }, onSignIn = { account.signIn() }, onCreate = { account.signIn(register = true) })
            else GenerosityApp(appState, GoogleCenterMapRenderer, ::selectLanguage, savingLanguage, languageSaveFailed,
                onSignIn = { account.signIn() }, onSignOut = account::signOut,
                authBusy = account.busy, authFailed = account.failed)
        }
    }

    override fun onResume() {
        super.onResume()
        appState.onResume()
        if (!usesDemoCatalog) account.restore()
        appState.setDeviceLanguages(AndroidLanguageSettings.deviceLanguages())
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        account.callback(intent.data)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        appState.setDeviceLanguages(AndroidLanguageSettings.deviceLanguages(newConfig))
    }

    private fun selectLanguage(locale: SupportedLocale?) {
        if (savingLanguage) return
        savingLanguage = true
        lifecycleScope.launch {
            try {
                languageSaveFailed = !languageSettings.save(locale)
                if (!languageSaveFailed) appState.setLocale(locale)
            } finally {
                savingLanguage = false
            }
        }
    }
}
