import SwiftUI

@MainActor
@Observable
final class LoginViewModel {

    private let authenticationService = AuthenticationService.shared

    var isLoading = false

    var phone = ""
    var password = ""

    var enabled: Bool {
        phone.hasPrefix("010") && phone.count == 11 && !password.isEmpty
    }

    func login() async -> Bool {
        guard !isLoading, enabled else { return false }
        guard let deviceId = TokenStorage.shared.deviceId else {
            ToastManager.shared.show("앱을 다시 실행해주시길 바랍니다.", style: .error)
            return false
        }

        isLoading = true
        defer { isLoading = false }

        do {
            let response = try await authenticationService.login(
                phone: phone,
                password: password,
                deviceId: deviceId
            )

            TokenStorage.shared.memberId = response.memberId
            TokenStorage.shared.accessToken = response.accessToken
            TokenStorage.shared.refreshToken = response.refreshToken

            ToastManager.shared.show("로그인이 완료되었습니다.", style: .info)
            return true
        } catch let error as APIError {
            ToastManager.shared.show(error.message, style: .error)
            return false
        } catch {
            ToastManager.shared.show(error.localizedDescription, style: .error)
            return false
        }
    }
}
