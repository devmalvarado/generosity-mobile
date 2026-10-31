package org.generosity.app.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.generosity.domain.Coordinates
import org.generosity.domain.KindnessCenter
import org.generosity.domain.SupportedLocale

data class CenterMapMarker(
    val centerId: String,
    val title: String,
    val coordinates: Coordinates
)

fun List<KindnessCenter>.toMapMarkers(locale: SupportedLocale): List<CenterMapMarker> =
    filter {
        it.coordinates.latitude in -90.0..90.0 && it.coordinates.longitude in -180.0..180.0
    }.map { CenterMapMarker(it.id, it.name.resolve(locale), it.coordinates) }

/** Rendering only: discovery, filtering and selection authority stay in shared state. */
interface CenterMapRenderer {
    @Composable
    fun Render(
        markers: List<CenterMapMarker>,
        selectedCenterId: String?,
        locale: SupportedLocale,
        onCenterSelected: (String) -> Unit,
        modifier: Modifier
    )
}
