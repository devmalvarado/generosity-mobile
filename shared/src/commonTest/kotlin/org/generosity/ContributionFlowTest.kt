package org.generosity

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.generosity.data.InMemoryContributionRepository
import org.generosity.domain.CenterCategory
import org.generosity.domain.CenterNeed
import org.generosity.domain.Contribution
import org.generosity.domain.ContributionOffer
import org.generosity.domain.ContributionResult
import org.generosity.domain.ContributionStatus
import org.generosity.domain.Coordinates
import org.generosity.domain.HelpMethod
import org.generosity.domain.InMemoryNeedCatalog
import org.generosity.domain.LocalizedText
import org.generosity.domain.NeedPriority
import org.generosity.domain.NeedStatus
import org.generosity.domain.RejectionReason
import org.generosity.domain.UserMode
import org.generosity.domain.UserProfile
import org.generosity.domain.distanceMetersTo
import org.generosity.usecase.CurrentNeedsUseCase
import org.generosity.usecase.PersonalImpactUseCase
import org.generosity.usecase.RecordContributionUseCase

class ContributionFlowTest {
    private val registered = UserProfile(id = "user-1", mode = UserMode.REGISTERED, displayName = "Ana")
    private val anonymous = UserProfile(mode = UserMode.ANONYMOUS)

    @Test
    fun committingGoodsReducesWhatIsStillNeeded() = runTest {
        val catalog = InMemoryNeedCatalog(listOf(diapers(required = 20, committed = 0)))
        val useCase = RecordContributionUseCase(InMemoryContributionRepository(), catalog)

        val result = useCase(
            registered, "contribution-1", "2026-05-13T09:00:00Z",
            catalog.needById("need-diapers"),
            ContributionOffer(HelpMethod.GOODS, quantity = 5)
        )

        val recorded = result as ContributionResult.Recorded
        assertEquals(5, recorded.updatedNeed?.quantityCommitted)
        assertEquals(15, recorded.updatedNeed?.quantityRemaining)
        assertEquals(NeedStatus.ACTIVE, recorded.updatedNeed?.status)
        assertEquals(15, catalog.needById("need-diapers")?.quantityRemaining)
    }

    @Test
    fun anOldNeedSnapshotCannotOverwriteAnEarlierCommitment() = runTest {
        val original = diapers(required = 20, committed = 0)
        val catalog = InMemoryNeedCatalog(listOf(original))
        val useCase = RecordContributionUseCase(InMemoryContributionRepository(), catalog)

        for (quantity in listOf(3, 4)) {
            useCase(
                registered, "contribution-$quantity", "2026-05-13T09:00:00Z",
                original, ContributionOffer(HelpMethod.GOODS, quantity = quantity)
            )
        }

        assertEquals(7, catalog.needById(original.id)?.quantityCommitted)
        assertEquals(13, catalog.needById(original.id)?.quantityRemaining)
    }

    @Test
    fun coveringTheFullQuantityClosesTheNeed() = runTest {
        val catalog = InMemoryNeedCatalog(listOf(diapers(required = 20, committed = 15)))
        val useCase = RecordContributionUseCase(InMemoryContributionRepository(), catalog)

        val result = useCase(
            registered, "contribution-2", "2026-05-13T09:00:00Z",
            catalog.needById("need-diapers"),
            ContributionOffer(HelpMethod.GOODS, quantity = 5)
        )

        assertEquals(NeedStatus.COVERED, (result as ContributionResult.Recorded).updatedNeed?.status)
    }

    @Test
    fun aCommitmentLargerThanWhatRemainsIsRefused() = runTest {
        val catalog = InMemoryNeedCatalog(listOf(diapers(required = 20, committed = 18)))
        val useCase = RecordContributionUseCase(InMemoryContributionRepository(), catalog)

        val result = useCase(
            registered, "contribution-3", "2026-05-13T09:00:00Z",
            catalog.needById("need-diapers"),
            ContributionOffer(HelpMethod.GOODS, quantity = 5)
        )

        assertEquals(
            RejectionReason.QUANTITY_EXCEEDS_REMAINING,
            (result as ContributionResult.Rejected).reason
        )
        assertEquals(18, catalog.needById("need-diapers")?.quantityCommitted)
    }

