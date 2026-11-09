import SwiftUI
import MapKit
import SharedBridge
import Shared

struct ContentView: View {
    private static let usesNewDesign = Bundle.main.object(forInfoDictionaryKey: "GenerosityChannel") as? String == "preview"
    private static var demoCatalog: Bool {
        #if DEBUG
        return ProcessInfo.processInfo.arguments.contains("--demo-catalog") || Self.previewHarness
        #else
        return false
        #endif
    }
    private static var previewHarness: Bool {
        #if DEBUG
        return ProcessInfo.processInfo.arguments.contains("--native-preview")
        #else
        return false
        #endif
    }
    private static var connectedHarness: Bool {
        #if DEBUG
        return ProcessInfo.processInfo.arguments.contains("--connected-design")
        #else
        return false
        #endif
    }
    @State private var showPreview = false
    @StateObject private var store: GenerosityStore
    @StateObject private var account: AccountSession
    @StateObject private var permissions = PermissionService()
    @State private var hasDeviceOrigin = false
    @ObservedObject private var push = IOSPush.shared
    @State private var tab = 0
    @State private var showInbox = false
    init() {
        let account = AccountSession()
        let connection = IOSConnection()
        connection.credential = account.credentialFor
        _account = StateObject(wrappedValue: account)
        _store = StateObject(wrappedValue: GenerosityStore(connection: Self.demoCatalog ? nil : connection))
    }
    @Environment(\.scenePhase) private var scenePhase
    @AppStorage("generosity.entered") private var entered = false
    private var locale: AppLocale { AppLocale(rawValue: store.localeCode) ?? .fallback }

    var body: some View {
        Group {
        if Self.previewHarness {
            NativePreview(groups: PreviewSession.companion.groupNames, language: locale.rawValue)
        } else if Self.connectedHarness {
            connectedPreview(groups: PreviewSession.companion.groupNames)
        } else if !entered && !store.state.profile.isRegistered {
            WelcomeView(locale: locale, account: account, store: store, onGuest: { entered = true })
        } else if Self.usesNewDesign {
            if store.state.features.previewGroups.isEmpty {
                VStack(spacing: GenSpace.lg) {
                    BrandWordmark()
                    if store.state.syncing {
                        ProgressView(L10n.resource("sync_busy", locale: locale))
                    } else {
                        Text(L10n.resource("design_unavailable", locale: locale))
                            .accessibilityIdentifier("preview-unavailable")
                        Button(L10n.resource("retry", locale: locale)) { store.onResume() }
                            .frame(minHeight: GenSize.iosTouch)
                        if !store.state.profile.isRegistered {
                            Button(L10n.resource("auth_sign_in", locale: locale)) { account.signIn(store: store) }
                                .frame(minHeight: GenSize.iosTouch).disabled(account.busy)
                            if account.failed { Text(L10n.resource("auth_failed", locale: locale)) }
                        }
                    }
                }.padding(GenSpace.lg).frame(maxWidth: .infinity, maxHeight: .infinity)
                    .background(GenPalette.background)
            } else {
                connectedPreview(groups: store.state.features.previewGroups)
            }
        } else {
        TabView(selection: $tab) {
            discovery(showMap: true)
                .tabItem { Label(L10n.text(.tabMap, locale: locale), systemImage: "map") }
                .tag(0)
            discovery(showMap: false)
                .tabItem { Label(L10n.text(.tabList, locale: locale), systemImage: "list.bullet") }
                .tag(1)
            ProfileView(locale: locale, state: store.state, account: account, store: store)
                .tabItem { Label(L10n.text(.tabProfile, locale: locale), systemImage: "person") }
                .tag(2)
            SettingsView(
                store: store, locale: locale, saveFailed: store.state.syncError == "save_failed", permissions: permissions, languageMode: Binding(get: { store.languageMode }, set: store.setLocale),
                nearbyNotifications: Binding(get: { store.state.preferences.nearbyCentersEnabled }, set: { store.setPreferences(nearby: $0, urgent: store.state.preferences.urgentNeedsEnabled, reminders: store.state.preferences.remindersEnabled) }),
                urgentNotifications: Binding(get: { store.state.preferences.urgentNeedsEnabled }, set: { store.setPreferences(nearby: store.state.preferences.nearbyCentersEnabled, urgent: $0, reminders: store.state.preferences.remindersEnabled) }),
                reminderNotifications: Binding(get: { store.state.preferences.remindersEnabled }, set: { store.setPreferences(nearby: store.state.preferences.nearbyCentersEnabled, urgent: store.state.preferences.urgentNeedsEnabled, reminders: $0) })
            )
            .tabItem { Label(L10n.text(.tabSettings, locale: locale), systemImage: "slider.horizontal.3") }
                .tag(3)
        }
        }
        }
        .fullScreenCover(isPresented: $showPreview) {
            connectedPreview(groups: store.state.features.previewGroups, onClose: { showPreview = false })
                .id(store.state.profile.id)
        }
        .onChange(of: store.state.profile.id) { _ in showPreview = false }
        .onChange(of: store.state.features.previewGroups) { if $0.isEmpty { showPreview = false } }
        .sheet(isPresented: $showInbox) {
            NavigationStack {
                List {
                    Text(L10n.resource("inbox_retention", locale: locale)).font(GenTypography.font(.footnote))
                    if store.state.notices.isEmpty { Text(L10n.resource("inbox_empty", locale: locale)) }
                    ForEach(store.state.notices, id: \.id) { notice in
                        Button { store.openNotice(notice.id) } label: {
                            Text((notice.read ? "" : "• ") + notice.title.resolve(locale))
                                .fontWeight(notice.read ? .regular : .bold)
                        }
                    }
                }.navigationTitle(L10n.resource("inbox_title", locale: locale))
                .toolbar { Button(L10n.resource("inbox_close", locale: locale)) { showInbox = false } }
            }
        }
        .safeAreaInset(edge: .top) {
            if !Self.usesNewDesign && !Self.previewHarness && !Self.connectedHarness && !store.state.features.previewGroups.isEmpty {
                Button(locale.rawValue == "es" ? "Explorar diseño de prueba" : "Explore design preview") { showPreview = true }
                    .frame(minHeight: 44).accessibilityIdentifier("open-native-preview")
            }
            if store.state.pushBanner != nil {
                HStack {
                    Button(L10n.resource("push_new", locale: locale)) { showInbox = true; store.dismissPushBanner() }
                    Button(L10n.resource("inbox_close", locale: locale)) { store.dismissPushBanner() }
                }.padding(GenSpace.sm).background(GenPalette.surface)
            }
        }
        .onChange(of: store.state.noticeDestinationId) { id in if id != nil && !Self.usesNewDesign && !showPreview && !Self.connectedHarness { showInbox = false; tab = 1 } }
        .onChange(of: store.state.profile.id) { _ in updatePush(); consumePush() }
        .onChange(of: store.state.preferences.urgentNeedsEnabled) { _ in updatePush() }
        .onChange(of: permissions.notificationsAuthorized) { _ in updatePush() }
        .onChange(of: push.token) { _ in updatePush() }
        .onChange(of: push.signal) { _ in consumePush() }
        .onChange(of: store.state.profile.isRegistered) { registered in entered = registered }
        .tint(GenPalette.accent)
        .font(GenTypography.font(.body))
        .environment(\.locale, Locale(identifier: locale.rawValue))
        .environment(\.layoutDirection, Locale.Language(identifier: locale.rawValue).characterDirection == .rightToLeft ? .rightToLeft : .leftToRight)
        .onChange(of: scenePhase) { phase in
            if phase == .active {
                store.onResume()
                updatePush()
                if entered && !Self.demoCatalog { permissions.resumeLocation() }
                Task { if !Self.demoCatalog { await account.restore(store: store) } }
            } else if phase == .background {
                permissions.suspendLocation()
            }
        }
        .onReceive(permissions.$currentLocation) { location in
            if let location {
                hasDeviceOrigin = true
                store.setDiscoveryArea(DiscoveryArea(label: L10n.resource("location_current", locale: locale),
                    coordinates: Coordinates(latitude: location.coordinate.latitude, longitude: location.coordinate.longitude)), preserveSelection: true)
            } else if hasDeviceOrigin {
                hasDeviceOrigin = false
                store.setDiscoveryArea(nil, preserveSelection: true)
            }
        }
        .task { if !Self.demoCatalog { await account.restore(store: store); updatePush(); consumePush() } }
        .onReceive(NotificationCenter.default.publisher(for: NSLocale.currentLocaleDidChangeNotification)) { _ in
            store.refreshDeviceLanguages()
        }
    }

