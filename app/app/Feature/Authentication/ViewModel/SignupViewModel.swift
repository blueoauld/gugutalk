import SwiftUI

@MainActor
@Observable
final class SignupViewModel {
    
    var phone = ""
    var verificationCode = ""
    var password = ""
    var passwordConfirm = ""
    var gender: Gender = .male
    var isSendCode = false
    
    var enabled: Bool {
        phone.hasPrefix("010") && phone.count == 11 && !verificationCode.isEmpty && !password.isEmpty && password == passwordConfirm
    }
    
    var sendCodeEnabled: Bool {
        !isSendCode && phone.hasPrefix("010") && phone.count == 11
    }
}
