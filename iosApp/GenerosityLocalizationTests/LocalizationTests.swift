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

final class ExternalMapTests: XCTestCase {
    func testDestinationsUseEachCentersCoordinatesAndLocalizedName() throws {
        for center in BundledKindnessCatalog.shared.centers() {
            let url = try XCTUnwrap(center.googleMapsURL)
            let parts = try XCTUnwrap(URLComponents(url: url, resolvingAgainstBaseURL: false))
            XCTAssertEqual(parts.scheme, "https")
            XCTAssertEqual(parts.host, "www.google.com")
            XCTAssertEqual(parts.path, "/maps/search/")
            XCTAssertEqual(parts.queryItems, [URLQueryItem(name: "api", value: "1"),
                URLQueryItem(name: "query", value: "\(center.coordinates.latitude),\(center.coordinates.longitude)")])
            for locale in [AppLocale.spanish, .english] {
                let item = try XCTUnwrap(center.appleMapsItem(locale: locale))
                XCTAssertEqual(item.name, center.name.resolve(locale))
                XCTAssertEqual(item.placemark.coordinate.latitude, center.coordinates.latitude)
                XCTAssertEqual(item.placemark.coordinate.longitude, center.coordinates.longitude)
                XCTAssertFalse(item.isCurrentLocation)
            }
        }
    }

    func testInvalidCoordinatesDoNotProduceMapActions() {
        let source = BundledKindnessCatalog.shared.centers()[0]
        for point in [Coordinates(latitude: .nan, longitude: 0), Coordinates(latitude: 91, longitude: 0),
                      Coordinates(latitude: 0, longitude: .infinity), Coordinates(latitude: 0, longitude: -181)] {
            let center = source.doCopy(id: source.id, name: source.name, description: source.description_,
                categories: source.categories, coordinates: point, address: source.address,
                contactInfo: source.contactInfo, verificationStatus: source.verificationStatus,
                needs: source.needs, importedAtIsoDate: source.importedAtIsoDate)
            XCTAssertNil(center.googleMapsURL)
            XCTAssertNil(center.appleMapsItem(locale: .spanish))
        }
    }
}

final class MapFramingTests: XCTestCase {
    func testSelectedSearchAreaFramesEmptyResultsAndSelectionTakesPrecedence() throws {
        let origin = CLLocationCoordinate2D(latitude: 19.35, longitude: -99.16)
        let area = try XCTUnwrap(discoveryMapRect(coordinates: [], selected: nil, origin: origin))
        XCTAssertTrue(area.contains(MKMapPoint(origin)))
        XCTAssertEqual(area.width / MKMapPointsPerMeterAtLatitude(origin.latitude), 20_000, accuracy: 1)
        let center = CLLocationCoordinate2D(latitude: 19.37, longitude: -99.17)
        let selected = try XCTUnwrap(discoveryMapRect(coordinates: [center], selected: center, origin: origin))
        XCTAssertEqual(selected.width / MKMapPointsPerMeterAtLatitude(center.latitude), 5_000, accuracy: 1)
        XCTAssertTrue(selected.contains(MKMapPoint(center)))
    }

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

@MainActor
final class CurrentLocationTests: XCTestCase {
    func testPermissionMustBeGrantedBeforeRequestingCoordinates() {
        let manager = LocationManagerStub()
        manager.status = .notDetermined
        let service = PermissionService(locationManager: manager)
        service.requestCurrentLocation()
        XCTAssertEqual(manager.permissionRequests, 1)
        XCTAssertEqual(manager.fixRequests, 0)
        XCTAssertTrue(service.locating)
        manager.status = .authorizedWhenInUse
        service.locationManagerDidChangeAuthorization(manager)
        XCTAssertEqual(manager.fixRequests, 1)
        let fix = CLLocation(latitude: 19.35, longitude: -99.16)
        service.locationManager(manager, didUpdateLocations: [fix])
        XCTAssertEqual(service.currentLocation, fix)
        XCTAssertFalse(service.locating)
        XCTAssertNil(service.locationMessage)
    }

    func testDeniedAndRevokedPermissionClearLocationAndStopRequests() {
        let manager = LocationManagerStub()
        let service = PermissionService(locationManager: manager)
        service.requestCurrentLocation()
        service.locationManager(manager, didUpdateLocations: [CLLocation(latitude: 19.35, longitude: -99.16)])
        XCTAssertNotNil(service.currentLocation)
        manager.status = .denied
        service.locationManagerDidChangeAuthorization(manager)
        XCTAssertNil(service.currentLocation)
        XCTAssertEqual(service.locationMessage, "location_denied")
        XCTAssertTrue(service.locationDenied)
        service.requestCurrentLocation()
        XCTAssertEqual(manager.fixRequests, 1)
        XCTAssertFalse(service.locating)
        XCTAssertGreaterThan(manager.stops, 0)
    }

