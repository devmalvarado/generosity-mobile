package org.generosity.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.generosity.app.GenerosityAppState
import org.generosity.app.GenerosityUiState
import org.generosity.domain.CenterCategory
import org.generosity.domain.CenterNeed
import org.generosity.domain.ContributionOffer
import org.generosity.domain.HelpMethod
import org.generosity.domain.KindnessCenter
import org.generosity.domain.NotificationPreference
import org.generosity.domain.RejectionReason
import org.generosity.domain.SupportedLocale

private enum class AppTab {
    MAP,
    LIST,
    PROFILE,
    SETTINGS
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
fun GenerosityApp(appState: GenerosityAppState) {
    val state by appState.state.collectAsState()
    val locale = state.locale
    val tab = remember { mutableStateOf(AppTab.MAP) }

    MaterialTheme {
        Scaffold(
            topBar = {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Generosity", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Row {
                            TextButton(onClick = { appState.setLocale(SupportedLocale.ENGLISH) }) {
                                Text(TextKey.LANGUAGE_ENGLISH.label(locale))
                            }
                            TextButton(onClick = { appState.setLocale(SupportedLocale.SPANISH) }) {
                                Text(TextKey.LANGUAGE_SPANISH.label(locale))
                            }
                        }
                    }
                    PrimaryTabRow(selectedTabIndex = tab.value.ordinal) {
                        AppTab.entries.forEach { appTab ->
                            Tab(
                                selected = tab.value == appTab,
                                onClick = { tab.value = appTab },
                                text = { Text(appTab.label(locale)) }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                when (tab.value) {
                    AppTab.MAP -> DiscoveryScreen(state, appState, showMap = true)
                    AppTab.LIST -> DiscoveryScreen(state, appState, showMap = false)
                    AppTab.PROFILE -> ProfileScreen(state, appState)
                    AppTab.SETTINGS -> SettingsScreen(state, appState)
                }
            }
        }
    }
}

@Composable
private fun DiscoveryScreen(state: GenerosityUiState, appState: GenerosityAppState, showMap: Boolean) {
    val locale = state.locale

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text(TextKey.NEARBY_CENTERS.label(locale), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.searchText,
                onValueChange = appState::setSearchText,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(TextKey.SEARCH_PLACEHOLDER.label(locale)) }
            )
        }

        item {
            CategoryFilters(locale, state.selectedCategories, appState::toggleCategory)
        }

        if (showMap) {
            item { MapSurface(locale, state.centers, appState::selectCenter) }
        }

        if (state.centers.isEmpty()) {
            item { Text(TextKey.EMPTY_CENTERS.label(locale)) }
        } else {
            items(state.centers, key = { it.id }) { center ->
                CenterRow(locale, center, onClick = { appState.selectCenter(center.id) })
            }
        }

        state.selectedCenter?.let { center ->
            item {
                CenterDetail(
                    locale = locale,
                    center = center,
                    needs = state.currentNeeds,
                    visitRecorded = state.visitSignalCenterId == center.id,
                    rejection = state.lastRejection,
                    onVisitSignal = { appState.signalVisit(center.id) },
                    onOfferGoods = { appState.offerHelp(it, ContributionOffer(HelpMethod.GOODS, quantity = 5)) },
                    onOfferTime = {
                        appState.offerHelp(it, ContributionOffer(HelpMethod.TIME, activity = "volunteer", availability = "weekday"))
                    }
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
private fun CategoryFilters(
    locale: SupportedLocale,
    selectedCategories: Set<CenterCategory>,
    onCategoryToggle: (CenterCategory) -> Unit
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CenterCategory.entries.forEach { category ->
            FilterChip(
                selected = category in selectedCategories,
                onClick = { onCategoryToggle(category) },
                label = { Text(category.label(locale)) }
            )
        }
    }
}

@Composable
private fun MapSurface(
    locale: SupportedLocale,
    centers: List<KindnessCenter>,
    onCenterSelected: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFEAF4EF))
            .padding(16.dp)
    ) {
        Text(TextKey.TAB_MAP.label(locale), color = Color(0xFF27594A), fontWeight = FontWeight.Bold)
        Column(
            modifier = Modifier.align(Alignment.Center),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            centers.forEach { center ->
                Row(
                    modifier = Modifier.clickable { onCenterSelected(center.id) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1B7F5A))
                    )
                    Text(
                        text = center.name.resolve(locale),
                        modifier = Modifier.padding(start = 8.dp),
                        color = Color(0xFF173D33)
                    )
                }
            }
        }
    }
}

