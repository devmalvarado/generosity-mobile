import Combine
import Foundation
import Shared

/// The Swift side of the shared app state. SwiftUI observes `state`; every
/// mutation goes through the Kotlin controller, so both platforms apply the
/// same rules.
@MainActor
public final class GenerosityStore: ObservableObject {
    @Published public private(set) var state: GenerosityUiState

    private let controller: GenerosityController
    private let defaults: UserDefaults
    private static let languageKey = "generosity.languageMode"

    public init(defaults: UserDefaults = .standard, connection: AppConnection? = nil) {
        self.defaults = defaults
        if let connection {
            controller = GenerosityController(connection: connection, selectedLocaleCode: defaults.string(forKey: Self.languageKey), deviceLanguageCodes: Locale.preferredLanguages)
        } else {
        controller = GenerosityController(
            selectedLocaleCode: defaults.string(forKey: Self.languageKey),
            deviceLanguageCodes: Locale.preferredLanguages
        )
        }
        state = controller.currentState
        controller.start { [weak self] updated in
            self?.state = updated
        }
    }

    deinit { controller.stop() }

    public var localeCode: String { state.locale.code }
    public var languageMode: String { state.localePreference.selectedLocale?.code ?? "system" }

    public func setLocale(_ code: String) {
        let supported = SupportedLocale.companion.fromCode(code: code)
        guard code == "system" || supported != nil else { return }
        defaults.set(supported?.code ?? "system", forKey: Self.languageKey)
        controller.setDeviceLanguages(codes: Locale.preferredLanguages)
        controller.setLocale(code: code)
        state = controller.currentState
    }

    public func refreshDeviceLanguages() {
        controller.setDeviceLanguages(codes: Locale.preferredLanguages)
        state = controller.currentState
    }

    public func onResume() {
        controller.onResume()
        refreshDeviceLanguages()
    }
    public func setSearchText(_ text: String) {
        controller.setSearchText(text: text)
        state = controller.currentState
    }
    public func toggleCategory(_ code: String) {
        controller.toggleCategory(code: code)
        state = controller.currentState
    }
    public func selectCenter(_ id: String?) {
        controller.selectCenter(centerId: id)
        state = controller.currentState
    }
    public func signIn(userId: String, displayName: String) {
        controller.signIn(userId: userId, displayName: displayName)
    }
    public func signOut() {
        controller.continueAnonymously()
    }
    public func offerGoods(needId: String, quantity: Int32) {
        controller.offerGoods(needId: needId, quantity: quantity)
    }
    public func offerTime(needId: String, activity: String, availability: String) {
        controller.offerTime(needId: needId, activity: activity, availability: availability)
    }
    public func signalVisit(_ centerId: String) {
        controller.signalVisit(centerId: centerId)
        state = controller.currentState
    }
    public func submitOffer(needId: String?, method: String, quantity: Int32, minutes: Int32, activity: String, availability: String, notes: String, unit: String) {
        controller.submitOffer(needId: needId, method: method, quantity: quantity, minutes: minutes, activity: activity, availability: availability, notes: notes, unit: unit)
    }
    public func transition(_ id: String, action: String, version: Int32) { controller.transitionContribution(id: id, action: action, version: version) }
    public func discardRejectedOperation(_ id: String) { controller.discardRejectedOperation(id: id) }
    public func setPreferences(nearby: Bool, urgent: Bool, reminders: Bool) {
        controller.setPreferences(nearby: nearby, urgent: urgent, reminders: reminders)
    }
}
