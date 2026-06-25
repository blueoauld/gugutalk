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
        } catch APIError.unauthorized {
            // 리프레시 토큰이 서버에서 무효화됨(만료·회전 소실 등).
            // 죽은 토큰으로 재시도하면 계속 UNAUTHORIZED_02 가 반복되므로,
            // 세션을 정리하고 재로그인을 유도한다.
            await MainActor.run {
                SessionStore.shared.logout()
            }
            return false
        } catch {
            // 네트워크 등 일시적 오류: 토큰을 유지하고 다음 기회에 다시 시도한다.
            return false
        }
    }
}
