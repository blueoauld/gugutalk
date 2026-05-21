import SwiftUI

@MainActor
@Observable
final class SignupViewModel {

    private let authenticationService = AuthenticationService.shared

    var isLoading = false

    var phone = ""
    var verificationCode = ""
    var password = ""
    var confirmPassword = ""
    var gender: Gender = .male
    var isSendCode = false

    var enabled: Bool {
        phone.hasPrefix("010") && phone.count == 11 && !verificationCode.isEmpty && !password.isEmpty && password == confirmPassword
    }

    var sendCodeEnabled: Bool {
        !isSendCode && phone.hasPrefix("010") && phone.count == 11
    }

    func sendVerificationCode() async {
        guard !isLoading, !isSendCode else { return }
        guard let deviceId = TokenStorage.shared.deviceId else {
            print("앱을 다시 실행해주시길 바랍니다.")
            return
        }

        isLoading = true
        defer { isLoading = false }

        do {
            try await authenticationService.sendVerificationCode(phone: phone, deviceId: deviceId)
            isSendCode = true
        } catch let error as APIError {
            print(error.message)
        } catch {
            print(error.localizedDescription)
        }
    }

    func signup() async -> Bool {
        guard !isLoading, enabled else { return false }
        guard let deviceId = TokenStorage.shared.deviceId else {
            print("앱을 다시 실행해주시길 바랍니다.")
            return false
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
            return true
        } catch let error as APIError {
            print(error.message)
            return false
        } catch {
            print(error.localizedDescription)
            return false
        }
    }
}
