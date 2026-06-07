import SwiftUI

@MainActor
@Observable
final class SettingViewModel {

    private let authenticationService = AuthenticationService.shared
    private let pointService = PointService.shared

    private(set) var isLoading = false

    func logout() async -> Result<Void, Error>? {
        guard !isLoading else { return nil }
        guard let accessToken = TokenStorage.shared.accessToken, let refreshToken = TokenStorage.shared.refreshToken else {
            return .failure(
                APIError.server(
                    code: "INTERNAL_CLIENT_ERROR",
                    message: "토큰을 찾을 수 없습니다.",
                    statusCode: 400
                )
            )
        }

        isLoading = true
        defer { isLoading = false }

        do {
            try await authenticationService.logout(accessToken: accessToken, refreshToken: refreshToken)
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    func delete() async -> Result<Void, Error>? {
        guard !isLoading else { return nil }
        guard let refreshToken = TokenStorage.shared.refreshToken else {
            return .failure(
                APIError.server(
                    code: "INTERNAL_CLIENT_ERROR",
                    message: "리프레쉬 토큰을 찾을 수 없습니다.",
                    statusCode: 400
                )
            )
        }

        isLoading = true
        defer { isLoading = false }

        do {
            try await authenticationService.deleteAccount(refreshToken: refreshToken)
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    func rewardAttendance() async -> Result<Void, Error>? {
        guard !isLoading else { return nil }

        isLoading = true
        defer { isLoading = false }

        do {
            try await pointService.rewardAttendance()
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    func rewardAdvertisement() async -> Result<Void, Error>? {
        guard !isLoading else { return nil }

        isLoading = true
        defer { isLoading = false }

        do {
            try await pointService.rewardAdvertisement()
            return .success(())
        } catch {
            return .failure(error)
        }
    }
}
