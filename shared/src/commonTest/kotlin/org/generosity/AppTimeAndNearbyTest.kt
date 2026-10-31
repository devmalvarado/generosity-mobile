package org.generosity

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.generosity.app.GenerosityAppState
import org.generosity.data.InMemoryContributionRepository
import org.generosity.domain.CenterCategory
import org.generosity.domain.CenterNeed
import org.generosity.domain.ContributionOffer
import org.generosity.domain.HelpMethod
import org.generosity.domain.InMemoryNeedCatalog
import org.generosity.domain.KindnessCenter
import org.generosity.domain.LocalizedText
import org.generosity.domain.NotificationPreference
import org.generosity.domain.RejectionReason
import org.generosity.domain.ReminderResolution
import org.generosity.domain.UserMode
import org.generosity.domain.UserProfile
import org.generosity.domain.VerificationStatus
import org.generosity.domain.Coordinates
import org.generosity.domain.formatInstantInZone
import org.generosity.domain.isDateOnlyExpired
import org.generosity.domain.resolveReminder
import org.generosity.usecase.NearbyAlertDefaults
import org.generosity.usecase.NearbyAlertPolicy
import org.generosity.usecase.NearbyDecision
import org.generosity.usecase.RecordContributionUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AppTimeAndNearbyTest {
    @Test
    fun dateOnlyExpiryIsInclusiveThroughTheUtcDate() {
        assertFalse(isDateOnlyExpired("2026-12-31", "2026-12-31T23:59:59Z"))
        assertTrue(isDateOnlyExpired("2026-12-31", "2027-01-01T00:00:00Z"))
    }

    @Test
    fun resumeDropsANeedThatExpiredWhileTheAppWasOpen() = runTest {
        var now = "2026-12-31T12:00:00Z"
        val app = GenerosityAppState(backgroundScope, clock = { now })
        app.start()
        runCurrent()
        app.selectCenter("kc-children-001")
        assertTrue(app.state.value.currentNeeds.any { it.id == "need-school-supplies" })

        now = "2027-01-01T00:00:00Z"
        app.onResume()
        runCurrent()

        assertTrue(app.state.value.currentNeeds.none { it.id == "need-school-supplies" })
        assertEquals("2027-01-01", app.state.value.referenceIsoDate)
    }

    @Test
    fun authorityTimeRejectsAnOfferTheDeviceStillShowsAsCurrent() = runTest {
        val need = CenterNeed(
            id = "need-dated",
            centerId = "center",
            title = LocalizedText.englishOnly("Dated"),
            description = LocalizedText.englishOnly("Dated"),
            helpMethods = setOf(HelpMethod.GOODS),
            expiresOnIsoDate = "2026-12-31",
            quantityRequired = 2
        )
        val catalog = InMemoryNeedCatalog(listOf(need))
        val result = RecordContributionUseCase(InMemoryContributionRepository(), catalog)(
            profile = UserProfile(id = "user", mode = UserMode.REGISTERED, displayName = "Ana"),
            contributionId = "offer-1",
            recordedAtIsoDateTime = "2026-12-31T18:00:00Z",
            need = need,
            offer = ContributionOffer(HelpMethod.GOODS, quantity = 1),
            authorityUtcInstant = "2027-01-01T00:00:00Z"
        )

        assertEquals(RejectionReason.NEED_NOT_OPEN, (result as org.generosity.domain.ContributionResult.Rejected).reason)
        assertEquals(2, catalog.needById("need-dated")?.quantityRemaining)
    }

    @Test
    fun ambiguousReminderTimesNeedAChoiceAndTravelKeepsTheInstant() {
        assertIs<ReminderResolution.NeedsChoice>(
            resolveReminder("2026-03-08", "02:30", "America/New_York")
        )
        assertIs<ReminderResolution.NeedsChoice>(
            resolveReminder("2026-11-01", "01:30", "America/New_York")
        )
        val saved = resolveReminder("2026-06-15", "09:00", "America/New_York")
        assertEquals(ReminderResolution.Saved("2026-06-15T13:00:00Z"), saved)
        val instant = (saved as ReminderResolution.Saved).utcInstant
        assertEquals("2026-06-15 15:00", formatInstantInZone(instant, "Europe/Madrid"))
        assertEquals(instant, (resolveReminder("2026-06-15", "09:00", "America/New_York") as ReminderResolution.Saved).utcInstant)
    }

    @Test
    fun nearbyRangeAndCooldownUseTheNamedDefaultsAtTheBoundary() {
        val policy = NearbyAlertPolicy()
        val center = sampleCenter()
        val optedIn = NotificationPreference(
            nearbyCentersEnabled = true,
            categoryInterests = setOf(CenterCategory.EDUCATION)
        )

        assertEquals(500, NearbyAlertDefaults.RANGE_METERS)
        assertEquals(24, NearbyAlertDefaults.COOLDOWN_HOURS)
        assertEquals(NearbyDecision.ELIGIBLE, decision(policy, center, optedIn, distance = 500.0))
        assertEquals(NearbyDecision.OUTSIDE_RANGE, decision(policy, center, optedIn, distance = 500.1))
        assertEquals(
            NearbyDecision.WITHIN_COOLDOWN,
            decision(policy, center, optedIn, lastAlertAtUtc = "2026-06-15T12:00:00Z", nowUtc = "2026-06-16T11:59:00Z")
        )
        assertEquals(
            NearbyDecision.ELIGIBLE,
            decision(policy, center, optedIn, lastAlertAtUtc = "2026-06-15T12:00:00Z", nowUtc = "2026-06-16T12:00:00Z")
        )
    }

    @Test
    fun emptyInterestsAreNotAllAndStaleOrBackgroundChecksDoNotAlert() {
        val policy = NearbyAlertPolicy()
        val center = sampleCenter()
        val optedIn = NotificationPreference(nearbyCentersEnabled = true)
        val explicitAll = optedIn.copy(allCategoryInterests = true)

        assertEquals(NearbyDecision.INTEREST_MISMATCH, decision(policy, center, optedIn))
        assertEquals(NearbyDecision.ELIGIBLE, decision(policy, center, explicitAll))
        assertEquals(NearbyDecision.STALE, decision(policy, center, explicitAll, catalogIsCurrent = false))
        assertEquals(
            NearbyDecision.WAITING_FOR_SUPPORTED_STATE,
            decision(policy, center, explicitAll, appIsForeground = false)
        )
        val notice = policy.settingsNotice()
        assertFalse(notice.resolve(org.generosity.domain.SupportedLocale.ENGLISH).contains("continuous tracking"))
        assertTrue(notice.resolve(org.generosity.domain.SupportedLocale.SPANISH).contains("aplicación abierta"))
    }

    private fun decision(
        policy: NearbyAlertPolicy,
        center: KindnessCenter,
        preferences: NotificationPreference,
        distance: Double = 100.0,
        lastAlertAtUtc: String? = null,
        nowUtc: String = "2026-06-15T12:00:00Z",
        catalogIsCurrent: Boolean = true,
        appIsForeground: Boolean = true
    ) = policy.evaluate(
        center = center,
        distanceMeters = distance,
        preferences = preferences,
        hasNotificationPermission = true,
        hasLocationPermission = true,
        lastAlertAtUtc = lastAlertAtUtc,
        nowUtc = nowUtc,
        catalogIsCurrent = catalogIsCurrent,
        appIsForeground = appIsForeground
    )

    private fun sampleCenter() = KindnessCenter(
        id = "kc-1",
        name = LocalizedText.englishOnly("Center"),
        description = LocalizedText.englishOnly("Center"),
        categories = setOf(CenterCategory.EDUCATION),
        coordinates = Coordinates(0.0, 0.0),
        address = LocalizedText.englishOnly("Here"),
        verificationStatus = VerificationStatus.IMPORTED
    )
}
