import UIKit
import UserNotifications
import FirebaseCore
import FirebaseMessaging
import SharedBridge

struct PushSignal: Equatable {
    let accountId: String
    let noticeId: String
    let open: Bool

    init?(_ data: [AnyHashable: Any], open: Bool) {
        guard let account = data["account_id"] as? String,
              account.range(of: "^[1-9][0-9]{0,18}$", options: .regularExpression) != nil,
              let notice = data["notice_id"] as? String, UUID(uuidString: notice) != nil else { return nil }
        accountId = account; noticeId = notice; self.open = open
    }
}

@MainActor
final class IOSPush: ObservableObject {
    static let shared = IOSPush()
    @Published var signal: PushSignal?
    @Published var token: String?
    private var registering = false

    func update(store: GenerosityStore) async {
        let settings = await UNUserNotificationCenter.current().notificationSettings()
        let enabled = store.state.remoteAccount && store.state.preferences.urgentNeedsEnabled
        let authorized = settings.authorizationStatus == .authorized || settings.authorizationStatus == .provisional
        let provisioned = Bundle.main.object(forInfoDictionaryKey: "GenerosityPushEnabled") as? String == "YES"
        store.setPushDevice(token, authorized: enabled && authorized && provisioned)
        if !enabled { UNUserNotificationCenter.current().removeAllDeliveredNotifications() }
        guard FirebaseApp.app() != nil else { return }
        Messaging.messaging().isAutoInitEnabled = enabled && authorized && provisioned
        if enabled && authorized && provisioned && !registering {
            registering = true
            UIApplication.shared.registerForRemoteNotifications()
        }
    }

    func registered(_ deviceToken: Data) {
        registering = false
        guard FirebaseApp.app() != nil else { return }
        Messaging.messaging().apnsToken = deviceToken
        Messaging.messaging().token { token, _ in
            Task { @MainActor in self.token = token }
        }
    }

    func registrationFailed() { registering = false }
}

final class PushAppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate, MessagingDelegate {
    func application(_ application: UIApplication, didFinishLaunchingWithOptions options: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {
        if Bundle.main.url(forResource: "GoogleService-Info", withExtension: "plist") != nil {
            FirebaseApp.configure()
            Messaging.messaging().delegate = self
        }
        UNUserNotificationCenter.current().delegate = self
        return true
    }

    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        Task { @MainActor in IOSPush.shared.registered(deviceToken) }
    }
    func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
        Task { @MainActor in IOSPush.shared.registrationFailed() }
    }
    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        Task { @MainActor in IOSPush.shared.token = fcmToken }
    }
    func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completion: @escaping (UNNotificationPresentationOptions) -> Void) {
        let signal = PushSignal(notification.request.content.userInfo, open: false)
        Task { @MainActor in IOSPush.shared.signal = signal }
        // The app renders one banner after reconciling its account-owned inbox.
        completion([])
    }
    func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse, withCompletionHandler completion: @escaping () -> Void) {
        let signal = PushSignal(response.notification.request.content.userInfo, open: true)
        Task { @MainActor in IOSPush.shared.signal = signal }
        completion()
    }
}
