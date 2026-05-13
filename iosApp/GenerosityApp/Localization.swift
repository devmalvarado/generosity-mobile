import Foundation

enum AppLocale: String, CaseIterable, Identifiable {
    case english = "en"
    case spanish = "es"

    var id: String { rawValue }
}

enum L10nKey: String {
    case tabMap
    case tabList
    case tabProfile
    case tabSettings
    case languageEnglish
    case languageSpanish
    case nearbyCenters
    case searchPlaceholder
    case emptyCenters
    case currentNeeds
    case noCurrentNeeds
    case urgent
    case moneyBoundary
    case anonymousProfile
    case registeredProfile
    case contributionHistoryEmpty
    case generosityPoints
    case nearbyNotifications
    case urgentNotifications
    case reminderNotifications
    case visitSignal
    case visitSignalSubmitted
    case officialVerificationBoundary
}

struct L10n {
    static func text(_ key: L10nKey, locale: AppLocale) -> String {
        switch locale {
        case .english:
            return english[key] ?? key.rawValue
        case .spanish:
            return spanish[key] ?? english[key] ?? key.rawValue
        }
    }

    private static let english: [L10nKey: String] = [
        .tabMap: "Map",
        .tabList: "List",
        .tabProfile: "Profile",
        .tabSettings: "Settings",
        .languageEnglish: "English",
        .languageSpanish: "Spanish",
        .nearbyCenters: "Nearby Kindness Centers",
        .searchPlaceholder: "Search centers",
        .emptyCenters: "No Kindness Centers found.",
        .currentNeeds: "Current needs",
        .noCurrentNeeds: "No current needs are listed.",
        .urgent: "Urgent",
        .moneyBoundary: "Money donations require a verified payment handoff. This app does not collect bank or payment details.",
        .anonymousProfile: "Anonymous browsing",
        .registeredProfile: "Registered profile",
        .contributionHistoryEmpty: "No contributions recorded yet.",
        .generosityPoints: "Generosity points are informational and not redeemable.",
        .nearbyNotifications: "Nearby center alerts",
        .urgentNotifications: "Urgent need alerts",
        .reminderNotifications: "Reminders",
        .visitSignal: "I visited this center",
        .visitSignalSubmitted: "Visit signal saved. Official verification happens through a future center workflow.",
        .officialVerificationBoundary: "Official verification requires a future backend and center backoffice workflow."
    ]

    private static let spanish: [L10nKey: String] = [
        .tabMap: "Mapa",
        .tabList: "Lista",
        .tabProfile: "Perfil",
        .tabSettings: "Ajustes",
        .languageEnglish: "Ingles",
        .languageSpanish: "Espanol",
        .nearbyCenters: "Centros de bondad cercanos",
        .searchPlaceholder: "Buscar centros",
        .emptyCenters: "No se encontraron centros de bondad.",
        .currentNeeds: "Necesidades actuales",
        .noCurrentNeeds: "No hay necesidades actuales publicadas.",
        .urgent: "Urgente",
        .moneyBoundary: "Las donaciones de dinero requieren una transferencia de pago verificada. Esta app no recopila datos bancarios ni de pago.",
        .anonymousProfile: "Navegacion anonima",
        .registeredProfile: "Perfil registrado",
        .contributionHistoryEmpty: "Aun no hay contribuciones registradas.",
        .generosityPoints: "Los puntos de generosidad son informativos y no son canjeables.",
        .nearbyNotifications: "Alertas de centros cercanos",
        .urgentNotifications: "Alertas de necesidades urgentes",
        .reminderNotifications: "Recordatorios",
        .visitSignal: "Visite este centro",
        .visitSignalSubmitted: "Senal de visita guardada. La verificacion oficial ocurre en un futuro flujo del centro.",
        .officialVerificationBoundary: "La verificacion oficial requiere un futuro backend y portal para centros."
    ]
}

