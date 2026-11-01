package org.generosity.app

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import org.generosity.data.BackendStore
import org.generosity.data.BackendFailure
import org.generosity.data.PendingOperation
import org.generosity.data.BundledKindnessCatalog
import org.generosity.data.ImportedCatalogRepository
import org.generosity.data.InMemoryContributionRepository
import org.generosity.data.InMemoryImportedCatalogDataSource
import org.generosity.data.InMemoryUserProfileRepository
import org.generosity.domain.currentUtcInstant
import org.generosity.domain.CenterCategory
import org.generosity.domain.CenterNeed
import org.generosity.domain.Contribution
import org.generosity.domain.ContributionOffer
import org.generosity.domain.ContributionRepository
import org.generosity.domain.ContributionResult
import org.generosity.domain.InMemoryNeedCatalog
import org.generosity.domain.KindnessCenter
import org.generosity.domain.KindnessCenterRepository
import org.generosity.domain.LocalePreference
import org.generosity.domain.NeedCatalog
import org.generosity.domain.NotificationPreference
import org.generosity.domain.PersonalImpact
import org.generosity.domain.RejectionReason
import org.generosity.domain.SupportedLocale
import org.generosity.domain.UserMode
import org.generosity.domain.UserProfile
import org.generosity.domain.UserProfileRepository
import org.generosity.domain.utcDate
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
    val localePreference: LocalePreference = LocalePreference(),
    val profile: UserProfile = UserProfile(mode = UserMode.ANONYMOUS),
    val searchText: String = "",
    val selectedCategories: Set<CenterCategory> = emptySet(),
    val centers: List<KindnessCenter> = emptyList(),
    val selectedCenterId: String? = null,
    val contributions: List<Contribution> = emptyList(),
    val impact: PersonalImpact = PersonalImpact(),
    val preferences: NotificationPreference = NotificationPreference(),
    val referenceIsoDate: String = "",
    val lastRejection: RejectionReason? = null,
    val visitSignalCenterId: String? = null,
    val visitedCenterIds: Set<String> = emptySet(),
    val remoteAccount: Boolean = false,
    val backendConfigured: Boolean = false,
    val syncing: Boolean = false,
    val writing: Boolean = false,
    val savedOffer: Int = 0,
    val syncError: String? = null,
    val catalogUpdatedAt: String? = null,
    val pendingOperations: List<PendingOperation> = emptyList()
) {
    val locale: SupportedLocale
        get() = localePreference.activeLocale

    val selectedCenter: KindnessCenter?
        get() = centers.firstOrNull { it.id == selectedCenterId }

    val currentNeeds: List<CenterNeed>
        get() = selectedCenter?.let { CurrentNeedsUseCase()(it.needs, referenceIsoDate) }.orEmpty()

    val canRecordContributions: Boolean
        get() = (!remoteAccount || backendConfigured) && ContributionPolicy().canRecordContribution(profile)
}

