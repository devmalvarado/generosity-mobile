package org.generosity.app

import android.os.Bundle
import android.content.Intent
import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import org.generosity.domain.SupportedLocale
import org.generosity.app.ui.GenerosityApp
import org.generosity.app.ui.map.google.GoogleCenterMapRenderer

class MainActivity : ComponentActivity() {
    private val languageSettings by lazy { AndroidLanguageSettings(this) }
    private val appState by lazy {
        GenerosityAppState(lifecycleScope, initialLocalePreference = languageSettings.load())
    }
    private val account by lazy { AccountSession(this, lifecycleScope, appState) }
    private var savingLanguage by mutableStateOf(false)
    private var languageSaveFailed by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        appState.start()
        account.callback(intent.data)
        setContent {
            GenerosityApp(appState, GoogleCenterMapRenderer, ::selectLanguage, savingLanguage, languageSaveFailed,
                onSignIn = account::signIn, onSignOut = account::signOut,
                authBusy = account.busy, authFailed = account.failed)
        }
    }

    override fun onResume() {
        super.onResume()
        appState.onResume()
        account.restore()
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
