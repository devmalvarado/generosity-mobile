package org.generosity.domain

enum class SupportedLocale(val code: String, val nativeName: String) {
    ENGLISH("en", "English"),
    SPANISH("es", "Español");

    companion object {
        val fallback: SupportedLocale = ENGLISH

        fun fromCode(code: String?): SupportedLocale? {
            var candidate = code?.trim()?.replace('_', '-').orEmpty()
            while (candidate.isNotEmpty()) {
                entries.firstOrNull { it.code.equals(candidate, ignoreCase = true) }?.let { return it }
                candidate = candidate.substringBeforeLast('-', "")
            }
            return null
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

    companion object {
        fun fromDeviceLanguages(selectedCode: String?, deviceCodes: List<String>): LocalePreference =
            LocalePreference(
                selectedLocale = SupportedLocale.fromCode(selectedCode),
                deviceLocale = deviceCodes.firstNotNullOfOrNull(SupportedLocale::fromCode)
            )
    }
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
    BLOOD_DONATION("blood_donation"),
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
) {
    fun links(): List<ContactLink> = buildList {
        phone?.replace(Regex("[ ()-]"), "")?.takeIf { it.matches(Regex("\\+?[0-9]{7,15}")) }
            ?.let { add(ContactLink("phone", "tel:$it")) }
        email?.takeIf { it.matches(Regex("[A-Za-z0-9._+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")) }
            ?.let { add(ContactLink("email", "mailto:$it")) }
        website?.takeIf { it.matches(Regex("https?://[A-Za-z0-9.-]+(?::[0-9]{1,5})?(?:[/?#][^\\s\\p{Cc}\\\\]*)?")) }
            ?.let { add(ContactLink("website", it)) }
    }
}

data class ContactLink(val kind: String, val url: String)

data class CenterNeed(
    val id: String,
    val centerId: String,
    val title: LocalizedText,
    val description: LocalizedText,
    val helpMethods: Set<HelpMethod>,
    val urgent: Boolean = false,
    val expiresOnIsoDate: String? = null,
    val category: CenterCategory? = null,
    val priority: NeedPriority = if (urgent) NeedPriority.URGENT else NeedPriority.NORMAL,
    val status: NeedStatus = NeedStatus.ACTIVE,
    val quantityRequired: Int? = null,
    val quantityCommitted: Int = 0,
    val publishedOnIsoDate: String? = null,
    val quantityUnit: String? = null
) {
    init {
        quantityRequired?.let { required ->
            require(required > 0) { "A required quantity must be greater than zero." }
            require(quantityCommitted in 0..required) {
                "Committed quantity must stay between zero and the required quantity."
            }
        }
    }

    val quantityRemaining: Int?
        get() = quantityRequired?.minus(quantityCommitted)

    /** Urgent is derived from priority so the two can never disagree. */
    val isUrgent: Boolean
        get() = priority.isUrgent

    fun isExpired(referenceIsoDate: String): Boolean {
        if (status == NeedStatus.EXPIRED) return true
        val expiration = expiresOnIsoDate ?: return false
        return expiration < referenceIsoDate
    }

    /** Date-only expiry compared with an authoritative UTC instant, not a device date. */
    fun isExpiredAt(authorityUtcInstant: String): Boolean {
        if (status == NeedStatus.EXPIRED) return true
        val expiration = expiresOnIsoDate ?: return false
        return isDateOnlyExpired(expiration, authorityUtcInstant)
    }

    fun isCurrent(referenceIsoDate: String): Boolean =
        status.isOpen && !isExpired(referenceIsoDate)

    fun canStartMoneyDonation(hasPaymentHandoff: Boolean): Boolean =
        HelpMethod.MONEY in helpMethods && hasPaymentHandoff

    /** Returns the need after committing [quantity] goods, or null when it does not fit. */
    fun commitGoods(quantity: Int): CenterNeed? {
        val required = quantityRequired ?: return null
        if (quantity !in 1..(required - quantityCommitted)) return null
        val committed = quantityCommitted + quantity
        return copy(
            quantityCommitted = committed,
            status = if (committed == required) NeedStatus.COVERED else status
        )
    }
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
    val note: String? = null,
    val needId: String? = null,
    val status: ContributionStatus = ContributionStatus.OFFERED,
    val quantity: Int? = null,
    val quantityUnit: String? = null,
    val activity: String? = null,
    val availability: String? = null,
    val plannedDurationMinutes: Int? = null,
    val actualDurationMinutes: Int? = null,
    val provenance: ContributionProvenance = ContributionProvenance.CENTER_OFFER,
    val syncState: SyncState = SyncState.LOCAL,
    val version: Int = 1,
    val events: List<ContributionEvent> = emptyList()
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
    val categoryInterests: Set<CenterCategory> = emptySet(),
    /** Empty interests are not an implicit All. This must be chosen explicitly. */
    val allCategoryInterests: Boolean = false
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

