package org.generosity.data

import org.generosity.domain.Contribution
import org.generosity.domain.ContributionRepository
import org.generosity.domain.NotificationPreference
import org.generosity.domain.NotificationPreferenceRepository
import org.generosity.domain.Reminder
import org.generosity.domain.ReminderRepository
import org.generosity.domain.UserMode
import org.generosity.domain.UserProfile
import org.generosity.domain.UserProfileRepository
import org.generosity.domain.VisitSignal
import org.generosity.domain.VisitSignalRepository

class InMemoryUserProfileRepository(
    initialProfile: UserProfile = UserProfile(mode = UserMode.ANONYMOUS)
) : UserProfileRepository {
    private var profile: UserProfile = initialProfile

    override suspend fun currentProfile(): UserProfile = profile

    override suspend fun saveProfile(profile: UserProfile) {
        this.profile = profile
    }
}

class InMemoryContributionRepository(
    initialContributions: List<Contribution> = emptyList()
) : ContributionRepository {
    private val contributions = initialContributions.toMutableList()

    override suspend fun contributionsForUser(userId: String): List<Contribution> =
        contributions.filter { it.userId == userId }

    override suspend fun recordContribution(contribution: Contribution): Contribution {
        contributions += contribution
        return contribution
    }
}

class InMemoryReminderRepository(
    initialReminders: List<Reminder> = emptyList()
) : ReminderRepository {
    private val reminders = initialReminders.toMutableList()

    override suspend fun remindersForUser(userId: String): List<Reminder> =
        reminders.filter { it.userId == userId }

    override suspend fun saveReminder(reminder: Reminder): Reminder {
        reminders.removeAll { it.id == reminder.id }
        reminders += reminder
        return reminder
    }
}

class InMemoryNotificationPreferenceRepository : NotificationPreferenceRepository {
    private val preferencesByUser = mutableMapOf<String, NotificationPreference>()

    override suspend fun preferencesForUser(userId: String): NotificationPreference =
        preferencesByUser[userId] ?: NotificationPreference()

    override suspend fun savePreferences(userId: String, preferences: NotificationPreference) {
        preferencesByUser[userId] = preferences
    }
}

class InMemoryVisitSignalRepository : VisitSignalRepository {
    private val signals = mutableListOf<VisitSignal>()

    override suspend fun submitVisitSignal(signal: VisitSignal): VisitSignal {
        signals += signal
        return signal
    }

    fun submittedSignals(): List<VisitSignal> = signals.toList()
}

