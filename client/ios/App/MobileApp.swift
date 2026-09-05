import SwiftUI

@main
struct MobilingApp: App {
    @UIApplicationDelegateAdaptor(PushNotificationAppDelegate.self) private var pushNotificationAppDelegate

    var body: some Scene {
        WindowGroup { ContentView() }
    }
}
