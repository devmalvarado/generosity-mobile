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
    languageSaveFailed: Boolean = false
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
                        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp)) {
                            BrandWordmark()
                        }
                    }
                },
                bottomBar = {
                    if (!WindowInsets.isImeVisible) {
                        if (LocalDensity.current.fontScale > 1.4f) {
                            Surface {
                                Column(Modifier.navigationBarsPadding().padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    AppTab.entries.chunked(2).forEach { pair ->
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            pair.forEach { appTab ->
                                                Row(
                                                    Modifier.weight(1f).heightIn(min = 48.dp)
                                                        .background(if (tab == appTab) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                                        .selectable(tab == appTab, role = Role.Tab) { tab = appTab }
                                                        .testTag("tab-" + appTab.name.lowercase()).padding(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                        AppTab.PROFILE -> ProfileScreen(state, appState)
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
    val keyboard = LocalSoftwareKeyboardController.current
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val mapHeight = (maxHeight * .52f).coerceIn(200.dp, 480.dp)
        LazyColumn(
            state = listState,
            modifier = Modifier.testTag("discovery-scroll"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(TextKey.NEARBY_CENTERS.label(locale), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.searchText, onValueChange = appState::setSearchText,
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
                    shape = RoundedCornerShape(12.dp),
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
                        Surface(shape = RoundedCornerShape(12.dp), shadowElevation = 2.dp) {
                            Column(Modifier.padding(12.dp)) {
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
                        shape = RoundedCornerShape(12.dp)
                    ) { Box(Modifier.padding(12.dp)) { CenterIdentity(locale, center) } }
                }
            }
            state.selectedCenter?.let { center ->
                item(key = "center-detail") {
                    CenterDetail(
                        locale, center, state.currentNeeds,
                        state.visitSignalCenterId == center.id, state.lastRejection,
                        { appState.signalVisit(center.id) },
                        { appState.offerHelp(it, ContributionOffer(HelpMethod.GOODS, quantity = 5)) },
                        { appState.offerHelp(it, ContributionOffer(HelpMethod.TIME, activity = "volunteer", availability = "weekday")) }
                    )
                }
            }
            item { Text(localizedString(R.string.demo_catalog, locale), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
private fun CategoryFilters(locale: SupportedLocale, selectedCategories: Set<CenterCategory>, onToggle: (CenterCategory) -> Unit) {
    val chips: @Composable () -> Unit = {
        CenterCategory.entries.forEach { category ->
            FilterChip(
                selected = category in selectedCategories, onClick = { onToggle(category) },
                modifier = Modifier.heightIn(min = 48.dp),
                label = { Text(category.label(locale)) },
                leadingIcon = if (category in selectedCategories) ({ Text("✓") }) else null
            )
        }
    }
    if (LocalDensity.current.fontScale > 1.4f) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { chips() }
    } else {
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) { chips() }
    }
}

@Composable
private fun CenterIdentity(locale: SupportedLocale, center: KindnessCenter) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        CategoryBadge(center.categories.firstOrNull())
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(center.name.resolve(locale), fontWeight = FontWeight.SemiBold)
            Text(center.address.resolve(locale), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(center.verificationStatus.label(locale), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CenterDetail(
    locale: SupportedLocale, center: KindnessCenter, needs: List<CenterNeed>,
    visitRecorded: Boolean, rejection: RejectionReason?, onVisitSignal: () -> Unit,
    onOfferGoods: (String) -> Unit, onOfferTime: (String) -> Unit
) {
    Column(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(16.dp).testTag("detail-" + center.id),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CategoryBadge(center.categories.firstOrNull(), 52)
        Text(center.name.resolve(locale), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(center.categories.map { it.label(locale) }.joinToString(" · "), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(center.address.resolve(locale), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(center.description.resolve(locale), style = MaterialTheme.typography.bodyMedium)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Text(TextKey.CURRENT_NEEDS.label(locale), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (needs.isEmpty()) EmptyMessage(TextKey.NO_CURRENT_NEEDS.label(locale))
        needs.forEach { need ->
            NeedRow(locale, need, onOfferGoods, onOfferTime)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
        if (rejection == RejectionReason.REGISTRATION_REQUIRED) {
            Text(TextKey.REGISTRATION_REQUIRED.label(locale), color = MaterialTheme.colorScheme.error)
        }
        OutlinedButton(onClick = onVisitSignal, enabled = !visitRecorded, modifier = Modifier.fillMaxWidth()) {
            Text(TextKey.VISIT_SIGNAL.label(locale))
        }
        if (visitRecorded) Text(TextKey.VISIT_SIGNAL_SUBMITTED.label(locale), style = MaterialTheme.typography.bodySmall)
        Text(center.verificationStatus.label(locale), style = MaterialTheme.typography.labelLarge)
        Text(TextKey.OFFICIAL_VERIFICATION_BOUNDARY.label(locale), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun NeedRow(locale: SupportedLocale, need: CenterNeed, onOfferGoods: (String) -> Unit, onOfferTime: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
            AppIcon(if (HelpMethod.GOODS in need.helpMethods) R.drawable.ic_gift else R.drawable.ic_clock, tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (HelpMethod.GOODS in need.helpMethods && need.quantityRemaining != null) {
                    Button(onClick = { onOfferGoods(need.id) }) { Text(localizedQuantity(R.plurals.offer_goods, 5, locale)) }
                }
                if (HelpMethod.TIME in need.helpMethods) {
                    Button(onClick = { onOfferTime(need.id) }) { Text(TextKey.OFFER_TIME.label(locale)) }
                }
            }
        }
    }
}

@Composable
private fun ProfileScreen(state: GenerosityUiState, appState: GenerosityAppState) {
    val locale = state.locale
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text(TextKey.TAB_PROFILE.label(locale), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Surface(shape = RoundedCornerShape(12.dp)) {
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                CategoryBadge(null, 52)
                Column(Modifier.weight(1f)) {
                    Text(if (state.profile.isRegistered) state.profile.displayName ?: TextKey.REGISTERED_PROFILE.label(locale) else TextKey.ANONYMOUS_PROFILE.label(locale),
                        fontWeight = FontWeight.SemiBold)
                    if (!state.profile.isRegistered) TextButton(onClick = { appState.signIn("user-1", "Ana") }) { Text(TextKey.SIGN_IN.label(locale)) }
                }
            }
        }
        Surface(color = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary, shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(localizedString(R.string.points_title, locale), style = MaterialTheme.typography.labelLarge)
                Text(localizedString(R.string.unavailable, locale), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(TextKey.GENEROSITY_POINTS.label(locale), style = MaterialTheme.typography.bodySmall)
            }
        }
        Text(TextKey.IMPACT_TITLE.label(locale), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Metric(localizedString(R.string.centers_supported, locale), if (state.profile.isRegistered) localizedNumber(state.impact.centersSupported, locale) else "—")
            Metric(localizedString(R.string.needs_supported, locale), if (state.profile.isRegistered) localizedNumber(state.impact.needsSupported, locale) else "—")
        }
        Text(localizedString(R.string.impact_basis, locale), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(localizedString(R.string.history, locale), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (state.contributions.isEmpty()) EmptyMessage(TextKey.CONTRIBUTION_HISTORY_EMPTY.label(locale))
        state.contributions.forEach { contribution ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(contribution.title.resolve(locale), fontWeight = FontWeight.SemiBold)
                val status = when (contribution.status) {
                    ContributionStatus.OFFERED -> R.string.contribution_offered
                    ContributionStatus.CONFIRMED -> R.string.contribution_confirmed
                    ContributionStatus.FULFILLED -> R.string.contribution_fulfilled
                    ContributionStatus.CANCELLED -> R.string.contribution_cancelled
                }
                Text(if (contribution.centerId == null) localizedString(R.string.personal_help, locale) else localizedString(status, locale),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
private fun Metric(label: String, value: String) {
    Surface(shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.widthIn(min = 140.dp, max = 220.dp).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AppIcon(R.drawable.ic_heart, tint = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun EmptyMessage(text: String) {
    Text(text, Modifier.fillMaxWidth().padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
}

@Composable
private fun SettingsScreen(state: GenerosityUiState, appState: GenerosityAppState, onLanguageSelected: (SupportedLocale?) -> Unit, savingLanguage: Boolean, languageSaveFailed: Boolean) {
    val locale = state.locale
    val preferences = state.preferences
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(TextKey.TAB_SETTINGS.label(locale), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(localizedString(R.string.language, locale), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Surface(shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.selectableGroup().padding(horizontal = 12.dp)) {
                (listOf<SupportedLocale?>(null) + SupportedLocale.entries).forEach { choice ->
                    val selected = state.localePreference.selectedLocale == choice
                    val label = choice?.nativeName ?: localizedString(R.string.language_device, locale)
                    Row(Modifier.fillMaxWidth().testTag("language-" + (choice?.code ?: "system"))
                        .selectable(selected, enabled = !savingLanguage, role = Role.RadioButton) { onLanguageSelected(choice) }
                        .padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        RadioButton(selected, onClick = null, enabled = !savingLanguage)
                        Text(label, Modifier.weight(1f))
                    }
                }
            }
        }
        if (languageSaveFailed) Text(localizedString(R.string.language_save_failed, locale), color = MaterialTheme.colorScheme.error)
        Text(localizedString(R.string.notifications_title, locale), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Surface(shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                NotificationToggle(TextKey.NEARBY_NOTIFICATIONS.label(locale), preferences.nearbyCentersEnabled) { appState.setPreferences(preferences.copy(nearbyCentersEnabled = it)) }
                NotificationToggle(TextKey.URGENT_NOTIFICATIONS.label(locale), preferences.urgentNeedsEnabled) { appState.setPreferences(preferences.copy(urgentNeedsEnabled = it)) }
                NotificationToggle(TextKey.REMINDER_NOTIFICATIONS.label(locale), preferences.remindersEnabled) { appState.setPreferences(preferences.copy(remindersEnabled = it)) }
                Text(org.generosity.usecase.NearbyAlertPolicy().settingsNotice().resolve(locale), style = MaterialTheme.typography.bodySmall)
            }
        }
        PlatformPermissionActions(locale)
    }
}

@Composable
private fun NotificationToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
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
