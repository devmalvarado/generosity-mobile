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
    @State private var showDetail = false
    @State private var showAreaSearch = false
    @State private var scrollTarget: String?
    @FocusState private var searching: Bool

    private func select(_ id: String, details: Bool = false) {
        searching = false
        store.selectCenter(id)
        showDetail = details || !showMap
    }

    var body: some View {
        GeometryReader { geometry in
            VStack(spacing: 8) {
                HStack(spacing: 8) {
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
                            .font(.caption).frame(minHeight: 44)
                    }
                    .accessibilityLabel(L10n.resource("area_search", locale: locale))
                    .accessibilityIdentifier("area-open")
                }
                .padding(12).frame(minHeight: 44)
                .background(GenPalette.surface, in: RoundedRectangle(cornerRadius: 12))
                .overlay(RoundedRectangle(cornerRadius: 12).stroke(GenPalette.line))
                CategoryFilterView(locale: locale, selectedCategories: store.state.selectedCategories,
                                   onToggle: { store.toggleCategory($0.code) }, horizontal: true)
                if showMap {
                    CenterMapView(locale: locale, centers: store.state.centers,
                                  selectedCenterId: store.state.selectedCenter?.id,
                                  onSelect: { id in select(id); scrollTarget = id }, origin: store.state.discoveryArea?.coordinates)
                        .frame(height: max(100, min(300, geometry.size.height * 0.4)))
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                }
                ScrollViewReader { proxy in
                    ScrollView {
                        LazyVStack(alignment: .leading, spacing: 12) {
                            if let area = store.state.discoveryArea {
                                Text(L10n.resource("area_scope", locale: locale) + " " + area.label)
                                    .font(.caption)
                                Button(L10n.resource("area_clear", locale: locale)) { store.setDiscoveryArea(nil) }
                                    .frame(minHeight: 44).accessibilityIdentifier("area-clear")
                            }
                            Text(L10n.text(.nearbyCenters, locale: locale)).font(.headline)
                            SyncStatusView(locale: locale, store: store)
                            if store.state.centers.isEmpty {
                                Text(L10n.text(.emptyCenters, locale: locale)).foregroundStyle(GenPalette.secondary)
                            }
                            ForEach(store.state.centers, id: \.id) { center in
                                let selected = store.state.selectedCenter?.id == center.id
                                VStack(alignment: .leading, spacing: 4) {
                                    Button { select(center.id) } label: {
                                        VStack(alignment: .leading, spacing: 4) {
                                            CenterIdentity(locale: locale, center: center)
                                            if let area = store.state.discoveryArea {
                                                Text(String(format: L10n.resource("area_distance", locale: locale), locale: Locale(identifier: locale.rawValue), area.distanceKilometersTo(coordinates: center.coordinates)))
                                                    .font(.caption)
                                            }
                                        }
                                            .padding(12).contentShape(Rectangle())
                                    }
                                    .buttonStyle(.plain)
                                    .accessibilityIdentifier("center-\(center.id)")
                                    .accessibilityAddTraits(selected ? .isSelected : [])
                                    Button(L10n.resource("view_details", locale: locale)) { select(center.id, details: true) }
                                        .accessibilityIdentifier("details-\(center.id)")
                                        .buttonStyle(.bordered).frame(minHeight: 44)
                                        .padding([.horizontal, .bottom], 12)
                                }
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .background(GenPalette.surface, in: RoundedRectangle(cornerRadius: 12))
                                .overlay(RoundedRectangle(cornerRadius: 12).stroke(selected ? GenPalette.accent : .clear, lineWidth: 2))
                                .id(center.id)
                            }
                            Text(L10n.resource(store.state.backendConfigured ? "sync_cached" : "demo_catalog", locale: locale))
                                .font(.caption).foregroundStyle(GenPalette.secondary)
                        }.padding(.vertical, 4).id("results-top")
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
            .padding(.horizontal, 16).padding(.top, 8)
            .background(GenPalette.background).foregroundStyle(GenPalette.text)
        }
        .sheet(isPresented: $showAreaSearch) { AreaSearchView(locale: locale, onSelect: store.setDiscoveryArea) }
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
            LazyVGrid(columns: [GridItem(.adaptive(minimum: 160), alignment: .leading)], alignment: .leading, spacing: 8) { chips }
        } else {
            ScrollView(.horizontal, showsIndicators: false) { HStack(spacing: 8) { chips } }
                .accessibilityIdentifier("category-filters")
        }
    }
    private var chips: some View {
        ForEach(CenterCategory.entries, id: \.code) { category in
            let selected = selectedCategories.contains(category)
            Button { onToggle(category) } label: {
                HStack(spacing: 4) {
                    if selected { Image(systemName: "checkmark").accessibilityHidden(true) }
                    Text(category.label(locale)).fixedSize(horizontal: false, vertical: true)
                }
                .font(.subheadline).padding(.horizontal, 12).frame(minHeight: 44)
                .foregroundStyle(selected ? GenPalette.onAccent : GenPalette.text)
                .background(selected ? GenPalette.accent : GenPalette.surface, in: RoundedRectangle(cornerRadius: 10))
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

    final class Coordinator: NSObject, MKMapViewDelegate {
        private var owner: CenterMapView
        private var pins: [String: Annotation] = [:]
        private var framedCoordinates: [String: [Double]] = [:]
        private var framedSelection: String?
        private var framedOrigin: Coordinates?
        init(_ owner: CenterMapView) { self.owner = owner }

        func update(_ map: MapCanvas, owner: CenterMapView) {
            self.owner = owner
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

        func mapView(_ mapView: MKMapView, viewFor annotation: MKAnnotation) -> MKAnnotationView? {
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
        HStack(alignment: .center, spacing: 12) {
            CategoryBadge(category: center.categories.sorted { $0.ordinal < $1.ordinal }.first)
            VStack(alignment: .leading, spacing: 4) {
                HStack(spacing: 4) {
                    Text(center.name.resolve(locale)).font(.subheadline.weight(.semibold)).foregroundStyle(GenPalette.text)
                    VerifiedCenterBadge(locale: locale, status: center.verificationStatus)
                }
                Text(center.address.resolve(locale)).font(.caption).foregroundStyle(GenPalette.secondary)
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
    @Environment(\.openURL) private var openURL
    @State private var contactFailed = false

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            CategoryBadge(category: center.categories.sorted { $0.ordinal < $1.ordinal }.first, size: 52)
            HStack(spacing: 4) {
                Text(center.name.resolve(locale)).font(.title2.weight(.bold))
                    .accessibilityIdentifier("detail-\(center.id)")
                VerifiedCenterBadge(locale: locale, status: center.verificationStatus)
            }
            Text(center.categories.sorted { $0.ordinal < $1.ordinal }.map { $0.label(locale) }.joined(separator: " · "))
                .font(.subheadline).foregroundStyle(GenPalette.accent)
            Text(center.address.resolve(locale)).font(.subheadline).foregroundStyle(GenPalette.secondary)
            Text(center.description_.resolve(locale)).font(.body)
            ForEach(center.contactInfo.links(), id: \.kind) { link in
                Button(L10n.resource("contact_" + link.kind, locale: locale)) {
                    if let url = URL(string: link.url) { openURL(url) { accepted in contactFailed = !accepted } }
                }.frame(minHeight: 44)
            }
            if contactFailed { Text(L10n.resource("contact_failed", locale: locale)).foregroundStyle(GenPalette.error) }
            Divider()
            Text(L10n.text(.currentNeeds, locale: locale)).font(.headline)
            if currentNeeds.isEmpty {
                Text(L10n.text(.noCurrentNeeds, locale: locale)).foregroundStyle(GenPalette.secondary)
            }
            ForEach(currentNeeds, id: \.id) { need in
                NeedView(locale: locale, need: need, store: store)
                Divider()
            }
            Button(L10n.text(.visitSignal, locale: locale), action: onVisit)
                .disabled(visitNoted || store.state.writing)
                .buttonStyle(.bordered).frame(minHeight: 44)
            if visitNoted {
                Text(store.state.backendConfigured ? L10n.resource("visit_saved", locale: locale) : L10n.text(.visitSignalSubmitted, locale: locale)).font(.footnote)
            }
            Text(L10n.text(.officialVerificationBoundary, locale: locale)).font(.footnote).foregroundStyle(GenPalette.secondary)
        }
        .padding(16).frame(maxWidth: .infinity, alignment: .leading)
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
        HStack(alignment: .top, spacing: 12) {
            IconTile(symbol: need.helpMethods.contains(.goods) ? "gift" : "clock", size: 40)
            VStack(alignment: .leading, spacing: 8) {
                Text(L10n.resource(need.isUrgent ? "urgent" : "priority_\(need.priority.code)", locale: locale))
                    .font(.caption.weight(.bold)).foregroundStyle(need.isUrgent ? GenPalette.error : GenPalette.accent)
                Text(need.title.resolve(locale)).font(.subheadline.weight(.semibold))
                Text(need.description_.resolve(locale)).font(.subheadline).foregroundStyle(GenPalette.secondary)
                if let remaining = need.quantityRemaining {
                    Text(L10n.remaining(remaining.intValue, locale: locale)).font(.subheadline.weight(.medium))
                        .accessibilityIdentifier("remaining-\(need.id)")
                }
                Text(need.helpMethods.sorted { $0.ordinal < $1.ordinal }.map { $0.label(locale) }.joined(separator: " · "))
                    .font(.footnote)
                if store.state.canRecordContributions {
                    ForEach(["goods", "time"].filter { code in need.helpMethods.contains { $0.code == code } }, id: \.self) { code in
                        Button(L10n.resource(code == "goods" ? "offer_goods_action" : "offer_time", locale: locale)) { method = code; offering = true }
                            .buttonStyle(.bordered).frame(minHeight: 44)
                    }
                } else { Text(L10n.resource("registration_required", locale: locale)).font(.footnote) }
                if need.helpMethods.contains(.money) {
                    Text(L10n.text(.moneyBoundary, locale: locale)).font(.footnote).foregroundStyle(GenPalette.secondary)
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
                VStack(alignment: .leading, spacing: 20) {
                    HStack(spacing: 12) {
                        IconTile(symbol: "person", size: 52)
                        Text(state.profile.isRegistered ? state.profile.displayName ?? L10n.text(.registeredProfile, locale: locale) : L10n.text(.anonymousProfile, locale: locale))
                            .font(.headline).frame(maxWidth: .infinity, alignment: .leading)
                    }
                    .padding(16).background(GenPalette.surface, in: RoundedRectangle(cornerRadius: 12))
                    Button(L10n.resource(state.profile.isRegistered ? "auth_sign_out" : "auth_sign_in", locale: locale)) {
                        if state.profile.isRegistered { Task { await account.signOut(store: store) } }
                        else { account.signIn(store: store) }
                    }.buttonStyle(.borderedProminent).foregroundStyle(GenPalette.onAccent).disabled(account.busy)
                    if !state.profile.isRegistered {
                        Button(L10n.resource("welcome_create", locale: locale)) {
                            account.signIn(store: store, register: true)
                        }.buttonStyle(.bordered).frame(minHeight: 44).disabled(account.busy)
                    }
                    if account.busy { ProgressView() }
                    if account.failed { Text(L10n.resource("auth_failed", locale: locale)).foregroundStyle(.red).accessibilityAddTraits(.updatesFrequently) }
                    Text(L10n.resource("auth_scope", locale: locale)).font(.footnote)
                    SyncStatusView(locale: locale, store: store)
                    if state.canRecordContributions {
                        Menu(L10n.resource("personal_add", locale: locale)) {
                            Button(L10n.resource("offer_goods_action", locale: locale)) { personalMethod = "goods"; offering = true }
                            Button(L10n.resource("offer_time", locale: locale)) { personalMethod = "time"; offering = true }
                        }.frame(minHeight: 44)
                    }
                    VStack(alignment: .leading, spacing: 8) {
                        Text(L10n.resource("points_title", locale: locale)).font(.subheadline)
                        Text(L10n.resource("unavailable", locale: locale)).font(.title2.weight(.bold))
                        Text(L10n.text(.generosityPoints, locale: locale)).font(.footnote)
                    }
                    .padding(20).frame(maxWidth: .infinity, alignment: .leading)
                    .foregroundStyle(GenPalette.onAccent).background(GenPalette.accent, in: RoundedRectangle(cornerRadius: 12))
                    Text(L10n.resource("impact_summary", locale: locale)).font(.headline)
                    LazyVGrid(columns: [GridItem(.adaptive(minimum: 140), alignment: .leading)], alignment: .leading, spacing: 12) {
                        metric("centers_supported", value: state.profile.isRegistered ? L10n.number(state.impact.centersSupported, locale: locale) : "—")
                        metric("needs_supported", value: state.profile.isRegistered ? L10n.number(state.impact.needsSupported, locale: locale) : "—")
                    }
                    Text(L10n.resource("impact_basis", locale: locale)).font(.footnote).foregroundStyle(GenPalette.secondary)
                    Text(L10n.resource("history", locale: locale)).font(.headline)
                    if state.contributions.isEmpty {
                        Text(L10n.text(.contributionHistoryEmpty, locale: locale)).foregroundStyle(GenPalette.secondary)
                    }
                    ForEach(state.contributions, id: \.id) { contribution in
                        VStack(alignment: .leading, spacing: 4) {
                            Text(contribution.title.resolve(locale)).font(.subheadline.weight(.semibold))
                            Text(L10n.resource("contribution_\(contribution.status.code)", locale: locale))
                                .font(.footnote).foregroundStyle(GenPalette.secondary)
                            if contribution.centerId == nil { Text(L10n.resource("personal_notice", locale: locale)).font(.footnote) }
                            if contribution.status == .offered || contribution.status == .confirmed {
                                Button(L10n.resource("contribution_cancel", locale: locale)) { store.transition(contribution.id, action: "cancel", version: contribution.version) }
                                    .disabled(state.writing || state.syncing || !state.pendingOperations.isEmpty).frame(minHeight: 44)
                                if contribution.centerId == nil {
                                    Button(L10n.resource("contribution_complete", locale: locale)) { store.transition(contribution.id, action: "complete", version: contribution.version) }
                                        .disabled(state.writing || state.syncing || !state.pendingOperations.isEmpty).frame(minHeight: 44)
                                }
                            }
                            Divider()
                        }
                    }
                }
                .padding(16)
            }
            .background(GenPalette.background).foregroundStyle(GenPalette.text)
            .sheet(isPresented: $offering) { OfferForm(locale: locale, store: store, need: nil, method: personalMethod) }
            .navigationTitle(L10n.text(.tabProfile, locale: locale)).navigationBarTitleDisplayMode(.inline)
            .toolbarBackground(GenPalette.surface, for: .navigationBar).toolbarBackground(.visible, for: .navigationBar)
        }
    }
    private func metric(_ key: String, value: String) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Image(systemName: "heart").foregroundStyle(GenPalette.accent).accessibilityHidden(true)
            Text(value).font(.title2.weight(.bold))
            Text(L10n.resource(key, locale: locale)).font(.footnote)
        }
        .padding(16).frame(maxWidth: .infinity, alignment: .leading)
        .background(GenPalette.surface, in: RoundedRectangle(cornerRadius: 12))
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
                    Text(L10n.resource("nearby_foreground_only", locale: locale)).font(.footnote)
                    Text(L10n.resource("permissions_explanation", locale: locale)).font(.footnote)
                    Text(L10n.resource("notifications_pending", locale: locale)).font(.footnote)
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
