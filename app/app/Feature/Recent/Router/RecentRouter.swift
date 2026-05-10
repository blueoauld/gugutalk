import SwiftUI

@Observable
final class RecentRouter {

    var path = NavigationPath()
    
    func push(_ route: RecentRoute) {
        path.append(route)
    }
    
    func pop() {
        path.removeLast()
    }
    
    func root() {
        path.removeLast(path.count)
    }
}
