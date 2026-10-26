package org.generosity.app

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.generosity.domain.CenterCategory
import org.generosity.domain.ContributionOffer
import org.generosity.domain.HelpMethod
import org.generosity.domain.NotificationPreference
import org.generosity.domain.SupportedLocale

/**
 * Swift cannot collect a Kotlin StateFlow, so this exposes the same state as
 * a callback plus methods that take only primitive arguments.
 */
@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "GenerosityController", swiftName = "GenerosityController", exact = true)
class GenerosityAppStateIos(
    private val state: GenerosityAppState
) {
    constructor() : this(GenerosityAppState(
        CoroutineScope(SupervisorJob() + Dispatchers.Main)
    ))

    fun start(onChange: (GenerosityUiState) -> Unit) {
        state.start()
        CoroutineScope(SupervisorJob() + Dispatchers.Main).launch {
            state.state.collect { onChange(it) }
        }
    }

    fun setLocale(code: String) {
        SupportedLocale.fromCode(code)?.let { state.setLocale(it) }
    }

    fun setSearchText(text: String) = state.setSearchText(text)

    fun toggleCategory(code: String) {
        CenterCategory.entries.firstOrNull { it.code == code }?.let { state.toggleCategory(it) }
    }

    fun selectCenter(centerId: String?) = state.selectCenter(centerId)

    fun signIn(userId: String, displayName: String) = state.signIn(userId, displayName)

    fun continueAnonymously() = state.continueAnonymously()

    fun offerGoods(needId: String, quantity: Int) =
        state.offerHelp(needId, ContributionOffer(HelpMethod.GOODS, quantity = quantity))

    fun offerTime(needId: String, activity: String, availability: String) =
        state.offerHelp(needId, ContributionOffer(HelpMethod.TIME, activity = activity, availability = availability))

    fun recordPersonalHelp(note: String) =
        state.recordPersonalHelp(ContributionOffer(HelpMethod.GOODS, note = note))

    fun signalVisit(centerId: String) = state.signalVisit(centerId)

    fun dismissRejection() = state.dismissRejection()

    fun setPreferences(nearby: Boolean, urgent: Boolean, reminders: Boolean) =
        state.setPreferences(
            NotificationPreference(
                nearbyCentersEnabled = nearby,
                urgentNeedsEnabled = urgent,
                remindersEnabled = reminders
            )
        )
}
