package org.generosity.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import org.generosity.app.ui.GenerosityApp

class MainActivity : ComponentActivity() {
    private val appState by lazy { GenerosityAppState(lifecycleScope) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appState.start()
        setContent {
            GenerosityApp(appState)
        }
    }
}
