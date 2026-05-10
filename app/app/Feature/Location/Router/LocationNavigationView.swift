import SwiftUI

struct LocationNavigationView: View {

    @Bindable var router: AppRouter

    var body: some View {
        NavigationStack(path: $router.path) {
            LocationView()
                .appDestination()
        }
        .environment(router)
    }
}
