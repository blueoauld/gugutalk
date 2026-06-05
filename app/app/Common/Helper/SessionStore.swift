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
        TokenStorage.shared.memberId = response.memberId
        TokenStorage.shared.accessToken = response.accessToken
        TokenStorage.shared.refreshToken = response.refreshToken

        banInfo = nil
        isLoggedIn = true
    }

    func logout() {
        TokenStorage.shared.clearAll()
        StompManager.shared.disconnect()
        isLoggedIn = false
    }

    func handleBan(_ ban: BanInfo) {
        guard banInfo == nil else { return }

        TokenStorage.shared.clearAll()
        
        banInfo = ban
        isLoggedIn = false
    }
}
