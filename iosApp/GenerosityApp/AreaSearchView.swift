import SwiftUI
import MapKit
import Shared

struct AreaSearchView: View {
    let locale: AppLocale
    let onSelect: (DiscoveryArea) -> Void
    @Environment(\.dismiss) private var dismiss
    @State private var query = ""
    @State private var results: [DiscoveryArea] = []
    @State private var message: String?
    @State private var busy = false
    @State private var lookup: MKLocalSearch?
    @State private var timeout: Task<Void, Never>?
    @State private var generation = UUID()

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 12) {
                    Text(L10n.resource("area_hint", locale: locale))
                    TextField(L10n.resource("area_query", locale: locale), text: $query)
                        .textFieldStyle(.roundedBorder).submitLabel(.search)
                        .accessibilityIdentifier("area-query")
                        .onChange(of: query) { _ in
                            cancel(); results = []; message = nil
                            if query.count > 200 { query = String(query.prefix(200)) }
                        }
                        .onSubmit(search)
                    Button(L10n.resource("area_submit", locale: locale), action: search)
                        .buttonStyle(.borderedProminent).disabled(busy || query.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
                        .accessibilityIdentifier("area-submit")
                    if busy { ProgressView(L10n.resource("area_loading", locale: locale)) }
                    if let message { Text(L10n.resource(message, locale: locale)) }
                    ForEach(Array(results.enumerated()), id: \.offset) { index, area in
                        Button { onSelect(area); dismiss() } label: {
                            Text(area.label).frame(maxWidth: .infinity, minHeight: 44, alignment: .leading)
                        }.accessibilityIdentifier("area-result-\(index)")
                        Divider()
                    }
                }.padding(16)
            }
            .navigationTitle(L10n.resource("area_search", locale: locale))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar { ToolbarItem(placement: .cancellationAction) {
                Button(L10n.resource("offer_cancel", locale: locale)) { dismiss() }
            } }
        }.onDisappear(perform: cancel)
    }

    private func cancel() {
        generation = UUID(); lookup?.cancel(); lookup = nil
        timeout?.cancel(); timeout = nil; busy = false
    }

    private func search() {
        let text = query.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !text.isEmpty, text.count <= 200, !busy else { return }
        cancel(); results = []; message = nil; busy = true
        let request = MKLocalSearch.Request()
        request.naturalLanguageQuery = text
        request.resultTypes = .address
        let search = MKLocalSearch(request: request)
        lookup = search
        let token = generation
        timeout = Task { @MainActor in
            try? await Task.sleep(nanoseconds: 15_000_000_000)
            guard !Task.isCancelled, generation == token else { return }
            cancel(); message = "area_failed"
        }
        search.start { response, error in
            Task { @MainActor in
                guard generation == token else { return }
                timeout?.cancel(); timeout = nil; lookup = nil; busy = false
                if let error, (error as? MKError)?.code != .placemarkNotFound {
                    message = "area_failed"; return
                }
                results = (response?.mapItems ?? []).prefix(5).compactMap { item in
                    let point = item.placemark.coordinate
                    let label = item.placemark.title ?? item.name ?? ""
                    guard CLLocationCoordinate2DIsValid(point), !label.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else { return nil }
                    return DiscoveryArea(label: String(decoding: label.utf16.prefix(500), as: UTF16.self), coordinates: Coordinates(latitude: point.latitude, longitude: point.longitude))
                }
                if results.isEmpty { message = "area_empty" }
            }
        }
    }
}
