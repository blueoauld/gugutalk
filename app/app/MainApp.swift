import SwiftUI
import GoogleMobileAds

@main
struct MainApp: App {

    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    @State private var showSplash = true

    init() {
        if TokenStorage.shared.deviceId == nil {
            TokenStorage.shared.deviceId = UUID().uuidString
        }

        MobileAds.shared.start(completionHandler: nil)
    }

    var body: some Scene {
        WindowGroup {
            ZStack {
                RootView()
                    .environment(SessionStore.shared)
                    .toastHost()

                if showSplash {
                    SplashView()
                        .transition(.opacity)
                        .zIndex(1)
                }
            }
            .task {
                try? await Task.sleep(for: .seconds(2.0))

                withAnimation(.easeInOut(duration: 0.4)) {
                    showSplash = false
                }
            }
        }
    }
}
