package org.generosity.app

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.generosity.data.ImportedCatalogRepository
import org.generosity.data.InMemoryContributionRepository
import org.generosity.data.InMemoryImportedCatalogDataSource
import org.generosity.data.InMemoryUserProfileRepository
import org.generosity.data.SampleImportedCatalog
import org.generosity.domain.CenterCategory
import org.generosity.domain.CenterNeed
import org.generosity.domain.Contribution
import org.generosity.domain.ContributionOffer
import org.generosity.domain.ContributionRepository
import org.generosity.domain.ContributionResult
import org.generosity.domain.InMemoryNeedCatalog
import org.generosity.domain.KindnessCenter
import org.generosity.domain.KindnessCenterRepository
import org.generosity.domain.NeedCatalog
import org.generosity.domain.NotificationPreference
import org.generosity.domain.PersonalImpact
import org.generosity.domain.RejectionReason
import org.generosity.domain.SupportedLocale
import org.generosity.domain.UserMode
import org.generosity.domain.UserProfile
import org.generosity.domain.UserProfileRepository
import org.generosity.usecase.ContributionPolicy
import org.generosity.usecase.CurrentNeedsUseCase
import org.generosity.usecase.DiscoverKindnessCentersUseCase
import org.generosity.usecase.DiscoveryQuery
import org.generosity.usecase.PersonalImpactUseCase
import org.generosity.usecase.RecordContributionUseCase

/**
 * The one object both native UIs talk to. It owns no platform types, so
 * SwiftUI and Compose stay free to render it however each platform expects.
 */
data class GenerosityUiState(
    val locale: SupportedLocale = SupportedLocale.ENGLISH,
    val profile: UserProfile = UserProfile(mode = UserMode.ANONYMOUS),
    val searchText: String = "",
    val selectedCategories: Set<CenterCategory> = emptySet(),
    val centers: List<KindnessCenter> = emptyList(),
    val selectedCenterId: String? = null,
    val contributions: List<Contribution> = emptyList(),
    val impact: PersonalImpact = PersonalImpact(),
    val preferences: NotificationPreference = NotificationPreference(),
    val referenceIsoDate: String = "2026-05-13",
    val lastRejection: RejectionReason? = null,
    val visitSignalCenterId: String? = null
) {
    val selectedCenter: KindnessCenter?
        get() = centers.firstOrNull { it.id == selectedCenterId }

    val currentNeeds: List<CenterNeed>
        get() = selectedCenter?.let { CurrentNeedsUseCase()(it.needs, referenceIsoDate) }.orEmpty()

    val canRecordContributions: Boolean
        get() = ContributionPolicy().canRecordContribution(profile)
}

class GenerosityAppState(
    private val scope: CoroutineScope,
    catalog: List<KindnessCenter> = SampleImportedCatalog.centers(),
    private val centersRepository: KindnessCenterRepository =
        ImportedCatalogRepository(InMemoryImportedCatalogDataSource(catalog)),
    private val needs: NeedCatalog = InMemoryNeedCatalog(catalog.flatMap { it.needs }),
    private val profiles: UserProfileRepository = InMemoryUserProfileRepository(),
    private val contributions: ContributionRepository = InMemoryContributionRepository(),
    private val clock: () -> String = { "2026-05-13T12:00:00Z" },
    ids: (() -> String)? = null
) {
    private var contributionCounter = 0
    private val ids: () -> String = ids ?: { "contribution-${++contributionCounter}" }
    private val discovery = DiscoverKindnessCentersUseCase(centersRepository)
    private val recordContribution = RecordContributionUseCase(contributions, needs)
    private val impact = PersonalImpactUseCase()

    private val _state = MutableStateFlow(GenerosityUiState())
    val state: StateFlow<GenerosityUiState> = _state.asStateFlow()

    fun start() {
        scope.launch { refresh() }
    }

    fun setLocale(locale: SupportedLocale) = update { copy(locale = locale) }

    fun setSearchText(text: String) {
        update { copy(searchText = text) }
        scope.launch { refreshCenters() }
    }

    fun toggleCategory(category: CenterCategory) {
        update {
            copy(
                selectedCategories = if (category in selectedCategories) {
                    selectedCategories - category
                } else {
                    selectedCategories + category
                }
            )
        }
        scope.launch { refreshCenters() }
    }

    fun selectCenter(centerId: String?) = update { copy(selectedCenterId = centerId, lastRejection = null) }

    fun setPreferences(preferences: NotificationPreference) = update { copy(preferences = preferences) }

    fun signIn(userId: String, displayName: String) {
        scope.launch {
            val profile = UserProfile(id = userId, mode = UserMode.REGISTERED, displayName = displayName)
            profiles.saveProfile(profile)
            update { copy(profile = profile) }
            refreshContributions()
        }
    }

    fun continueAnonymously() {
        scope.launch {
            val profile = UserProfile(mode = UserMode.ANONYMOUS)
            profiles.saveProfile(profile)
            update { copy(profile = profile, contributions = emptyList(), impact = PersonalImpact()) }
        }
    }

    fun offerHelp(needId: String, offer: ContributionOffer) {
        scope.launch {
            val current = _state.value
            val need = needs.needById(needId)
            if (need == null) {
                update { copy(lastRejection = RejectionReason.NEED_NOT_FOUND) }
                return@launch
            }
            when (val result = recordContribution(current.profile, ids(), clock(), need, offer)) {
                is ContributionResult.Recorded -> refresh()
                is ContributionResult.Rejected -> update { copy(lastRejection = result.reason) }
            }
        }
    }

    /** A helping action that belongs to the user, not to a published need. */
    fun recordPersonalHelp(offer: ContributionOffer) {
        scope.launch {
            val current = _state.value
            when (val result = recordContribution(current.profile, ids(), clock(), need = null, offer = offer)) {
                is ContributionResult.Recorded -> refreshContributions()
                is ContributionResult.Rejected -> update { copy(lastRejection = result.reason) }
            }
        }
    }

    fun signalVisit(centerId: String) = update { copy(visitSignalCenterId = centerId) }

    fun dismissRejection() = update { copy(lastRejection = null) }

    private suspend fun refresh() {
        refreshCenters()
        refreshContributions()
    }

    private suspend fun refreshCenters() {
        val current = _state.value
        val discovered = discovery(
            DiscoveryQuery(
                searchText = current.searchText,
                categories = current.selectedCategories,
                locale = current.locale
            )
        )
        val withLiveNeeds = discovered.map { center ->
            center.copy(needs = needs.needsForCenter(center.id))
        }
        update { copy(centers = withLiveNeeds, referenceIsoDate = clock().take(10)) }
    }

    private suspend fun refreshContributions() {
        val profile = _state.value.profile
        val history = ContributionPolicy().visibleHistory(
            profile,
            profile.id?.let { contributions.contributionsForUser(it) }.orEmpty()
        )
        update { copy(contributions = history, impact = impact(history)) }
    }

    private fun update(transform: GenerosityUiState.() -> GenerosityUiState) {
        _state.update { it.transform() }
    }
}