    private func connectedPreview(groups: [String], onClose: (() -> Void)? = nil) -> some View {
        NativePreview(groups: groups, language: locale.rawValue, connectedContent: { route, navigate in
            AnyView(ConnectedDesignScreen(route: route, navigate: navigate, groups: groups, store: store, account: account,
                permissions: permissions, hasDeviceOrigin: hasDeviceOrigin, onSelectArea: { area in
                    hasDeviceOrigin = false; permissions.useManualArea(); store.setDiscoveryArea(area)
                }).id(route))
        }, onClose: onClose).id(store.state.profile.id)
    }

    private func updatePush() { if !Self.demoCatalog { Task { await push.update(store: store) } } }
    private func consumePush() {
        guard let signal = push.signal, store.state.profile.id != nil else { return }
        store.receivePush(accountId: signal.accountId, noticeId: signal.noticeId, open: signal.open)
        push.signal = nil
    }
    private func discovery(showMap: Bool) -> some View {
        NavigationStack {
            DiscoveryView(locale: locale, store: store, permissions: permissions, hasDeviceOrigin: hasDeviceOrigin,
                          showMap: showMap, handlesNotices: !showPreview && !Self.connectedHarness, onSelectArea: { area in
                hasDeviceOrigin = false
                permissions.useManualArea()
                store.setDiscoveryArea(area)
            })
                .onAppear { if !Self.demoCatalog { permissions.resumeLocation() } }
                .navigationBarTitleDisplayMode(.inline)
                 .toolbar {
                    ToolbarItem(placement: .principal) { BrandWordmark() }
                    ToolbarItem(placement: .navigationBarTrailing) {
                        if store.state.remoteAccount {
                            Button { showInbox = true; store.onResume() } label: {
                                Label(L10n.resource("inbox_title", locale: locale), systemImage: store.state.notices.contains { !$0.read } ? "bell.badge" : "bell")
                            }.accessibilityIdentifier("notification-inbox")
                        }
                    }
                }
                .toolbarBackground(GenPalette.surface, for: .navigationBar)
                .toolbarBackground(.visible, for: .navigationBar)
        }
    }
}


private struct ConnectedDesignScreen: View {
    let route: String
    let navigate: (String) -> Void
    let groups: [String]
    private var allowContributions: Bool { groups.contains("contributions") }
    @ObservedObject var store: GenerosityStore
    @ObservedObject var account: AccountSession
    @ObservedObject var permissions: PermissionService
    let hasDeviceOrigin: Bool
    let onSelectArea: (DiscoveryArea?) -> Void
    @State private var areaSearch = false
    @State private var personalMethod = "goods"
    @State private var offering = false
    @FocusState private var searching: Bool
    @Environment(\.dynamicTypeSize) private var textSize
    private var locale: AppLocale { AppLocale(rawValue: store.localeCode) ?? .fallback }
    private var state: GenerosityUiState { store.state }

