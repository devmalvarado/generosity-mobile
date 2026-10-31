package org.generosity.service

/**
 * Identity, payment and delivery stay behind ports. An unconfigured port
 * refuses the action. It never invents a session, charge or sent message.
 */
interface IdentityProvider {
    fun validate(credential: String): IdentityResult
}

sealed class IdentityResult {
    data class Accepted(val accountId: String) : IdentityResult()
    data object Rejected : IdentityResult()
    data object Unavailable : IdentityResult()
}

object UnconfiguredIdentityProvider : IdentityProvider {
    override fun validate(credential: String): IdentityResult = IdentityResult.Unavailable
}

interface PaymentGate {
    val configured: Boolean
}

object UnconfiguredPaymentGate : PaymentGate {
    override val configured: Boolean = false
}

interface NotificationDelivery {
    val configured: Boolean
}

object UnconfiguredNotificationDelivery : NotificationDelivery {
    override val configured: Boolean = false
}

enum class Allowance {
    PERMITTED,
    LIMITED,
    UNCONFIGURED
}

/** No production threshold is selected. A missing limit disables the write. */
interface OfferAllowance {
    fun permit(accountId: String): Allowance
}

object UnconfiguredOfferAllowance : OfferAllowance {
    override fun permit(accountId: String): Allowance = Allowance.UNCONFIGURED
}

sealed class StaffAccess {
    data class Assigned(val actorId: String) : StaffAccess()
    data object Denied : StaffAccess()
    data object Unavailable : StaffAccess()
}

/** Production has no staff assignment until an operator port is configured. */
interface StaffAssignment {
    fun actorFor(credential: String, centerId: String): StaffAccess
}

object UnconfiguredStaffAssignment : StaffAssignment {
    override fun actorFor(credential: String, centerId: String): StaffAccess = StaffAccess.Unavailable
}

class DeploymentSecrets(values: Set<String>) {
    private val secrets = values.filter { it.isNotEmpty() }

    fun redact(text: String): String = secrets.fold(text) { acc, secret -> acc.replace(secret, "[redacted]") }
}
