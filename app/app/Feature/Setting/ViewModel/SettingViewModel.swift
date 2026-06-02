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

    func rewardAttendance() async {
        guard !isLoading else { return }

        isLoading = true
        defer { isLoading = false }

        do {
            try await pointService.rewardAttendance()

            ToastManager.shared.show("출석 체크가 완료되었습니다.", style: .info)
        } catch let error as APIError {
            ToastManager.shared.show(error.message, style: .error)
        } catch {
            ToastManager.shared.show(error.localizedDescription, style: .error)
        }
    }

    func rewardAdvertisement() async {
        guard !isLoading else { return }

        isLoading = true
        defer { isLoading = false }

        do {
            try await pointService.rewardAdvertisement()

            ToastManager.shared.show("광고 보상이 지급되었습니다.", style: .info)
        } catch let error as APIError {
            ToastManager.shared.show(error.message, style: .error)
        } catch {
            ToastManager.shared.show(error.localizedDescription, style: .error)
        }
    }
}