    var body: some View {
        content
            .sheet(isPresented: $areaSearch) { AreaSearchView(locale: locale, onSelect: { area in store.setSearchText(""); onSelectArea(area); areaSearch = false }, initialQuery: state.searchText) }
            .sheet(isPresented: $offering) { OfferForm(locale: locale, store: store, need: nil, method: personalMethod) }
            .onChange(of: state.noticeDestinationId) { id in
                if id != nil { navigate("center"); store.clearNoticeDestination() }
            }
    }
    @ViewBuilder private var content: some View {
        switch route {
        case "map":
            if textSize.isAccessibilitySize { ScrollView { map.frame(height: 900) } }
            else { map }
        case "list":
            SyncStatusView(locale: locale, store: store)
            if state.centers.isEmpty { Text(L10n.text(.emptyCenters, locale: locale)) }
            ForEach(state.centers, id: \.id) { center in
                Button { store.selectCenter(center.id); navigate("center") } label: { CenterIdentity(locale: locale, center: center) }
                    .frame(minHeight: GenSize.iosTouch).accessibilityIdentifier("connected-center-" + center.id)
            }
        case "filters":
            CategoryFilterView(locale: locale, selectedCategories: state.selectedCategories, onToggle: { store.toggleCategory($0.code) })
            Button(L10n.resource("area_search", locale: locale)) { areaSearch = true }.frame(minHeight: GenSize.iosTouch)
            locationControls
            if state.discoveryArea != nil { Button(L10n.resource("area_clear", locale: locale)) { onSelectArea(nil) }.frame(minHeight: GenSize.iosTouch) }
            SyncStatusView(locale: locale, store: store)
        case "profile", "history", "impact":
            VStack {
                if route == "profile" {
                    HStack {
                        if allowContributions { Button(L10n.resource("history", locale: locale)) { navigate("history") } }
                        Button(L10n.text(.tabSettings, locale: locale)) { navigate("settings") }
                    }.frame(minHeight: GenSize.iosTouch)
                }
                ProfileView(locale: locale, state: state, account: account, store: store, section: route)
            }
        case "settings":
            SettingsView(store: store, locale: locale, saveFailed: state.syncError != nil, permissions: permissions,
                languageMode: Binding(get: { store.languageMode }, set: store.setLocale),
                nearbyNotifications: Binding(get: { state.preferences.nearbyCentersEnabled }, set: { store.setPreferences(nearby: $0, urgent: state.preferences.urgentNeedsEnabled, reminders: state.preferences.remindersEnabled) }),
                urgentNotifications: Binding(get: { state.preferences.urgentNeedsEnabled }, set: { store.setPreferences(nearby: state.preferences.nearbyCentersEnabled, urgent: $0, reminders: state.preferences.remindersEnabled) }),
                reminderNotifications: Binding(get: { state.preferences.remindersEnabled }, set: { store.setPreferences(nearby: state.preferences.nearbyCentersEnabled, urgent: state.preferences.urgentNeedsEnabled, reminders: $0) }), embedded: true)
        case "welcome":
            WelcomeView(locale: locale, account: account, store: store, onGuest: { navigate("map") })
        default:
            ScrollView {
                LazyVStack(alignment: .leading, spacing: GenSpace.md) {
                    SyncStatusView(locale: locale, store: store)
                    switch route {
                    case "center", "offer":
                        if let center = state.selectedCenter {
                            CenterDetail(locale: locale, center: center, store: store, currentNeeds: state.currentNeeds,
                                visitNoted: state.visitSignalCenterId == center.id || state.visitedCenterIds.contains(center.id),
                                onVisit: { store.signalVisit(center.id) }, allowContributions: allowContributions)
                        } else { Text(L10n.text(.emptyCenters, locale: locale)) }
                    case "needs":
                        if state.discoveredNeeds.isEmpty { Text(L10n.text(.noCurrentNeeds, locale: locale)) }
                        ForEach(state.discoveredNeeds, id: \.id) { need in
                            if let center = state.centers.first(where: { $0.id == need.centerId }) {
                                Button(center.name.resolve(locale)) { store.selectCenter(center.id); navigate("center") }.frame(minHeight: GenSize.iosTouch)
                            }
                            NeedView(locale: locale, need: need, store: store, allowContributions: allowContributions)
                            Divider()
                        }
                    case "help", "personal":
                        if route == "help" && groups.contains("discovery") { Button(L10n.text(.currentNeeds, locale: locale)) { navigate("needs") }.buttonStyle(.borderedProminent) }
                        Text(L10n.resource("personal_add", locale: locale)).font(GenTypography.font(.headline))
                        Text(L10n.resource("personal_notice", locale: locale))
                        ForEach(["goods", "time"], id: \.self) { method in
                            Button(L10n.resource(method == "goods" ? "offer_goods_action" : "offer_time", locale: locale)) { personalMethod = method; offering = true }
                                .buttonStyle(.bordered).disabled(!state.canRecordContributions).frame(minHeight: GenSize.iosTouch)
                        }
                        if !state.canRecordContributions { Text(L10n.resource(state.profile.isRegistered ? "feature_unavailable" : "registration_required", locale: locale)) }
                    case "inbox":
                        Text(L10n.resource("inbox_retention", locale: locale))
                        if !state.profile.isRegistered { Button(L10n.resource("auth_sign_in", locale: locale)) { account.signIn(store: store) }.frame(minHeight: GenSize.iosTouch) }
                        if state.notices.isEmpty { Text(L10n.resource("inbox_empty", locale: locale)) }
                        ForEach(state.notices, id: \.id) { notice in
                            Button { store.openNotice(notice.id) } label: { Text((notice.read ? "" : "• ") + notice.title.resolve(locale)).fontWeight(notice.read ? .regular : .bold) }.frame(minHeight: GenSize.iosTouch)
                        }
                    default: EmptyView()
                    }
                }.padding(GenSpace.md)
            }
        }
    }
    private var mapCenter: KindnessCenter? { state.selectedCenter ?? state.centers.first }
    private var map: some View {
        ZStack {
            CenterMapView(locale: locale, centers: state.centers, selectedCenterId: mapCenter?.id,
                onSelect: { store.selectCenter($0) }, origin: state.discoveryArea?.coordinates, deviceOrigin: hasDeviceOrigin)
            VStack(spacing: GenSpace.xs) {
                searchBar
                CategoryFilterView(locale: locale, selectedCategories: state.selectedCategories, onToggle: { store.toggleCategory($0.code) }, horizontal: true)
                    .background(.regularMaterial, in: RoundedRectangle(cornerRadius: GenRadius.card))
                Spacer(minLength: GenSpace.lg)
                VStack(alignment: .leading, spacing: GenSpace.sm) {
                    if state.syncing { ProgressView() }
                    if state.syncError != nil { Text(L10n.resource("sync_failed", locale: locale)).font(GenTypography.font(.footnote)).foregroundStyle(GenPalette.error) }
                    if let message = permissions.locationMessage { Text(L10n.resource(message, locale: locale)).font(GenTypography.font(.footnote)) }
                    if permissions.locationDenied { locationControls }
                    if let area = state.discoveryArea { Text(hasDeviceOrigin ? L10n.resource("location_scope", locale: locale) : area.label).font(GenTypography.font(.caption)) }
                    if let center = mapCenter {
                        CenterIdentity(locale: locale, center: center)
                        Button(L10n.resource("view_details", locale: locale)) { searching = false; store.selectCenter(center.id); navigate("center") }
                            .buttonStyle(.borderedProminent).accessibilityIdentifier("connected-details")
                    } else { Text(L10n.text(.emptyCenters, locale: locale)) }
                }.padding(GenSpace.md).frame(maxWidth: .infinity, alignment: .leading)
                    .background(GenPalette.surface, in: RoundedRectangle(cornerRadius: GenRadius.card))
            }.padding(GenSpace.sm).padding(.bottom, 28) // Keep MapKit attribution visible.
        }.onAppear { permissions.resumeLocation() }
    }
    private var searchContent: some View {
        HStack {
            Button { searching = false; areaSearch = true } label: { Image(systemName: "magnifyingglass").frame(minWidth: 44, minHeight: 44) }
                .accessibilityLabel(L10n.resource("area_search", locale: locale)).accessibilityIdentifier("connected-area")
            TextField("", text: Binding(get: { state.searchText }, set: store.setSearchText), prompt: Text(L10n.text(.searchPlaceholder, locale: locale)).foregroundColor(GenPalette.secondary))
                .focused($searching).submitLabel(.search).onSubmit { searching = false; areaSearch = true }.accessibilityIdentifier("connected-query")
            Button { searching = false; permissions.requestCurrentLocation() } label: { Image(systemName: "location").frame(minWidth: 44, minHeight: 44) }
                .accessibilityLabel(L10n.resource("location_use", locale: locale)).accessibilityIdentifier("location-use").disabled(permissions.locating)
        }.padding(GenSpace.sm)
    }
    @ViewBuilder private var searchBar: some View {
        if #available(iOS 26.0, *) { searchContent.glassEffect(.regular, in: Capsule()) }
        else { searchContent.background(.regularMaterial, in: Capsule()) }
    }
    private var locationControls: some View {
        VStack(alignment: .leading) {
            Button(L10n.resource("location_use", locale: locale)) { permissions.requestCurrentLocation() }.disabled(permissions.locating).frame(minHeight: GenSize.iosTouch)
            if permissions.locationDenied { Button(L10n.resource("location_settings", locale: locale)) { if let url = URL(string: UIApplication.openSettingsURLString) { UIApplication.shared.open(url) } }.frame(minHeight: GenSize.iosTouch) }
        }
    }
}

