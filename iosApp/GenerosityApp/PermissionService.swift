import CoreLocation
import Foundation
import UserNotifications

final class PermissionService: NSObject, ObservableObject, CLLocationManagerDelegate {
    @Published var locationAuthorization: CLAuthorizationStatus = .notDetermined
    @Published var notificationsAuthorized = false

    private let locationManager = CLLocationManager()

    override init() {
        super.init()
        locationManager.delegate = self
        locationAuthorization = locationManager.authorizationStatus
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
    }
}

