import SwiftUI

struct RecentNavigationView: View {

    @State private var router = RecentRouter()

    var body: some View {

        NavigationStack(path: Bindable(router).path) {
            RecentView()
                .navigationTitle("최근")
                .navigationBarTitleDisplayMode(.inline)
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