private struct DiscoveryView: View {
    let locale: AppLocale
    @ObservedObject var store: GenerosityStore
    @ObservedObject var permissions: PermissionService
    let hasDeviceOrigin: Bool
    let showMap: Bool
    var handlesNotices = true
    let onSelectArea: (DiscoveryArea?) -> Void
    @State private var showDetail = false
    @State private var showAreaSearch = false
    @State private var showLocationSettings = false
    @State private var scrollTarget: String?
    @FocusState private var searching: Bool

    private func select(_ id: String, details: Bool = false) {
        searching = false
        store.selectCenter(id)
        showDetail = details || !showMap
    }

    var body: some View {
        GeometryReader { geometry in
            VStack(spacing: GenSpace.sm) {
                HStack(spacing: GenSpace.sm) {
                    Image(systemName: "magnifyingglass").foregroundStyle(GenPalette.secondary).accessibilityHidden(true)
                    TextField(L10n.text(.searchPlaceholder, locale: locale), text: Binding(
                        get: { store.state.searchText }, set: store.setSearchText
                    ))
                    .focused($searching)
                    .accessibilityIdentifier("discovery-search")
                    .submitLabel(.search)
                    .onSubmit { searching = false }
                    Button { searching = false; showAreaSearch = true } label: {
                        Label(L10n.resource("area_search", locale: locale), systemImage: "map")
                            .font(GenTypography.font(.caption)).frame(minHeight: GenSize.iosTouch)
                    }
                    .accessibilityLabel(L10n.resource("area_search", locale: locale))
                    .accessibilityIdentifier("area-open")
                }
                .padding(GenSpace.md).frame(minHeight: GenSize.iosTouch)
                .background(GenPalette.surface, in: RoundedRectangle(cornerRadius: GenRadius.card))
                .overlay(RoundedRectangle(cornerRadius: GenRadius.card).stroke(GenPalette.line))
                HStack {
                    Button {
                        searching = false
                        permissions.requestCurrentLocation()
                        showLocationSettings = permissions.locationDenied
                    } label: {
                        Label(L10n.resource("location_use", locale: locale), systemImage: "location.fill")
                            .frame(minHeight: GenSize.iosTouch)
                    }
                    .disabled(permissions.locating).accessibilityIdentifier("location-use")
                    Spacer(minLength: GenSpace.sm)
                    if permissions.locationDenied {
                        Button(L10n.resource("location_settings", locale: locale)) {
                            if let url = URL(string: UIApplication.openSettingsURLString) { UIApplication.shared.open(url) }
                        }.frame(minHeight: GenSize.iosTouch).accessibilityIdentifier("location-settings")
                    }
                }.font(GenTypography.font(.subheadline))
                CategoryFilterView(locale: locale, selectedCategories: store.state.selectedCategories,
                                   onToggle: { store.toggleCategory($0.code) }, horizontal: true)
                if showMap {
                    CenterMapView(locale: locale, centers: store.state.centers,
                                  selectedCenterId: store.state.selectedCenter?.id,
                                  onSelect: { id in select(id); scrollTarget = id }, origin: store.state.discoveryArea?.coordinates,
                                  deviceOrigin: hasDeviceOrigin)
                        .frame(height: max(100, min(300, geometry.size.height * 0.4)))
                        .clipShape(RoundedRectangle(cornerRadius: GenRadius.card))
                }
                ScrollViewReader { proxy in
                    ScrollView {
                        LazyVStack(alignment: .leading, spacing: GenSpace.md) {
                            if permissions.locating { ProgressView(L10n.resource("location_loading", locale: locale)) }
                            if let message = permissions.locationMessage {
                                Text(L10n.resource(message, locale: locale)).accessibilityIdentifier("location-status")
                            }
                            if let area = store.state.discoveryArea {
                                Text(hasDeviceOrigin ? L10n.resource("location_scope", locale: locale) : L10n.resource("area_scope", locale: locale) + " " + area.label)
                                    .font(GenTypography.font(.caption))
                                    .accessibilityIdentifier("discovery-origin-label")
                                if hasDeviceOrigin && permissions.approximateLocation {
                                    Text(L10n.resource("location_approximate", locale: locale)).font(GenTypography.font(.caption))
                                }
                                Button(L10n.resource("area_clear", locale: locale)) { onSelectArea(nil) }
                                    .frame(minHeight: GenSize.iosTouch).accessibilityIdentifier("area-clear")
                            }
                            Text(L10n.text(.nearbyCenters, locale: locale)).font(GenTypography.font(.headline))
                            SyncStatusView(locale: locale, store: store)
                            if store.state.centers.isEmpty {
                                Text(L10n.text(.emptyCenters, locale: locale)).foregroundStyle(GenPalette.secondary)
                            }
                            ForEach(store.state.centers, id: \.id) { center in
                                let selected = store.state.selectedCenter?.id == center.id
                                VStack(alignment: .leading, spacing: GenSpace.xs) {
                                    Button { select(center.id) } label: {
                                        VStack(alignment: .leading, spacing: GenSpace.xs) {
                                            CenterIdentity(locale: locale, center: center)
                                            if let area = store.state.discoveryArea {
                                                Text(String(format: L10n.resource(hasDeviceOrigin ? "location_distance" : "area_distance", locale: locale), locale: Locale(identifier: locale.rawValue), area.distanceKilometersTo(coordinates: center.coordinates)))
                                                    .font(GenTypography.font(.caption))
                                            }
                                        }
                                            .padding(GenSpace.md).contentShape(Rectangle())
                                    }
                                    .buttonStyle(.plain)
                                    .accessibilityIdentifier("center-\(center.id)")
                                    .accessibilityAddTraits(selected ? .isSelected : [])
                                    Button(L10n.resource("view_details", locale: locale)) { select(center.id, details: true) }
                                        .accessibilityIdentifier("details-\(center.id)")
                                        .buttonStyle(.bordered).frame(minHeight: GenSize.iosTouch)
                                        .padding([.horizontal, .bottom], GenSpace.md)
                                }
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .background(GenPalette.surface, in: RoundedRectangle(cornerRadius: GenRadius.card))
                                .overlay(RoundedRectangle(cornerRadius: GenRadius.card).stroke(selected ? GenPalette.accent : .clear, lineWidth: 2))
                                .id(center.id)
                            }
                            Text(L10n.resource(store.state.backendConfigured ? "sync_cached" : "demo_catalog", locale: locale))
                                .font(GenTypography.font(.caption)).foregroundStyle(GenPalette.secondary)
                        }.padding(.vertical, GenSpace.xs).id("results-top")
                    }
                    .accessibilityIdentifier("discovery-scroll")
                    .onChange(of: store.state.discoveryArea) { _ in proxy.scrollTo("results-top", anchor: .top) }
                    .onChange(of: store.state.searchText) { _ in proxy.scrollTo("results-top", anchor: .top) }
                    .onChange(of: store.state.selectedCategories) { _ in proxy.scrollTo("results-top", anchor: .top) }
                    .onChange(of: scrollTarget) { target in
                        guard let target else { return }
                        withAnimation { proxy.scrollTo(target, anchor: .top) }
                        scrollTarget = nil
                    }
                }
                .frame(maxHeight: .infinity)
            }
            .padding(.horizontal, GenSpace.lg).padding(.top, GenSpace.sm)
            .background(GenPalette.background).foregroundStyle(GenPalette.text)
        }
        .sheet(isPresented: $showAreaSearch) { AreaSearchView(locale: locale, onSelect: onSelectArea) }
        .alert(L10n.resource("location_use", locale: locale), isPresented: $showLocationSettings) {
            Button(L10n.resource("location_settings", locale: locale)) {
                if let url = URL(string: UIApplication.openSettingsURLString) { UIApplication.shared.open(url) }
            }
            Button(L10n.resource("offer_cancel", locale: locale), role: .cancel) { }
        } message: {
            Text(L10n.resource("location_denied", locale: locale))
        }
        .onChange(of: store.state.noticeDestinationId) { id in
            if id != nil && !showMap && handlesNotices { showDetail = true; store.clearNoticeDestination() }
        }
        .onAppear {
            if store.state.noticeDestinationId != nil && !showMap { showDetail = true; store.clearNoticeDestination() }
        }
        .fullScreenCover(isPresented: $showDetail) {
            NavigationStack {
                ScrollView {
                    if let center = store.state.selectedCenter {
                        CenterDetail(locale: locale, center: center, store: store, currentNeeds: store.state.currentNeeds,
                                     visitNoted: store.state.visitSignalCenterId == center.id || store.state.visitedCenterIds.contains(center.id),
                                     onVisit: { store.signalVisit(center.id) })
                    } else { Text(L10n.text(.emptyCenters, locale: locale)) }
                }
                .accessibilityIdentifier("detail-scroll")
                .background(GenPalette.background).foregroundStyle(GenPalette.text)
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .navigationBarLeading) {
                        Button(L10n.resource("offer_cancel", locale: locale)) { showDetail = false }
                            .accessibilityIdentifier("detail-back")
                    }
                    ToolbarItem(placement: .principal) { BrandWordmark() }
                }
            }
        }
    }
}

