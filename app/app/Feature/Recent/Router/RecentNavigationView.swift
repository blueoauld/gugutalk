import SwiftUI

struct RecentNavigationView: View {

    @Bindable var router: RecentRouter

    var body: some View {
        NavigationStack(path: Bindable(router).path) {
            RecentView()
                .navigationDestination(for: RecentRoute.self) { route in
                    switch route {
                    case .member(let memberId):
                        MemberView(memberId: memberId)
                    }
                }
        }
        .environment(router)
    }
}
