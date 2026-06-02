import SwiftUI

@MainActor
@Observable
final class SignupViewModel {

    private let authenticationService = AuthenticationService.shared

    private(set) var isLoading = false
    private(set) var isSendCode = false

    var phone = ""
    var verificationCode = ""
    var password = ""
    var confirmPassword = ""
    var gender: Gender = .male

    var enabled: Bool {
        phone.hasPrefix("010") && phone.count == 11 && verificationCode.count == 6 && !password.isEmpty && password == confirmPassword
    }

    var sendCodeEnabled: Bool {
        !isSendCode && phone.hasPrefix("010") && phone.count == 11
    }

    func sendVerificationCode() async -> Result<Void, Error>? {
        guard !isLoading, !isSendCode else { return nil }
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
            try await authenticationService.sendVerificationCode(phone: phone, deviceId: deviceId)

            isSendCode = true
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    func signup() async -> Result<Void, Error>? {
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
            let response = try await authenticationService.signup(
                phone: phone,
                deviceId: deviceId,
                verificationCode: verificationCode,
                password: password,
                confirmPassword: confirmPassword,
                gender: gender
            )

            TokenStorage.shared.memberId = response.memberId
            TokenStorage.shared.accessToken = response.accessToken
            TokenStorage.shared.refreshToken = response.refreshToken
            return .success(())
        } catch {
            return .failure(error)
        }
    }
}
