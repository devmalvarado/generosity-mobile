import XCTest
import MapKit
import Shared
@testable import Generosity

final class LocalizationTests: XCTestCase {
    func testSpanishAndEnglishPluralsUseTheSelectedLanguage() {
        XCTAssertEqual(L10n.remaining(0, locale: .spanish), "Faltan 0 unidades")
        XCTAssertEqual(L10n.remaining(1, locale: .spanish), "Falta 1 unidad")
        XCTAssertEqual(L10n.remaining(15, locale: .spanish), "Faltan 15 unidades")
        XCTAssertEqual(L10n.remaining(1, locale: .english), "1 item still needed")
        XCTAssertEqual(L10n.remaining(15, locale: .english), "15 items still needed")
        XCTAssertEqual(L10n.plural("offer_goods", count: 1, locale: .spanish), "Puedo llevar 1 artículo")
        XCTAssertEqual(L10n.plural("offer_goods", count: 5, locale: .spanish), "Puedo llevar 5 artículos")
    }

    func testRegionalLanguageAndAccentedResources() {
        let locale = AppLocale(rawValue: "es_MX")!
        XCTAssertEqual(locale.rawValue, "es")
        XCTAssertEqual(locale.nativeName, "Español")
        XCTAssertEqual(L10n.resource("visit_signal", locale: locale), "Visité este centro")
        XCTAssertEqual(L10n.resource("category_organ_tissue_donation", locale: locale), "Donación de órganos y tejidos")
        XCTAssertEqual(locale.shared, AppLocale.spanish.shared)
        XCTAssertNil(AppLocale(rawValue: "not-a-language"))
    }
}

final class MapFramingTests: XCTestCase {
    @MainActor
    func testNativeMapUpdatesPinsAndCameraWithoutStaleRegionFeedback() {
        let centers = BundledKindnessCatalog.shared.centers()
        let initial = CenterMapView(locale: .english, centers: centers, selectedCenterId: nil, onSelect: { _ in })
        let coordinator = initial.makeCoordinator()
        let map = CenterMapView.MapCanvas(frame: CGRect(x: 0, y: 0, width: 320, height: 400))
        map.delegate = coordinator
        coordinator.update(map, owner: initial)
        XCTAssertEqual(map.annotations.count, centers.count)
        for center in centers.reversed() {
            let selected = CenterMapView(locale: .english, centers: centers, selectedCenterId: center.id, onSelect: { _ in })
            coordinator.update(map, owner: selected)
            XCTAssertEqual(map.centerCoordinate.latitude, center.coordinates.latitude, accuracy: 0.0001)
            XCTAssertEqual(map.centerCoordinate.longitude, center.coordinates.longitude, accuracy: 0.0001)
            XCTAssertEqual((map.selectedAnnotations.first as? CenterMapView.Annotation)?.id, center.id)
        }
        let pan = CLLocationCoordinate2D(latitude: 32.78, longitude: -96.8)
        map.setCenter(pan, animated: false)
        coordinator.update(map, owner: CenterMapView(locale: .spanish, centers: centers, selectedCenterId: centers.first!.id, onSelect: { _ in }))
        XCTAssertEqual(map.centerCoordinate.latitude, pan.latitude, accuracy: 0.0001)
        XCTAssertEqual(map.centerCoordinate.longitude, pan.longitude, accuracy: 0.0001)
        coordinator.update(map, owner: CenterMapView(locale: .english, centers: [], selectedCenterId: nil, onSelect: { _ in }))
        XCTAssertTrue(map.annotations.isEmpty)
        XCTAssertTrue(map.selectedAnnotations.isEmpty)
    }

    @MainActor
    func testCatalogOutsideDemoAreaAndSelectedCenterStayVisible() throws {
        let london = CLLocationCoordinate2D(latitude: 51.5, longitude: -0.12)
        let dallas = CLLocationCoordinate2D(latitude: 32.78, longitude: -96.8)
        let region = try XCTUnwrap(discoveryMapRect(coordinates: [london, dallas], selected: nil))
        for point in [london, dallas] {
            XCTAssertTrue(region.contains(MKMapPoint(point)))
        }
        // MapKit clamps continental zoom on small views; verify padded rendering with city results.
        let cityResults = [CLLocationCoordinate2D(latitude: 47.61, longitude: -122.33),
                           CLLocationCoordinate2D(latitude: 47.25, longitude: -122.44)]
        let cityRegion = try XCTUnwrap(discoveryMapRect(coordinates: cityResults, selected: nil))
        let map = CenterMapView.MapCanvas(frame: .zero)
        map.frameRect(cityRegion)
        map.frame = CGRect(x: 0, y: 0, width: 320, height: 400)
        map.layoutIfNeeded()
        for point in cityResults {
            XCTAssertTrue(map.bounds.contains(map.convert(point, toPointTo: map)))
        }
        map.frame.size = CGSize(width: 240, height: 320)
        map.layoutIfNeeded()
        for point in cityResults {
            let projected = map.convert(point, toPointTo: map)
            XCTAssertTrue(map.bounds.insetBy(dx: 40, dy: 40).contains(projected), "Projected \(point): \(projected), bounds \(map.bounds), visible \(map.visibleMapRect)")
        }
        let selected = try XCTUnwrap(discoveryMapRect(coordinates: [london, dallas], selected: dallas))
        let midpoint = MKMapPoint(x: selected.midX, y: selected.midY).coordinate
        XCTAssertEqual(midpoint.latitude, dallas.latitude, accuracy: 0.00001)
        XCTAssertEqual(midpoint.longitude, dallas.longitude, accuracy: 0.00001)
        XCTAssertLessThan(selected.width, region.width)
        let pendingMap = CenterMapView.MapCanvas(frame: .zero)
        pendingMap.frameRect(region)
        pendingMap.frame = CGRect(x: 0, y: 0, width: 320, height: 400)
        pendingMap.frameRect(selected)
        pendingMap.layoutIfNeeded()
        XCTAssertEqual(pendingMap.centerCoordinate.latitude, dallas.latitude, accuracy: 0.0001)
        XCTAssertEqual(pendingMap.centerCoordinate.longitude, dallas.longitude, accuracy: 0.0001)
        let filtered = try XCTUnwrap(discoveryMapRect(coordinates: [london], selected: nil))
        XCTAssertTrue(filtered.contains(MKMapPoint(london)))
        XCTAssertGreaterThan(filtered.height, 0)
        XCTAssertNil(discoveryMapRect(coordinates: [], selected: nil))
        XCTAssertNil(discoveryMapRect(coordinates: [.init(latitude: .nan, longitude: 1)], selected: nil))
    }

}
