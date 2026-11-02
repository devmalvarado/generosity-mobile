import SwiftUI
import MapKit
import SharedBridge
import Shared

struct ContentView: View {
    private static var demoCatalog: Bool {
        #if DEBUG
        return ProcessInfo.processInfo.arguments.contains("--demo-catalog")
        #else
        return false
        #endif
    }
    @StateObject private var store: GenerosityStore
    @StateObject private var account: AccountSession
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
        if !entered && !store.state.profile.isRegistered {
            WelcomeView(locale: locale, account: account, store: store, onGuest: { entered = true })
        } else {
        TabView {
            discovery(showMap: true)
                .tabItem { Label(L10n.text(.tabMap, locale: locale), systemImage: "map") }
            discovery(showMap: false)
                .tabItem { Label(L10n.text(.tabList, locale: locale), systemImage: "list.bullet") }
            ProfileView(locale: locale, state: store.state, account: account, store: store)
                .tabItem { Label(L10n.text(.tabProfile, locale: locale), systemImage: "person") }
            SettingsView(
                locale: locale, saveFailed: store.state.syncError == "save_failed", languageMode: Binding(get: { store.languageMode }, set: store.setLocale),
                nearbyNotifications: Binding(get: { store.state.preferences.nearbyCentersEnabled }, set: { store.setPreferences(nearby: $0, urgent: store.state.preferences.urgentNeedsEnabled, reminders: store.state.preferences.remindersEnabled) }),
                urgentNotifications: Binding(get: { store.state.preferences.urgentNeedsEnabled }, set: { store.setPreferences(nearby: store.state.preferences.nearbyCentersEnabled, urgent: $0, reminders: store.state.preferences.remindersEnabled) }),
                reminderNotifications: Binding(get: { store.state.preferences.remindersEnabled }, set: { store.setPreferences(nearby: store.state.preferences.nearbyCentersEnabled, urgent: store.state.preferences.urgentNeedsEnabled, reminders: $0) })
            )
            .tabItem { Label(L10n.text(.tabSettings, locale: locale), systemImage: "slider.horizontal.3") }
        }
        }
        }
        .onChange(of: store.state.profile.isRegistered) { registered in entered = registered }
        .tint(GenPalette.accent)
        .font(GenTypography.font(.body))
        .environment(\.locale, Locale(identifier: locale.rawValue))
        .environment(\.layoutDirection, Locale.Language(identifier: locale.rawValue).characterDirection == .rightToLeft ? .rightToLeft : .leftToRight)
        .onChange(of: scenePhase) { phase in
            if phase == .active {
                store.onResume()
                Task { if !Self.demoCatalog { await account.restore(store: store) } }
            }
        }
        .task { if !Self.demoCatalog { await account.restore(store: store) } }
        .onReceive(NotificationCenter.default.publisher(for: NSLocale.currentLocaleDidChangeNotification)) { _ in
            store.refreshDeviceLanguages()
        }
    }

    private func discovery(showMap: Bool) -> some View {
        NavigationStack {
            DiscoveryView(locale: locale, store: store, showMap: showMap)
                .navigationBarTitleDisplayMode(.inline)
                .toolbar { ToolbarItem(placement: .principal) { BrandWordmark() } }
                .toolbarBackground(GenPalette.surface, for: .navigationBar)
                .toolbarBackground(.visible, for: .navigationBar)
        }
    }
}

private struct DiscoveryView: View {
    let locale: AppLocale
    @ObservedObject var store: GenerosityStore
    let showMap: Bool