@Composable
private fun CenterRow(locale: SupportedLocale, center: KindnessCenter, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        tonalElevation = 1.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(center.name.resolve(locale), fontWeight = FontWeight.SemiBold)
            Text(center.address.resolve(locale), style = MaterialTheme.typography.bodySmall)
            Text(center.verificationStatus.label(locale), style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun CenterDetail(
    locale: SupportedLocale,
    center: KindnessCenter,
    needs: List<CenterNeed>,
    visitRecorded: Boolean,
    rejection: RejectionReason?,
    onVisitSignal: () -> Unit,
    onOfferGoods: (String) -> Unit,
    onOfferTime: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(center.name.resolve(locale), style = MaterialTheme.typography.titleLarge)
            Text(center.description.resolve(locale))
            Text(center.address.resolve(locale))
            Text(center.categories.joinToString { it.label(locale) }, style = MaterialTheme.typography.bodySmall)
            Text(center.verificationStatus.label(locale), fontWeight = FontWeight.SemiBold)
            Text(TextKey.OFFICIAL_VERIFICATION_BOUNDARY.label(locale), style = MaterialTheme.typography.bodySmall)

            Text(TextKey.CURRENT_NEEDS.label(locale), style = MaterialTheme.typography.titleMedium)
            if (needs.isEmpty()) {
                Text(TextKey.NO_CURRENT_NEEDS.label(locale))
            } else {
                needs.forEach { need ->
                    NeedRow(locale, need, onOfferGoods, onOfferTime)
                }
            }

            if (rejection == RejectionReason.REGISTRATION_REQUIRED) {
                Text(TextKey.REGISTRATION_REQUIRED.label(locale), color = Color(0xFFB3261E))
            }

            Button(onClick = onVisitSignal, enabled = !visitRecorded) {
                Text(TextKey.VISIT_SIGNAL.label(locale))
            }
            if (visitRecorded) {
                Text(TextKey.VISIT_SIGNAL_SUBMITTED.label(locale), color = Color(0xFF1B7F5A))
            }
        }
    }
}

@Composable
private fun NeedRow(
    locale: SupportedLocale,
    need: CenterNeed,
    onOfferGoods: (String) -> Unit,
    onOfferTime: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(need.title.resolve(locale), fontWeight = FontWeight.SemiBold)
            if (need.isUrgent) {
                Text(TextKey.URGENT.label(locale), color = Color(0xFFB3261E), fontWeight = FontWeight.Bold)
            }
        }
        Text(need.description.resolve(locale))
        need.quantityRemaining?.let { remaining ->
            Text("$remaining ${TextKey.REMAINING.label(locale)}", style = MaterialTheme.typography.bodySmall)
        }
        Text(need.helpMethods.joinToString { it.label(locale) }, style = MaterialTheme.typography.bodySmall)
        if (HelpMethod.MONEY in need.helpMethods) {
            Text(TextKey.MONEY_BOUNDARY.label(locale), style = MaterialTheme.typography.bodySmall)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (HelpMethod.GOODS in need.helpMethods && need.quantityRemaining != null) {
                TextButton(onClick = { onOfferGoods(need.id) }) { Text(TextKey.OFFER_GOODS.label(locale)) }
            }
            if (HelpMethod.TIME in need.helpMethods) {
                TextButton(onClick = { onOfferTime(need.id) }) { Text(TextKey.OFFER_TIME.label(locale)) }
            }
        }
    }
}

@Composable
private fun ProfileScreen(state: GenerosityUiState, appState: GenerosityAppState) {
    val locale = state.locale

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(TextKey.TAB_PROFILE.label(locale), style = MaterialTheme.typography.titleLarge)
        if (state.profile.isRegistered) {
            Text(state.profile.displayName ?: TextKey.REGISTERED_PROFILE.label(locale))
        } else {
            Text(TextKey.ANONYMOUS_PROFILE.label(locale))
            Button(onClick = { appState.signIn("user-1", "Ana") }) {
                Text(TextKey.SIGN_IN.label(locale))
            }
        }

        if (state.contributions.isEmpty()) {
            Text(TextKey.CONTRIBUTION_HISTORY_EMPTY.label(locale))
        } else {
            state.contributions.forEach { contribution ->
                Text(contribution.title.resolve(locale))
            }
        }

        Text(TextKey.IMPACT_TITLE.label(locale), style = MaterialTheme.typography.titleMedium)
        Text(TextKey.GENEROSITY_POINTS.label(locale), style = MaterialTheme.typography.bodySmall)
        Text("${state.impact.goodsCommitted} · ${state.impact.needsSupported} · ${state.impact.centersSupported}")
    }
}

@Composable
private fun SettingsScreen(state: GenerosityUiState, appState: GenerosityAppState) {
    val locale = state.locale
    val preferences = state.preferences

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(TextKey.TAB_SETTINGS.label(locale), style = MaterialTheme.typography.titleLarge)
        NotificationToggle(TextKey.NEARBY_NOTIFICATIONS.label(locale), preferences.nearbyCentersEnabled) {
            appState.setPreferences(preferences.copy(nearbyCentersEnabled = it))
        }
        NotificationToggle(TextKey.URGENT_NOTIFICATIONS.label(locale), preferences.urgentNeedsEnabled) {
            appState.setPreferences(preferences.copy(urgentNeedsEnabled = it))
        }
        NotificationToggle(TextKey.REMINDER_NOTIFICATIONS.label(locale), preferences.remindersEnabled) {
            appState.setPreferences(preferences.copy(remindersEnabled = it))
        }
        PlatformPermissionActions(locale)
    }
}

@Composable
private fun NotificationToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun AppTab.label(locale: SupportedLocale): String = when (this) {
    AppTab.MAP -> TextKey.TAB_MAP.label(locale)
    AppTab.LIST -> TextKey.TAB_LIST.label(locale)
    AppTab.PROFILE -> TextKey.TAB_PROFILE.label(locale)
    AppTab.SETTINGS -> TextKey.TAB_SETTINGS.label(locale)
}
