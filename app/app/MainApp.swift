import SwiftUI

@main
struct MainApp: App {

    @State private var router = AppRouter()

    var body: some Scene {
        WindowGroup {
            RootView()
                .environment(router)
        }
    }
}