private struct CategoryFilterView: View {
    let locale: AppLocale
    let selectedCategories: Set<CenterCategory>
    let onToggle: (CenterCategory) -> Void
    var horizontal = false
    @Environment(\.dynamicTypeSize) private var typeSize

    var body: some View {
        if typeSize.isAccessibilitySize && !horizontal {
            LazyVGrid(columns: [GridItem(.adaptive(minimum: 160), alignment: .leading)], alignment: .leading, spacing: GenSpace.sm) { chips }
        } else {
            ScrollView(.horizontal, showsIndicators: false) { HStack(spacing: GenSpace.sm) { chips } }
                .accessibilityIdentifier("category-filters")
        }
    }
    private var chips: some View {
        ForEach(CenterCategory.entries, id: \.code) { category in
            let selected = selectedCategories.contains(category)
            Button { onToggle(category) } label: {
                HStack(spacing: GenSpace.xs) {
                    if selected { Image(systemName: "checkmark").accessibilityHidden(true) }
                    Text(category.label(locale)).fixedSize(horizontal: false, vertical: true)
                }
                .font(GenTypography.font(.subheadline)).padding(.horizontal, GenSpace.md).frame(minHeight: GenSize.iosTouch)
                .foregroundStyle(selected ? GenPalette.onAccent : GenPalette.text)
                .background(selected ? GenPalette.accent : GenPalette.surface, in: RoundedRectangle(cornerRadius: GenRadius.control))
            }
            .buttonStyle(.plain)
            .accessibilityIdentifier("category-\(category.code)")
            .accessibilityAddTraits(selected ? .isSelected : [])
        }
    }
}

struct CenterMapView: UIViewRepresentable {
    let locale: AppLocale
    let centers: [KindnessCenter]
    let selectedCenterId: String?
    let onSelect: (String) -> Void
    var origin: Coordinates? = nil
    var deviceOrigin = false

    func makeCoordinator() -> Coordinator { Coordinator(self) }

    func makeUIView(context: Context) -> MapCanvas {
        let map = MapCanvas()
        map.delegate = context.coordinator
        map.isRotateEnabled = false
        map.isPitchEnabled = false
        map.accessibilityIdentifier = "center-map"
        return map
    }

    func updateUIView(_ map: MapCanvas, context: Context) { context.coordinator.update(map, owner: self) }

    final class MapCanvas: MKMapView {
        private var targetRect: MKMapRect?
        private var framedSize: CGSize = .zero
        func frameRect(_ region: MKMapRect) {
            targetRect = region
            guard bounds.width > 0, bounds.height > 0 else { return }
            framedSize = bounds.size
            setVisibleMapRect(region, edgePadding: UIEdgeInsets(top: 48, left: 48, bottom: 48, right: 48), animated: false)
        }
        override func layoutSubviews() {
            super.layoutSubviews()
            if let region = targetRect, bounds.size != framedSize, bounds.width > 0, bounds.height > 0 {
                frameRect(region)
            }
        }
    }

    final class Annotation: NSObject, MKAnnotation {
        let id: String
        @objc dynamic var coordinate: CLLocationCoordinate2D
        @objc dynamic var title: String?
        var category: CenterCategory?
        init(_ center: KindnessCenter, locale: AppLocale) {
            id = center.id
            coordinate = center.coordinate
            title = center.name.resolve(locale)
            category = center.categories.sorted { $0.ordinal < $1.ordinal }.first
        }
    }

    final class OriginAnnotation: NSObject, MKAnnotation {
        @objc dynamic var coordinate: CLLocationCoordinate2D
        @objc dynamic var title: String?
        init(_ coordinate: CLLocationCoordinate2D) { self.coordinate = coordinate }
    }

    final class Coordinator: NSObject, MKMapViewDelegate {
        private var owner: CenterMapView
        private var pins: [String: Annotation] = [:]
        private var framedCoordinates: [String: [Double]] = [:]
        private var framedSelection: String?
        private var framedOrigin: Coordinates?
        private var originPin: OriginAnnotation?
        init(_ owner: CenterMapView) { self.owner = owner }