    var body: some View {
        GeometryReader { geometry in
            ScrollViewReader { proxy in
                ScrollView {
                    VStack(alignment: .leading, spacing: GenSpace.lg) {
                        SyncStatusView(locale: locale, store: store)
                        Text(L10n.text(.nearbyCenters, locale: locale)).font(GenTypography.font(.headline))
                        HStack(spacing: 10) {
                            Image(systemName: "magnifyingglass").foregroundStyle(GenPalette.secondary).accessibilityHidden(true)
                            TextField(L10n.text(.searchPlaceholder, locale: locale), text: Binding(
                                get: { store.state.searchText }, set: store.setSearchText
                            ))
                            .accessibilityIdentifier("discovery-search")
                            .submitLabel(.search)
                        }
                        .padding(14).frame(minHeight: 48)
                        .background(GenPalette.surface, in: RoundedRectangle(cornerRadius: GenRadius.card))
                        .overlay(RoundedRectangle(cornerRadius: GenRadius.card).stroke(GenPalette.line))
                        CategoryFilterView(
                            locale: locale, selectedCategories: store.state.selectedCategories,
                            onToggle: { store.toggleCategory($0.code) }
                        )
                        if showMap {
                            CenterMapView(
                                locale: locale, centers: store.state.centers,
                                selectedCenterId: store.state.selectedCenter?.id,
                                onSelect: { store.selectCenter($0) }
                            )
                            .frame(height: max(200, min(480, geometry.size.height * 0.52)))
                            .clipShape(RoundedRectangle(cornerRadius: GenRadius.card))
                            if let center = store.state.selectedCenter {
                                VStack(alignment: .leading, spacing: GenSpace.sm) {
                                    Text(L10n.resource("selected_center", locale: locale)).font(GenTypography.font(.caption)).foregroundStyle(GenPalette.accent)
                                    CenterIdentity(locale: locale, center: center)
                                    Button(L10n.resource("view_details", locale: locale)) {
                                        withAnimation { proxy.scrollTo("center-detail", anchor: .top) }
                                    }
                                    .buttonStyle(.bordered).frame(minHeight: GenSize.iosTouch)
                                }
                                .padding(GenSpace.md).frame(maxWidth: .infinity, alignment: .leading)
                                .background(GenPalette.surface, in: RoundedRectangle(cornerRadius: GenRadius.card))
                            }
                        }
                        if store.state.centers.isEmpty {
                            Text(L10n.text(.emptyCenters, locale: locale)).foregroundStyle(GenPalette.secondary).padding(.vertical, GenSpace.md)
                        }
                        ForEach(store.state.centers, id: \.id) { center in
                            Button { store.selectCenter(center.id) } label: {
                                CenterIdentity(locale: locale, center: center).padding(GenSpace.md)
                                    .frame(maxWidth: .infinity, alignment: .leading)
                                    .background(GenPalette.surface, in: RoundedRectangle(cornerRadius: GenRadius.card))
                            }
                            .buttonStyle(.plain)
                            .accessibilityIdentifier("center-\(center.id)")
                        }
                        if let center = store.state.selectedCenter {
                            CenterDetail(
                                locale: locale, center: center, store: store, currentNeeds: store.state.currentNeeds,
                                visitNoted: store.state.visitSignalCenterId == center.id || store.state.visitedCenterIds.contains(center.id),
                                onVisit: { store.signalVisit(center.id) }
                            )
                            .id("center-detail")
                        }
                        Text(L10n.resource(store.state.backendConfigured ? "sync_cached" : "demo_catalog", locale: locale)).font(GenTypography.font(.caption)).foregroundStyle(GenPalette.secondary)
                    }
                    .padding(GenSpace.lg)
                }
                .accessibilityIdentifier("discovery-scroll")
                .background(GenPalette.background)
                .foregroundStyle(GenPalette.text)
            }
        }
    }
}

private struct CategoryFilterView: View {
    let locale: AppLocale
    let selectedCategories: Set<CenterCategory>
    let onToggle: (CenterCategory) -> Void
    @Environment(\.dynamicTypeSize) private var typeSize

