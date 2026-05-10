import SwiftUI

struct SettingNavigationView: View {

    @Bindable var router: AppRouter

    var body: some View {
        NavigationStack(path: $router.path) {
            SettingView()
                .appDestination()
        }
        .environment(router)
    }
}
