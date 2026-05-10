import SwiftUI

@Observable
final class SessionStore {

    var isLoggedIn = false

    func login() {
        isLoggedIn = true
    }

    func logout() {
        isLoggedIn = false
    }
}
