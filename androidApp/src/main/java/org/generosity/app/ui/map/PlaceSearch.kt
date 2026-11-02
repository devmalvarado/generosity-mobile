package org.generosity.app.ui.map

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.runInterruptible
import org.generosity.domain.Coordinates
import org.generosity.domain.DiscoveryArea
import org.generosity.domain.SupportedLocale

/** Platform lookup only; radius, category filters and ordering belong to shared discovery. */
suspend fun searchPlaces(context: Context, query: String, locale: SupportedLocale): List<DiscoveryArea> {
    require(query.isNotBlank() && query.length <= 200)
    check(Geocoder.isPresent())
    val geocoder = Geocoder(context, Locale.forLanguageTag(locale.code))
    val addresses = if (Build.VERSION.SDK_INT >= 33) {
        suspendCancellableCoroutine { continuation ->
            geocoder.getFromLocationName(query, 5, object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: MutableList<Address>) {
                    if (continuation.isActive) continuation.resume(addresses.toList())
                }
                override fun onError(errorMessage: String?) {
                    if (continuation.isActive) continuation.resumeWithException(IllegalStateException("geocoder_unavailable"))
                }
            })
        }
    } else runInterruptible(Dispatchers.IO) {
        @Suppress("DEPRECATION")
        geocoder.getFromLocationName(query, 5).orEmpty()
    }
    return addresses.mapNotNull { address ->
        if (!address.hasLatitude() || !address.hasLongitude()) return@mapNotNull null
        val label = address.getAddressLine(0)
            ?: listOfNotNull(address.featureName, address.locality, address.countryName).distinct().joinToString(", ")
        runCatching { DiscoveryArea(label.take(500), Coordinates(address.latitude, address.longitude)) }.getOrNull()
    }.distinct().take(5)
}
