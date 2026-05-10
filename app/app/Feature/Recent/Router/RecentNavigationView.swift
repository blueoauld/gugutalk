import SwiftUI

struct RecentNavigationView: View {

    @Bindable var router: AppRouter

    var body: some View {
        NavigationStack(path: $router.path) {
            RecentView()
                .appDestination()
        }
        .environment(router)
    }
}
