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
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.generosity.data.SampleImportedCatalog
import org.generosity.domain.CenterCategory
import org.generosity.domain.CenterNeed
import org.generosity.domain.HelpMethod
import org.generosity.domain.KindnessCenter
import org.generosity.domain.NotificationPreference
import org.generosity.domain.SupportedLocale
import org.generosity.domain.UserMode
import org.generosity.domain.UserProfile
import org.generosity.usecase.CurrentNeedsUseCase
import org.generosity.usecase.DiscoveryQuery
import org.generosity.usecase.MoneyDonationPolicy

private enum class AppTab {
    MAP,
    LIST,
    PROFILE,
    SETTINGS
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
fun GenerosityApp() {
    var locale by remember { mutableStateOf(SupportedLocale.ENGLISH) }
    var tab by remember { mutableStateOf(AppTab.MAP) }
    var searchText by remember { mutableStateOf("") }
    var selectedCategories by remember { mutableStateOf(emptySet<CenterCategory>()) }
    var selectedCenter by remember { mutableStateOf<KindnessCenter?>(null) }
    var visitMessage by remember { mutableStateOf<String?>(null) }
    var preferences by remember { mutableStateOf(NotificationPreference()) }

    val centers = remember { SampleImportedCatalog.centers() }
    val query = DiscoveryQuery(searchText = searchText, categories = selectedCategories, locale = locale)
    val filteredCenters = remember(centers, query) {
        centers
            .filter { center -> query.categories.isEmpty() || center.categories.any { it in query.categories } }
            .filter { center ->
                val text = query.searchText.trim().lowercase()
                text.isEmpty() ||
                    center.name.resolve(locale).lowercase().contains(text) ||
                    center.description.resolve(locale).lowercase().contains(text) ||
                    center.address.resolve(locale).lowercase().contains(text)
            }
            .sortedBy { it.name.resolve(locale) }
    }

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
                            TextButton(onClick = { locale = SupportedLocale.ENGLISH }) {
                                Text(TextKey.LANGUAGE_ENGLISH.label(locale))
                            }
                            TextButton(onClick = { locale = SupportedLocale.SPANISH }) {
                                Text(TextKey.LANGUAGE_SPANISH.label(locale))
                            }
                        }
                    }
                    TabRow(selectedTabIndex = tab.ordinal) {
                        AppTab.entries.forEach { appTab ->
                            Tab(
                                selected = tab == appTab,
                                onClick = { tab = appTab },
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
                when (tab) {
                    AppTab.MAP -> DiscoveryScreen(
                        locale = locale,
                        centers = filteredCenters,
                        searchText = searchText,
                        onSearchTextChange = { searchText = it },
                        selectedCategories = selectedCategories,
                        onCategoryToggle = { category ->
                            selectedCategories = if (category in selectedCategories) {
                                selectedCategories - category
                            } else {
                                selectedCategories + category
                            }
                        },
                        selectedCenter = selectedCenter,
                        visitMessage = visitMessage,
                        onCenterSelected = {
                            selectedCenter = it
                            visitMessage = null
                        },
                        onVisitSignal = {
                            visitMessage = TextKey.VISIT_SIGNAL_SUBMITTED.label(locale)
                        },
                        showMap = true
                    )
                    AppTab.LIST -> DiscoveryScreen(
                        locale = locale,
                        centers = filteredCenters,
                        searchText = searchText,
                        onSearchTextChange = { searchText = it },
                        selectedCategories = selectedCategories,
                        onCategoryToggle = { category ->
                            selectedCategories = if (category in selectedCategories) {
                                selectedCategories - category
                            } else {
                                selectedCategories + category
                            }
                        },
                        selectedCenter = selectedCenter,
                        visitMessage = visitMessage,
                        onCenterSelected = {
                            selectedCenter = it
                            visitMessage = null
                        },
                        onVisitSignal = {
                            visitMessage = TextKey.VISIT_SIGNAL_SUBMITTED.label(locale)
                        },
                        showMap = false
                    )
                    AppTab.PROFILE -> ProfileScreen(locale)
                    AppTab.SETTINGS -> SettingsScreen(locale, preferences) { preferences = it }
                }
            }
        }
    }
}

