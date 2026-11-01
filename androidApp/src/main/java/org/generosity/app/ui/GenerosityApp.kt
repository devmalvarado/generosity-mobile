@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package org.generosity.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import android.text.TextUtils
import android.view.View
import java.util.Locale
import kotlinx.coroutines.launch
import org.generosity.app.GenerosityAppState
import org.generosity.app.GenerosityUiState
import org.generosity.app.R
import org.generosity.app.ui.map.CenterMapRenderer
import org.generosity.app.ui.map.toMapMarkers
import org.generosity.domain.*

private enum class AppTab(val icon: Int) {
    MAP(R.drawable.ic_map), LIST(R.drawable.ic_list), PROFILE(R.drawable.ic_person), SETTINGS(R.drawable.ic_settings)
}

@Composable
fun GenerosityApp(
    appState: GenerosityAppState,
    mapRenderer: CenterMapRenderer,
    onLanguageSelected: (SupportedLocale?) -> Unit = appState::setLocale,
    savingLanguage: Boolean = false,
    languageSaveFailed: Boolean = false,
    onSignIn: () -> Unit = {}, onSignOut: () -> Unit = {}, onCreate: () -> Unit = {},
    authBusy: Boolean = false, authFailed: Boolean = false
) {
    val state by appState.state.collectAsState()
    val locale = state.locale
    var tab by rememberSaveable { mutableStateOf(AppTab.MAP) }
    val mapState = rememberSaveableStateHolder()
    val direction = if (TextUtils.getLayoutDirectionFromLocale(Locale.forLanguageTag(locale.code)) == View.LAYOUT_DIRECTION_RTL)
        LayoutDirection.Rtl else LayoutDirection.Ltr
    CompositionLocalProvider(LocalLayoutDirection provides direction) {
        GenerosityTheme {
            Scaffold(
                topBar = {
                    Surface {
                        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = GenSpace.lg, vertical = GenSpace.sm)) {
                            BrandWordmark()
                        }
                    }
                },
                bottomBar = {
                    if (!WindowInsets.isImeVisible) {
                        if (LocalDensity.current.fontScale > 1.4f) {
                            Surface {
                                Column(Modifier.navigationBarsPadding().padding(GenSpace.sm), verticalArrangement = Arrangement.spacedBy(GenSpace.xs)) {
                                    AppTab.entries.chunked(2).forEach { pair ->
                                        Row(horizontalArrangement = Arrangement.spacedBy(GenSpace.sm)) {
                                            pair.forEach { appTab ->
                                                Row(
                                                    Modifier.weight(1f).heightIn(min = GenSize.androidTouch)
                                                        .background(if (tab == appTab) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface, RoundedCornerShape(GenRadius.card))
                                                        .selectable(tab == appTab, role = Role.Tab) { tab = appTab }
                                                        .testTag("tab-" + appTab.name.lowercase()).padding(GenSpace.sm),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(GenSpace.sm)
                                                ) {
                                                    AppIcon(appTab.icon, tint = MaterialTheme.colorScheme.primary)
                                                    Text(appTab.label(locale), Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                                AppTab.entries.forEach { appTab ->
                                    NavigationBarItem(
                                        modifier = Modifier.testTag("tab-" + appTab.name.lowercase()),
                                        selected = tab == appTab,
                                        onClick = { tab = appTab },
                                        colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.primaryContainer),
                                        icon = { AppIcon(appTab.icon, tint = if (tab == appTab) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) },
                                        label = { Text(appTab.label(locale), style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }
                        }
                    }
                }
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding).imePadding()) {
                    when (tab) {
                        AppTab.MAP -> DiscoveryScreen(state, appState, true, mapRenderer, mapState)
                        AppTab.LIST -> DiscoveryScreen(state, appState, false, mapRenderer, mapState)
                        AppTab.PROFILE -> ProfileScreen(state, appState, onSignIn, onSignOut, onCreate, authBusy, authFailed)
                        AppTab.SETTINGS -> SettingsScreen(state, appState, onLanguageSelected, savingLanguage, languageSaveFailed)
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscoveryScreen(
    state: GenerosityUiState, appState: GenerosityAppState, showMap: Boolean,
    mapRenderer: CenterMapRenderer, mapState: SaveableStateHolder
) {
    val locale = state.locale
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var offer by remember { mutableStateOf<Pair<CenterNeed, HelpMethod>?>(null) }
    offer?.let { (need, method) -> OfferDialog(state, appState, need, method) { offer = null } }
    val keyboard = LocalSoftwareKeyboardController.current
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val mapHeight = (maxHeight * .52f).coerceIn(200.dp, 480.dp)
        LazyColumn(
            state = listState,
            modifier = Modifier.testTag("discovery-scroll"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(GenSpace.lg)
        ) {
            item { SyncStatus(state, appState) }
            item {
                Text(TextKey.NEARBY_CENTERS.label(locale), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.searchText, onValueChange = appState::setSearchText,
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
                    shape = RoundedCornerShape(GenRadius.card),
                    leadingIcon = { AppIcon(R.drawable.ic_search) },
                    label = { Text(TextKey.SEARCH_PLACEHOLDER.label(locale)) }
                )
            }
            item { CategoryFilters(locale, state.selectedCategories, appState::toggleCategory) }
            if (showMap) {
                item(key = "discovery-map") {
                    mapState.SaveableStateProvider("discovery-map") {
                        mapRenderer.Render(
                            markers = state.centers.toMapMarkers(locale), selectedCenterId = state.selectedCenter?.id,
                            locale = locale, onCenterSelected = appState::selectCenter,
                            modifier = Modifier.fillMaxWidth().height(mapHeight)
                        )
                    }
                }
                state.selectedCenter?.let { center ->
                    item(key = "selected-preview") {
                        Surface(shape = RoundedCornerShape(GenRadius.card), shadowElevation = 2.dp) {
                            Column(Modifier.padding(GenSpace.md)) {
                                Text(localizedString(R.string.selected_center, locale), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                CenterIdentity(locale, center)
                                TextButton(onClick = {
                                    scope.launch { listState.animateScrollToItem(4 + state.centers.size) }
                                }) { Text(localizedString(R.string.view_details, locale)) }
                            }
                        }
                    }
                }
            }
            if (state.centers.isEmpty()) {
                item { EmptyMessage(TextKey.EMPTY_CENTERS.label(locale)) }
            } else {
                items(state.centers, key = { it.id }) { center ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable(role = Role.Button) { appState.selectCenter(center.id) }
                            .testTag("center-" + center.id),
                        shape = RoundedCornerShape(GenRadius.card)
                    ) { Box(Modifier.padding(GenSpace.md)) { CenterIdentity(locale, center) } }
                }
            }
            state.selectedCenter?.let { center ->
                item(key = "center-detail") {
                    CenterDetail(
                        locale, center, state.currentNeeds,
                        state.visitSignalCenterId == center.id || center.id in state.visitedCenterIds, state.lastRejection, !state.canRecordContributions, state.backendConfigured,
                        { appState.signalVisit(center.id) },
                        { id -> state.currentNeeds.firstOrNull { it.id == id }?.let { offer = it to HelpMethod.GOODS } },
                        { id -> state.currentNeeds.firstOrNull { it.id == id }?.let { offer = it to HelpMethod.TIME } }
                    )
                }
            }
            item { Text(localizedString(if (state.backendConfigured) R.string.sync_cached else R.string.demo_catalog, locale), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
private fun CategoryFilters(locale: SupportedLocale, selectedCategories: Set<CenterCategory>, onToggle: (CenterCategory) -> Unit) {
    val chips: @Composable () -> Unit = {
        CenterCategory.entries.forEach { category ->
            FilterChip(
                selected = category in selectedCategories, onClick = { onToggle(category) },
                modifier = Modifier.heightIn(min = GenSize.androidTouch),
                label = { Text(category.label(locale)) },
                leadingIcon = if (category in selectedCategories) ({ Text("✓") }) else null
            )
        }
    }
    if (LocalDensity.current.fontScale > 1.4f) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(GenSpace.sm), verticalArrangement = Arrangement.spacedBy(GenSpace.sm)) { chips() }
    } else {
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(GenSpace.sm)) { chips() }
    }
}

@Composable
private fun CenterIdentity(locale: SupportedLocale, center: KindnessCenter) {
    Row(horizontalArrangement = Arrangement.spacedBy(GenSpace.md), verticalAlignment = Alignment.CenterVertically) {
        CategoryBadge(center.categories.firstOrNull())
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GenSpace.xs)) {
            Text(center.name.resolve(locale), fontWeight = FontWeight.SemiBold)
            Text(center.address.resolve(locale), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(center.verificationStatus.label(locale), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CenterDetail(
    locale: SupportedLocale, center: KindnessCenter, needs: List<CenterNeed>,
    visitRecorded: Boolean, rejection: RejectionReason?, remoteAccount: Boolean, backendConfigured: Boolean, onVisitSignal: () -> Unit,
    onOfferGoods: (String) -> Unit, onOfferTime: (String) -> Unit
) {
    Column(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(GenSpace.lg).testTag("detail-" + center.id),
        verticalArrangement = Arrangement.spacedBy(GenSpace.lg)
    ) {
        CategoryBadge(center.categories.firstOrNull(), 52.dp)
        Text(center.name.resolve(locale), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(center.categories.map { it.label(locale) }.joinToString(" · "), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(center.address.resolve(locale), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(center.description.resolve(locale), style = MaterialTheme.typography.bodyMedium)
        ContactActions(center.contactInfo, locale)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Text(TextKey.CURRENT_NEEDS.label(locale), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (needs.isEmpty()) EmptyMessage(TextKey.NO_CURRENT_NEEDS.label(locale))
        needs.forEach { need ->
            NeedRow(locale, need, remoteAccount, onOfferGoods, onOfferTime)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
        if (remoteAccount || rejection == RejectionReason.REGISTRATION_REQUIRED) {
            Text(TextKey.REGISTRATION_REQUIRED.label(locale), color = MaterialTheme.colorScheme.error)
        }
        OutlinedButton(onClick = onVisitSignal, enabled = !visitRecorded, modifier = Modifier.fillMaxWidth()) {
            Text(TextKey.VISIT_SIGNAL.label(locale))
        }
        if (visitRecorded) Text(if (backendConfigured) localizedString(R.string.visit_saved, locale) else TextKey.VISIT_SIGNAL_SUBMITTED.label(locale), style = MaterialTheme.typography.bodySmall)
        Text(center.verificationStatus.label(locale), style = MaterialTheme.typography.labelLarge)
        Text(TextKey.OFFICIAL_VERIFICATION_BOUNDARY.label(locale), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun NeedRow(locale: SupportedLocale, need: CenterNeed, remoteAccount: Boolean, onOfferGoods: (String) -> Unit, onOfferTime: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(GenSpace.md)) {
        Box(Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(GenRadius.control)), contentAlignment = Alignment.Center) {
            AppIcon(if (HelpMethod.GOODS in need.helpMethods) R.drawable.ic_gift else R.drawable.ic_clock, tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GenSpace.sm)) {
            val priority = when (need.priority) {
                NeedPriority.URGENT -> R.string.urgent
                NeedPriority.HIGH -> R.string.priority_high
                NeedPriority.MEDIUM -> R.string.priority_medium
                NeedPriority.NORMAL -> R.string.priority_normal
            }
            Text(localizedString(priority, locale), color = if (need.isUrgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text(need.title.resolve(locale), fontWeight = FontWeight.SemiBold)
            Text(need.description.resolve(locale), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            need.quantityRemaining?.let { Text(localizedQuantity(R.plurals.remaining_units, it, locale), style = MaterialTheme.typography.labelLarge) }
            Text(need.helpMethods.map { it.label(locale) }.joinToString(" · "), style = MaterialTheme.typography.bodySmall)
            if (HelpMethod.MONEY in need.helpMethods) Text(TextKey.MONEY_BOUNDARY.label(locale), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(GenSpace.sm)) {
                if (HelpMethod.GOODS in need.helpMethods) {
                    Button(onClick = { onOfferGoods(need.id) }, enabled = !remoteAccount) { Text(localizedString(R.string.offer_goods_action, locale)) }
                }
                if (HelpMethod.TIME in need.helpMethods) {
                    Button(onClick = { onOfferTime(need.id) }, enabled = !remoteAccount) { Text(TextKey.OFFER_TIME.label(locale)) }
                }
            }
        }
    }
}

@Composable
private fun ProfileScreen(state: GenerosityUiState, appState: GenerosityAppState, onSignIn: () -> Unit, onSignOut: () -> Unit, onCreate: () -> Unit, authBusy: Boolean, authFailed: Boolean) {
    val locale = state.locale
    var personalMethod by remember { mutableStateOf<HelpMethod?>(null) }
    personalMethod?.let { OfferDialog(state, appState, null, it) { personalMethod = null } }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(GenSpace.lg), verticalArrangement = Arrangement.spacedBy(GenSpace.xl)) {
        Text(TextKey.TAB_PROFILE.label(locale), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Surface(shape = RoundedCornerShape(GenRadius.card)) {
            Row(Modifier.fillMaxWidth().padding(GenSpace.lg), horizontalArrangement = Arrangement.spacedBy(GenSpace.md), verticalAlignment = Alignment.CenterVertically) {
                CategoryBadge(null, 52.dp)
                Column(Modifier.weight(1f)) {
                    Text(if (state.profile.isRegistered) state.profile.displayName ?: TextKey.REGISTERED_PROFILE.label(locale) else TextKey.ANONYMOUS_PROFILE.label(locale),
                        fontWeight = FontWeight.SemiBold)
                    TextButton(onClick = if (state.profile.isRegistered) onSignOut else onSignIn, enabled = !authBusy) {
                        Text(localizedString(if (state.profile.isRegistered) R.string.auth_sign_out else R.string.auth_sign_in, locale))
                    }
                    if (!state.profile.isRegistered) {
                        OutlinedButton(onClick = onCreate, enabled = !authBusy, modifier = Modifier.heightIn(min = GenSize.androidTouch)) {
                            Text(localizedString(R.string.welcome_create, locale))
                        }
                    }
                    if (authBusy) CircularProgressIndicator(Modifier.size(24.dp))
                    if (authFailed) Text(localizedString(R.string.auth_failed, locale), color = MaterialTheme.colorScheme.error)
                    Text(localizedString(R.string.auth_scope, locale), style = MaterialTheme.typography.bodySmall)

                }
            }
        }
        Surface(color = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary, shape = RoundedCornerShape(GenRadius.card)) {
            Column(Modifier.fillMaxWidth().padding(GenSpace.xl), verticalArrangement = Arrangement.spacedBy(GenSpace.sm)) {
                Text(localizedString(R.string.points_title, locale), style = MaterialTheme.typography.labelLarge)
                Text(localizedString(R.string.unavailable, locale), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(TextKey.GENEROSITY_POINTS.label(locale), style = MaterialTheme.typography.bodySmall)
            }
        }
        SyncStatus(state, appState)
        if (state.canRecordContributions) {
            Text(localizedString(R.string.personal_add, locale), style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(GenSpace.sm)) {
                Button(onClick = { personalMethod = HelpMethod.GOODS }) { Text(localizedString(R.string.offer_goods_action, locale)) }
                Button(onClick = { personalMethod = HelpMethod.TIME }) { Text(TextKey.OFFER_TIME.label(locale)) }
            }
        }
        Text(TextKey.IMPACT_TITLE.label(locale), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(GenSpace.md), verticalArrangement = Arrangement.spacedBy(GenSpace.md)) {
            Metric(localizedString(R.string.centers_supported, locale), if (state.profile.isRegistered) localizedNumber(state.impact.centersSupported, locale) else "—")
            Metric(localizedString(R.string.needs_supported, locale), if (state.profile.isRegistered) localizedNumber(state.impact.needsSupported, locale) else "—")
        }
        Text(localizedString(R.string.impact_basis, locale), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(localizedString(R.string.history, locale), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (state.contributions.isEmpty()) EmptyMessage(TextKey.CONTRIBUTION_HISTORY_EMPTY.label(locale))
        state.contributions.forEach { contribution ->
            Column(verticalArrangement = Arrangement.spacedBy(GenSpace.xs)) {
                Text(contribution.title.resolve(locale), fontWeight = FontWeight.SemiBold)
                val status = when (contribution.status) {
                    ContributionStatus.OFFERED -> R.string.contribution_offered
                    ContributionStatus.CONFIRMED -> R.string.contribution_confirmed
                    ContributionStatus.FULFILLED -> R.string.contribution_fulfilled
                    ContributionStatus.CANCELLED -> R.string.contribution_cancelled
                }
                Text(localizedString(status, locale),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (contribution.centerId == null) {
                    Text(localizedString(R.string.personal_help, locale), style = MaterialTheme.typography.bodySmall)
                    Text(localizedString(R.string.personal_notice, locale), style = MaterialTheme.typography.bodySmall)
                }
                if (contribution.status == ContributionStatus.OFFERED || contribution.status == ContributionStatus.CONFIRMED) {
                    TextButton(onClick = { appState.transitionContribution(contribution.id, "cancel", contribution.version) }, enabled = !state.writing && !state.syncing && state.pendingOperations.isEmpty()) { Text(localizedString(R.string.contribution_cancel, locale)) }
                    if (contribution.centerId == null) TextButton(onClick = { appState.transitionContribution(contribution.id, "complete", contribution.version) }, enabled = !state.writing && !state.syncing && state.pendingOperations.isEmpty()) { Text(localizedString(R.string.contribution_complete, locale)) }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
private fun Metric(label: String, value: String) {
    Surface(shape = RoundedCornerShape(GenRadius.card)) {
        Column(Modifier.widthIn(min = 140.dp, max = 220.dp).padding(GenSpace.lg), verticalArrangement = Arrangement.spacedBy(GenSpace.sm)) {
            AppIcon(R.drawable.ic_heart, tint = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun EmptyMessage(text: String) {
    Text(text, Modifier.fillMaxWidth().padding(vertical = GenSpace.md), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
}

@Composable
private fun SettingsScreen(state: GenerosityUiState, appState: GenerosityAppState, onLanguageSelected: (SupportedLocale?) -> Unit, savingLanguage: Boolean, languageSaveFailed: Boolean) {
    val locale = state.locale
    val preferences = state.preferences
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(GenSpace.lg), verticalArrangement = Arrangement.spacedBy(GenSpace.lg)) {
        Text(TextKey.TAB_SETTINGS.label(locale), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(localizedString(R.string.language, locale), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Surface(shape = RoundedCornerShape(GenRadius.card)) {
            Column(Modifier.selectableGroup().padding(horizontal = GenSpace.md)) {
                (listOf<SupportedLocale?>(null) + SupportedLocale.entries).forEach { choice ->
                    val selected = state.localePreference.selectedLocale == choice
                    val label = choice?.nativeName ?: localizedString(R.string.language_device, locale)
                    Row(Modifier.fillMaxWidth().testTag("language-" + (choice?.code ?: "system"))
                        .selectable(selected, enabled = !savingLanguage, role = Role.RadioButton) { onLanguageSelected(choice) }
                        .padding(vertical = GenSpace.sm), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GenSpace.md)) {
                        RadioButton(selected, onClick = null, enabled = !savingLanguage)
                        Text(label, Modifier.weight(1f))
                    }
                }
            }
        }
        if (languageSaveFailed) Text(localizedString(R.string.language_save_failed, locale), color = MaterialTheme.colorScheme.error)
        Text(localizedString(R.string.notifications_title, locale), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Surface(shape = RoundedCornerShape(GenRadius.card)) {
            Column(Modifier.padding(GenSpace.md), verticalArrangement = Arrangement.spacedBy(GenSpace.md)) {
                NotificationToggle(TextKey.NEARBY_NOTIFICATIONS.label(locale), preferences.nearbyCentersEnabled) { appState.setPreferences(preferences.copy(nearbyCentersEnabled = it)) }
                NotificationToggle(TextKey.URGENT_NOTIFICATIONS.label(locale), preferences.urgentNeedsEnabled) { appState.setPreferences(preferences.copy(urgentNeedsEnabled = it)) }
                NotificationToggle(TextKey.REMINDER_NOTIFICATIONS.label(locale), preferences.remindersEnabled) { appState.setPreferences(preferences.copy(remindersEnabled = it)) }
                Text(org.generosity.usecase.NearbyAlertPolicy().settingsNotice().resolve(locale), style = MaterialTheme.typography.bodySmall)
            }
        }
        Text(localizedString(R.string.notifications_pending, locale), style = MaterialTheme.typography.bodySmall)
        if (state.syncError != null) Text(localizedString(R.string.sync_failed, locale), color = MaterialTheme.colorScheme.error)
        PlatformPermissionActions(locale)
    }
}

@Composable
private fun NotificationToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GenSpace.md), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun AppTab.label(locale: SupportedLocale): String = when (this) {
    AppTab.MAP -> TextKey.TAB_MAP.label(locale)
    AppTab.LIST -> TextKey.TAB_LIST.label(locale)
    AppTab.PROFILE -> TextKey.TAB_PROFILE.label(locale)
    AppTab.SETTINGS -> TextKey.TAB_SETTINGS.label(locale)
}
