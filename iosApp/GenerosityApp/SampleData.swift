import Foundation
import MapKit

struct AppLocalizedText {
    let values: [String: String]

    func resolve(_ locale: AppLocale, fallback: AppLocale = .english) -> String {
        if let preferred = values[locale.rawValue], !preferred.isEmpty {
            return preferred
        }
        if let fallbackValue = values[fallback.rawValue], !fallbackValue.isEmpty {
            return fallbackValue
        }
        return values.values.first(where: { !$0.isEmpty }) ?? ""
    }
}

enum AppCategory: String, CaseIterable, Identifiable {
    case childrenSupport = "children_support"
    case olderAdults = "older_adults"
    case disabilitySupport = "disability_support"
    case health = "health"
    case addictionRecovery = "addiction_recovery"
    case communityKitchen = "community_kitchen"
    case education = "education"
    case vulnerableFamilies = "vulnerable_families"
    case organTissueDonation = "organ_tissue_donation"

    var id: String { rawValue }

    func label(_ locale: AppLocale) -> String {
        switch (self, locale) {
        case (.childrenSupport, .english): return "Children support"
        case (.childrenSupport, .spanish): return "Apoyo a ninos"
        case (.olderAdults, .english): return "Older adults"
        case (.olderAdults, .spanish): return "Adultos mayores"
        case (.disabilitySupport, .english): return "Disability support"
        case (.disabilitySupport, .spanish): return "Apoyo a discapacidad"
        case (.health, .english): return "Health"
        case (.health, .spanish): return "Salud"
        case (.addictionRecovery, .english): return "Addiction recovery"
        case (.addictionRecovery, .spanish): return "Recuperacion de adicciones"
        case (.communityKitchen, .english): return "Community kitchen"
        case (.communityKitchen, .spanish): return "Comedor comunitario"
        case (.education, .english): return "Education"
        case (.education, .spanish): return "Educacion"
        case (.vulnerableFamilies, .english): return "Vulnerable families"
        case (.vulnerableFamilies, .spanish): return "Familias vulnerables"
        case (.organTissueDonation, .english): return "Organ or tissue donation"
        case (.organTissueDonation, .spanish): return "Donacion de organos o tejidos"
        }
    }
}

enum AppHelpMethod: String, Identifiable {
    case time
    case goods
    case money

    var id: String { rawValue }

    func label(_ locale: AppLocale) -> String {
        switch (self, locale) {
        case (.time, .english): return "Donate time"
        case (.time, .spanish): return "Donar tiempo"
        case (.goods, .english): return "Donate goods"
        case (.goods, .spanish): return "Donar bienes"
        case (.money, .english): return "Donate money"
        case (.money, .spanish): return "Donar dinero"
        }
    }
}

enum AppVerificationStatus: String {
    case imported
    case visitSignaled
    case officiallyVerified

    func label(_ locale: AppLocale) -> String {
        switch (self, locale) {
        case (.imported, .english): return "Imported"
        case (.imported, .spanish): return "Importado"
        case (.visitSignaled, .english): return "Visited by user"
        case (.visitSignaled, .spanish): return "Visitado por usuario"
        case (.officiallyVerified, .english): return "Officially verified"
        case (.officiallyVerified, .spanish): return "Verificado oficialmente"
        }
    }
}

struct AppNeed: Identifiable {
    let id: String
    let title: AppLocalizedText
    let description: AppLocalizedText
    let helpMethods: [AppHelpMethod]
    let urgent: Bool
    let expiresOnIsoDate: String?

    func isCurrent(referenceIsoDate: String) -> Bool {
        guard let expiresOnIsoDate else { return true }
        return expiresOnIsoDate >= referenceIsoDate
    }
}

struct AppKindnessCenter: Identifiable {
    let id: String
    let name: AppLocalizedText
    let description: AppLocalizedText
    let categories: [AppCategory]
    let coordinate: CLLocationCoordinate2D
    let address: AppLocalizedText
    let verificationStatus: AppVerificationStatus
    let needs: [AppNeed]
}

enum SampleData {
    static let centers: [AppKindnessCenter] = [
        AppKindnessCenter(
            id: "kc-children-001",
            name: .init(values: ["en": "Bright Futures Home", "es": "Hogar Futuros Brillantes"]),
            description: .init(values: ["en": "Support center for children and families.", "es": "Centro de apoyo para ninos y familias."]),
            categories: [.childrenSupport, .education],
            coordinate: CLLocationCoordinate2D(latitude: 19.4326, longitude: -99.1332),
            address: .init(values: ["en": "Historic Center, Mexico City", "es": "Centro Historico, Ciudad de Mexico"]),
            verificationStatus: .imported,
            needs: [
                AppNeed(
                    id: "need-school-supplies",
                    title: .init(values: ["en": "School supplies", "es": "Utiles escolares"]),
                    description: .init(values: ["en": "Notebooks, pencils, backpacks, and art supplies.", "es": "Cuadernos, lapices, mochilas y material de arte."]),
                    helpMethods: [.goods, .money],
                    urgent: true,
                    expiresOnIsoDate: "2026-12-31"
                )
            ]
        ),
        AppKindnessCenter(
            id: "kc-seniors-001",
            name: .init(values: ["en": "Golden Years Community Kitchen", "es": "Comedor Comunitario Anos Dorados"]),
            description: .init(values: ["en": "Meals and social support for older adults.", "es": "Alimentos y apoyo social para adultos mayores."]),
            categories: [.olderAdults, .communityKitchen],
            coordinate: CLLocationCoordinate2D(latitude: 19.4285, longitude: -99.1277),
            address: .init(values: ["en": "Doctores, Mexico City", "es": "Doctores, Ciudad de Mexico"]),
            verificationStatus: .visitSignaled,
            needs: [
                AppNeed(
                    id: "need-volunteers-lunch",
                    title: .init(values: ["en": "Lunch volunteers", "es": "Voluntarios para comida"]),
                    description: .init(values: ["en": "Two-hour shifts to serve lunch during weekdays.", "es": "Turnos de dos horas para servir comida entre semana."]),
                    helpMethods: [.time],
                    urgent: false,
                    expiresOnIsoDate: nil
                )
            ]
        ),
        AppKindnessCenter(
            id: "kc-health-001",
            name: .init(values: ["en": "Open Health Bridge", "es": "Puente de Salud Abierta"]),
            description: .init(values: ["en": "Health, disability, recovery, and vulnerable-family support.", "es": "Apoyo de salud, discapacidad, recuperacion y familias vulnerables."]),
            categories: [.health, .disabilitySupport, .addictionRecovery, .vulnerableFamilies, .organTissueDonation],
            coordinate: CLLocationCoordinate2D(latitude: 19.4371, longitude: -99.1448),
            address: .init(values: ["en": "Juarez, Mexico City", "es": "Juarez, Ciudad de Mexico"]),
            verificationStatus: .officiallyVerified,
            needs: [
                AppNeed(
                    id: "need-health-kits",
                    title: .init(values: ["en": "Health kits", "es": "Kits de salud"]),
                    description: .init(values: ["en": "Basic hygiene and first-aid kits for families.", "es": "Kits basicos de higiene y primeros auxilios para familias."]),
                    helpMethods: [.goods, .money],
                    urgent: true,
                    expiresOnIsoDate: "2026-08-31"
                )
            ]
        )
    ]
}