@Composable
private fun DiscoveryScreen(
    locale: SupportedLocale,
    centers: List<KindnessCenter>,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    selectedCategories: Set<CenterCategory>,
    onCategoryToggle: (CenterCategory) -> Unit,
    selectedCenter: KindnessCenter?,
    visitMessage: String?,
    onCenterSelected: (KindnessCenter) -> Unit,
    onVisitSignal: () -> Unit,
    showMap: Boolean
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text(TextKey.NEARBY_CENTERS.label(locale), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = searchText,
                onValueChange = onSearchTextChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(TextKey.SEARCH_PLACEHOLDER.label(locale)) }
            )
        }

        item {
            CategoryFilters(locale, selectedCategories, onCategoryToggle)
        }

        if (showMap) {
            item {
                MapSurface(locale, centers, onCenterSelected)
            }
        }

        if (centers.isEmpty()) {
            item {
                Text(TextKey.EMPTY_CENTERS.label(locale))
            }
        } else {
            items(centers, key = { it.id }) { center ->
                CenterRow(locale, center, onClick = { onCenterSelected(center) })
            }
        }

        selectedCenter?.let { center ->
            item {
                CenterDetail(locale, center, visitMessage, onVisitSignal)
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
    onCenterSelected: (KindnessCenter) -> Unit
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
                    modifier = Modifier.clickable { onCenterSelected(center) },
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
    visitMessage: String?,
    onVisitSignal: () -> Unit
) {
    val currentNeeds = remember(center) { CurrentNeedsUseCase()(center.needs, "2026-05-13") }

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
            if (currentNeeds.isEmpty()) {
                Text(TextKey.NO_CURRENT_NEEDS.label(locale))
            } else {
                currentNeeds.forEach { need -> NeedRow(locale, need) }
            }

            Button(onClick = onVisitSignal) {
                Text(TextKey.VISIT_SIGNAL.label(locale))
            }
            visitMessage?.let { Text(it, color = Color(0xFF1B7F5A)) }
        }
    }
}

@Composable
private fun NeedRow(locale: SupportedLocale, need: CenterNeed) {
    val canStartMoney = remember(need) { MoneyDonationPolicy().canStartDonation(need, false) }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(need.title.resolve(locale), fontWeight = FontWeight.SemiBold)
            if (need.urgent) {
                Text(TextKey.URGENT.label(locale), color = Color(0xFFB3261E), fontWeight = FontWeight.Bold)
            }
        }
        Text(need.description.resolve(locale))
        Text(need.helpMethods.joinToString { it.label(locale) }, style = MaterialTheme.typography.bodySmall)
        if (HelpMethod.MONEY in need.helpMethods && !canStartMoney) {
            Text(TextKey.MONEY_BOUNDARY.label(locale), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ProfileScreen(locale: SupportedLocale) {
    val profile = UserProfile(mode = UserMode.ANONYMOUS)

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(TextKey.TAB_PROFILE.label(locale), style = MaterialTheme.typography.titleLarge)
        Text(if (profile.isRegistered) TextKey.REGISTERED_PROFILE.label(locale) else TextKey.ANONYMOUS_PROFILE.label(locale))
        Text(TextKey.CONTRIBUTION_HISTORY_EMPTY.label(locale))
        Text(TextKey.GENEROSITY_POINTS.label(locale))
    }
}

@Composable
private fun SettingsScreen(
    locale: SupportedLocale,
    preferences: NotificationPreference,
    onPreferencesChange: (NotificationPreference) -> Unit
) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(TextKey.TAB_SETTINGS.label(locale), style = MaterialTheme.typography.titleLarge)
        NotificationToggle(
            label = TextKey.NEARBY_NOTIFICATIONS.label(locale),
            checked = preferences.nearbyCentersEnabled,
            onCheckedChange = { onPreferencesChange(preferences.copy(nearbyCentersEnabled = it)) }
        )
        NotificationToggle(
            label = TextKey.URGENT_NOTIFICATIONS.label(locale),
            checked = preferences.urgentNeedsEnabled,
            onCheckedChange = { onPreferencesChange(preferences.copy(urgentNeedsEnabled = it)) }
        )
        NotificationToggle(
            label = TextKey.REMINDER_NOTIFICATIONS.label(locale),
            checked = preferences.remindersEnabled,
            onCheckedChange = { onPreferencesChange(preferences.copy(remindersEnabled = it)) }
        )
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
