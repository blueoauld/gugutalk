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
        phone.hasPrefix("010") && phone.count == 11 && verificationCode.count == 6 && !password.isEmpty && password == confirmPassword
    }
    
    var sendCodeEnabled: Bool {
        !isSendCode && phone.hasPrefix("010") && phone.count == 11
    }
    
    func sendVerificationCode() async {
        guard !isLoading, !isSendCode else { return }
        guard let deviceId = TokenStorage.shared.deviceId else {
            ToastManager.shared.show("앱을 다시 실행해주시길 바랍니다.", style: .error)
            return
        }
        
        isLoading = true
        defer { isLoading = false }
        
        do {
            try await authenticationService.sendVerificationCode(phone: phone, deviceId: deviceId)
            
            isSendCode = true
            ToastManager.shared.show("인증 번호가 전송되었습니다.", style: .info)
        } catch let error as APIError {
            ToastManager.shared.show(error.message, style: .error)
        } catch {
            ToastManager.shared.show(error.localizedDescription, style: .error)
        }
    }
    
    func signup() async -> Bool {
        guard !isLoading, enabled else { return false }
        guard let deviceId = TokenStorage.shared.deviceId else {
            ToastManager.shared.show("앱을 다시 실행해주시길 바랍니다.", style: .error)
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
            
            ToastManager.shared.show("회원 가입이 완료되었습니다.", style: .info)
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
