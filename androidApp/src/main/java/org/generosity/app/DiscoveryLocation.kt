package org.generosity.app

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.generosity.domain.Coordinates

enum class LocationAccess { NONE, APPROXIMATE, PRECISE }
enum class LocationProblem { DENIED, DISABLED, UNAVAILABLE }

data class DeviceLocationFix(val coordinates: Coordinates, val accuracyMeters: Float, val elapsedMillis: Long) {
    fun isUsable(nowMillis: Long): Boolean =
        coordinates.latitude in -90.0..90.0 && coordinates.longitude in -180.0..180.0 &&
            accuracyMeters.isFinite() && accuracyMeters in 0f..5_000f &&
            elapsedMillis > 0 && nowMillis - elapsedMillis in -5_000L..120_000L
}

interface DeviceLocationSource {
    val access: LocationAccess
    val enabled: Boolean
    suspend fun currentFix(): DeviceLocationFix?
}

data class DiscoveryLocationState(
    val fix: DeviceLocationFix? = null,
    val locating: Boolean = false,
    val approximate: Boolean = false,
    val problem: LocationProblem? = null
)

/** Foreground, single-fix discovery; manual origin choices win over late callbacks. */
class DiscoveryLocation(
    private val source: DeviceLocationSource,
    private val scope: CoroutineScope,
    private val nowMillis: () -> Long,
    private val onDeviceOrigin: (Coordinates?) -> Unit,
    private val timeoutMillis: Long = 15_000
) {
    private val mutableState = MutableStateFlow(DiscoveryLocationState())
    val state = mutableState.asStateFlow()
    private var request: Job? = null
    private var generation = 0
    private var foreground = false
    private var choseOrigin = false
    private var usingDevice = false
    private var fixAccess: LocationAccess? = null

    fun resume() {
        foreground = true
        if (!choseOrigin && source.access != LocationAccess.NONE) requestLocation()
        else if (usingDevice) {
            when {
                source.access == LocationAccess.NONE -> fail(LocationProblem.DENIED)
                !source.enabled -> fail(LocationProblem.DISABLED)
                state.value.locating -> Unit
                state.value.fix?.isUsable(nowMillis()) != true || fixAccess != source.access -> {
                    clearFix()
                    requestLocation()
                }
                else -> mutableState.value = state.value.copy(approximate =
                    source.access == LocationAccess.APPROXIMATE || state.value.fix!!.accuracyMeters > 1_000)
            }
        }
    }

    fun suspend() {
        foreground = false
        stopRequest()
    }

    fun useManualOrigin() {
        choseOrigin = true
        usingDevice = false
        stopRequest()
        mutableState.value = DiscoveryLocationState()
        fixAccess = null
        // The caller applies the selected manual area, avoiding an intermediate origin reset.
    }

    fun requestLocation() {
        choseOrigin = true
        usingDevice = true
        stopRequest()
        if (!foreground) return
        if (source.access == LocationAccess.NONE) { fail(LocationProblem.DENIED); return }
        if (!source.enabled) { fail(LocationProblem.DISABLED); return }
        if (state.value.fix?.isUsable(nowMillis()) == false) clearFix()
        mutableState.value = state.value.copy(locating = true, problem = null)
        val token = generation
        request = scope.launch {
            try {
                val fix = withTimeoutOrNull(timeoutMillis) { source.currentFix() }
                if (token != generation || !foreground || !usingDevice) return@launch
                when {
                    source.access == LocationAccess.NONE -> fail(LocationProblem.DENIED)
                    !source.enabled -> fail(LocationProblem.DISABLED)
                    fix?.isUsable(nowMillis()) != true -> fail(LocationProblem.UNAVAILABLE)
                    else -> {
                        fixAccess = source.access
                        mutableState.value = DiscoveryLocationState(fix = fix,
                            approximate = source.access == LocationAccess.APPROXIMATE || fix.accuracyMeters > 1_000)
                        onDeviceOrigin(fix.coordinates)
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: SecurityException) {
                if (token == generation) fail(LocationProblem.DENIED)
            } catch (_: Exception) {
                if (token == generation) fail(LocationProblem.UNAVAILABLE)
            }
        }
    }

    private fun stopRequest() {
        generation++
        request?.cancel()
        request = null
        mutableState.value = state.value.copy(locating = false)
    }

    private fun clearFix() {
        if (state.value.fix != null) {
            mutableState.value = state.value.copy(fix = null, approximate = false)
            fixAccess = null
            onDeviceOrigin(null)
        }
    }

    private fun fail(problem: LocationProblem) {
        stopRequest()
        clearFix()
        mutableState.value = state.value.copy(problem = problem)
    }
}