        func update(_ map: MapCanvas, owner: CenterMapView) {
            self.owner = owner
            if let origin = owner.origin {
                let point = CLLocationCoordinate2D(latitude: origin.latitude, longitude: origin.longitude)
                if originPin == nil { originPin = OriginAnnotation(point); map.addAnnotation(originPin!) }
                originPin?.coordinate = point
                originPin?.title = L10n.resource(owner.deviceOrigin ? "location_current" : "area_search", locale: owner.locale)
                if let pin = originPin, let view = map.view(for: pin) as? MKMarkerAnnotationView { configureOrigin(view) }
            } else if let pin = originPin { map.removeAnnotation(pin); originPin = nil }
            let centers = owner.centers.filter { CLLocationCoordinate2DIsValid($0.coordinate) }
            let coordinates = Dictionary(uniqueKeysWithValues: centers.map { ($0.id, [$0.coordinates.latitude, $0.coordinates.longitude]) })
            for id in pins.keys.filter({ coordinates[$0] == nil }) {
                if let pin = pins.removeValue(forKey: id) { map.removeAnnotation(pin) }
            }
            for center in centers {
                if let pin = pins[center.id] {
                    if pin.coordinate.latitude != center.coordinates.latitude || pin.coordinate.longitude != center.coordinates.longitude {
                        pin.coordinate = center.coordinate
                    }
                    pin.title = center.name.resolve(owner.locale)
                    pin.category = center.categories.sorted { $0.ordinal < $1.ordinal }.first
                } else {
                    let pin = Annotation(center, locale: owner.locale)
                    pins[center.id] = pin
                    map.addAnnotation(pin)
                }
            }
            // No two-way camera binding: MapKit must not write an old region over a new selection.
            if coordinates != framedCoordinates || owner.selectedCenterId != framedSelection || owner.origin != framedOrigin {
                let selected = owner.selectedCenterId.flatMap { pins[$0] }
                if let region = discoveryMapRect(coordinates: centers.map(\.coordinate), selected: selected?.coordinate, origin: owner.origin.map { CLLocationCoordinate2D(latitude: $0.latitude, longitude: $0.longitude) }) {
                    map.frameRect(region)
                }
                for annotation in map.selectedAnnotations where (annotation as? Annotation)?.id != selected?.id {
                    map.deselectAnnotation(annotation, animated: false)
                }
                if let selected { map.selectAnnotation(selected, animated: false) }
                framedCoordinates = coordinates
                framedSelection = owner.selectedCenterId
                framedOrigin = owner.origin
            }
            for pin in pins.values {
                if let view = map.view(for: pin) as? MKMarkerAnnotationView { configure(view, pin: pin) }
            }
        }

        private func configure(_ view: MKMarkerAnnotationView, pin: Annotation) {
            let selected = pin.id == owner.selectedCenterId
            view.glyphImage = UIImage(systemName: pin.category?.symbol ?? "mappin")
            view.markerTintColor = UIColor(selected ? GenPalette.accent : (pin.category?.tint ?? GenPalette.secondary))
            view.displayPriority = .required
            view.canShowCallout = false
            view.isAccessibilityElement = true
            view.accessibilityIdentifier = "marker-\(pin.id)"
            view.accessibilityLabel = pin.title
            view.accessibilityTraits = selected ? [.button, .selected] : [.button]
        }

        private func configureOrigin(_ view: MKMarkerAnnotationView) {
            view.glyphImage = UIImage(systemName: owner.deviceOrigin ? "location.fill" : "map")
            view.markerTintColor = owner.deviceOrigin ? .systemBlue : UIColor(GenPalette.accent)
            view.displayPriority = .required
            view.isAccessibilityElement = true
            view.accessibilityIdentifier = "discovery-origin-pin"
            view.accessibilityLabel = originPin?.title
        }

        func mapView(_ mapView: MKMapView, viewFor annotation: MKAnnotation) -> MKAnnotationView? {
            if annotation is OriginAnnotation {
                let view = mapView.dequeueReusableAnnotationView(withIdentifier: "origin") as? MKMarkerAnnotationView
                    ?? MKMarkerAnnotationView(annotation: annotation, reuseIdentifier: "origin")
                view.annotation = annotation
                configureOrigin(view)
                return view
            }
            guard let pin = annotation as? Annotation else { return nil }
            let view = mapView.dequeueReusableAnnotationView(withIdentifier: "center") as? MKMarkerAnnotationView
                ?? MKMarkerAnnotationView(annotation: pin, reuseIdentifier: "center")
            view.annotation = pin
            configure(view, pin: pin)
            return view
        }

        func mapView(_ mapView: MKMapView, didSelect view: MKAnnotationView) {
            guard let pin = view.annotation as? Annotation, pin.id != owner.selectedCenterId else { return }
            owner.onSelect(pin.id)
        }
    }
}

