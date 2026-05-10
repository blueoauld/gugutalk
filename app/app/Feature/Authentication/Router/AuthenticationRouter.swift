import SwiftUI

@Observable
final class AuthenticationRouter {
    
    var path = NavigationPath()
    
    func push(_ route: AuthenticationRoute) {
        path.append(route)
    }
    
    func pop() {
        path.removeLast()
    }
    
    func root() {
        path.removeLast(path.count)
    }
}
