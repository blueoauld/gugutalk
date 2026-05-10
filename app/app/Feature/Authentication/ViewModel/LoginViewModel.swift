import SwiftUI

@MainActor
@Observable
final class LoginViewModel {

    var phone = ""
    var password = ""

    var enabled: Bool {
        !phone.isEmpty && !password.isEmpty
    }
}
