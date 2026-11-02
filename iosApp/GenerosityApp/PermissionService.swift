import Combine
import CoreLocation
import Foundation
import UserNotifications

final class PermissionService: NSObject, ObservableObject, CLLocationManagerDelegate {
    @Published private(set) var locationAuthorization: CLAuthorizationStatus = .notDetermined
    @Published private(set) var currentLocation: CLLocation?
    @Published private(set) var locating = false
    @Published private(set) var locationMessage: String?
    @Published var notificationsAuthorized = false

    private let locationManager: CLLocationManager
    private let timeoutSeconds: TimeInterval
    private var timeout: Task<Void, Never>?
    private var usingCurrentLocation = false
    private var choseOrigin = false
    private var requestingFix = false

    init(locationManager: CLLocationManager = CLLocationManager(), timeoutSeconds: TimeInterval = 15) {
        self.locationManager = locationManager
        self.timeoutSeconds = timeoutSeconds
        super.init()
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyHundredMeters
        locationAuthorization = locationManager.authorizationStatus
    }

    deinit { timeout?.cancel(); locationManager.stopUpdatingLocation() }

    var locationDenied: Bool { locationAuthorization == .denied || locationAuthorization == .restricted }
    var approximateLocation: Bool {
        currentLocation != nil && (locationManager.accuracyAuthorization == .reducedAccuracy || currentLocation!.horizontalAccuracy > 1_000)
    }

    func requestCurrentLocation() {
        choseOrigin = true
        usingCurrentLocation = true
        stopRequest()
        locationMessage = nil
        locationAuthorization = locationManager.authorizationStatus
        switch locationAuthorization {
        case .authorizedWhenInUse, .authorizedAlways: beginFix()
        case .notDetermined:
            locating = true
            locationManager.requestWhenInUseAuthorization()
        default: failLocation("location_denied")
        }
    }

    func resumeLocation() {
        locationAuthorization = locationManager.authorizationStatus
        if !choseOrigin && (locationAuthorization == .authorizedWhenInUse || locationAuthorization == .authorizedAlways) {
            requestCurrentLocation()
        } else if usingCurrentLocation {
            if locationDenied { failLocation("location_denied") }
            else if !locating && (currentLocation == nil || !Self.isUsable(currentLocation!)) {
                currentLocation = nil
                requestCurrentLocation()
            }
        }
    }

    func suspendLocation() { stopRequest() }

    func useManualArea() {
        choseOrigin = true
        usingCurrentLocation = false
        stopRequest()
        currentLocation = nil
        locationMessage = nil
    }

    private func beginFix() {
        guard !requestingFix else { return }
        requestingFix = true
        locating = true
        timeout = Task { @MainActor [weak self, timeoutSeconds] in
            try? await Task.sleep(nanoseconds: UInt64(timeoutSeconds * 1_000_000_000))
            guard !Task.isCancelled else { return }
            self?.failLocation("location_unavailable")
        }
        locationManager.requestLocation()
    }

    private func stopRequest() {
        timeout?.cancel(); timeout = nil
        locationManager.stopUpdatingLocation()
        requestingFix = false
        locating = false
    }

    private func failLocation(_ message: String) {
        stopRequest()
        currentLocation = nil
        locationMessage = message
    }

    static func isUsable(_ location: CLLocation, now: Date = Date()) -> Bool {
        let age = now.timeIntervalSince(location.timestamp)
        return CLLocationCoordinate2DIsValid(location.coordinate) && location.horizontalAccuracy.isFinite &&
            location.horizontalAccuracy >= 0 && location.horizontalAccuracy <= 5_000 && age >= -5 && age <= 120
    }

    func requestLocationPermission() {
        locationManager.requestWhenInUseAuthorization()
    }

    func requestNotificationPermission() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .badge, .sound]) { granted, _ in
            DispatchQueue.main.async {
                self.notificationsAuthorized = granted
            }
        }
    }

    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        locationAuthorization = manager.authorizationStatus
        guard usingCurrentLocation else { return }
        if locationDenied { failLocation("location_denied") }
        else if locating && !requestingFix && (locationAuthorization == .authorizedWhenInUse || locationAuthorization == .authorizedAlways) { beginFix() }
    }

    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard usingCurrentLocation, requestingFix else { return }
        guard manager.authorizationStatus == .authorizedWhenInUse || manager.authorizationStatus == .authorizedAlways else {
            failLocation("location_denied"); return
        }
        guard let fix = locations.last(where: { Self.isUsable($0) }) else {
            failLocation("location_unavailable"); return
        }
        stopRequest()
        locationMessage = nil
        currentLocation = fix
    }

    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        guard usingCurrentLocation, requestingFix else { return }
        failLocation((error as? CLError)?.code == .denied ? "location_denied" : "location_unavailable")
    }
}

