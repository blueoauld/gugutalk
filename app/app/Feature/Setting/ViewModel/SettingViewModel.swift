import SwiftUI

@MainActor
@Observable
final class SettingViewModel {

    private let authenticationService = AuthenticationService.shared
    private let pointService = PointService.shared

    private(set) var isLoading = false

    func logout() async {
        guard !isLoading else { return }
        guard let accessToken = TokenStorage.shared.accessToken, let refreshToken = TokenStorage.shared.refreshToken else {
            ToastManager.shared.show("토큰을 찾을 수 없습니다.", style: .error)
            return
        }

        isLoading = true
        defer { isLoading = false }

        do {
            try await authenticationService.logout(accessToken: accessToken, refreshToken: refreshToken)

            ToastManager.shared.show("정상적으로 로그아웃되었습니다.", style: .info)
            TokenStorage.shared.clearAll()
        } catch let error as APIError {
            ToastManager.shared.show(error.message, style: .error)
        } catch {
            ToastManager.shared.show(error.localizedDescription, style: .error)
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
