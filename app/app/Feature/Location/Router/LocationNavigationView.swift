import SwiftUI

struct LocationNavigationView: View {

    @State private var router = LocationRouter()

    var body: some View {
        NavigationStack(path: Bindable(router).path) {
            LocationView()
                .navigationTitle("위치")
                .navigationBarTitleDisplayMode(.inline)
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
