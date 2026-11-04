@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package org.generosity.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalContext
import org.generosity.app.ui.map.searchPlaces
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import android.text.TextUtils
import android.view.View
import java.util.Locale
import org.generosity.app.GenerosityAppState
import org.generosity.app.GenerosityUiState
import org.generosity.app.DeviceLocationSource
import org.generosity.app.DiscoveryLocation
import org.generosity.app.R
import org.generosity.app.AndroidPush
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
    authBusy: Boolean = false, authFailed: Boolean = false,
    placeSearch: (suspend (String, SupportedLocale) -> List<DiscoveryArea>)? = null,
    locationSource: DeviceLocationSource? = null,
    designHarness: Boolean = false
) {
    val context = LocalContext.current
    val search: suspend (String, SupportedLocale) -> List<DiscoveryArea> = placeSearch ?: { query, locale -> searchPlaces(context, query, locale) }
    val state by appState.state.collectAsState()
    val locale = state.locale
    val location = locationSource?.let { rememberDiscoveryLocation(it, appState) }
    var tab by rememberSaveable { mutableStateOf(AppTab.MAP) }
    var showInbox by rememberSaveable { mutableStateOf(false) }
    var showPreview by remember(state.profile.id) { mutableStateOf(designHarness) }
    val previewGroups = if (designHarness && org.generosity.app.BuildConfig.DEBUG) org.generosity.preview.PreviewSession.groupNames else state.features.previewGroups
    LaunchedEffect(previewGroups) { if (previewGroups.isEmpty()) showPreview = false }
    LaunchedEffect(state.noticeDestinationId) {
        if (state.noticeDestinationId != null) { tab = AppTab.LIST; showInbox = false }
    }
    val mapState = rememberSaveableStateHolder()
    val direction = if (TextUtils.getLayoutDirectionFromLocale(Locale.forLanguageTag(locale.code)) == View.LAYOUT_DIRECTION_RTL)
        LayoutDirection.Rtl else LayoutDirection.Ltr
    CompositionLocalProvider(LocalLayoutDirection provides direction) {
        GenerosityTheme {
            if (showPreview && previewGroups.isNotEmpty()) {
                ConnectedDesign(state, appState, previewGroups, mapRenderer, mapState, search, location, locationSource,
                    onLanguageSelected, savingLanguage, languageSaveFailed, onSignIn, onSignOut, onCreate, authBusy, authFailed) { showPreview = false }
                return@GenerosityTheme
            }
            if (showInbox) AlertDialog(onDismissRequest = { showInbox = false },
                title = { Text(localizedString(R.string.inbox_title, locale)) },
                text = {
                    LazyColumn(Modifier.heightIn(max = 480.dp)) {
                        item { Text(localizedString(R.string.inbox_retention, locale)) }
                        if (state.notices.isEmpty()) item { Text(localizedString(R.string.inbox_empty, locale)) }
                        items(state.notices, key = { it.id }) { notice ->
                            TextButton(onClick = { appState.openNotice(notice.id) }) {
                                Text((if (notice.read) "" else "• ") + notice.title.resolve(locale), fontWeight = if (notice.read) FontWeight.Normal else FontWeight.Bold)
                            }
                        }
                    }
                }, confirmButton = { TextButton(onClick = { showInbox = false }) { Text(localizedString(R.string.inbox_close, locale)) } })
            Scaffold(
                topBar = {
                    Surface {
                        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = GenSpace.lg, vertical = GenSpace.sm)) {
                            BrandWordmark()
                            Spacer(Modifier.weight(1f))
                            if (state.remoteAccount) TextButton(onClick = { showInbox = true; appState.onResume() }, modifier = Modifier.testTag("notification-inbox")) {
                                Text(localizedString(R.string.inbox_title, locale) + " (${state.notices.count { !it.read }})")
                            }
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
                    Column {
                    if (state.features.previewGroups.isNotEmpty()) TextButton(onClick = { showPreview = true }, modifier = Modifier.fillMaxWidth().testTag("open-native-preview")) {
                        Text(if (locale.code == "es") "Explorar diseño de prueba" else "Explore design preview")
                    }
                    if (state.pushBanner != null) Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { showInbox = true; appState.dismissPushBanner() }, modifier = Modifier.weight(1f)) { Text(localizedString(R.string.push_new, locale)) }
                        TextButton(onClick = appState::dismissPushBanner) { Text(localizedString(R.string.inbox_close, locale)) }
                    }
                    when (tab) {
                        AppTab.MAP -> DiscoveryScreen(state, appState, true, mapRenderer, mapState, search, location, locationSource)
                        AppTab.LIST -> DiscoveryScreen(state, appState, false, mapRenderer, mapState, search, location, locationSource)
                        AppTab.PROFILE -> ProfileScreen(state, appState, onSignIn, onSignOut, onCreate, authBusy, authFailed)
                        AppTab.SETTINGS -> SettingsScreen(state, appState, onLanguageSelected, savingLanguage, languageSaveFailed)
                    }
                    }
                }
            }
        }
    }
}


