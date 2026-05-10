import SwiftUI

struct AuthenticationNavigationView: View {

    @State private var router = AuthenticationRouter()

    var body: some View {
        NavigationStack(path: Bindable(router).path) {
            LoginView()
                .navigationTitle("로그인")
                .navigationBarTitleDisplayMode(.inline)
                .navigationDestination(for: AuthenticationRoute.self) { route in
                    switch route {
                    case .login:
                        LoginView()
                    case .signup:
                        SignupView()
                    case .setup:
                        SetupView()
                    }
                }
        }
        .environment(router)
    }
}
