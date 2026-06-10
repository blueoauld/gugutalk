import SwiftUI

@MainActor
@Observable
final class LoginViewModel {

    private let authenticationService = AuthenticationService.shared

    private(set) var isLoading = false

    var phone = ""
    var password = ""

    var enabled: Bool {
        phone.hasPrefix("010") && phone.count == 11 && !password.isEmpty
    }

    func login() async -> Result<LoginResponse, Error>? {
        guard !isLoading, enabled else { return nil }
        guard let deviceId = TokenStorage.shared.deviceId else {
            return .failure(
                APIError.server(
                    code: "INTERNAL_CLIENT_ERROR",
                    message: "앱을 다시 실행해주시길 바랍니다.",
                    statusCode: 400
                )
            )
        }

        isLoading = true
        defer { isLoading = false }

        do {
            let response = try await authenticationService.login(
                phone: phone,
                password: password,
                deviceId: deviceId
            )
            return .success(response)
        } catch {
            if let apiError = error as? APIError, case .cancelled = apiError {
                return nil
            }

            return .failure(error)
        }
    }
}
