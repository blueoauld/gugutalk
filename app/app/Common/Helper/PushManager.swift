import Foundation

final class PushManager {

    static let shared = PushManager()

    private let key = "apns.token"

    private var apnsToken: String? {
        didSet { UserDefaults.standard.set(apnsToken, forKey: key) }
    }

    private init() {
        apnsToken = UserDefaults.standard.string(forKey: key)
    }

    func updateToken(_ token: String) {
        apnsToken = token
        syncIfLoggedIn()
    }

    func didLogin() {
        syncIfLoggedIn()
    }

    func didLogout() {
        guard let token = apnsToken else { return }
        
        Task {
            try? await PushService.shared.delete(token: token)
        }
    }

    private func syncIfLoggedIn() {
        guard SessionStore.shared.isLoggedIn, let token = apnsToken else { return }

        Task {
            try? await PushService.shared.upsert(token: token)
        }
    }
}