@Composable
private fun ConnectedDesign(
    state: GenerosityUiState, app: GenerosityAppState, groups: List<String>,
    renderer: CenterMapRenderer, mapState: SaveableStateHolder,
    search: suspend (String, SupportedLocale) -> List<DiscoveryArea>,
    location: DiscoveryLocation?, source: DeviceLocationSource?,
    onLanguage: (SupportedLocale?) -> Unit, savingLanguage: Boolean, languageFailed: Boolean,
    onSignIn: () -> Unit, onSignOut: () -> Unit, onCreate: () -> Unit, authBusy: Boolean, authFailed: Boolean,
    onClose: () -> Unit
) {
    val canOffer = state.canRecordContributions && "contributions" in groups
    val mapCenter = state.selectedCenter ?: state.centers.firstOrNull()
    val density = LocalDensity.current
    var mapTop by remember { mutableStateOf(0.dp) }
    var mapBottom by remember { mutableStateOf(0.dp) }
    val locale = state.locale
    val device = location?.state?.collectAsState()?.value
    val areaLabel = localizedString(R.string.area_search, locale)
    var areaSearch by remember { mutableStateOf(false) }
    var offer by remember(state.profile.id) { mutableStateOf<Pair<CenterNeed?, HelpMethod>?>(null) }
    if (areaSearch) AreaSearchDialog(locale, search, {
        location?.useManualOrigin(); app.setSearchText(""); app.setDiscoveryArea(it); areaSearch = false
    }, { areaSearch = false }, initialQuery = state.searchText)
    offer?.let { (need, method) -> OfferDialog(state, app, need, method) { offer = null } }
    NativePreview(groups, locale.code, state.profile.id.orEmpty(), connectedContent = { route, navigate ->
        LaunchedEffect(state.noticeDestinationId) {
            if (state.noticeDestinationId != null) { navigate("center"); app.clearNoticeDestination() }
        }
        when (route) {
            "map" -> Box(Modifier.fillMaxSize().testTag("connected-map")) {
                mapState.SaveableStateProvider("connected-map") {
                    renderer.Render(state.centers.toMapMarkers(locale), mapCenter?.id, locale, app::selectCenter,
                        Modifier.fillMaxSize(), state.discoveryArea?.coordinates, state.discoveryArea?.label, PaddingValues(top = mapTop, bottom = mapBottom))
                }
                Column(Modifier.align(Alignment.TopCenter).onSizeChanged { mapTop = with(density) { it.height.toDp() } }.padding(GenSpace.sm), verticalArrangement = Arrangement.spacedBy(GenSpace.xs)) {
                    Surface(shape = RoundedCornerShape(GenRadius.pill), shadowElevation = 4.dp) {
                        TextField(state.searchText, app::setSearchText, modifier = Modifier.fillMaxWidth().testTag("connected-query"),
                            placeholder = { Text(TextKey.SEARCH_PLACEHOLDER.label(locale), maxLines = 1) }, singleLine = true,
                            leadingIcon = { IconButton(onClick = { areaSearch = true }) { AppIcon(R.drawable.ic_search, Modifier.semantics { contentDescription = areaLabel }) } },
                            trailingIcon = { if (location != null && source != null) LocationActions(location, source, locale, compact = true) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { areaSearch = true }))
                    }
                    Surface(shape = RoundedCornerShape(GenRadius.card)) { CategoryFilters(locale, state.selectedCategories, app::toggleCategory, horizontal = true) }
                }
                Surface(Modifier.align(Alignment.BottomCenter).fillMaxWidth().onSizeChanged { mapBottom = with(density) { it.height.toDp() } }.padding(GenSpace.sm), shape = RoundedCornerShape(GenRadius.card), shadowElevation = 3.dp) {
                    Column(Modifier.padding(GenSpace.md), verticalArrangement = Arrangement.spacedBy(GenSpace.xs)) {
                        if (state.syncing) LinearProgressIndicator(Modifier.fillMaxWidth())
                        if (state.syncError != null) Text(localizedString(R.string.sync_failed, locale), color = MaterialTheme.colorScheme.error)
                        if (device?.problem != null) Text(localizedString(when (device.problem) {
                            org.generosity.app.LocationProblem.DENIED -> R.string.location_denied
                            org.generosity.app.LocationProblem.DISABLED -> R.string.location_disabled
                            else -> R.string.location_unavailable
                        }, locale), style = MaterialTheme.typography.bodySmall)
                        state.discoveryArea?.let { Text(if (device?.fix != null) localizedString(R.string.location_scope, locale) else it.label, style = MaterialTheme.typography.bodySmall) }
                        mapCenter?.let { center ->
                            CenterIdentity(locale, center)
                            Button(onClick = { app.selectCenter(center.id); navigate("center") }, modifier = Modifier.testTag("connected-details")) { Text(localizedString(R.string.view_details, locale)) }
                        } ?: Text(TextKey.EMPTY_CENTERS.label(locale))
                    }
                }
            }
            "list" -> {
                SyncStatus(state, app)
                if (state.centers.isEmpty()) EmptyMessage(TextKey.EMPTY_CENTERS.label(locale))
                state.centers.forEach { center ->
                    TextButton(onClick = { app.selectCenter(center.id); navigate("center") }, modifier = Modifier.testTag("connected-center-${center.id}")) { CenterIdentity(locale, center) }
                }
            }
            "filters" -> {
                CategoryFilters(locale, state.selectedCategories, app::toggleCategory)
                TextButton(onClick = { areaSearch = true }) { Text(localizedString(R.string.area_search, locale)) }
                if (location != null && source != null) LocationActions(location, source, locale)
                if (state.discoveryArea != null) TextButton(onClick = { location?.useManualOrigin(); app.setDiscoveryArea(null) }) { Text(localizedString(R.string.area_clear, locale)) }
                SyncStatus(state, app)
            }
            "settings" -> SettingsScreen(state, app, onLanguage, savingLanguage, languageFailed)
            "profile", "history", "impact" -> Column {
                if (route == "profile") Row {
                    if ("contributions" in groups) TextButton(onClick = { navigate("history") }) { Text(localizedString(R.string.history, locale)) }
                    TextButton(onClick = { navigate("settings") }) { Text(TextKey.TAB_SETTINGS.label(locale)) }
                }
                ProfileScreen(state, app, onSignIn, onSignOut, onCreate, authBusy, authFailed, section = route)
            }
            "welcome" -> WelcomeScreen(locale, authBusy, authFailed, { navigate("map") }, onSignIn, onCreate)
            else -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(GenSpace.lg).testTag("connected-content"), verticalArrangement = Arrangement.spacedBy(GenSpace.md)) {
                SyncStatus(state, app)
                when (route) {
                    "center", "offer" -> state.selectedCenter?.let { center ->
                        CenterDetail(locale, center, state.currentNeeds, center.id in state.visitedCenterIds || state.visitSignalCenterId == center.id,
                            state.lastRejection, !canOffer, state.backendConfigured, state.features.showNeedProgress, state.profile.isRegistered,
                            { app.signalVisit(center.id) },
                            { id -> state.currentNeeds.firstOrNull { it.id == id }?.let { offer = it to HelpMethod.GOODS } },
                            { id -> state.currentNeeds.firstOrNull { it.id == id }?.let { offer = it to HelpMethod.TIME } })
                    } ?: Text(TextKey.EMPTY_CENTERS.label(locale))
                    "needs" -> {
                        if (state.discoveredNeeds.isEmpty()) EmptyMessage(TextKey.NO_CURRENT_NEEDS.label(locale))
                        state.discoveredNeeds.forEach { need ->
                            state.centers.firstOrNull { it.id == need.centerId }?.let { center ->
                                TextButton(onClick = { app.selectCenter(center.id); navigate("center") }) { Text(center.name.resolve(locale)) }
                            }
                            NeedRow(locale, need, !canOffer, state.features.showNeedProgress,
                                { app.selectCenter(need.centerId); offer = need to HelpMethod.GOODS },
                                { app.selectCenter(need.centerId); offer = need to HelpMethod.TIME })
                            HorizontalDivider()
                        }
                    }
                    "help", "personal" -> {
                        if (route == "help" && "discovery" in groups) Button(onClick = { navigate("needs") }) { Text(TextKey.CURRENT_NEEDS.label(locale)) }
                        Text(localizedString(R.string.personal_add, locale), style = MaterialTheme.typography.titleMedium)
                        Text(localizedString(R.string.personal_notice, locale))
                        Button(onClick = { offer = null to HelpMethod.GOODS }, enabled = canOffer) { Text(localizedString(R.string.offer_goods_action, locale)) }
                        Button(onClick = { offer = null to HelpMethod.TIME }, enabled = canOffer) { Text(TextKey.OFFER_TIME.label(locale)) }
                        if (!canOffer) Text(localizedString(if (state.profile.isRegistered) R.string.feature_unavailable else R.string.registration_required, locale))
                    }
                    "inbox" -> {
                        Text(localizedString(R.string.inbox_retention, locale))
                        if (!state.profile.isRegistered) TextButton(onClick = onSignIn) { Text(localizedString(R.string.auth_sign_in, locale)) }
                        if (state.notices.isEmpty()) Text(localizedString(R.string.inbox_empty, locale))
                        state.notices.forEach { notice -> TextButton(onClick = { app.openNotice(notice.id) }) { Text((if (notice.read) "" else "• ") + notice.title.resolve(locale), fontWeight = if (notice.read) FontWeight.Normal else FontWeight.Bold) } }
                    }
                }
            }
        }
    }, onClose = onClose)
}

