import MapKit
import Shared

// Native coordinate conversion only; catalog data and discovery rules stay in Kotlin.
extension KindnessCenter {
    var coordinate: CLLocationCoordinate2D {
        CLLocationCoordinate2D(latitude: coordinates.latitude, longitude: coordinates.longitude)
    }
}

extension LocalizedText {
    func resolve(_ locale: AppLocale) -> String {
        resolve(preferredLocale: locale.shared, fallbackLocale: SupportedLocale.companion.fallback)
    }
}

extension CenterCategory {
    func label(_ locale: AppLocale) -> String {
        L10n.resource("category_\(code)", locale: locale)
    }
}

extension HelpMethod {
    func label(_ locale: AppLocale) -> String {
        L10n.resource("help_with_\(code)", locale: locale)
    }
}

extension VerificationStatus {
    func label(_ locale: AppLocale) -> String {
        L10n.resource("verification_\(code)", locale: locale)
    }
}