    @Test
    fun anonymousBrowsingCannotRecordAContribution() = runTest {
        val catalog = InMemoryNeedCatalog(listOf(diapers(required = 20, committed = 0)))
        val useCase = RecordContributionUseCase(InMemoryContributionRepository(), catalog)

        val result = useCase(
            anonymous, "contribution-4", "2026-05-13T09:00:00Z",
            catalog.needById("need-diapers"),
            ContributionOffer(HelpMethod.TIME, activity = "Lunch shift")
        )

        assertEquals(RejectionReason.REGISTRATION_REQUIRED, (result as ContributionResult.Rejected).reason)
    }

    @Test
    fun moneyIsNeverRecordedWithoutAPaymentHandoff() = runTest {
        val catalog = InMemoryNeedCatalog(listOf(diapers(required = 20, committed = 0)))
        val useCase = RecordContributionUseCase(InMemoryContributionRepository(), catalog)

        val result = useCase(
            registered, "contribution-5", "2026-05-13T09:00:00Z",
            catalog.needById("need-diapers"),
            ContributionOffer(HelpMethod.MONEY)
        )

        assertEquals(RejectionReason.PAYMENT_NOT_AVAILABLE, (result as ContributionResult.Rejected).reason)
    }

    @Test
    fun aClosedNeedAcceptsNoFurtherHelp() = runTest {
        val catalog = InMemoryNeedCatalog(
            listOf(diapers(required = 20, committed = 20).copy(status = NeedStatus.COVERED))
        )
        val useCase = RecordContributionUseCase(InMemoryContributionRepository(), catalog)

        val result = useCase(
            registered, "contribution-6", "2026-05-13T09:00:00Z",
            catalog.needById("need-diapers"),
            ContributionOffer(HelpMethod.TIME)
        )

        assertEquals(RejectionReason.NEED_NOT_OPEN, (result as ContributionResult.Rejected).reason)
    }

    @Test
    fun impactCountsOnlyConfirmedParticipationAndNeverRanksPeople() = runTest {
        val repository = InMemoryContributionRepository()
        val catalog = InMemoryNeedCatalog(listOf(diapers(required = 20, committed = 0)))
        val useCase = RecordContributionUseCase(repository, catalog)
        useCase(
            registered, "contribution-7", "2026-05-13T09:00:00Z",
            catalog.needById("need-diapers"),
            ContributionOffer(HelpMethod.GOODS, quantity = 4)
        )
        val offered = repository.contributionsForUser("user-1").single()
        repository.recordContribution(offered.copy(status = ContributionStatus.CONFIRMED))

        val impact = PersonalImpactUseCase()(repository.contributionsForUser("user-1"))

        assertEquals(4, impact.goodsCommitted)
        assertEquals(1, impact.needsSupported)
        assertEquals(1, impact.centersSupported)
        assertFalse(impact.isEmpty)
    }

    @Test
    fun recordedOfferKeepsEnteredUnitDurationAndHistory() = runTest {
        val catalog = InMemoryNeedCatalog(listOf(diapers(required = 20, committed = 0)))
        val repository = InMemoryContributionRepository()
        val result = RecordContributionUseCase(repository, catalog)(
            registered, "contribution-unit", "2026-05-13T09:00:00Z",
            catalog.needById("need-diapers"),
            ContributionOffer(
                HelpMethod.GOODS,
                quantity = 3,
                quantityUnit = "item",
                note = "notebooks"
            )
        )

        val recorded = (result as ContributionResult.Recorded).contribution
        assertEquals("item", recorded.quantityUnit)
        assertEquals(1, recorded.version)
        assertEquals(ContributionStatus.OFFERED, recorded.events.single().status)
        assertEquals("user-1", recorded.events.single().actorId)
    }