    var body: some View {
        if typeSize.isAccessibilitySize {
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

private struct CenterMapView: View {
    private struct Annotation: Identifiable {
        let center: KindnessCenter
        var id: String { center.id }
    }
    let locale: AppLocale
    let centers: [KindnessCenter]
    let selectedCenterId: String?
    let onSelect: (String) -> Void
    @State private var region = MKCoordinateRegion(
        center: CLLocationCoordinate2D(latitude: 19.4326, longitude: -99.1332),
        span: MKCoordinateSpan(latitudeDelta: 0.04, longitudeDelta: 0.04)
    )

    var body: some View {
        Map(coordinateRegion: $region, annotationItems: centers.map { Annotation(center: $0) }) { annotation in
            MapAnnotation(coordinate: annotation.center.coordinate) {
                let selected = selectedCenterId == annotation.id
                Button { onSelect(annotation.id) } label: {
                    Image(systemName: annotation.center.categories.sorted { $0.ordinal < $1.ordinal }.first?.symbol ?? "mappin")
                        .font(.system(size: 17, weight: .semibold))
                        .foregroundStyle(selected ? GenPalette.onAccent : GenPalette.text)
                        .frame(width: 40, height: 40)
                        .background(selected ? GenPalette.accent : GenPalette.surface, in: Circle())
                        .overlay(Circle().stroke(GenPalette.accent, lineWidth: selected ? 3 : 1))
                        .padding(3)
                }
                .accessibilityLabel(annotation.center.name.resolve(locale))
                .accessibilityIdentifier("marker-\(annotation.id)")
                .accessibilityAddTraits(selected ? .isSelected : [])
            }
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
                Text(center.name.resolve(locale)).font(GenTypography.font(.subheadline).weight(.semibold)).foregroundStyle(GenPalette.text)
                Text(center.address.resolve(locale)).font(GenTypography.font(.caption)).foregroundStyle(GenPalette.secondary)
                Text(center.verificationStatus.label(locale)).font(GenTypography.font(.caption)).foregroundStyle(GenPalette.secondary)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
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
    @Environment(\.openURL) private var openURL
    @State private var contactFailed = false

    var body: some View {
        VStack(alignment: .leading, spacing: GenSpace.lg) {
            CategoryBadge(category: center.categories.sorted { $0.ordinal < $1.ordinal }.first, size: 52)
            Text(center.name.resolve(locale)).font(GenTypography.font(.title2).weight(.bold))
                .accessibilityIdentifier("detail-\(center.id)")
            Text(center.categories.sorted { $0.ordinal < $1.ordinal }.map { $0.label(locale) }.joined(separator: " · "))
                .font(GenTypography.font(.subheadline)).foregroundStyle(GenPalette.accent)
            Text(center.address.resolve(locale)).font(GenTypography.font(.subheadline)).foregroundStyle(GenPalette.secondary)
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
                NeedView(locale: locale, need: need, store: store)
                Divider()
            }
            Button(L10n.text(.visitSignal, locale: locale), action: onVisit)
                .disabled(visitNoted || store.state.writing)
                .buttonStyle(.bordered).frame(minHeight: GenSize.iosTouch)
            if visitNoted {
                Text(store.state.backendConfigured ? L10n.resource("visit_saved", locale: locale) : L10n.text(.visitSignalSubmitted, locale: locale)).font(GenTypography.font(.footnote))
            }
            Text(center.verificationStatus.label(locale)).font(GenTypography.font(.subheadline).weight(.semibold))
            Text(L10n.text(.officialVerificationBoundary, locale: locale)).font(GenTypography.font(.footnote)).foregroundStyle(GenPalette.secondary)
        }
        .padding(GenSpace.lg).frame(maxWidth: .infinity, alignment: .leading)
        .background(GenPalette.surface)
    }
}

private struct NeedView: View {
    let locale: AppLocale
    let need: CenterNeed
    @ObservedObject var store: GenerosityStore
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
                if let remaining = need.quantityRemaining {
                    Text(L10n.remaining(remaining.intValue, locale: locale)).font(GenTypography.font(.subheadline).weight(.medium))
                        .accessibilityIdentifier("remaining-\(need.id)")
                }
                Text(need.helpMethods.sorted { $0.ordinal < $1.ordinal }.map { $0.label(locale) }.joined(separator: " · "))
                    .font(GenTypography.font(.footnote))
                if store.state.canRecordContributions {
                    ForEach(["goods", "time"].filter { code in need.helpMethods.contains { $0.code == code } }, id: \.self) { code in
                        Button(L10n.resource(code == "goods" ? "offer_goods_action" : "offer_time", locale: locale)) { method = code; offering = true }
                            .buttonStyle(.bordered).frame(minHeight: GenSize.iosTouch)
                    }
                } else { Text(L10n.resource("registration_required", locale: locale)).font(GenTypography.font(.footnote)) }
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
    @State private var personalMethod = "goods"
    @State private var offering = false
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: GenSpace.xl) {
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
                    SyncStatusView(locale: locale, store: store)
                    if state.canRecordContributions {
                        Menu(L10n.resource("personal_add", locale: locale)) {
                            Button(L10n.resource("offer_goods_action", locale: locale)) { personalMethod = "goods"; offering = true }
                            Button(L10n.resource("offer_time", locale: locale)) { personalMethod = "time"; offering = true }
                        }.frame(minHeight: GenSize.iosTouch)
                    }
                    VStack(alignment: .leading, spacing: GenSpace.sm) {
                        Text(L10n.resource("points_title", locale: locale)).font(GenTypography.font(.subheadline))
                        Text(L10n.resource("unavailable", locale: locale)).font(GenTypography.font(.title2).weight(.bold))
                        Text(L10n.text(.generosityPoints, locale: locale)).font(GenTypography.font(.footnote))
                    }
                    .padding(GenSpace.xl).frame(maxWidth: .infinity, alignment: .leading)
                    .foregroundStyle(GenPalette.onAccent).background(GenPalette.accent, in: RoundedRectangle(cornerRadius: GenRadius.card))
                    Text(L10n.resource("impact_summary", locale: locale)).font(GenTypography.font(.headline))
                    LazyVGrid(columns: [GridItem(.adaptive(minimum: 140), alignment: .leading)], alignment: .leading, spacing: GenSpace.md) {
                        metric("centers_supported", value: state.profile.isRegistered ? L10n.number(state.impact.centersSupported, locale: locale) : "—")
                        metric("needs_supported", value: state.profile.isRegistered ? L10n.number(state.impact.needsSupported, locale: locale) : "—")
                    }
                    Text(L10n.resource("impact_basis", locale: locale)).font(GenTypography.font(.footnote)).foregroundStyle(GenPalette.secondary)
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
                .padding(GenSpace.lg)
            }
            .background(GenPalette.background).foregroundStyle(GenPalette.text)
            .sheet(isPresented: $offering) { OfferForm(locale: locale, store: store, need: nil, method: personalMethod) }
            .navigationTitle(L10n.text(.tabProfile, locale: locale)).navigationBarTitleDisplayMode(.inline)
            .toolbarBackground(GenPalette.surface, for: .navigationBar).toolbarBackground(.visible, for: .navigationBar)
        }
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
    let locale: AppLocale
    let saveFailed: Bool
    @Binding var languageMode: String
    @Binding var nearbyNotifications: Bool
    @Binding var urgentNotifications: Bool
    @Binding var reminderNotifications: Bool
    @StateObject private var permissions = PermissionService()

    var body: some View {
        NavigationStack {
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
                    Toggle(isOn: $urgentNotifications) { settingsLabel(.urgentNotifications) }
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
    }

    private func settingsLabel(_ key: L10nKey) -> some View {
        Text(L10n.text(key, locale: locale)).fixedSize(horizontal: false, vertical: true)
    }
}
