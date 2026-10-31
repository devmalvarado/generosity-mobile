package org.generosity.app.ui.map.google

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import org.generosity.app.BuildConfig
import org.generosity.app.ui.TextKey
import org.generosity.app.ui.label
import org.generosity.app.ui.map.CenterMapMarker
import org.generosity.app.ui.map.CenterMapRenderer
import org.generosity.app.ui.map.MapLoadFeedback
import org.generosity.app.ui.map.MapUnavailable
import org.generosity.domain.SupportedLocale

object GoogleCenterMapRenderer : CenterMapRenderer {
    @Composable
    override fun Render(
        markers: List<CenterMapMarker>,
        selectedCenterId: String?,
        locale: SupportedLocale,
        onCenterSelected: (String) -> Unit,
        modifier: Modifier
    ) {
        val context = LocalContext.current
        var attempt by remember { mutableIntStateOf(0) }
        val servicesAvailable = remember(context, attempt) {
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
        }
        Column(modifier) {
            when {
                !BuildConfig.MAPS_API_KEY_CONFIGURED -> MapUnavailable(locale)
                !servicesAvailable -> MapUnavailable(locale, onRetry = { attempt++ })
                else -> {
                    // Keep the camera across retries; only recreate the failed SDK view.
                    val camera = rememberCameraPositionState()
                    var framedCatalog by rememberSaveable { mutableStateOf(false) }
                    key(attempt) {
                        var loaded by remember { mutableStateOf(false) }
                        GoogleMap(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            cameraPositionState = camera,
                            contentDescription = TextKey.TAB_MAP.label(locale),
                            properties = MapProperties(isMyLocationEnabled = false),
                            uiSettings = MapUiSettings(
                                mapToolbarEnabled = false,
                                myLocationButtonEnabled = false,
                                rotationGesturesEnabled = false,
                                tiltGesturesEnabled = false
                            ),
                            onMapLoaded = { loaded = true }
                        ) {
                            markers.forEach { marker ->
                                key(marker.centerId) {
                                    Marker(
                                        state = rememberUpdatedMarkerState(marker.toLatLng()),
                                        title = marker.title,
                                        icon = BitmapDescriptorFactory.defaultMarker(
                                            if (marker.centerId == selectedCenterId) {
                                                BitmapDescriptorFactory.HUE_ORANGE
                                            } else {
                                                BitmapDescriptorFactory.HUE_RED
                                            }
                                        ),
                                        onClick = {
                                            onCenterSelected(marker.centerId)
                                            false
                                        }
                                    )
                                }
                            }
                        }
                        LaunchedEffect(loaded, markers.map { it.coordinates }) {
                            if (loaded && !framedCatalog && markers.isNotEmpty()) {
                                val positions = markers.map { it.toLatLng() }.distinct()
                                val update = if (positions.size == 1) {
                                    CameraUpdateFactory.newCameraPosition(
                                        CameraPosition.fromLatLngZoom(positions.single(), 13f)
                                    )
                                } else {
                                    val bounds = LatLngBounds.builder()
                                    positions.forEach(bounds::include)
                                    CameraUpdateFactory.newLatLngBounds(bounds.build(), 64)
                                }
                                camera.move(update)
                                framedCatalog = true
                            }
                        }
                        MapLoadFeedback(loaded, locale, onRetry = { attempt++ })
                    }
                }
            }
        }
    }
}

private fun CenterMapMarker.toLatLng() = LatLng(coordinates.latitude, coordinates.longitude)
