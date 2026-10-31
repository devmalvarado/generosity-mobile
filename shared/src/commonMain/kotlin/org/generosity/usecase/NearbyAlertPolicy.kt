package org.generosity.usecase

import org.generosity.domain.CenterCategory
import org.generosity.domain.KindnessCenter
import org.generosity.domain.LocalizedText
import org.generosity.domain.NotificationPreference
import org.generosity.domain.plusUtcMinutes

/**
 * Nearby eligibility only. Delivery stays with the platform tasks.
 * Empty interests are not an implicit All.
 */
object NearbyAlertDefaults {
    const val RANGE_METERS = 500
    const val COOLDOWN_HOURS = 24
}

data class NearbyAlertSettings(
    val rangeMeters: Int = NearbyAlertDefaults.RANGE_METERS,
    val cooldownHours: Int = NearbyAlertDefaults.COOLDOWN_HOURS,
    val backgroundChecksSupported: Boolean = false
)

enum class NearbyDecision {
    ELIGIBLE,
    OUTSIDE_RANGE,
    WITHIN_COOLDOWN,
    NOT_OPTED_IN,
    PERMISSION_MISSING,
    INTEREST_MISMATCH,
    STALE,
    WAITING_FOR_SUPPORTED_STATE
}

class NearbyAlertPolicy(
    private val settings: NearbyAlertSettings = NearbyAlertSettings()
) {
    fun evaluate(
        center: KindnessCenter?,
        distanceMeters: Double?,
        preferences: NotificationPreference,
        hasNotificationPermission: Boolean,
        hasLocationPermission: Boolean,
        lastAlertAtUtc: String?,
        nowUtc: String,
        catalogIsCurrent: Boolean,
        appIsForeground: Boolean
    ): NearbyDecision {
        if (!catalogIsCurrent || center == null) return NearbyDecision.STALE
        if (!preferences.nearbyCentersEnabled) return NearbyDecision.NOT_OPTED_IN
        if (!hasNotificationPermission || !hasLocationPermission) return NearbyDecision.PERMISSION_MISSING
        if (!appIsForeground && !settings.backgroundChecksSupported) {
            return NearbyDecision.WAITING_FOR_SUPPORTED_STATE
        }
        if (!matchesInterests(center, preferences)) return NearbyDecision.INTEREST_MISMATCH
        if (distanceMeters == null || distanceMeters > settings.rangeMeters) return NearbyDecision.OUTSIDE_RANGE
        if (!cooldownElapsed(lastAlertAtUtc, nowUtc)) return NearbyDecision.WITHIN_COOLDOWN
        return NearbyDecision.ELIGIBLE
    }

    fun settingsNotice(): LocalizedText = LocalizedText.of(
        "Nearby checks run only while the app is open. They are not always-on detection.",
        "Las comprobaciones cercanas ocurren solo con la aplicación abierta. No son detección permanente."
    )

    private fun matchesInterests(center: KindnessCenter, preferences: NotificationPreference): Boolean {
        if (preferences.allCategoryInterests) return true
        if (preferences.categoryInterests.isEmpty()) return false
        return center.categories.any { it in preferences.categoryInterests }
    }

    private fun cooldownElapsed(lastAlertAtUtc: String?, nowUtc: String): Boolean {
        if (lastAlertAtUtc == null) return true
        return nowUtc >= plusUtcMinutes(lastAlertAtUtc, settings.cooldownHours * 60)
    }
}

fun NotificationPreference.matchesCategory(category: CenterCategory?): Boolean {
    if (allCategoryInterests) return true
    if (categoryInterests.isEmpty() || category == null) return false
    return category in categoryInterests
}