    func testManualSelectionAndBackgroundIgnoreLateFixes() {
        let manager = LocationManagerStub()
        let service = PermissionService(locationManager: manager)
        service.requestCurrentLocation()
        service.useManualArea()
        let fix = CLLocation(latitude: 19.35, longitude: -99.16)
        service.locationManager(manager, didUpdateLocations: [fix])
        service.resumeLocation()
        XCTAssertNil(service.currentLocation)
        XCTAssertEqual(manager.fixRequests, 1)
        service.requestCurrentLocation()
        service.suspendLocation()
        service.locationManager(manager, didUpdateLocations: [fix])
        XCTAssertNil(service.currentLocation)
        service.resumeLocation()
        XCTAssertEqual(manager.fixRequests, 3)
        service.useManualArea()
    }

    func testOldInvalidAndExcessivelyInaccurateFixesAreRejected() {
        let now = Date()
        func fix(_ age: TimeInterval, accuracy: Double = 100, latitude: Double = 19.35) -> CLLocation {
            CLLocation(coordinate: .init(latitude: latitude, longitude: -99.16), altitude: 0,
                       horizontalAccuracy: accuracy, verticalAccuracy: -1, timestamp: now.addingTimeInterval(-age))
        }
        XCTAssertTrue(PermissionService.isUsable(fix(120, accuracy: 5_000), now: now))
        for candidate in [fix(121), fix(-6), fix(0, accuracy: -1), fix(0, accuracy: 5_001),
                          fix(0, accuracy: .infinity), fix(0, latitude: .nan)] {
            XCTAssertFalse(PermissionService.isUsable(candidate, now: now))
        }
        let manager = LocationManagerStub()
        let service = PermissionService(locationManager: manager)
        service.requestCurrentLocation()
        service.locationManager(manager, didUpdateLocations: [fix(300)])
        XCTAssertNil(service.currentLocation)
        XCTAssertEqual(service.locationMessage, "location_unavailable")
    }

    func testApproximateFixIsAcceptedAndReportedAsApproximate() {
        let manager = LocationManagerStub()
        manager.precision = .reducedAccuracy
        let service = PermissionService(locationManager: manager)
        service.requestCurrentLocation()
        service.locationManager(manager, didUpdateLocations: [CLLocation(coordinate: .init(latitude: 19.35, longitude: -99.16),
            altitude: 0, horizontalAccuracy: 3_000, verticalAccuracy: -1, timestamp: Date())])
        XCTAssertNotNil(service.currentLocation)
        XCTAssertTrue(service.approximateLocation)
    }

    func testTimeoutAllowsRetryAndDoesNotAcceptLateResponse() async throws {
        let manager = LocationManagerStub()
        let service = PermissionService(locationManager: manager, timeoutSeconds: 0.01)
        service.requestCurrentLocation()
        try await Task.sleep(nanoseconds: 80_000_000)
        XCTAssertFalse(service.locating)
        XCTAssertEqual(service.locationMessage, "location_unavailable")
        service.locationManager(manager, didUpdateLocations: [CLLocation(latitude: 19.35, longitude: -99.16)])
        XCTAssertNil(service.currentLocation)
        service.requestCurrentLocation()
        XCTAssertEqual(manager.fixRequests, 2)
        XCTAssertNil(service.locationMessage)
        service.useManualArea()
    }

    func testOriginPinUsesLocationLabelAndIsRemovedWhenCleared() {
        let point = Coordinates(latitude: 19.35, longitude: -99.16)
        let owner = CenterMapView(locale: .spanish, centers: [], selectedCenterId: nil, onSelect: { _ in }, origin: point, deviceOrigin: true)
        let coordinator = owner.makeCoordinator()
        let map = CenterMapView.MapCanvas(frame: CGRect(x: 0, y: 0, width: 320, height: 240))
        map.delegate = coordinator
        coordinator.update(map, owner: owner)
        XCTAssertEqual(map.annotations.count, 1)
        XCTAssertEqual(map.annotations.first?.title, "Tu ubicación")
        XCTAssertEqual(map.centerCoordinate.latitude, point.latitude, accuracy: 0.001)
        coordinator.update(map, owner: CenterMapView(locale: .spanish, centers: [], selectedCenterId: nil, onSelect: { _ in }))
        XCTAssertTrue(map.annotations.isEmpty)
    }
}

private final class LocationManagerStub: CLLocationManager {
    var status = CLAuthorizationStatus.authorizedWhenInUse
    var precision = CLAccuracyAuthorization.fullAccuracy
    var permissionRequests = 0
    var fixRequests = 0
    var stops = 0
    override var authorizationStatus: CLAuthorizationStatus { status }
    override var accuracyAuthorization: CLAccuracyAuthorization { precision }
    override func requestWhenInUseAuthorization() { permissionRequests += 1 }
    override func requestLocation() { fixRequests += 1 }
    override func stopUpdatingLocation() { stops += 1 }
}
