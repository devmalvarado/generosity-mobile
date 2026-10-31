import Foundation
import Shared

struct AppLocale: RawRepresentable, Hashable, Identifiable {
    let rawValue: String
    var id: String { rawValue }

    init?(rawValue: String) {
        guard let supported = SupportedLocale.companion.fromCode(code: rawValue) else { return nil }
        self.rawValue = supported.code
    }

    var shared: SupportedLocale { SupportedLocale.companion.fromCode(code: rawValue)! }
    var nativeName: String { shared.nativeName }
    static var allCases: [AppLocale] { SupportedLocale.entries.map { AppLocale(rawValue: $0.code)! } }
    static let english = AppLocale(rawValue: "en")!
    static let spanish = AppLocale(rawValue: "es")!
    static var fallback: AppLocale { AppLocale(rawValue: SupportedLocale.companion.fallback.code)! }
}

enum L10nKey: String {
    case tabMap = "tab_map"
    case tabList = "tab_list"
    case tabProfile = "tab_profile"
    case tabSettings = "tab_settings"
    case language
    case languageDevice = "language_device"
    case nearbyCenters = "nearby_centers"
    case searchPlaceholder = "search_placeholder"
    case emptyCenters = "empty_centers"
    case currentNeeds = "current_needs"
    case noCurrentNeeds = "no_current_needs"
    case urgent
    case moneyBoundary = "money_boundary"
    case anonymousProfile = "anonymous_profile"
    case registeredProfile = "registered_profile"
    case contributionHistoryEmpty = "contribution_history_empty"
    case generosityPoints = "generosity_points"
    case nearbyNotifications = "nearby_notifications"
    case urgentNotifications = "urgent_notifications"
    case reminderNotifications = "reminder_notifications"
    case visitSignal = "visit_signal"
    case visitSignalSubmitted = "visit_signal_submitted"
    case officialVerificationBoundary = "official_verification_boundary"
}

struct L10n {
    static func text(_ key: L10nKey, locale: AppLocale) -> String {
        resource(key.rawValue, locale: locale)
    }

    static func resource(_ key: String, locale: AppLocale) -> String {
        let fallback = bundle(for: .fallback)?.localizedString(forKey: key, value: key, table: nil) ?? key
        let translated = bundle(for: locale)?.localizedString(forKey: key, value: fallback, table: nil)
        return translated?.isEmpty == false ? translated! : fallback
    }

    static func remaining(_ quantity: Int, locale: AppLocale) -> String {
        plural("remaining_units", count: quantity, locale: locale)
    }

    static func plural(_ key: String, count: Int, locale: AppLocale) -> String {
        String(format: resource(key, locale: locale),
               locale: Locale(identifier: locale.rawValue), arguments: [count])
    }

    static func number(_ value: Int32, locale: AppLocale) -> String {
        value.formatted(.number.locale(Locale(identifier: locale.rawValue)))
    }

    private static func bundle(for locale: AppLocale) -> Bundle? {
        Bundle.main.path(forResource: locale.rawValue, ofType: "lproj").flatMap(Bundle.init(path:))
    }
}

