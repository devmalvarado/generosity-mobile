package org.generosity.usecase

import org.generosity.domain.CenterNeed
import org.generosity.domain.Contribution
import org.generosity.domain.ContributionEvent
import org.generosity.domain.ContributionOffer
import org.generosity.domain.ContributionProvenance
import org.generosity.domain.ContributionRepository
import org.generosity.domain.ContributionResult
import org.generosity.domain.ContributionStatus
import org.generosity.domain.HelpMethod
import org.generosity.domain.LocalizedText
import org.generosity.domain.NeedCatalog
import org.generosity.domain.PersonalImpact
import org.generosity.domain.RejectionReason
import org.generosity.domain.SyncState
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
        hasPaymentHandoff: Boolean = false,
        authorityUtcInstant: String = recordedAtIsoDateTime
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
            !currentNeed.status.isOpen || expiredByAuthority(currentNeed, authorityUtcInstant) ->
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
            quantityUnit = offer.quantityUnit,
            activity = offer.activity,
            availability = offer.availability,
            plannedDurationMinutes = offer.plannedDurationMinutes,
            actualDurationMinutes = offer.actualDurationMinutes,
            provenance = if (currentNeed == null) {
                ContributionProvenance.SELF_REPORTED
            } else {
                ContributionProvenance.CENTER_OFFER
            },
            syncState = SyncState.LOCAL,
            version = 1,
            events = listOf(
                ContributionEvent(
                    version = 1,
                    status = ContributionStatus.OFFERED,
                    atUtc = recordedAtIsoDateTime,
                    actorId = userId
                )
            ),
            note = offer.note
        )

        updatedNeed?.let { needs.save(it) }
        return ContributionResult.Recorded(
            contribution = contributions.recordContribution(contribution),
            updatedNeed = updatedNeed
        )
    }
}

private fun expiredByAuthority(need: CenterNeed, authorityUtcInstant: String): Boolean =
    try {
        need.isExpiredAt(authorityUtcInstant)
    } catch (error: IllegalArgumentException) {
        true
    }

/**
 * Turns confirmed contributions into personal indicators. Points and rankings
 * are intentionally absent: impact describes participation, it does not
 * compare people.
 */
class PersonalImpactUseCase {
    operator fun invoke(contributions: List<Contribution>): PersonalImpact {
        val counted = contributions.filter { it.status.countsTowardImpact }
        val goods = counted.filter { it.helpMethod == HelpMethod.GOODS }
        val groups = goods.groupBy { record ->
            record.quantityUnit?.takeIf { it.isNotBlank() } ?: UNSPECIFIED_UNIT
        }
        val totals = groups.mapValues { (_, records) -> records.sumOf { it.quantity ?: 0 } }
        val time = counted.filter { it.helpMethod == HelpMethod.TIME }
        return PersonalImpact(
            hoursOffered = 0,
            goodsCommitted = if (groups.size == 1) totals.values.single() else 0,
            monetaryContributions = 0,
            needsSupported = counted.mapNotNull { it.needId }.distinct().size,
            centersSupported = counted.mapNotNull { it.centerId }.distinct().size,
            personalActions = counted.count { it.provenance == ContributionProvenance.SELF_REPORTED },
            goodsByUnit = if (groups.size == 1 && groups.keys.single() == UNSPECIFIED_UNIT) emptyMap() else totals,
            plannedDurationMinutes = time
                .filter { it.status != ContributionStatus.FULFILLED }
                .sumOf { it.plannedDurationMinutes?.takeIf { minutes -> minutes > 0 } ?: 0 },
            durationMinutes = time
                .filter { it.status == ContributionStatus.FULFILLED }
                .sumOf { it.actualDurationMinutes?.takeIf { minutes -> minutes > 0 } ?: 0 },
            durationUnavailable = time.any { recordedDuration(it) == null },
            monetaryUnavailable = true
        )
    }

    private fun recordedDuration(contribution: Contribution): Int? =
        contribution.actualDurationMinutes?.takeIf { it > 0 }
            ?: contribution.plannedDurationMinutes?.takeIf { it > 0 }

    private companion object {
        const val UNSPECIFIED_UNIT = "unspecified"
    }
}
