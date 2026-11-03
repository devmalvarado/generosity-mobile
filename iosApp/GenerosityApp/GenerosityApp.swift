import SwiftUI

@main
struct GenerosityApp: App {
    @UIApplicationDelegateAdaptor(PushAppDelegate.self) private var pushDelegate
    init() {
        #if DEBUG
        if ProcessInfo.processInfo.arguments.contains("--reset-entry") {
            UserDefaults.standard.removeObject(forKey: "generosity.entered")
        }
        #endif
    }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

