package org.generosity.app.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import org.generosity.app.R
import org.generosity.domain.Coordinates
import org.generosity.domain.KindnessCenter
import org.generosity.domain.SupportedLocale

internal fun Coordinates.googleMapsUri(): Uri? {
    if (!latitude.isFinite() || !longitude.isFinite() || latitude !in -90.0..90.0 || longitude !in -180.0..180.0) return null
    return Uri.parse("https://www.google.com/maps/search/").buildUpon()
        .appendQueryParameter("api", "1").appendQueryParameter("query", "$latitude,$longitude").build()
}

internal fun openCenterMap(context: Context, center: KindnessCenter, locale: SupportedLocale, deviceMap: Boolean): Boolean {
    val google = center.coordinates.googleMapsUri() ?: return false
    val point = "${center.coordinates.latitude},${center.coordinates.longitude}"
    val destination = if (deviceMap) Uri.parse("geo:0,0?q=" + Uri.encode("$point(${center.name.resolve(locale)})")) else google
    return try {
        context.startActivity(Intent(Intent.ACTION_VIEW, destination))
        true
    } catch (_: ActivityNotFoundException) {
        if (!deviceMap) false else try {
            context.startActivity(Intent(Intent.ACTION_VIEW, google))
            true
        } catch (_: ActivityNotFoundException) { false }
        catch (_: SecurityException) { false }
    } catch (_: SecurityException) { false }
}

@Composable
internal fun CenterMapActions(center: KindnessCenter, locale: SupportedLocale) {
    val context = LocalContext.current
    var failed by remember(center.id) { mutableStateOf(false) }
    Column {
        if (center.coordinates.googleMapsUri() == null) {
            Text(localizedString(R.string.map_location_unavailable, locale))
        } else {
            OutlinedButton(onClick = { failed = !openCenterMap(context, center, locale, deviceMap = false) },
                modifier = Modifier.testTag("map-open-google")) {
                Text(localizedString(R.string.map_open_google, locale))
            }
            OutlinedButton(onClick = { failed = !openCenterMap(context, center, locale, deviceMap = true) },
                modifier = Modifier.testTag("map-open-device")) {
                Text(localizedString(R.string.map_open_device, locale))
            }
        }
        if (failed) Text(localizedString(R.string.map_open_failed, locale),
            modifier = Modifier.testTag("map-open-error"), color = MaterialTheme.colorScheme.error)
    }
}
