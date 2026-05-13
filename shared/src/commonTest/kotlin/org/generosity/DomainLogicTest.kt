package org.generosity

import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.generosity.data.ImportedCatalogDataSource
import org.generosity.data.ImportedCatalogRepository
import org.generosity.data.SampleImportedCatalog
import org.generosity.domain.CenterCategory
import org.generosity.domain.CenterNeed
import org.generosity.domain.Coordinates
import org.generosity.domain.HelpMethod
import org.generosity.domain.LocalizedText
import org.generosity.domain.NotificationPreference
import org.generosity.domain.NotificationType
import org.generosity.domain.SupportedLocale
import org.generosity.domain.UserMode
import org.generosity.domain.UserProfile
import org.generosity.domain.VerificationStatus
import org.generosity.domain.VisitSignal
import org.generosity.usecase.ContributionPolicy
import org.generosity.usecase.CurrentNeedsUseCase
import org.generosity.usecase.DiscoverKindnessCentersUseCase
import org.generosity.usecase.DiscoveryQuery
import org.generosity.usecase.LocalizedNotificationTextProvider
import org.generosity.usecase.MoneyDonationPolicy
import org.generosity.usecase.NotificationEligibilityPolicy
import org.generosity.usecase.VisitSignalPolicy

class DomainLogicTest {
    @Test
    fun localizedTextFallsBackToEnglish() {
        val text = LocalizedText.englishOnly("Only English")

        assertEquals("Only English", text.resolve(SupportedLocale.SPANISH))
    }

    @Test
    fun localePreferenceUsesSelectedBeforeDevice() {
        val locale = SupportedLocale.resolve(selectedCode = "es-MX", deviceCode = "en-US")

        assertEquals(SupportedLocale.SPANISH, locale)
    }

    @Test
    fun importedCatalogRepositoryReturnsEmptyListWhenDataSourceFails() {
        val repository = ImportedCatalogRepository(
            object : ImportedCatalogDataSource {
                override fun loadCenters() = throw RuntimeException("Bad import")
            }
        )

        val centers = runSuspend { repository.centers() }

        assertTrue(centers.isEmpty())
    }

    @Test
    fun discoveryFiltersByCategoryAndSearchText() {
        val repository = ImportedCatalogRepository(
            object : ImportedCatalogDataSource {
                override fun loadCenters() = SampleImportedCatalog.centers()
            }
        )
        val useCase = DiscoverKindnessCentersUseCase(repository)

        val centers = runSuspend {
            useCase(
                DiscoveryQuery(
                    searchText = "health",
                    categories = setOf(CenterCategory.HEALTH),
                    locale = SupportedLocale.ENGLISH
                )
            )
        }

        assertEquals(listOf("kc-health-001"), centers.map { it.id })
    }

    @Test
    fun currentNeedsExcludeExpiredNeeds() {
        val active = CenterNeed(
            id = "active",
            centerId = "center",
            title = LocalizedText.englishOnly("Active"),
            description = LocalizedText.englishOnly("Active"),
            helpMethods = setOf(HelpMethod.TIME),
            expiresOnIsoDate = "2026-12-01"
        )
        val expired = active.copy(id = "expired", expiresOnIsoDate = "2026-01-01")

        val current = CurrentNeedsUseCase()(listOf(active, expired), "2026-05-13")

        assertEquals(listOf("active"), current.map { it.id })
    }

    @Test
    fun moneyDonationRequiresPaymentHandoff() {
        val need = CenterNeed(
            id = "money",
            centerId = "center",
            title = LocalizedText.englishOnly("Money"),
            description = LocalizedText.englishOnly("Money"),
            helpMethods = setOf(HelpMethod.MONEY)
        )

        assertFalse(MoneyDonationPolicy().canStartDonation(need, hasBackendPaymentSessionOrExternalHandoff = false))
        assertTrue(MoneyDonationPolicy().canStartDonation(need, hasBackendPaymentSessionOrExternalHandoff = true))
    }

    @Test
    fun nearbyNotificationsRequireLocationAndNotificationPermissions() {
        val preferences = NotificationPreference(nearbyCentersEnabled = true)
        val policy = NotificationEligibilityPolicy()

        assertFalse(
            policy.canNotify(
                NotificationType.NEARBY_CENTER,
                preferences,
                hasNotificationPermission = true,
                hasLocationPermission = false
            )
        )
        assertTrue(
            policy.canNotify(
                NotificationType.NEARBY_CENTER,
                preferences,
                hasNotificationPermission = true,
                hasLocationPermission = true
            )
        )
    }

    @Test
    fun notificationTextUsesActiveLocale() {
        val provider = LocalizedNotificationTextProvider()

        assertEquals("Recordatorio para ayudar", provider.title(NotificationType.REMINDER, SupportedLocale.SPANISH))
        assertEquals("You scheduled time to help.", provider.body(NotificationType.REMINDER, SupportedLocale.ENGLISH))
    }

    @Test
    fun registeredUsersCanViewOwnContributionHistory() {
        val profile = UserProfile(id = "user-1", mode = UserMode.REGISTERED)

        assertTrue(ContributionPolicy().canRecordContribution(profile))
    }

    @Test
    fun anonymousUsersCannotRecordContributionHistory() {
        val profile = UserProfile(mode = UserMode.ANONYMOUS)

        assertFalse(ContributionPolicy().canRecordContribution(profile))
    }

    @Test
    fun visitSignalsRequireExplicitUserAction() {
        assertFailsWith<IllegalArgumentException> {
            VisitSignal(
                id = "signal-1",
                centerId = "center",
                userId = null,
                submittedAtIsoDateTime = "2026-05-13T10:00:00Z",
                coordinateEvidence = Coordinates(19.4, -99.1),
                explicitUserAction = false
            )
        }
    }

    @Test
    fun visitSignalDoesNotCreateOfficialVerification() {
        val policy = VisitSignalPolicy()

        assertEquals(VerificationStatus.VISIT_SIGNALED, policy.statusAfterSignal(VerificationStatus.IMPORTED))
        assertEquals(
            VerificationStatus.OFFICIALLY_VERIFIED,
            policy.statusAfterSignal(VerificationStatus.OFFICIALLY_VERIFIED)
        )
    }
}

private fun <T> runSuspend(block: suspend () -> T): T {
    var completed: Any? = null
    block.startCoroutine(
        object : Continuation<T> {
            override val context = EmptyCoroutineContext

            override fun resumeWith(result: Result<T>) {
                completed = result
            }
        }
    )
    @Suppress("UNCHECKED_CAST")
    return (completed as Result<T>).getOrThrow()
}

