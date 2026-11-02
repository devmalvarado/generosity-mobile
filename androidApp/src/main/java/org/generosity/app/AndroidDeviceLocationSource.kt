package org.generosity.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.SystemClock
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import android.os.CancellationSignal
import kotlinx.coroutines.suspendCancellableCoroutine
import org.generosity.domain.Coordinates
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AndroidDeviceLocationSource(context: Context) : DeviceLocationSource {
    private val context = context.applicationContext
    private val manager = this.context.getSystemService(LocationManager::class.java)
    override val access: LocationAccess get() = when {
        granted(Manifest.permission.ACCESS_FINE_LOCATION) -> LocationAccess.PRECISE
        granted(Manifest.permission.ACCESS_COARSE_LOCATION) -> LocationAccess.APPROXIMATE
        else -> LocationAccess.NONE
    }
    override val enabled: Boolean get() = LocationManagerCompat.isLocationEnabled(manager)

    private fun granted(permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    override suspend fun currentFix(): DeviceLocationFix? {
        if (access == LocationAccess.NONE) throw SecurityException("Location permission required")
        val enabledProviders = manager.getProviders(true)
        val providers = buildList {
            if (Build.VERSION.SDK_INT >= 31) add(LocationManager.FUSED_PROVIDER)
            add(LocationManager.NETWORK_PROVIDER)
            add(LocationManager.GPS_PROVIDER)
        }.filter { it in enabledProviders }
        if (providers.isEmpty()) return null
        // Coarse requests are throttled; reuse only a permission-filtered, recent OS fix.
        providers.mapNotNull { manager.getLastKnownLocation(it)?.usableFix() }
            .maxByOrNull { it.elapsedMillis }?.let { return it }
        return suspendCancellableCoroutine { continuation ->
            val signals = providers.map { CancellationSignal() }
            continuation.invokeOnCancellation { signals.forEach { it.cancel() } }
            var pending = providers.size
            providers.forEachIndexed { index, provider ->
                if (!continuation.isActive) return@forEachIndexed
                try {
                    LocationManagerCompat.getCurrentLocation(manager, provider, signals[index], ContextCompat.getMainExecutor(context)) { location ->
                        if (!continuation.isActive) return@getCurrentLocation
                        val fix = location?.usableFix()
                        if (fix != null || --pending == 0) {
                            continuation.resume(fix)
                            signals.forEach { it.cancel() }
                        }
                    }
                } catch (denied: SecurityException) {
                    if (continuation.isActive) continuation.resumeWithException(denied)
                    signals.forEach { it.cancel() }
                } catch (_: IllegalArgumentException) {
                    if (--pending == 0 && continuation.isActive) continuation.resume(null)
                }
            }
        }
    }

    private fun Location.usableFix(): DeviceLocationFix? = takeIf { hasAccuracy() }?.let {
        DeviceLocationFix(Coordinates(latitude, longitude), accuracy, elapsedRealtimeNanos / 1_000_000)
    }?.takeIf { it.isUsable(SystemClock.elapsedRealtime()) }
}
