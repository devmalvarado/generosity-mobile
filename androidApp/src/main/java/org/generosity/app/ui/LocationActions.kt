package org.generosity.app.ui

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.net.Uri
import android.os.SystemClock
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.generosity.app.*
import org.generosity.app.R
import org.generosity.domain.DiscoveryArea
import org.generosity.domain.SupportedLocale

@Composable
fun rememberDiscoveryLocation(source: DeviceLocationSource, appState: GenerosityAppState): DiscoveryLocation {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val app by appState.state.collectAsState()
    val label by rememberUpdatedState(localizedString(R.string.location_current, app.locale))
    val location = remember(source, appState) {
        DiscoveryLocation(source, scope, SystemClock::elapsedRealtime, { coordinates ->
            appState.setDiscoveryArea(coordinates?.let { DiscoveryArea(label, it) }, preserveSelection = true)
        })
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(location, lifecycle, context) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> location.resume()
                Lifecycle.Event.ON_PAUSE -> location.suspend()
                else -> Unit
            }
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) location.resume()
            }
        }
        lifecycle.addObserver(observer)
        ContextCompat.registerReceiver(context, receiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION), ContextCompat.RECEIVER_NOT_EXPORTED)
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) location.resume()
        onDispose {
            lifecycle.removeObserver(observer)
            context.unregisterReceiver(receiver)
            location.suspend()
        }
    }
    return location
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LocationActions(location: DiscoveryLocation, source: DeviceLocationSource, locale: SupportedLocale, compact: Boolean = false) {
    val context = LocalContext.current
    val state by location.state.collectAsState()
    var settingsFailed by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        location.requestLocation()
    }
    Column(Modifier.semantics { liveRegion = LiveRegionMode.Polite }) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(GenSpace.sm)) {
            TextButton(onClick = {
                settingsFailed = false
                location.requestLocation()
                if (source.access == LocationAccess.NONE) launcher.launch(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            }, enabled = !state.locating, modifier = Modifier.testTag("location-use")) {
                if (compact) androidx.compose.material3.Icon(androidx.compose.ui.res.painterResource(R.drawable.ic_location),
                    contentDescription = localizedString(if (state.locating) R.string.location_loading else R.string.location_use, locale))
                else Text(localizedString(if (state.locating) R.string.location_loading else R.string.location_use, locale))
            }
            if (state.problem == LocationProblem.DENIED || state.problem == LocationProblem.DISABLED) {
                TextButton(onClick = {
                    val intent = if (state.problem == LocationProblem.DISABLED) Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    else Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
                    settingsFailed = runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }.isFailure
                }, modifier = Modifier.testTag("location-settings")) {
                    if (compact) androidx.compose.material3.Icon(androidx.compose.ui.res.painterResource(R.drawable.ic_settings), contentDescription = localizedString(R.string.location_settings, locale))
                    else Text(localizedString(R.string.location_settings, locale))
                }
            }
        }
        val message = if (settingsFailed) R.string.location_unavailable else when (state.problem) {
            LocationProblem.DENIED -> R.string.location_denied
            LocationProblem.DISABLED -> R.string.location_disabled
            LocationProblem.UNAVAILABLE -> R.string.location_unavailable
            null -> null
        }
        if (!compact) message?.let { Text(localizedString(it, locale), Modifier.testTag("location-status"), style = MaterialTheme.typography.bodySmall) }
    }
}
