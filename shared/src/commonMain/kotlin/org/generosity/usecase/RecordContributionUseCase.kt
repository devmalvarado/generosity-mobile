package org.generosity.usecase

import org.generosity.domain.CenterNeed
import org.generosity.domain.Contribution
import org.generosity.domain.ContributionOffer
import org.generosity.domain.ContributionRepository
import org.generosity.domain.ContributionResult
import org.generosity.domain.ContributionStatus
import org.generosity.domain.HelpMethod
import org.generosity.domain.LocalizedText
import org.generosity.domain.NeedCatalog
import org.generosity.domain.PersonalImpact
import org.generosity.domain.RejectionReason
import org.generosity.domain.UserProfile

/**
 * Records one step of Center -> Need -> Contribution.
 *
 * Money is deliberately not recorded here: a monetary contribution only
 * exists once a payment provider confirms it, and this app never sees card
 * data. The use case refuses to start one unless a handoff exists.
 */
class RecordContributionUseCase(
    private val contributions: ContributionRepository,
    private val needs: NeedCatalog
) {
    suspend operator fun invoke(
        profile: UserProfile,
        contributionId: String,
        recordedAtIsoDateTime: String,
        need: CenterNeed?,
        offer: ContributionOffer,
        hasPaymentHandoff: Boolean = false
    ): ContributionResult {
        if (!ContributionPolicy().canRecordContribution(profile)) {
            return ContributionResult.Rejected(RejectionReason.REGISTRATION_REQUIRED)
        }
        val userId = checkNotNull(profile.id)

        if (offer.helpMethod == HelpMethod.MONEY && !hasPaymentHandoff) {
            return ContributionResult.Rejected(RejectionReason.PAYMENT_NOT_AVAILABLE)
        }

        // A UI can hold an old snapshot after another contribution updates the catalog.
        val currentNeed = need?.let {
            needs.needById(it.id)
                ?: return ContributionResult.Rejected(RejectionReason.NEED_NOT_FOUND)
        }
        val updatedNeed = when {
            currentNeed == null -> null
            !currentNeed.status.isOpen || currentNeed.isExpired(recordedAtIsoDateTime.take(10)) ->
                return ContributionResult.Rejected(RejectionReason.NEED_NOT_OPEN)
            offer.helpMethod !in currentNeed.helpMethods ->
                return ContributionResult.Rejected(RejectionReason.HELP_METHOD_NOT_ACCEPTED)
            offer.helpMethod == HelpMethod.GOODS && currentNeed.quantityRequired != null -> {
                val quantity = offer.quantity
                    ?: return ContributionResult.Rejected(RejectionReason.QUANTITY_REQUIRED)
                currentNeed.commitGoods(quantity)
                    ?: return ContributionResult.Rejected(RejectionReason.QUANTITY_EXCEEDS_REMAINING)
            }
            else -> null
        }

        val contribution = Contribution(
            id = contributionId,
            userId = userId,
            centerId = currentNeed?.centerId,
            needId = currentNeed?.id,
            title = currentNeed?.title ?: LocalizedText.of(
                offer.note ?: offer.activity ?: "Personal help",
                offer.note ?: offer.activity ?: "Ayuda personal"
            ),
            helpMethod = offer.helpMethod,
            recordedAtIsoDateTime = recordedAtIsoDateTime,
            status = ContributionStatus.OFFERED,
            quantity = offer.quantity,
            activity = offer.activity,
            availability = offer.availability,
            note = offer.note
        )

        updatedNeed?.let { needs.save(it) }
        return ContributionResult.Recorded(
            contribution = contributions.recordContribution(contribution),
            updatedNeed = updatedNeed
        )
    }
}

/**
 * Turns confirmed contributions into personal indicators. Points and rankings
 * are intentionally absent: impact describes participation, it does not
 * compare people.
 */
class PersonalImpactUseCase {
    operator fun invoke(contributions: List<Contribution>): PersonalImpact {
        val counted = contributions.filter { it.status.countsTowardImpact }
        return PersonalImpact(
            hoursOffered = counted.count { it.helpMethod == HelpMethod.TIME },
            goodsCommitted = counted.filter { it.helpMethod == HelpMethod.GOODS }.sumOf { it.quantity ?: 0 },
            monetaryContributions = counted.count { it.helpMethod == HelpMethod.MONEY },
            needsSupported = counted.mapNotNull { it.needId }.distinct().size,
            centersSupported = counted.mapNotNull { it.centerId }.distinct().size,
            personalActions = counted.count { it.centerId == null }
        )
    }
}