    @Test
    fun impactSeparatesUnitsAndDoesNotInventMissingMetrics() {
        val impact = PersonalImpactUseCase()(
            listOf(
                recordedGoods("liters", ContributionStatus.CONFIRMED, 2, "liter"),
                recordedGoods("items", ContributionStatus.FULFILLED, 5, "item"),
                recordedGoods("offered", ContributionStatus.OFFERED, 9, "item"),
                recordedGoods("cancelled", ContributionStatus.CANCELLED, 8, "item"),
                recordedTime("missing-duration", ContributionStatus.CONFIRMED, planned = null, actual = null),
                recordedTime("planned", ContributionStatus.CONFIRMED, planned = 90, actual = null),
                recordedTime("done", ContributionStatus.FULFILLED, planned = 30, actual = 45),
                recordedGoods("money", ContributionStatus.CONFIRMED, 1, "currency").copy(helpMethod = HelpMethod.MONEY)
            )
        )

        assertEquals(0, impact.goodsCommitted)
        assertEquals(0, impact.hoursOffered)
        assertEquals(0, impact.monetaryContributions)
        assertEquals(mapOf("liter" to 2, "item" to 5), impact.goodsByUnit)
        assertEquals(90, impact.plannedDurationMinutes)
        assertEquals(45, impact.durationMinutes)
        assertTrue(impact.durationUnavailable)
        assertTrue(impact.monetaryUnavailable)
    }

    @Test
    fun currentNeedsLeadWithTheMostUrgentAndDropClosedOnes() {
        val urgent = diapers(required = 10, committed = 0).copy(id = "urgent", priority = NeedPriority.URGENT, urgent = true)
        val normal = diapers(required = 10, committed = 0).copy(id = "normal", priority = NeedPriority.NORMAL)
        val covered = diapers(required = 10, committed = 10).copy(id = "covered", status = NeedStatus.COVERED)

        val current = CurrentNeedsUseCase()(listOf(normal, covered, urgent), "2026-05-13")

        assertEquals(listOf("urgent", "normal"), current.map { it.id })
    }

    @Test
    fun nearbyOrderingUsesMetersRatherThanRawCoordinates() {
        val center = Coordinates(19.4326, -99.1332)
        val threeHundredMetersAway = Coordinates(19.4353, -99.1332)

        val distance = center.distanceMetersTo(threeHundredMetersAway)

        assertTrue(distance in 250.0..350.0, "Expected roughly 300 m, was $distance")
    }

    private fun diapers(required: Int, committed: Int) = CenterNeed(
        id = "need-diapers",
        centerId = "kc-children-001",
        title = LocalizedText.of("Diapers", "Pañales"),
        description = LocalizedText.of("Diaper packs", "Paquetes de pañales"),
        helpMethods = setOf(HelpMethod.GOODS, HelpMethod.MONEY, HelpMethod.TIME),
        category = CenterCategory.CHILDREN_SUPPORT,
        quantityRequired = required,
        quantityCommitted = committed,
        expiresOnIsoDate = "2026-12-31"
    )

    private fun recordedGoods(
        id: String,
        status: ContributionStatus,
        quantity: Int,
        unit: String
    ) = Contribution(
        id = id,
        userId = "user-1",
        centerId = "kc-children-001",
        needId = "need-diapers",
        title = LocalizedText.of("Goods", "Bienes"),
        helpMethod = HelpMethod.GOODS,
        recordedAtIsoDateTime = "2026-05-13T09:00:00Z",
        status = status,
        quantity = quantity,
        quantityUnit = unit
    )

    private fun recordedTime(
        id: String,
        status: ContributionStatus,
        planned: Int?,
        actual: Int?
    ) = Contribution(
        id = id,
        userId = "user-1",
        centerId = "kc-children-001",
        needId = "need-time",
        title = LocalizedText.of("Time", "Tiempo"),
        helpMethod = HelpMethod.TIME,
        recordedAtIsoDateTime = "2026-05-13T09:00:00Z",
        status = status,
        plannedDurationMinutes = planned,
        actualDurationMinutes = actual
    )
}
