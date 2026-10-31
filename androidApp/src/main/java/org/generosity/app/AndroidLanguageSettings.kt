package org.generosity.app

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.generosity.domain.LocalePreference
import org.generosity.domain.SupportedLocale

class AndroidLanguageSettings(context: Context) {
    private val preferences = context.getSharedPreferences("language_settings", Context.MODE_PRIVATE)

    fun load(deviceCodes: List<String> = deviceLanguages()): LocalePreference =
        LocalePreference.fromDeviceLanguages(preferences.getString("mode", null), deviceCodes)

    suspend fun save(locale: SupportedLocale?): Boolean = withContext(Dispatchers.IO) {
        preferences.edit().putString("mode", locale?.code ?: "system").commit()
    }

    companion object {
        fun deviceLanguages(configuration: Configuration = Resources.getSystem().configuration): List<String> =
            (0 until configuration.locales.size()).map { configuration.locales[it].toLanguageTag() }
    }
}
