import SwiftUI

@main
struct MainApp: App {

    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    init() {
        if TokenStorage.shared.deviceId == nil {
            TokenStorage.shared.deviceId = UUID().uuidString
        }
    }

    var body: some Scene {
        WindowGroup {
            RootView()
                .environment(SessionStore.shared)
                .toastHost()
        }
    }
}
