package org.generosity.usecase

import org.generosity.data.NotificationCopy
import org.generosity.domain.CenterCategory
import org.generosity.domain.CenterNeed
import org.generosity.domain.Contribution
import org.generosity.domain.Coordinates
import org.generosity.domain.HelpMethod
import org.generosity.domain.KindnessCenter
import org.generosity.domain.KindnessCenterRepository
import org.generosity.domain.NotificationPreference
import org.generosity.domain.NotificationType
import org.generosity.domain.SupportedLocale
import org.generosity.domain.UserMode
import org.generosity.domain.UserProfile
import org.generosity.domain.VerificationStatus
import org.generosity.domain.VisitSignal
import org.generosity.domain.distanceMetersTo

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
        val normalizedSearch = query.searchText.searchKey()
        val filtered = repository.centers().filter { center ->
            val categoryMatch = query.categories.isEmpty() || center.categories.any { it in query.categories }
            val searchMatch = normalizedSearch.isEmpty() ||
                center.name.resolve(query.locale).searchKey().contains(normalizedSearch) ||
                center.description.resolve(query.locale).searchKey().contains(normalizedSearch) ||
                center.address.resolve(query.locale).searchKey().contains(normalizedSearch)
            categoryMatch && searchMatch
        }

        return query.location?.let { origin ->
            filtered.sortedBy { it.coordinates.distanceMetersTo(origin) }
        } ?: filtered.sortedBy { it.name.resolve(query.locale) }
    }
}

class CurrentNeedsUseCase {
    operator fun invoke(needs: List<CenterNeed>, referenceIsoDate: String): List<CenterNeed> =
        needs
            .filter { it.isCurrent(referenceIsoDate) }
            .sortedWith(compareByDescending<CenterNeed> { it.priority.rank }.thenBy { it.publishedOnIsoDate ?: "" })
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

}

class LocalizedNotificationTextProvider {
    fun title(type: NotificationType, locale: SupportedLocale): String =
        NotificationCopy.titles.getValue(type).resolve(locale)

    fun body(type: NotificationType, locale: SupportedLocale): String =
        NotificationCopy.bodies.getValue(type).resolve(locale)
}
