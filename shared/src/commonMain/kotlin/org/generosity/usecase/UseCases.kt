package org.generosity.usecase

import kotlin.math.pow
import kotlin.math.sqrt
import org.generosity.domain.CenterCategory
import org.generosity.domain.CenterNeed
import org.generosity.domain.Contribution
import org.generosity.domain.Coordinates
import org.generosity.domain.HelpMethod
import org.generosity.domain.KindnessCenter
import org.generosity.domain.KindnessCenterRepository
import org.generosity.domain.LocalizedText
import org.generosity.domain.NotificationPreference
import org.generosity.domain.NotificationType
import org.generosity.domain.SupportedLocale
import org.generosity.domain.UserMode
import org.generosity.domain.UserProfile
import org.generosity.domain.VerificationStatus
import org.generosity.domain.VisitSignal

data class DiscoveryQuery(
    val searchText: String = "",
    val categories: Set<CenterCategory> = emptySet(),
    val location: Coordinates? = null,
    val locale: SupportedLocale = SupportedLocale.fallback
)

class DiscoverKindnessCentersUseCase(
    private val repository: KindnessCenterRepository
) {
    suspend operator fun invoke(query: DiscoveryQuery): List<KindnessCenter> {
        val normalizedSearch = query.searchText.trim().lowercase()
        val filtered = repository.centers().filter { center ->
            val categoryMatch = query.categories.isEmpty() || center.categories.any { it in query.categories }
            val searchMatch = normalizedSearch.isEmpty() ||
                center.name.resolve(query.locale).lowercase().contains(normalizedSearch) ||
                center.description.resolve(query.locale).lowercase().contains(normalizedSearch) ||
                center.address.resolve(query.locale).lowercase().contains(normalizedSearch)
            categoryMatch && searchMatch
        }

        return query.location?.let { origin ->
            filtered.sortedBy { it.coordinates.distanceScoreTo(origin) }
        } ?: filtered.sortedBy { it.name.resolve(query.locale) }
    }
}

class CurrentNeedsUseCase {
    operator fun invoke(needs: List<CenterNeed>, referenceIsoDate: String): List<CenterNeed> =
        needs.filterNot { it.isExpired(referenceIsoDate) }
}

class MoneyDonationPolicy {
    fun canStartDonation(need: CenterNeed, hasBackendPaymentSessionOrExternalHandoff: Boolean): Boolean =
        need.canStartMoneyDonation(hasBackendPaymentSessionOrExternalHandoff)
}

class NotificationEligibilityPolicy {
    fun canNotify(
        type: NotificationType,
        preferences: NotificationPreference,
        hasNotificationPermission: Boolean,
        hasLocationPermission: Boolean = false
    ): Boolean = when (type) {
        NotificationType.NEARBY_CENTER ->
            hasNotificationPermission && hasLocationPermission && preferences.nearbyCentersEnabled
        NotificationType.URGENT_NEED ->
            hasNotificationPermission && preferences.urgentNeedsEnabled
        NotificationType.REMINDER ->
            hasNotificationPermission && preferences.remindersEnabled
    }
}

class VisitSignalPolicy {
    fun createExplicitSignal(
        id: String,
        centerId: String,
        userId: String?,
        submittedAtIsoDateTime: String,
        coordinateEvidence: Coordinates?
    ): VisitSignal = VisitSignal(
        id = id,
        centerId = centerId,
        userId = userId,
        submittedAtIsoDateTime = submittedAtIsoDateTime,
        coordinateEvidence = coordinateEvidence,
        explicitUserAction = true
    )

    fun statusAfterSignal(currentStatus: VerificationStatus): VerificationStatus =
        if (currentStatus == VerificationStatus.OFFICIALLY_VERIFIED) {
            VerificationStatus.OFFICIALLY_VERIFIED
        } else {
            VerificationStatus.VISIT_SIGNALED
        }
}

class ContributionPolicy {
    fun canRecordContribution(profile: UserProfile): Boolean = profile.mode == UserMode.REGISTERED && profile.id != null

    fun visibleHistory(profile: UserProfile, contributions: List<Contribution>): List<Contribution> =
        if (canRecordContribution(profile)) {
            contributions.filter { it.userId == profile.id }
        } else {
            emptyList()
        }

    fun pointsLabel(points: Int, locale: SupportedLocale): LocalizedText =
        LocalizedText(
            mapOf(
                SupportedLocale.ENGLISH.code to "$points generosity points",
                SupportedLocale.SPANISH.code to "$points puntos de generosidad"
            )
        )
}

class LocalizedNotificationTextProvider {
    fun title(type: NotificationType, locale: SupportedLocale): String = when (type) {
        NotificationType.NEARBY_CENTER -> when (locale) {
            SupportedLocale.ENGLISH -> "Kindness Center nearby"
            SupportedLocale.SPANISH -> "Centro de bondad cercano"
        }
        NotificationType.URGENT_NEED -> when (locale) {
            SupportedLocale.ENGLISH -> "Urgent need"
            SupportedLocale.SPANISH -> "Necesidad urgente"
        }
        NotificationType.REMINDER -> when (locale) {
            SupportedLocale.ENGLISH -> "Helping reminder"
            SupportedLocale.SPANISH -> "Recordatorio para ayudar"
        }
    }

    fun body(type: NotificationType, locale: SupportedLocale): String = when (type) {
        NotificationType.NEARBY_CENTER -> when (locale) {
            SupportedLocale.ENGLISH -> "There is a Kindness Center near you."
            SupportedLocale.SPANISH -> "Hay un centro de bondad cerca de ti."
        }
        NotificationType.URGENT_NEED -> when (locale) {
            SupportedLocale.ENGLISH -> "A Kindness Center has an urgent need that matches your interests."
            SupportedLocale.SPANISH -> "Un centro de bondad tiene una necesidad urgente que coincide con tus intereses."
        }
        NotificationType.REMINDER -> when (locale) {
            SupportedLocale.ENGLISH -> "You scheduled time to help."
            SupportedLocale.SPANISH -> "Programaste un momento para ayudar."
        }
    }
}

private fun Coordinates.distanceScoreTo(other: Coordinates): Double =
    sqrt((latitude - other.latitude).pow(2) + (longitude - other.longitude).pow(2))
