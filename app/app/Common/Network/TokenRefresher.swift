import Foundation

actor TokenRefresher {

    private var isRefreshing = false
    private var pendingContinuations: [CheckedContinuation<Bool, Never>] = []

    func refreshIfNeeded() async -> Bool {
        if isRefreshing {
            return await withCheckedContinuation { continuation in
                pendingContinuations.append(continuation)
            }
        }

        isRefreshing = true

        let success = await performRefresh()

        isRefreshing = false

        let continuations = pendingContinuations
        pendingContinuations.removeAll()
        continuations.forEach { $0.resume(returning: success) }
        return success
    }

    private func performRefresh() async -> Bool {
        guard
            let memberId = await TokenStorage.shared.memberId,
            let accessToken = await TokenStorage.shared.accessToken,
            let refreshToken = await TokenStorage.shared.refreshToken
        else {
            return false
        }

        do {
            let response = try await AuthenticationService.shared.rotateToken(
                memberId: memberId,
                accessToken: accessToken,
                refreshToken: refreshToken
            )

            await MainActor.run {
                TokenStorage.shared.accessToken = response.accessToken
                TokenStorage.shared.refreshToken = response.refreshToken
                StompManager.shared.reconnect()
            }
            return true
        } catch {
            return false
        }
    }
}
