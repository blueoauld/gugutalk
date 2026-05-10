import SwiftUI

struct RootView: View {

    @Environment(AppRouter.self) private var router

    var body: some View {
        NavigationStack(path: Bindable(router).path) {
            LoginView()
                .navigationDestination(for: AppRoute.self) { route in
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
    }
}
