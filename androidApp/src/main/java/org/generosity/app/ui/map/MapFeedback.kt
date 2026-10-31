package org.generosity.app.ui.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.generosity.app.ui.TextKey
import org.generosity.app.ui.label
import org.generosity.app.ui.AppIcon
import org.generosity.app.R
import org.generosity.domain.SupportedLocale

@Composable
internal fun MapUnavailable(locale: SupportedLocale, onRetry: (() -> Unit)? = null) {
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .verticalScroll(rememberScrollState()).padding(24.dp).testTag("map-unavailable"),
        verticalArrangement = Arrangement.Center
    ) {
        AppIcon(R.drawable.ic_map, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))
        Text(TextKey.MAP_UNAVAILABLE.label(locale), style = MaterialTheme.typography.bodyMedium)
        if (onRetry != null) {
            TextButton(onClick = onRetry) { Text(TextKey.RETRY.label(locale)) }
        }
    }
}

@Composable
internal fun MapLoadFeedback(loaded: Boolean, locale: SupportedLocale, onRetry: () -> Unit) {
    var timedOut by remember { mutableStateOf(false) }
    LaunchedEffect(loaded) {
        timedOut = false
        if (!loaded) {
            delay(15_000)
            timedOut = true
        }
    }
    if (!loaded) {
        Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            if (timedOut) {
                Text(TextKey.MAP_LOAD_DELAYED.label(locale))
                TextButton(onClick = onRetry) { Text(TextKey.RETRY.label(locale)) }
            } else {
                LinearProgressIndicator(Modifier.fillMaxWidth())
                Text(TextKey.MAP_LOADING.label(locale))
            }
        }
    }
}
