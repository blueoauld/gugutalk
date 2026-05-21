import SwiftUI

@MainActor
@Observable
final class SetupViewModel {

    private let authenticationService = AuthenticationService.shared

    var isLoading = false

    var nickname = ""
    var birthYear = ""
    var region: Region? = nil
    var bio = ""

    var enabled: Bool {
        (2...10).contains(nickname.count) && birthYear.count == 4 && region != nil
    }

    func setup() async -> Bool {
        guard !isLoading, enabled else { return false }

        isLoading = true
        defer { isLoading = false }

        do {
            try await authenticationService.setup(nickname: nickname, birthYear: birthYear, region: region!, bio: bio)

            ToastManager.shared.show("계정이 활성화되었습니다.", style: .success)
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