@Composable
private fun DiscoveryScreen(
    state: GenerosityUiState, appState: GenerosityAppState, showMap: Boolean,
    mapRenderer: CenterMapRenderer, mapState: SaveableStateHolder,
    placeSearch: suspend (String, SupportedLocale) -> List<DiscoveryArea>,
    location: DiscoveryLocation?, locationSource: DeviceLocationSource?
) {
    val locale = state.locale
    val deviceLocation = location?.state?.collectAsState()?.value
    val hasDeviceOrigin = deviceLocation?.fix != null
    val listState = rememberLazyListState()
    val keyboard = LocalSoftwareKeyboardController.current
    var showDetail by remember { mutableStateOf(false) }
    LaunchedEffect(state.noticeDestinationId, showMap) {
        if (state.noticeDestinationId != null && !showMap) { showDetail = true; appState.clearNoticeDestination() }
    }
    var showAreaSearch by remember { mutableStateOf(false) }
    var scrollTarget by remember { mutableStateOf<String?>(null) }
    fun selectCenter(id: String, details: Boolean = false) {
        keyboard?.hide()
        appState.selectCenter(id)
        showDetail = details || !showMap
    }
    LaunchedEffect(scrollTarget) {
        val index = state.centers.indexOfFirst { it.id == scrollTarget }
        if (index >= 0) listState.animateScrollToItem(index + 1)
        scrollTarget = null
    }
    LaunchedEffect(state.discoveryArea, state.searchText, state.selectedCategories) { listState.scrollToItem(0) }
    if (showAreaSearch) AreaSearchDialog(locale, placeSearch, {
        location?.useManualOrigin()
        appState.setDiscoveryArea(it); showAreaSearch = false
    }, { showAreaSearch = false })
    var offer by remember { mutableStateOf<Pair<CenterNeed, HelpMethod>?>(null) }
    offer?.let { (need, method) -> OfferDialog(state, appState, need, method) { offer = null } }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val mapHeight = (maxHeight * .4f).coerceIn(100.dp, 300.dp)
        Column(Modifier.fillMaxSize().padding(horizontal = GenSpace.lg), verticalArrangement = Arrangement.spacedBy(GenSpace.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GenSpace.sm)) {
                OutlinedTextField(
                    value = state.searchText, onValueChange = appState::setSearchText,
                    modifier = Modifier.weight(1f), singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
                    shape = RoundedCornerShape(GenRadius.card),
                    leadingIcon = { AppIcon(R.drawable.ic_search) },
                    label = { Text(TextKey.SEARCH_PLACEHOLDER.label(locale)) }
                )
                TextButton(onClick = { keyboard?.hide(); showAreaSearch = true }, modifier = Modifier.testTag("area-open")) {
                    Text(localizedString(R.string.area_search, locale))
                }
            }
            if (location != null && locationSource != null) LocationActions(location, locationSource, locale)
            CategoryFilters(locale, state.selectedCategories, appState::toggleCategory, horizontal = true)
            if (showMap) {
                mapState.SaveableStateProvider("discovery-map") {
                    mapRenderer.Render(
                        markers = state.centers.toMapMarkers(locale), selectedCenterId = state.selectedCenter?.id,
                        locale = locale, onCenterSelected = { id -> selectCenter(id); scrollTarget = id },
                        modifier = Modifier.fillMaxWidth().height(mapHeight), origin = state.discoveryArea?.coordinates,
                        originLabel = if (hasDeviceOrigin) localizedString(R.string.location_current, locale) else state.discoveryArea?.label
                    )
                }
            }
            LazyColumn(state = listState, modifier = Modifier.weight(1f).testTag("discovery-scroll"),
                contentPadding = PaddingValues(vertical = GenSpace.xs), verticalArrangement = Arrangement.spacedBy(GenSpace.md)) {
                item {
                    Column {
                        state.discoveryArea?.let { area ->
                            Text(if (hasDeviceOrigin) localizedString(R.string.location_scope, locale) else localizedString(R.string.area_scope, locale) + " " + area.label,
                                modifier = Modifier.testTag("discovery-origin-label"), style = MaterialTheme.typography.bodySmall)
                            if (hasDeviceOrigin && deviceLocation.approximate) Text(localizedString(R.string.location_approximate, locale), style = MaterialTheme.typography.bodySmall)
                            TextButton(onClick = { location?.useManualOrigin(); appState.setDiscoveryArea(null) }, modifier = Modifier.testTag("area-clear")) { Text(localizedString(R.string.area_clear, locale)) }
                        }
                        Text(TextKey.NEARBY_CENTERS.label(locale), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        SyncStatus(state, appState)
                    }
                }
                if (state.centers.isEmpty()) item { EmptyMessage(TextKey.EMPTY_CENTERS.label(locale)) }
                items(state.centers, key = { it.id }) { center ->
                    val selected = state.selectedCenter?.id == center.id
                    Surface(modifier = Modifier.fillMaxWidth(), border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                        shape = RoundedCornerShape(GenRadius.card)) {
                        Column {
                            Column(Modifier.fillMaxWidth().clickable(role = Role.Button) { selectCenter(center.id) }
                                .testTag("center-" + center.id).semantics { this.selected = selected }.padding(GenSpace.md)) {
                                CenterIdentity(locale, center)
                                state.discoveryArea?.let { area ->
                                    Text(localizedString(if (hasDeviceOrigin) R.string.location_distance else R.string.area_distance, locale, center.coordinates.distanceMetersTo(area.coordinates) / 1000), style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            TextButton(onClick = { selectCenter(center.id, details = true) }, modifier = Modifier.padding(horizontal = GenSpace.md).testTag("details-${center.id}")) {
                                Text(localizedString(R.string.view_details, locale))
                            }
                        }
                    }
                }
                item { Text(localizedString(if (state.backendConfigured) R.string.sync_cached else R.string.demo_catalog, locale), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
    if (showDetail) Dialog(onDismissRequest = { showDetail = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize()) {
            Column {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { showDetail = false }, modifier = Modifier.testTag("detail-back")) { Text(localizedString(R.string.offer_cancel, locale)) }
                    BrandWordmark()
                }
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).testTag("detail-scroll")) {
                    state.selectedCenter?.let { center ->
                        CenterDetail(locale, center, state.currentNeeds,
                            state.visitSignalCenterId == center.id || center.id in state.visitedCenterIds, state.lastRejection, !state.canRecordContributions, state.backendConfigured, state.features.showNeedProgress, state.profile.isRegistered,
                            { appState.signalVisit(center.id) },
                            { id -> state.currentNeeds.firstOrNull { it.id == id }?.let { offer = it to HelpMethod.GOODS } },
                            { id -> state.currentNeeds.firstOrNull { it.id == id }?.let { offer = it to HelpMethod.TIME } })
                    } ?: EmptyMessage(TextKey.EMPTY_CENTERS.label(locale))
                }
            }
        }
    }
}

@Composable
private fun CategoryFilters(locale: SupportedLocale, selectedCategories: Set<CenterCategory>, onToggle: (CenterCategory) -> Unit, horizontal: Boolean = false) {
    val chips: @Composable () -> Unit = {
        CenterCategory.entries.forEach { category ->
            FilterChip(
                selected = category in selectedCategories, onClick = { onToggle(category) },
                modifier = Modifier.heightIn(min = GenSize.androidTouch).testTag("category-${category.code}"),
                label = { Text(category.label(locale)) },
                leadingIcon = if (category in selectedCategories) ({ Text("✓") }) else null
            )
        }
    }
    if (LocalDensity.current.fontScale > 1.4f && !horizontal) {
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
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GenSpace.xs)) {
                Text(center.name.resolve(locale), Modifier.weight(1f, fill = false), fontWeight = FontWeight.SemiBold)
                VerifiedCenterBadge(locale, center.verificationStatus)
            }
            Text(center.address.resolve(locale), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun VerifiedCenterBadge(locale: SupportedLocale, status: VerificationStatus) {
    if (status == VerificationStatus.OFFICIALLY_VERIFIED) {
        val label = localizedString(R.string.verification_officially_verified, locale)
        AppIcon(R.drawable.ic_verified, Modifier.semantics { contentDescription = label }, tint = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun CenterDetail(
    locale: SupportedLocale, center: KindnessCenter, needs: List<CenterNeed>,
    visitRecorded: Boolean, rejection: RejectionReason?, remoteAccount: Boolean, backendConfigured: Boolean, showProgress: Boolean, registered: Boolean, onVisitSignal: () -> Unit,
    onOfferGoods: (String) -> Unit, onOfferTime: (String) -> Unit
) {
    Column(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(GenSpace.lg).testTag("detail-" + center.id),
        verticalArrangement = Arrangement.spacedBy(GenSpace.lg)
    ) {
        CategoryBadge(center.categories.firstOrNull(), 52.dp)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GenSpace.xs)) {
            Text(center.name.resolve(locale), Modifier.weight(1f, fill = false), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            VerifiedCenterBadge(locale, center.verificationStatus)
        }
        Text(center.categories.map { it.label(locale) }.joinToString(" · "), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(center.address.resolve(locale), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        CenterMapActions(center, locale)
        Text(center.description.resolve(locale), style = MaterialTheme.typography.bodyMedium)
        ContactActions(center.contactInfo, locale)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Text(TextKey.CURRENT_NEEDS.label(locale), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (needs.isEmpty()) EmptyMessage(TextKey.NO_CURRENT_NEEDS.label(locale))
        needs.forEach { need ->
            NeedRow(locale, need, remoteAccount, showProgress, onOfferGoods, onOfferTime)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
        if (remoteAccount || rejection == RejectionReason.REGISTRATION_REQUIRED) {
            Text(if (registered) localizedString(R.string.feature_unavailable, locale) else TextKey.REGISTRATION_REQUIRED.label(locale), color = MaterialTheme.colorScheme.error)
        }
        OutlinedButton(onClick = onVisitSignal, enabled = !visitRecorded, modifier = Modifier.fillMaxWidth()) {
            Text(TextKey.VISIT_SIGNAL.label(locale))
        }
        if (visitRecorded) Text(if (backendConfigured) localizedString(R.string.visit_saved, locale) else TextKey.VISIT_SIGNAL_SUBMITTED.label(locale), style = MaterialTheme.typography.bodySmall)
        Text(TextKey.OFFICIAL_VERIFICATION_BOUNDARY.label(locale), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun NeedRow(locale: SupportedLocale, need: CenterNeed, remoteAccount: Boolean, showProgress: Boolean, onOfferGoods: (String) -> Unit, onOfferTime: (String) -> Unit) {
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
            if (showProgress) NeedProgress(need, locale)
            Text(need.helpMethods.map { it.label(locale) }.joinToString(" · "), style = MaterialTheme.typography.bodySmall)
            if (HelpMethod.MONEY in need.helpMethods) Text(TextKey.MONEY_BOUNDARY.label(locale), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(GenSpace.sm)) {
                if (HelpMethod.GOODS in need.helpMethods) {
                    Button(onClick = { onOfferGoods(need.id) }, enabled = !remoteAccount && need.status.isOpen && need.quantityRemaining != 0) { Text(localizedString(R.string.offer_goods_action, locale)) }
                }
                if (HelpMethod.TIME in need.helpMethods) {
                    Button(onClick = { onOfferTime(need.id) }, enabled = !remoteAccount && need.status.isOpen) { Text(TextKey.OFFER_TIME.label(locale)) }
                }
            }
        }
    }
}

@Composable
private fun ProfileScreen(state: GenerosityUiState, appState: GenerosityAppState, onSignIn: () -> Unit, onSignOut: () -> Unit, onCreate: () -> Unit, authBusy: Boolean, authFailed: Boolean, section: String = "all") {
    val locale = state.locale
    var personalMethod by remember { mutableStateOf<HelpMethod?>(null) }
    personalMethod?.let { OfferDialog(state, appState, null, it) { personalMethod = null } }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(GenSpace.lg), verticalArrangement = Arrangement.spacedBy(GenSpace.xl)) {
        if (section in listOf("all", "profile")) {
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
        }
        SyncStatus(state, appState)
        if (section == "all" && state.canRecordContributions) {
            Text(localizedString(R.string.personal_add, locale), style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(GenSpace.sm)) {
                Button(onClick = { personalMethod = HelpMethod.GOODS }) { Text(localizedString(R.string.offer_goods_action, locale)) }
                Button(onClick = { personalMethod = HelpMethod.TIME }) { Text(TextKey.OFFER_TIME.label(locale)) }
            }
        }
        if (section in listOf("all", "impact")) {
        Text(TextKey.IMPACT_TITLE.label(locale), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(GenSpace.md), verticalArrangement = Arrangement.spacedBy(GenSpace.md)) {
            Metric(localizedString(R.string.centers_supported, locale), if (state.profile.isRegistered) localizedNumber(state.impact.centersSupported, locale) else "—")
            Metric(localizedString(R.string.needs_supported, locale), if (state.profile.isRegistered) localizedNumber(state.impact.needsSupported, locale) else "—")
        }
        Text(localizedString(R.string.impact_basis, locale), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (section in listOf("all", "history")) {
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
    val context = LocalContext.current
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
                Text(localizedString(R.string.push_consent, locale), style = MaterialTheme.typography.bodySmall)
                NotificationToggle(TextKey.URGENT_NOTIFICATIONS.label(locale), preferences.urgentNeedsEnabled) { appState.setPreferences(preferences.copy(urgentNeedsEnabled = it)) }
                NotificationToggle(localizedString(R.string.push_all_categories, locale), preferences.allCategoryInterests) { appState.setPreferences(preferences.copy(allCategoryInterests = it)) }
                NotificationToggle(TextKey.REMINDER_NOTIFICATIONS.label(locale), preferences.remindersEnabled) { appState.setPreferences(preferences.copy(remindersEnabled = it)) }
                Text(org.generosity.usecase.NearbyAlertPolicy().settingsNotice().resolve(locale), style = MaterialTheme.typography.bodySmall)
            }
        }
        Text(localizedString(R.string.notifications_pending, locale), style = MaterialTheme.typography.bodySmall)
        if (state.syncError != null) Text(localizedString(R.string.sync_failed, locale), color = MaterialTheme.colorScheme.error)
        Text(localizedString(if (state.pushSyncError) R.string.push_failed else if (state.pushConfigured) R.string.push_ready else R.string.push_unavailable, locale), style = MaterialTheme.typography.bodySmall)
        PlatformPermissionActions(locale) { AndroidPush.update(context, state.remoteAccount && preferences.urgentNeedsEnabled, appState) }
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