class GenerosityAppState(
    private val scope: CoroutineScope,
    private val backend: BackendStore? = null,
    catalog: List<KindnessCenter> = BundledKindnessCatalog.centers(),
    private val centersRepository: KindnessCenterRepository =
        backend ?: ImportedCatalogRepository(InMemoryImportedCatalogDataSource(catalog)),
    private val needs: NeedCatalog = backend ?: InMemoryNeedCatalog(catalog.flatMap { it.needs }),
    private val profiles: UserProfileRepository = InMemoryUserProfileRepository(),
    private val contributions: ContributionRepository = InMemoryContributionRepository(),
    private val clock: () -> String = ::currentUtcInstant,
    ids: (() -> String)? = null,
    initialLocalePreference: LocalePreference = LocalePreference()
) {
    private var contributionCounter = 0
    private val ids: () -> String = ids ?: { "contribution-${++contributionCounter}" }
    private val discovery = DiscoverKindnessCentersUseCase(centersRepository)
    private val recordContribution = RecordContributionUseCase(contributions, needs)
    private val impact = PersonalImpactUseCase()
    private val syncMutex = Mutex()
    private var refreshRequested = false

    private val _state = MutableStateFlow(
        GenerosityUiState(
            localePreference = initialLocalePreference,
            backendConfigured = backend != null,
            referenceIsoDate = runCatching { utcDate(clock()) }.getOrDefault("")
        )
    )
    val state: StateFlow<GenerosityUiState> = _state.asStateFlow()

    fun start() {
        scope.launch { refresh() }
    }

    /** Recomputes expiry from the clock. A resumed app must not keep a stale current list. */
    fun onResume() {
        scope.launch { refresh() }
    }

    fun setLocale(locale: SupportedLocale?) {
        updateLocalePreference(_state.value.localePreference.copy(selectedLocale = locale))
    }

    fun setDeviceLanguages(codes: List<String>) {
        updateLocalePreference(LocalePreference.fromDeviceLanguages(
            selectedCode = _state.value.localePreference.selectedLocale?.code,
            deviceCodes = codes
        ))
    }

    private fun updateLocalePreference(preference: LocalePreference) {
        val previous = _state.value.locale
        update { copy(localePreference = preference) }
        if (previous != preference.activeLocale) scope.launch { refreshCenters() }
    }

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

    fun setPreferences(preferences: NotificationPreference) {
        try {
            backend?.savePreferences(_state.value.profile.id, preferences)
            update { copy(preferences = preferences) }
        } catch (_: Exception) { update { copy(syncError = "save_failed") } }
    }

    fun signIn(userId: String, displayName: String, remoteAccount: Boolean = false) {
        scope.launch {
            val profile = UserProfile(id = userId, mode = UserMode.REGISTERED, displayName = displayName)
            profiles.saveProfile(profile)
            update { copy(profile = profile, remoteAccount = remoteAccount,
                contributions = if (this.profile.id == userId) contributions else emptyList(),
                impact = if (this.profile.id == userId) impact else PersonalImpact(), pendingOperations = emptyList(), visitedCenterIds = emptySet(), visitSignalCenterId = null, preferences = NotificationPreference()) }
            refresh()
        }
    }

    fun continueAnonymously() {
        scope.launch {
            val profile = UserProfile(mode = UserMode.ANONYMOUS)
            profiles.saveProfile(profile)
            update { copy(profile = profile, remoteAccount = false, contributions = emptyList(), impact = PersonalImpact(), pendingOperations = emptyList(), visitedCenterIds = emptySet(), visitSignalCenterId = null, preferences = NotificationPreference()) }
            refresh()
        }
    }

    fun offerHelp(needId: String, offer: ContributionOffer) {
        scope.launch {
            val current = _state.value
            if (current.remoteAccount) {
                if (backend != null) remoteWrite(offer = true) { backend.offer(requireNotNull(current.profile.id), needId, offer) }
                return@launch
            }
            if (backend != null) { update { copy(lastRejection = RejectionReason.REGISTRATION_REQUIRED) }; return@launch }
            val need = needs.needById(needId)
            if (need == null) {
                update { copy(lastRejection = RejectionReason.NEED_NOT_FOUND) }
                return@launch
            }
            when (val result = recordContribution(current.profile, ids(), clock(), need, offer)) {
                is ContributionResult.Recorded -> { update { copy(savedOffer = savedOffer + 1) }; refresh() }
                is ContributionResult.Rejected -> update { copy(lastRejection = result.reason) }
            }
        }
    }

    /** A helping action that belongs to the user, not to a published need. */
    fun recordPersonalHelp(offer: ContributionOffer) {
        scope.launch {
            val current = _state.value
            if (current.remoteAccount) {
                if (backend != null) remoteWrite(offer = true) { backend.offer(requireNotNull(current.profile.id), null, offer) }
                return@launch
            }
            if (backend != null) { update { copy(lastRejection = RejectionReason.REGISTRATION_REQUIRED) }; return@launch }
            when (val result = recordContribution(current.profile, ids(), clock(), need = null, offer = offer)) {
                is ContributionResult.Recorded -> { update { copy(savedOffer = savedOffer + 1) }; refreshContributions() }
                is ContributionResult.Rejected -> update { copy(lastRejection = result.reason) }
            }
        }
    }

    fun signalVisit(centerId: String) {
        if (backend == null) { update { copy(visitSignalCenterId = centerId) }; return }
        val owner = _state.value.profile.id
        scope.launch { remoteWrite { backend.recordVisit(owner, centerId, clock()) } }
    }

    fun dismissRejection() = update { copy(lastRejection = null) }

    fun transitionContribution(id: String, action: String, version: Int) {
        val userId = _state.value.profile.id ?: return
        val server = backend ?: return
        scope.launch { remoteWrite { server.transition(userId, id, action, version) } }
    }

    fun discardRejectedOperation(id: String) {
        val userId = _state.value.profile.id ?: return
        val server = backend ?: return
        scope.launch { remoteWrite { server.discard(userId, id) } }
    }

    private suspend fun remoteWrite(offer: Boolean = false, write: suspend () -> Unit) {
        if (_state.value.writing) return
        update { copy(writing = true, syncError = null) }
        try {
            write()
            if (offer) update { copy(savedOffer = savedOffer + 1) }
            refreshContributions()
            refresh()
        } catch (error: CancellationException) { throw error }
        catch (error: Exception) { update { copy(syncError = (error as? BackendFailure)?.code ?: "save_failed") } }
        finally { update { copy(writing = false) } }
    }

    private suspend fun refresh() {
        if (!syncMutex.tryLock()) { refreshRequested = true; return }
        try {
            refreshCenters()
            try { refreshContributions() }
            catch (error: CancellationException) { throw error }
            catch (_: Exception) { update { copy(syncError = "cache_unavailable") } }
            if (backend != null) {
                update { copy(syncing = true, syncError = null) }
                // Public discovery must still refresh when the private session expires.
                try { backend.refreshCatalog(); refreshCenters(); update { copy(catalogUpdatedAt = backend.updatedAt) } }
                catch (error: CancellationException) { throw error }
                catch (error: Exception) { update { copy(syncError = (error as? BackendFailure)?.code ?: "sync_failed") } }
                val userId = _state.value.profile.id
                if (userId != null && _state.value.remoteAccount) {
                    backend.flush(userId) { _state.value.profile.id == userId && _state.value.remoteAccount }
                    if (_state.value.profile.id == userId) backend.refreshAccount(userId)
                }
                refreshContributions()
            }
        } catch (error: CancellationException) { throw error }
        catch (error: Exception) { update { copy(syncError = (error as? BackendFailure)?.code ?: "sync_failed") } }
        finally {
            update { copy(syncing = false) }
            syncMutex.unlock()
            if (refreshRequested) { refreshRequested = false; scope.launch { refresh() } }
        }
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
        // A slower lookup in the previous language must not replace current results.
        update {
            if (locale == current.locale && searchText == current.searchText &&
                selectedCategories == current.selectedCategories
            ) copy(centers = withLiveNeeds, referenceIsoDate = clock().take(10)) else this
        }
    }

    private suspend fun refreshContributions() {
        val profile = _state.value.profile
        val history = ContributionPolicy().visibleHistory(
            profile,
            profile.id?.let { if (backend != null) backend.history(it) else contributions.contributionsForUser(it) }.orEmpty()
        )
        val pending = profile.id?.let { backend?.pending(it) }.orEmpty()
        val visits = backend?.visitedCenters(profile.id).orEmpty()
        val preferences = backend?.preferences(profile.id) ?: _state.value.preferences
        update { if (this.profile.id == profile.id) copy(contributions = history, impact = impact(history), pendingOperations = pending, visitedCenterIds = visits, preferences = preferences) else this }
    }

    private fun update(transform: GenerosityUiState.() -> GenerosityUiState) {
        _state.update { it.transform() }
    }
}
