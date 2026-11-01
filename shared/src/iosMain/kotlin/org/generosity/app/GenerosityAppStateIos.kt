package org.generosity.app

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.generosity.domain.CenterCategory
import org.generosity.domain.ContributionOffer
import org.generosity.domain.HelpMethod
import org.generosity.domain.NotificationPreference
import org.generosity.domain.LocalePreference
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
    constructor(selectedLocaleCode: String?, deviceLanguageCodes: List<String>) : this(GenerosityAppState(
        CoroutineScope(SupervisorJob() + Dispatchers.Main),
        initialLocalePreference = LocalePreference.fromDeviceLanguages(selectedLocaleCode, deviceLanguageCodes)
    ))

    constructor() : this(null, emptyList())

    private val observationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var observation: Job? = null
    val currentState: GenerosityUiState get() = state.state.value

    fun onResume() = state.onResume()

    fun start(onChange: (GenerosityUiState) -> Unit) {
        stop()
        state.start()
        observation = observationScope.launch {
            state.state.collect { onChange(it) }
        }
    }

    fun stop() {
        observation?.cancel()
        observation = null
    }

    fun setLocale(code: String?) = state.setLocale(SupportedLocale.fromCode(code))

    fun setDeviceLanguages(codes: List<String>) = state.setDeviceLanguages(codes)

    fun setSearchText(text: String) = state.setSearchText(text)

    fun toggleCategory(code: String) {
        CenterCategory.entries.firstOrNull { it.code == code }?.let { state.toggleCategory(it) }
    }

    fun selectCenter(centerId: String?) = state.selectCenter(centerId)

    fun signIn(userId: String, displayName: String) = state.signIn(userId, displayName, remoteAccount = true)

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
