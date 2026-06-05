import SwiftUI

@Observable
final class SessionStore {

    static let shared = SessionStore()

    var isLoggedIn: Bool
    var banInfo: BanInfo?

    init() {
        isLoggedIn = TokenStorage.shared.accessToken != nil
    }

    func login(_ response: LoginResponse) {
        banInfo = nil
        isLoggedIn = true
        
        TokenStorage.shared.memberId = response.memberId
        TokenStorage.shared.accessToken = response.accessToken
        TokenStorage.shared.refreshToken = response.refreshToken
        PushManager.shared.didLogin()
    }

    func logout() {
        PushManager.shared.didLogout()
        TokenStorage.shared.clearAll()

        isLoggedIn = false
    }

    func handleBan(_ ban: BanInfo) {
        guard banInfo == nil else { return }

        PushManager.shared.didLogout()
        TokenStorage.shared.clearAll()

        banInfo = ban
        isLoggedIn = false
    }
}
