import MapKit
import Shared

func discoveryMapRect(coordinates: [CLLocationCoordinate2D], selected: CLLocationCoordinate2D?, origin: CLLocationCoordinate2D? = nil) -> MKMapRect? {
    let valid = coordinates.filter { CLLocationCoordinate2DIsValid($0) }
    if let focus = selected ?? origin ?? (valid.count == 1 ? valid.first : nil), CLLocationCoordinate2DIsValid(focus) {
        let point = MKMapPoint(focus)
        let radius = (selected == nil && origin != nil ? 10_000.0 : 2_500.0) * MKMapPointsPerMeterAtLatitude(focus.latitude)
        return MKMapRect(x: point.x - radius, y: point.y - radius, width: radius * 2, height: radius * 2)
    }
    guard !valid.isEmpty else { return nil }
    return valid.map { MKMapPoint($0) }.reduce(MKMapRect.null) { rect, point in
        rect.union(MKMapRect(x: point.x, y: point.y, width: 1, height: 1))
    }
}

// Native coordinate conversion only; catalog data and discovery rules stay in Kotlin.
extension KindnessCenter {
    var coordinate: CLLocationCoordinate2D {
        CLLocationCoordinate2D(latitude: coordinates.latitude, longitude: coordinates.longitude)
    }

    var googleMapsURL: URL? {
        guard CLLocationCoordinate2DIsValid(coordinate) else { return nil }
        var url = URLComponents(string: "https://www.google.com/maps/search/")!
        url.queryItems = [URLQueryItem(name: "api", value: "1"),
                         URLQueryItem(name: "query", value: "\(coordinates.latitude),\(coordinates.longitude)")]
        return url.url
    }

    func appleMapsItem(locale: AppLocale) -> MKMapItem? {
        guard CLLocationCoordinate2DIsValid(coordinate) else { return nil }
        let item = MKMapItem(placemark: MKPlacemark(coordinate: coordinate))
        item.name = name.resolve(locale)
        return item
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
