package org.generosity.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.generosity.app.ui.GenerosityTheme
import org.generosity.app.ui.NativePreview
import org.generosity.domain.LocalePreference
import org.generosity.preview.PreviewSession

/** Standalone design exploration; absent from release builds and never creates an account or connection. */
class NativePreviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val language = LocalePreference.fromDeviceLanguages(intent.getStringExtra("language"), AndroidLanguageSettings.deviceLanguages()).activeLocale.code
        setContent {
            GenerosityTheme {
                NativePreview(PreviewSession.groupNames, language, "local-design") { finish() }
            }
        }
    }
}