private struct CenterIdentity: View {
    let locale: AppLocale
    let center: KindnessCenter
    var body: some View {
        HStack(alignment: .center, spacing: GenSpace.md) {
            CategoryBadge(category: center.categories.sorted { $0.ordinal < $1.ordinal }.first)
            VStack(alignment: .leading, spacing: GenSpace.xs) {
                HStack(spacing: GenSpace.xs) {
                    Text(center.name.resolve(locale)).font(GenTypography.font(.subheadline).weight(.semibold)).foregroundStyle(GenPalette.text)
                    VerifiedCenterBadge(locale: locale, status: center.verificationStatus)
                }
                Text(center.address.resolve(locale)).font(GenTypography.font(.caption)).foregroundStyle(GenPalette.secondary)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }
}

private struct VerifiedCenterBadge: View {
    let locale: AppLocale
    let status: VerificationStatus
    var body: some View {
        if status == .officiallyVerified {
            Image(systemName: "checkmark.circle.fill")
                .foregroundStyle(GenPalette.accent)
                .accessibilityLabel(L10n.resource("verification_officially_verified", locale: locale))
        }
    }
}

private struct CenterDetail: View {
    let locale: AppLocale
    let center: KindnessCenter
    @ObservedObject var store: GenerosityStore
    let currentNeeds: [CenterNeed]
    let visitNoted: Bool
    let onVisit: () -> Void
    var allowContributions = true
    @Environment(\.openURL) private var openURL
    @State private var contactFailed = false

    var body: some View {
        VStack(alignment: .leading, spacing: GenSpace.lg) {
            CategoryBadge(category: center.categories.sorted { $0.ordinal < $1.ordinal }.first, size: 52)
            HStack(spacing: GenSpace.xs) {
                Text(center.name.resolve(locale)).font(GenTypography.font(.title2).weight(.bold))
                    .accessibilityIdentifier("detail-\(center.id)")
                VerifiedCenterBadge(locale: locale, status: center.verificationStatus)
            }
            Text(center.categories.sorted { $0.ordinal < $1.ordinal }.map { $0.label(locale) }.joined(separator: " · "))
                .font(GenTypography.font(.subheadline)).foregroundStyle(GenPalette.accent)
            Text(center.address.resolve(locale)).font(GenTypography.font(.subheadline)).foregroundStyle(GenPalette.secondary)
            CenterMapActions(locale: locale, center: center).id(center.id)
            Text(center.description_.resolve(locale)).font(GenTypography.font(.body))
            ForEach(center.contactInfo.links(), id: \.kind) { link in
                Button(L10n.resource("contact_" + link.kind, locale: locale)) {
                    if let url = URL(string: link.url) { openURL(url) { accepted in contactFailed = !accepted } }
                }.frame(minHeight: GenSize.iosTouch)
            }
            if contactFailed { Text(L10n.resource("contact_failed", locale: locale)).foregroundStyle(GenPalette.error) }
            Divider()
            Text(L10n.text(.currentNeeds, locale: locale)).font(GenTypography.font(.headline))
            if currentNeeds.isEmpty {
                Text(L10n.text(.noCurrentNeeds, locale: locale)).foregroundStyle(GenPalette.secondary)
            }
            ForEach(currentNeeds, id: \.id) { need in
                NeedView(locale: locale, need: need, store: store, allowContributions: allowContributions)
                Divider()
            }
            Button(L10n.text(.visitSignal, locale: locale), action: onVisit)
                .disabled(visitNoted || store.state.writing)
                .buttonStyle(.bordered).frame(minHeight: GenSize.iosTouch)
            if visitNoted {
                Text(store.state.backendConfigured ? L10n.resource("visit_saved", locale: locale) : L10n.text(.visitSignalSubmitted, locale: locale)).font(GenTypography.font(.footnote))
            }
            Text(L10n.text(.officialVerificationBoundary, locale: locale)).font(GenTypography.font(.footnote)).foregroundStyle(GenPalette.secondary)
        }
        .padding(GenSpace.lg).frame(maxWidth: .infinity, alignment: .leading)
        .background(GenPalette.surface)
    }
}

private struct CenterMapActions: View {
    let locale: AppLocale
    let center: KindnessCenter
    @Environment(\.openURL) private var openURL
    @State private var failed = false

    var body: some View {
        VStack(alignment: .leading, spacing: GenSpace.sm) {
            if let google = center.googleMapsURL, let apple = center.appleMapsItem(locale: locale) {
                Button {
                    openURL(google) { accepted in failed = !accepted }
                } label: {
                    Label(L10n.resource("map_open_google", locale: locale), systemImage: "map")
                }.accessibilityIdentifier("map-open-google")
                Button {
                    failed = !apple.openInMaps()
                } label: {
                    Label(L10n.resource("map_open_apple", locale: locale), systemImage: "map.fill")
                }.accessibilityIdentifier("map-open-apple")
            } else {
                Text(L10n.resource("map_location_unavailable", locale: locale))
            }
            if failed {
                Text(L10n.resource("map_open_failed", locale: locale)).foregroundStyle(GenPalette.error)
                    .accessibilityIdentifier("map-open-error")
            }
        }.buttonStyle(.bordered).controlSize(.large)
    }
}

private struct NeedView: View {
    let locale: AppLocale
    let need: CenterNeed
    @ObservedObject var store: GenerosityStore
    var allowContributions = true
    @State private var method = "goods"
    @State private var offering = false
    var body: some View {
        HStack(alignment: .top, spacing: GenSpace.md) {
            IconTile(symbol: need.helpMethods.contains(.goods) ? "gift" : "clock", size: 40)
            VStack(alignment: .leading, spacing: GenSpace.sm) {
                Text(L10n.resource(need.isUrgent ? "urgent" : "priority_\(need.priority.code)", locale: locale))
                    .font(GenTypography.font(.caption).weight(.bold)).foregroundStyle(need.isUrgent ? GenPalette.error : GenPalette.accent)
                Text(need.title.resolve(locale)).font(GenTypography.font(.subheadline).weight(.semibold))
                Text(need.description_.resolve(locale)).font(GenTypography.font(.subheadline)).foregroundStyle(GenPalette.secondary)
                if store.state.features.showNeedProgress { NeedProgressView(locale: locale, need: need) }
                Text(need.helpMethods.sorted { $0.ordinal < $1.ordinal }.map { $0.label(locale) }.joined(separator: " · "))
                    .font(GenTypography.font(.footnote))
                if allowContributions && store.state.canRecordContributions {
                    ForEach(["goods", "time"].filter { code in need.helpMethods.contains { $0.code == code } }, id: \.self) { code in
                        Button(L10n.resource(code == "goods" ? "offer_goods_action" : "offer_time", locale: locale)) { method = code; offering = true }
                            .buttonStyle(.bordered).frame(minHeight: GenSize.iosTouch)
                            .disabled(!need.status.isOpen || (code == "goods" && need.quantityRemaining?.intValue == 0))
                    }
                } else { Text(L10n.resource(store.state.profile.isRegistered ? "feature_unavailable" : "registration_required", locale: locale)).font(GenTypography.font(.footnote)) }
                if need.helpMethods.contains(.money) {
                    Text(L10n.text(.moneyBoundary, locale: locale)).font(GenTypography.font(.footnote)).foregroundStyle(GenPalette.secondary)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .sheet(isPresented: $offering) { OfferForm(locale: locale, store: store, need: need, method: method) }
    }
}

private struct ProfileView: View {
    let locale: AppLocale
    let state: GenerosityUiState
    @ObservedObject var account: AccountSession
    @ObservedObject var store: GenerosityStore
    var section = "all"
    @State private var personalMethod = "goods"
    @State private var offering = false
    @ViewBuilder var body: some View {
        if section == "all" { NavigationStack { content } }
        else { content }
    }
    private var content: some View {
            ScrollView {
                VStack(alignment: .leading, spacing: GenSpace.xl) {
                    if section == "all" || section == "profile" {
                    HStack(spacing: GenSpace.md) {
                        IconTile(symbol: "person", size: 52)
                        Text(state.profile.isRegistered ? state.profile.displayName ?? L10n.text(.registeredProfile, locale: locale) : L10n.text(.anonymousProfile, locale: locale))
                            .font(GenTypography.font(.headline)).frame(maxWidth: .infinity, alignment: .leading)
                    }
                    .padding(GenSpace.lg).background(GenPalette.surface, in: RoundedRectangle(cornerRadius: GenRadius.card))
                    Button(L10n.resource(state.profile.isRegistered ? "auth_sign_out" : "auth_sign_in", locale: locale)) {
                        if state.profile.isRegistered { Task { await account.signOut(store: store) } }
                        else { account.signIn(store: store) }
                    }.buttonStyle(.borderedProminent).foregroundStyle(GenPalette.onAccent).disabled(account.busy)
                    if !state.profile.isRegistered {
                        Button(L10n.resource("welcome_create", locale: locale)) {
                            account.signIn(store: store, register: true)
                        }.buttonStyle(.bordered).frame(minHeight: GenSize.iosTouch).disabled(account.busy)
                    }
                    if account.busy { ProgressView() }
                    if account.failed { Text(L10n.resource("auth_failed", locale: locale)).foregroundStyle(.red).accessibilityAddTraits(.updatesFrequently) }
                    Text(L10n.resource("auth_scope", locale: locale)).font(GenTypography.font(.footnote))
                    }
                    SyncStatusView(locale: locale, store: store)
                    if section == "all" && state.canRecordContributions {
                        Menu(L10n.resource("personal_add", locale: locale)) {
                            Button(L10n.resource("offer_goods_action", locale: locale)) { personalMethod = "goods"; offering = true }
                            Button(L10n.resource("offer_time", locale: locale)) { personalMethod = "time"; offering = true }
                        }.frame(minHeight: GenSize.iosTouch)
                    }
                    if section == "all" || section == "profile" {
                    VStack(alignment: .leading, spacing: GenSpace.sm) {
                        Text(L10n.resource("points_title", locale: locale)).font(GenTypography.font(.subheadline))
                        Text(L10n.resource("unavailable", locale: locale)).font(GenTypography.font(.title2).weight(.bold))
                        Text(L10n.text(.generosityPoints, locale: locale)).font(GenTypography.font(.footnote))
                    }
                    .padding(GenSpace.xl).frame(maxWidth: .infinity, alignment: .leading)
                    .foregroundStyle(GenPalette.onAccent).background(GenPalette.accent, in: RoundedRectangle(cornerRadius: GenRadius.card))
                    }
                    if section == "all" || section == "impact" {
                    Text(L10n.resource("impact_summary", locale: locale)).font(GenTypography.font(.headline))
                    LazyVGrid(columns: [GridItem(.adaptive(minimum: 140), alignment: .leading)], alignment: .leading, spacing: GenSpace.md) {
                        metric("centers_supported", value: state.profile.isRegistered ? L10n.number(state.impact.centersSupported, locale: locale) : "—")
                        metric("needs_supported", value: state.profile.isRegistered ? L10n.number(state.impact.needsSupported, locale: locale) : "—")
                    }
                    Text(L10n.resource("impact_basis", locale: locale)).font(GenTypography.font(.footnote)).foregroundStyle(GenPalette.secondary)
                    }
                    if section == "all" || section == "history" {
                    Text(L10n.resource("history", locale: locale)).font(GenTypography.font(.headline))
                    if state.contributions.isEmpty {
                        Text(L10n.text(.contributionHistoryEmpty, locale: locale)).foregroundStyle(GenPalette.secondary)
                    }
                    ForEach(state.contributions, id: \.id) { contribution in
                        VStack(alignment: .leading, spacing: GenSpace.xs) {
                            Text(contribution.title.resolve(locale)).font(GenTypography.font(.subheadline).weight(.semibold))
                            Text(L10n.resource("contribution_\(contribution.status.code)", locale: locale))
                                .font(GenTypography.font(.footnote)).foregroundStyle(GenPalette.secondary)
                            if contribution.centerId == nil { Text(L10n.resource("personal_notice", locale: locale)).font(GenTypography.font(.footnote)) }
                            if contribution.status == .offered || contribution.status == .confirmed {
                                Button(L10n.resource("contribution_cancel", locale: locale)) { store.transition(contribution.id, action: "cancel", version: contribution.version) }
                                    .disabled(state.writing || state.syncing || !state.pendingOperations.isEmpty).frame(minHeight: GenSize.iosTouch)
                                if contribution.centerId == nil {
                                    Button(L10n.resource("contribution_complete", locale: locale)) { store.transition(contribution.id, action: "complete", version: contribution.version) }
                                        .disabled(state.writing || state.syncing || !state.pendingOperations.isEmpty).frame(minHeight: GenSize.iosTouch)
                                }
                            }
                            Divider()
                        }
                    }
                    }
                }
                .padding(GenSpace.lg)
            }
            .background(GenPalette.background).foregroundStyle(GenPalette.text)
            .sheet(isPresented: $offering) { OfferForm(locale: locale, store: store, need: nil, method: personalMethod) }
            .navigationTitle(L10n.resource(section == "history" ? "history" : section == "impact" ? "impact_summary" : "tab_profile", locale: locale)).navigationBarTitleDisplayMode(.inline)
            .toolbarBackground(GenPalette.surface, for: .navigationBar).toolbarBackground(.visible, for: .navigationBar)
    }
    private func metric(_ key: String, value: String) -> some View {
        VStack(alignment: .leading, spacing: GenSpace.sm) {
            Image(systemName: "heart").foregroundStyle(GenPalette.accent).accessibilityHidden(true)
            Text(value).font(GenTypography.font(.title2).weight(.bold))
            Text(L10n.resource(key, locale: locale)).font(GenTypography.font(.footnote))
        }
        .padding(GenSpace.lg).frame(maxWidth: .infinity, alignment: .leading)
        .background(GenPalette.surface, in: RoundedRectangle(cornerRadius: GenRadius.card))
    }
}

private struct SettingsView: View {
    @ObservedObject var store: GenerosityStore
    let locale: AppLocale
    let saveFailed: Bool
    @ObservedObject var permissions: PermissionService
    @Binding var languageMode: String
    @Binding var nearbyNotifications: Bool
    @Binding var urgentNotifications: Bool
    @Binding var reminderNotifications: Bool

    var embedded = false

    @ViewBuilder var body: some View {
        if embedded { content }
        else { NavigationStack { content } }
    }
    private var content: some View {
            Form {
                Section {
                    Picker(L10n.text(.language, locale: locale), selection: $languageMode) {
                        Text(L10n.text(.languageDevice, locale: locale)).tag("system")
                        ForEach(AppLocale.allCases) { language in
                            Text(language.nativeName).tag(language.rawValue)
                        }
                    }
                    .pickerStyle(.inline)
                    .fixedSize(horizontal: false, vertical: true)
                } header: {
                    Text(L10n.text(.language, locale: locale)).foregroundStyle(GenPalette.secondary)
                }
                Section {
                    Toggle(isOn: $nearbyNotifications) { settingsLabel(.nearbyNotifications) }
                    Text(L10n.resource("push_consent", locale: locale)).font(GenTypography.font(.footnote))
                    Toggle(isOn: $urgentNotifications) { settingsLabel(.urgentNotifications) }
                    Toggle(L10n.resource("push_all_categories", locale: locale), isOn: Binding(get: { store.state.preferences.allCategoryInterests }, set: store.setAllNotificationCategories))
                    Text(L10n.resource(store.state.pushSyncError ? "push_failed" : (store.state.pushConfigured ? "push_ready" : "push_unavailable"), locale: locale)).font(GenTypography.font(.footnote))
                    Toggle(isOn: $reminderNotifications) { settingsLabel(.reminderNotifications) }
                    Text(L10n.resource("nearby_foreground_only", locale: locale)).font(GenTypography.font(.footnote))
                    Text(L10n.resource("permissions_explanation", locale: locale)).font(GenTypography.font(.footnote))
                    Text(L10n.resource("notifications_pending", locale: locale)).font(GenTypography.font(.footnote))
                    if saveFailed { Text(L10n.resource("sync_failed", locale: locale)).foregroundStyle(GenPalette.error) }
                    Button(L10n.resource("request_permissions", locale: locale)) {
                        permissions.requestLocationPermission()
                        permissions.requestNotificationPermission()
                    }
                } header: {
                    Text(L10n.resource("notifications_title", locale: locale)).foregroundStyle(GenPalette.secondary)
                }
            }
            .scrollContentBackground(.hidden).background(GenPalette.background)
            .navigationTitle(L10n.text(.tabSettings, locale: locale)).navigationBarTitleDisplayMode(.inline)
            .toolbarBackground(GenPalette.surface, for: .navigationBar).toolbarBackground(.visible, for: .navigationBar)
    }

    private func settingsLabel(_ key: L10nKey) -> some View {
        Text(L10n.text(key, locale: locale)).fixedSize(horizontal: false, vertical: true)
    }
}
