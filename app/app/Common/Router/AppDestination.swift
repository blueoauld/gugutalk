import SwiftUI

struct AppDestination: ViewModifier {

    func body(content: Content) -> some View {
        content
            .navigationDestination(for: AppRoute.self) { route in
                switch route {
                case .recent:
                    RecentView()
                case .location:
                    LocationView()
                case .member(let memberId):
                    MemberView(memberId: memberId)
                }
            }
    }
}

extension View {

    func appDestination() -> some View {
        modifier(AppDestination())
    }
}
