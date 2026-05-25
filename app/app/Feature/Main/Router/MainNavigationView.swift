import SwiftUI

struct MainNavigationView: View {

    @Bindable var router: AppRouter

    var body: some View {
        NavigationStack(path: $router.path) {
            MainView()
                .appDestination()
        }
        .environment(router)
    }
}
