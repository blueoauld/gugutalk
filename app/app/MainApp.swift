import SwiftUI

@main
struct MainApp: App {

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
