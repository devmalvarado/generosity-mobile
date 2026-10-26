import Combine
import Foundation
import Shared

/// The Swift side of the shared app state. SwiftUI observes `state`; every
/// mutation goes through the Kotlin controller, so both platforms apply the
/// same rules.
@MainActor
public final class GenerosityStore: ObservableObject {
    @Published public private(set) var state: GenerosityUiState

    private let controller = GenerosityController()

    public init() {
        state = GenerosityUiState(
            locale: .english,
            profile: UserProfile(id: nil, mode: .anonymous, displayName: nil, generosityPoints: 0),
            searchText: "",
            selectedCategories: [],
            centers: [],
            selectedCenterId: nil,
            contributions: [],
            impact: PersonalImpact(
                hoursOffered: 0, goodsCommitted: 0, monetaryContributions: 0,
                needsSupported: 0, centersSupported: 0, personalActions: 0
            ),
            preferences: NotificationPreference(
                nearbyCentersEnabled: false, urgentNeedsEnabled: false,
                remindersEnabled: false, categoryInterests: []
            ),
            referenceIsoDate: "",
            lastRejection: nil,
            visitSignalCenterId: nil
        )
        controller.start { [weak self] updated in
            Task { @MainActor in self?.state = updated }
        }
    }

    public func setLocale(_ code: String) { controller.setLocale(code: code) }
    public func setSearchText(_ text: String) { controller.setSearchText(text: text) }
    public func toggleCategory(_ code: String) { controller.toggleCategory(code: code) }
    public func selectCenter(_ id: String?) { controller.selectCenter(centerId: id) }
    public func signIn(userId: String, displayName: String) {
        controller.signIn(userId: userId, displayName: displayName)
    }
    public func offerGoods(needId: String, quantity: Int32) {
        controller.offerGoods(needId: needId, quantity: quantity)
    }
    public func offerTime(needId: String, activity: String, availability: String) {
        controller.offerTime(needId: needId, activity: activity, availability: availability)
    }
    public func signalVisit(_ centerId: String) { controller.signalVisit(centerId: centerId) }
    public func setPreferences(nearby: Bool, urgent: Bool, reminders: Bool) {
        controller.setPreferences(nearby: nearby, urgent: urgent, reminders: reminders)
    }
}
