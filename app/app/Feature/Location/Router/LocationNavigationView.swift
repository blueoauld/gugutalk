import SwiftUI

struct LocationNavigationView: View {

    @Bindable var router: LocationRouter

    var body: some View {
        NavigationStack(path: Bindable(router).path) {
            LocationView()
                .navigationDestination(for: LocationRoute.self) { route in
                    switch route {
                    case .member(let memberId):
                        MemberView(memberId: memberId)
                    }
                }
        }
        .environment(router)
    }
}
