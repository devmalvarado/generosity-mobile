import SwiftUI
import Shared
import SharedBridge

struct SyncStatusView: View {
    let locale: AppLocale
    @ObservedObject var store: GenerosityStore
    var body: some View {
        if store.state.backendConfigured {
            VStack(alignment: .leading, spacing: GenSpace.sm) {
                Button(L10n.resource(store.state.syncing ? "sync_busy" : "sync_refresh", locale: locale), action: store.onResume)
                    .disabled(store.state.syncing || store.state.writing).frame(minHeight: GenSize.iosTouch)
                if let error = store.state.syncError {
                    Text(L10n.resource(error == "unauthenticated" ? "sync_auth" : "sync_failed", locale: locale)).foregroundStyle(GenPalette.error)
                }
                if !store.state.pendingOperations.isEmpty { Text(L10n.resource("sync_pending", locale: locale)) }
                ForEach(store.state.pendingOperations.filter { $0.errorCode != nil }, id: \.id) { pending in
                    Text(L10n.resource("sync_rejected", locale: locale)).foregroundStyle(GenPalette.error)
                    Button(L10n.resource("sync_discard", locale: locale)) { store.discardRejectedOperation(pending.id) }
                        .disabled(store.state.writing).frame(minHeight: GenSize.iosTouch)
                }
            }.font(GenTypography.font(.footnote))
        }
    }
}

struct OfferForm: View {
    let locale: AppLocale
    @ObservedObject var store: GenerosityStore
    let need: CenterNeed?
    let method: String
    @Environment(\.dismiss) private var dismiss
    @State private var quantity = ""
    @State private var unit = ""
    @State private var minutes = ""
    @State private var activity = ""
    @State private var availability = ""
    @State private var notes = ""
    private var valid: Bool {
        let detailsValid = notes.count <= 2000 && availability.count <= 500 && unit.count <= 60
        if method == "goods" { let value = Int(quantity) ?? 0; return detailsValid && value > 0 && value <= (need?.quantityRemaining?.intValue ?? 1_000_000) }
        return detailsValid && (1...525600).contains(Int(minutes) ?? 0) && !activity.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty && activity.count <= 500
    }
    var body: some View {
        NavigationStack {
            Form {
                Text(need?.title.resolve(locale) ?? L10n.resource("personal_add", locale: locale)).font(GenTypography.font(.headline))
                if method == "goods" {
                    field("offer_quantity", $quantity).keyboardType(.numberPad)
                    if let need { Text(need.quantityUnit ?? "") }
                    else { field("offer_unit", $unit) }
                } else {
                    field("offer_minutes", $minutes).keyboardType(.numberPad)
                    field("offer_activity", $activity)
                    field("offer_availability", $availability)
                }
                field("offer_notes", $notes)
                if need == nil { Text(L10n.resource("personal_notice", locale: locale)).font(GenTypography.font(.footnote)) }
                if store.state.syncError != nil { Text(L10n.resource("sync_failed", locale: locale)).foregroundStyle(GenPalette.error) }
                Button(L10n.resource("offer_send", locale: locale)) {
                    store.submitOffer(needId: need?.id, method: method, quantity: Int32(quantity) ?? 0, minutes: Int32(minutes) ?? 0,
                                      activity: activity.trimmingCharacters(in: .whitespacesAndNewlines), availability: availability, notes: notes, unit: need?.quantityUnit ?? unit)
                }.disabled(!valid || store.state.writing || !store.state.canRecordContributions)
            }
            .navigationTitle(L10n.resource("offer_title", locale: locale)).navigationBarTitleDisplayMode(.inline)
            .toolbar { ToolbarItem(placement: .cancellationAction) { Button(L10n.resource("offer_cancel", locale: locale)) { dismiss() }.disabled(store.state.writing) } }
        }
        .interactiveDismissDisabled(store.state.writing)
        .onChange(of: store.state.savedOffer) { _ in dismiss() }
        .onChange(of: store.state.profile.id) { _ in dismiss() }
    }
    private func field(_ key: String, _ value: Binding<String>) -> some View {
        VStack(alignment: .leading) {
            Text(L10n.resource(key, locale: locale)).font(GenTypography.font(.caption))
            TextField(L10n.resource(key, locale: locale), text: value, axis: .vertical).accessibilityIdentifier(key)
        }
    }
}
