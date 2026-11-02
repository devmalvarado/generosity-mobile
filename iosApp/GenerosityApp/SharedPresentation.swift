import MapKit
import Shared

func discoveryMapRect(coordinates: [CLLocationCoordinate2D], selected: CLLocationCoordinate2D?) -> MKMapRect? {
    let valid = coordinates.filter { CLLocationCoordinate2DIsValid($0) }
    guard !valid.isEmpty else { return nil }
    if let focus = selected ?? (valid.count == 1 ? valid.first : nil), CLLocationCoordinate2DIsValid(focus) {
        let point = MKMapPoint(focus)
        let radius = 2_500 * MKMapPointsPerMeterAtLatitude(focus.latitude)
        return MKMapRect(x: point.x - radius, y: point.y - radius, width: radius * 2, height: radius * 2)
    }
    return valid.map { MKMapPoint($0) }.reduce(MKMapRect.null) { rect, point in
        rect.union(MKMapRect(x: point.x, y: point.y, width: 1, height: 1))
    }
}

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
