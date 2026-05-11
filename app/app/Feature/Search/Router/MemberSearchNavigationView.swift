import SwiftUI

struct MemberSearchNavigationView: View {

    @Bindable var router: AppRouter

    var body: some View {
        NavigationStack(path: $router.path) {
            MemberSearchView()
                .appDestination()
        }
        .environment(router)
    }
}
