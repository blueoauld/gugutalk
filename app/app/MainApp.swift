import SwiftUI

@main
struct MainApp: App {

    @State private var session = SessionStore()

    init() {
        if TokenStorage.shared.deviceId == nil {
            TokenStorage.shared.deviceId = UUID().uuidString
        }
    }

    var body: some Scene {
        WindowGroup {
            RootView()
                .environment(session)
                .toastHost()
        }
    }
}
