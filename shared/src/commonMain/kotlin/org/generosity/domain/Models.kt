package org.generosity.domain

enum class SupportedLocale(val code: String) {
    ENGLISH("en"),
    SPANISH("es");

    companion object {
        val fallback: SupportedLocale = ENGLISH

        fun fromCode(code: String?): SupportedLocale? {
            val normalized = code?.trim()?.lowercase().orEmpty()
            return when {
                normalized == "en" || normalized.startsWith("en-") || normalized.startsWith("en_") -> ENGLISH
                normalized == "es" || normalized.startsWith("es-") || normalized.startsWith("es_") -> SPANISH
                else -> null
            }
        }

        fun resolve(
            selectedCode: String?,
            deviceCode: String?,
            fallbackLocale: SupportedLocale = fallback
        ): SupportedLocale = fromCode(selectedCode) ?: fromCode(deviceCode) ?: fallbackLocale
    }
}

data class LocalePreference(
    val selectedLocale: SupportedLocale? = null,
    val deviceLocale: SupportedLocale? = null,
    val fallbackLocale: SupportedLocale = SupportedLocale.fallback
) {
    val activeLocale: SupportedLocale
        get() = selectedLocale ?: deviceLocale ?: fallbackLocale
}

data class LocalizedText(
    val values: Map<String, String>
) {
    fun resolve(
        preferredLocale: SupportedLocale,
        fallbackLocale: SupportedLocale = SupportedLocale.fallback
    ): String {
        val preferred = values[preferredLocale.code]?.takeIf { it.isNotBlank() }
        if (preferred != null) return preferred

        val fallback = values[fallbackLocale.code]?.takeIf { it.isNotBlank() }
        if (fallback != null) return fallback

        return SupportedLocale.entries
            .asSequence()
            .mapNotNull { values[it.code]?.takeIf(String::isNotBlank) }
            .firstOrNull()
            ?: values.values.firstOrNull { it.isNotBlank() }
            ?: ""
    }

    companion object {
        fun of(english: String, spanish: String): LocalizedText = LocalizedText(
            mapOf(
                SupportedLocale.ENGLISH.code to english,
                SupportedLocale.SPANISH.code to spanish
            )
        )

        fun englishOnly(value: String): LocalizedText = LocalizedText(
            mapOf(SupportedLocale.ENGLISH.code to value)
        )

        fun spanishOnly(value: String): LocalizedText = LocalizedText(
            mapOf(SupportedLocale.SPANISH.code to value)
        )
    }
}

enum class CenterCategory(val code: String) {
    CHILDREN_SUPPORT("children_support"),
    OLDER_ADULTS("older_adults"),
    DISABILITY_SUPPORT("disability_support"),
    HEALTH("health"),
    ADDICTION_RECOVERY("addiction_recovery"),
    COMMUNITY_KITCHEN("community_kitchen"),
    EDUCATION("education"),
    VULNERABLE_FAMILIES("vulnerable_families"),
    ORGAN_TISSUE_DONATION("organ_tissue_donation")
}

enum class HelpMethod(val code: String) {
    TIME("time"),
    GOODS("goods"),
    MONEY("money")
}

enum class VerificationStatus(val code: String) {
    IMPORTED("imported"),
    VISIT_SIGNALED("visit_signaled"),
    OFFICIALLY_VERIFIED("officially_verified")
}

enum class UserMode(val code: String) {
    ANONYMOUS("anonymous"),
    REGISTERED("registered")
}

enum class NotificationType(val code: String) {
    NEARBY_CENTER("nearby_center"),
    URGENT_NEED("urgent_need"),
    REMINDER("reminder")
}

data class Coordinates(
    val latitude: Double,
    val longitude: Double
)

data class ContactInfo(
    val phone: String? = null,
    val email: String? = null,
    val website: String? = null
)

data class CenterNeed(
    val id: String,
    val centerId: String,
    val title: LocalizedText,
    val description: LocalizedText,
    val helpMethods: Set<HelpMethod>,
    val urgent: Boolean = false,
    val expiresOnIsoDate: String? = null
) {
    fun isExpired(referenceIsoDate: String): Boolean {
        val expiration = expiresOnIsoDate ?: return false
        return expiration < referenceIsoDate
    }

    fun canStartMoneyDonation(hasPaymentHandoff: Boolean): Boolean =
        HelpMethod.MONEY in helpMethods && hasPaymentHandoff
}

data class KindnessCenter(
    val id: String,
    val name: LocalizedText,
    val description: LocalizedText,
    val categories: Set<CenterCategory>,
    val coordinates: Coordinates,
    val address: LocalizedText,
    val contactInfo: ContactInfo = ContactInfo(),
    val verificationStatus: VerificationStatus = VerificationStatus.IMPORTED,
    val needs: List<CenterNeed> = emptyList(),
    val importedAtIsoDate: String? = null
)

data class UserProfile(
    val id: String? = null,
    val mode: UserMode = UserMode.ANONYMOUS,
    val displayName: String? = null,
    val generosityPoints: Int = 0
) {
    val isRegistered: Boolean
        get() = mode == UserMode.REGISTERED && id != null
}

data class Contribution(
    val id: String,
    val userId: String,
    val centerId: String? = null,
    val title: LocalizedText,
    val helpMethod: HelpMethod,
    val recordedAtIsoDateTime: String,
    val pointsAwarded: Int = 0,
    val note: String? = null
)

data class Reminder(
    val id: String,
    val userId: String,
    val title: LocalizedText,
    val dueAtIsoDateTime: String,
    val enabled: Boolean = true
)

data class NotificationPreference(
    val nearbyCentersEnabled: Boolean = false,
    val urgentNeedsEnabled: Boolean = false,
    val remindersEnabled: Boolean = false,
    val categoryInterests: Set<CenterCategory> = emptySet()
) {
    fun isEnabled(type: NotificationType): Boolean = when (type) {
        NotificationType.NEARBY_CENTER -> nearbyCentersEnabled
        NotificationType.URGENT_NEED -> urgentNeedsEnabled
        NotificationType.REMINDER -> remindersEnabled
    }
}

data class VisitSignal(
    val id: String,
    val centerId: String,
    val userId: String?,
    val submittedAtIsoDateTime: String,
    val coordinateEvidence: Coordinates? = null,
    val explicitUserAction: Boolean
) {
    init {
        require(explicitUserAction) { "Visit signals require explicit user action." }
    }

    fun resultingVerificationStatus(currentStatus: VerificationStatus): VerificationStatus =
        if (currentStatus == VerificationStatus.OFFICIALLY_VERIFIED) {
            VerificationStatus.OFFICIALLY_VERIFIED
        } else {
            VerificationStatus.VISIT_SIGNALED
        }
}

