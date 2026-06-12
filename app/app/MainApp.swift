import SwiftUI
import GoogleMobileAds

@main
struct MainApp: App {

    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    @State private var showSplash = true
    @State private var showUpdateAlert = false

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
                async let updateAvailable = VersionChecker.isUpdateAvailable()
                try? await Task.sleep(for: .seconds(2.0))

                withAnimation(.easeInOut(duration: 0.4)) {
                    showSplash = false
                }

                if await updateAvailable {
                    showUpdateAlert = true
                }
            }
            .alert("안내", isPresented: $showUpdateAlert) {
                Button("설치") {
                    if let url = URL(string: "itms-apps://itunes.apple.com/app/6778419443") {
                        UIApplication.shared.open(url)
                    }
                }
                Button("닫기", role: .cancel) { }
            } message: {
                Text("최신 버전으로 업데이트 해주시길 바랍니다.")
            }
        }
    }
}
