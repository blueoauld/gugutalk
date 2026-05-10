import SwiftUI

@Observable
final class AppRouter {

    var path = NavigationPath()

    func push(_ route: AppRoute) {
        path.append(route)
    }

    func pop() {
        path.removeLast()
    }

    func root() {
        path.removeLast(path.count)
    }

    func clear(_ route: AppRoute) {
        path = NavigationPath([route])
    }
}
