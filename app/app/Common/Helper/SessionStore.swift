import SwiftUI

@Observable
final class SessionStore {

    var isLoggedIn: Bool

    init() {
        isLoggedIn = TokenStorage.shared.accessToken != nil
    }

    func login(_ response: LoginResponse) {
        TokenStorage.shared.memberId = response.memberId
        TokenStorage.shared.accessToken = response.accessToken
        TokenStorage.shared.refreshToken = response.refreshToken
        isLoggedIn = true
    }

    func logout() {
        TokenStorage.shared.clearAll()
        StompManager.shared.disconnect()
        isLoggedIn = false
    }
}
