package org.generosity.domain

interface KindnessCenterRepository {
    suspend fun centers(): List<KindnessCenter>
    suspend fun centerById(id: String): KindnessCenter? = centers().firstOrNull { it.id == id }
}

interface NeedsRepository {
    suspend fun needsForCenter(centerId: String): List<CenterNeed>
}

interface UserProfileRepository {
    suspend fun currentProfile(): UserProfile
    suspend fun saveProfile(profile: UserProfile)
}

interface ContributionRepository {
    suspend fun contributionsForUser(userId: String): List<Contribution>
    suspend fun recordContribution(contribution: Contribution): Contribution
}

interface ReminderRepository {
    suspend fun remindersForUser(userId: String): List<Reminder>
    suspend fun saveReminder(reminder: Reminder): Reminder
}

interface NotificationPreferenceRepository {
    suspend fun preferencesForUser(userId: String): NotificationPreference
    suspend fun savePreferences(userId: String, preferences: NotificationPreference)
}

interface VisitSignalRepository {
    suspend fun submitVisitSignal(signal: VisitSignal): VisitSignal
}

