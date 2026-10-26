package org.generosity.domain

/**
 * Lifecycle of a published need. Centers own the transitions; the mobile
 * app only observes them and never publishes needs itself.
 */
enum class NeedStatus(val code: String) {
    ACTIVE("active"),
    COVERED("covered"),
    EXPIRED("expired"),
    CANCELLED("cancelled");

    val isOpen: Boolean
        get() = this == ACTIVE
}

enum class NeedPriority(val code: String, val rank: Int) {
    NORMAL("normal", 0),
    MEDIUM("medium", 1),
    HIGH("high", 2),
    URGENT("urgent", 3);

    val isUrgent: Boolean
        get() = this == URGENT
}

enum class ContributionStatus(val code: String) {
    OFFERED("offered"),
    CONFIRMED("confirmed"),
    CANCELLED("cancelled"),
    FULFILLED("fulfilled");

    val countsTowardImpact: Boolean
        get() = this == CONFIRMED || this == FULFILLED
}

/** What the user is offering. Quantity only applies to goods. */
data class ContributionOffer(
    val helpMethod: HelpMethod,
    val quantity: Int? = null,
    val activity: String? = null,
    val availability: String? = null,
    val note: String? = null
) {
    init {
        quantity?.let { require(it > 0) { "A goods commitment must be greater than zero." } }
    }
}

/**
 * Outcome of recording a contribution. Failures are values, not exceptions,
 * so both native UIs can render them without catching platform exceptions.
 */
sealed class ContributionResult {
    data class Recorded(val contribution: Contribution, val updatedNeed: CenterNeed?) : ContributionResult()
    data class Rejected(val reason: RejectionReason) : ContributionResult()
}

enum class RejectionReason(val code: String) {
    REGISTRATION_REQUIRED("registration_required"),
    NEED_NOT_FOUND("need_not_found"),
    NEED_NOT_OPEN("need_not_open"),
    HELP_METHOD_NOT_ACCEPTED("help_method_not_accepted"),
    QUANTITY_REQUIRED("quantity_required"),
    QUANTITY_EXCEEDS_REMAINING("quantity_exceeds_remaining"),
    PAYMENT_NOT_AVAILABLE("payment_not_available")
}

data class PersonalImpact(
    val hoursOffered: Int = 0,
    val goodsCommitted: Int = 0,
    val monetaryContributions: Int = 0,
    val needsSupported: Int = 0,
    val centersSupported: Int = 0,
    val personalActions: Int = 0
) {
    val isEmpty: Boolean
        get() = hoursOffered == 0 && goodsCommitted == 0 && monetaryContributions == 0 &&
            needsSupported == 0 && centersSupported == 0 && personalActions == 0
}
