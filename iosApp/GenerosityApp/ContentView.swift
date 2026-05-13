import SwiftUI
import MapKit

struct ContentView: View {
    @State private var locale: AppLocale = .english
    @State private var query: String = ""
    @State private var selectedCategories: Set<AppCategory> = []
    @State private var selectedCenter: AppKindnessCenter?
    @State private var visitMessage: String?
    @State private var nearbyNotifications = false
    @State private var urgentNotifications = false
    @State private var reminderNotifications = false

    private var filteredCenters: [AppKindnessCenter] {
        SampleData.centers
            .filter { center in
                selectedCategories.isEmpty || !Set(center.categories).isDisjoint(with: selectedCategories)
            }
            .filter { center in
                let normalized = query.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
                guard !normalized.isEmpty else { return true }
                return center.name.resolve(locale).lowercased().contains(normalized)
                    || center.description.resolve(locale).lowercased().contains(normalized)
                    || center.address.resolve(locale).lowercased().contains(normalized)
            }
            .sorted { $0.name.resolve(locale) < $1.name.resolve(locale) }
    }

    var body: some View {
        TabView {
            NavigationStack {
                DiscoveryView(
                    locale: locale,
                    query: $query,
                    selectedCategories: $selectedCategories,
                    centers: filteredCenters,
                    selectedCenter: $selectedCenter,
                    visitMessage: $visitMessage,
                    showMap: true
                )
                .navigationTitle("Generosity")
                .toolbar { languageToolbar }
            }
            .tabItem { Text(L10n.text(.tabMap, locale: locale)) }

            NavigationStack {
                DiscoveryView(
                    locale: locale,
                    query: $query,
                    selectedCategories: $selectedCategories,
                    centers: filteredCenters,
                    selectedCenter: $selectedCenter,
                    visitMessage: $visitMessage,
                    showMap: false
                )
                .navigationTitle(L10n.text(.tabList, locale: locale))
                .toolbar { languageToolbar }
            }
            .tabItem { Text(L10n.text(.tabList, locale: locale)) }

            ProfileView(locale: locale)
                .tabItem { Text(L10n.text(.tabProfile, locale: locale)) }

            SettingsView(
                locale: locale,
                nearbyNotifications: $nearbyNotifications,
                urgentNotifications: $urgentNotifications,
                reminderNotifications: $reminderNotifications
            )
            .tabItem { Text(L10n.text(.tabSettings, locale: locale)) }
        }
    }

    @ToolbarContentBuilder
    private var languageToolbar: some ToolbarContent {
        ToolbarItem(placement: .automatic) {
            Picker("", selection: $locale) {
                Text(L10n.text(.languageEnglish, locale: locale)).tag(AppLocale.english)
                Text(L10n.text(.languageSpanish, locale: locale)).tag(AppLocale.spanish)
            }
            .pickerStyle(.segmented)
            .frame(width: 180)
        }
    }
}

private struct DiscoveryView: View {
    let locale: AppLocale
    @Binding var query: String
    @Binding var selectedCategories: Set<AppCategory>
    let centers: [AppKindnessCenter]
    @Binding var selectedCenter: AppKindnessCenter?
    @Binding var visitMessage: String?
    let showMap: Bool

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                Text(L10n.text(.nearbyCenters, locale: locale))
                    .font(.title2)
                    .fontWeight(.semibold)

                TextField(L10n.text(.searchPlaceholder, locale: locale), text: $query)
                    .textFieldStyle(.roundedBorder)

                CategoryFilterView(locale: locale, selectedCategories: $selectedCategories)

                if showMap {
                    CenterMapView(centers: centers, selectedCenter: $selectedCenter, visitMessage: $visitMessage)
                        .frame(height: 260)
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                }

                if centers.isEmpty {
                    Text(L10n.text(.emptyCenters, locale: locale))
                        .foregroundStyle(.secondary)
                } else {
                    ForEach(centers) { center in
                        Button {
                            selectedCenter = center
                            visitMessage = nil
                        } label: {
                            CenterRow(locale: locale, center: center)
                        }
                        .buttonStyle(.plain)
                    }
                }

                if let selectedCenter {
                    CenterDetail(locale: locale, center: selectedCenter, visitMessage: $visitMessage)
                }
            }
            .padding()
        }
    }
}

private struct CategoryFilterView: View {
    let locale: AppLocale
    @Binding var selectedCategories: Set<AppCategory>

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack {
                ForEach(AppCategory.allCases) { category in
                    Button {
                        if selectedCategories.contains(category) {
                            selectedCategories.remove(category)
                        } else {
                            selectedCategories.insert(category)
                        }
                    } label: {
                        Text(category.label(locale))
                            .font(.caption)
                            .padding(.horizontal, 10)
                            .padding(.vertical, 6)
                            .background(selectedCategories.contains(category) ? Color.green.opacity(0.18) : Color.gray.opacity(0.12))
                            .clipShape(Capsule())
                    }
                    .buttonStyle(.plain)
                }
            }
        }
    }
}

