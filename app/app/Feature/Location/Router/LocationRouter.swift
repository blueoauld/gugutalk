import SwiftUI

@Observable
final class LocationRouter {

    var path = NavigationPath()
    
    func push(_ route: LocationRoute) {
        path.append(route)
    }
    
    func pop() {
        path.removeLast()
    }
    
    func root() {
        path.removeLast(path.count)
    }
}