private struct CenterMapView: View {
    let centers: [AppKindnessCenter]
    @Binding var selectedCenter: AppKindnessCenter?
    @Binding var visitMessage: String?
    @State private var region = MKCoordinateRegion(
        center: CLLocationCoordinate2D(latitude: 19.4326, longitude: -99.1332),
        span: MKCoordinateSpan(latitudeDelta: 0.04, longitudeDelta: 0.04)
    )

    var body: some View {
        Map(coordinateRegion: $region, annotationItems: centers) { center in
            MapAnnotation(coordinate: center.coordinate) {
                Button {
                    selectedCenter = center
                    visitMessage = nil
                } label: {
                    Circle()
                        .fill(Color.green)
                        .frame(width: 18, height: 18)
                        .overlay(Circle().stroke(Color.white, lineWidth: 3))
                }
            }
        }
    }
}

private struct CenterRow: View {
    let locale: AppLocale
    let center: AppKindnessCenter

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(center.name.resolve(locale))
                .fontWeight(.semibold)
            Text(center.address.resolve(locale))
                .font(.footnote)
                .foregroundStyle(.secondary)
            Text(center.verificationStatus.label(locale))
                .font(.caption)
                .foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding()
        .background(Color.gray.opacity(0.08))
        .clipShape(RoundedRectangle(cornerRadius: 8))
    }
}

private struct CenterDetail: View {
    let locale: AppLocale
    let center: AppKindnessCenter
    @Binding var visitMessage: String?

    private var currentNeeds: [AppNeed] {
        center.needs.filter { $0.isCurrent(referenceIsoDate: "2026-05-13") }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text(center.name.resolve(locale))
                .font(.title2)
                .fontWeight(.bold)
            Text(center.description.resolve(locale))
            Text(center.address.resolve(locale))
            Text(center.categories.map { $0.label(locale) }.joined(separator: ", "))
                .font(.footnote)
                .foregroundStyle(.secondary)
            Text(center.verificationStatus.label(locale))
                .fontWeight(.semibold)
            Text(L10n.text(.officialVerificationBoundary, locale: locale))
                .font(.footnote)
                .foregroundStyle(.secondary)

            Text(L10n.text(.currentNeeds, locale: locale))
                .font(.headline)

            if currentNeeds.isEmpty {
                Text(L10n.text(.noCurrentNeeds, locale: locale))
            } else {
                ForEach(currentNeeds) { need in
                    NeedView(locale: locale, need: need)
                }
            }

            Button(L10n.text(.visitSignal, locale: locale)) {
                visitMessage = L10n.text(.visitSignalSubmitted, locale: locale)
            }
            .buttonStyle(.borderedProminent)

            if let visitMessage {
                Text(visitMessage)
                    .foregroundStyle(.green)
                    .font(.footnote)
            }
        }
        .padding()
        .background(Color.green.opacity(0.08))
        .clipShape(RoundedRectangle(cornerRadius: 8))
    }
}

private struct NeedView: View {
    let locale: AppLocale
    let need: AppNeed

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack {
                Text(need.title.resolve(locale))
                    .fontWeight(.semibold)
                if need.urgent {
                    Text(L10n.text(.urgent, locale: locale))
                        .foregroundStyle(.red)
                        .fontWeight(.bold)
                }
            }
            Text(need.description.resolve(locale))
            Text(need.helpMethods.map { $0.label(locale) }.joined(separator: ", "))
                .font(.footnote)
                .foregroundStyle(.secondary)
            if need.helpMethods.contains(.money) {
                Text(L10n.text(.moneyBoundary, locale: locale))
                    .font(.footnote)
                    .foregroundStyle(.secondary)
            }
        }
    }
}

private struct ProfileView: View {
    let locale: AppLocale

    var body: some View {
        NavigationStack {
            VStack(alignment: .leading, spacing: 16) {
                Text(L10n.text(.anonymousProfile, locale: locale))
                Text(L10n.text(.contributionHistoryEmpty, locale: locale))
                Text(L10n.text(.generosityPoints, locale: locale))
                Spacer()
            }
            .padding()
            .navigationTitle(L10n.text(.tabProfile, locale: locale))
        }
    }
}

private struct SettingsView: View {
    let locale: AppLocale
    @Binding var nearbyNotifications: Bool
    @Binding var urgentNotifications: Bool
    @Binding var reminderNotifications: Bool
    @StateObject private var permissions = PermissionService()

    var body: some View {
        NavigationStack {
            Form {
                Toggle(L10n.text(.nearbyNotifications, locale: locale), isOn: $nearbyNotifications)
                Toggle(L10n.text(.urgentNotifications, locale: locale), isOn: $urgentNotifications)
                Toggle(L10n.text(.reminderNotifications, locale: locale), isOn: $reminderNotifications)
                Button(L10n.text(.nearbyNotifications, locale: locale)) {
                    permissions.requestLocationPermission()
                    permissions.requestNotificationPermission()
                }
            }
            .navigationTitle(L10n.text(.tabSettings, locale: locale))
        }
    }
}
