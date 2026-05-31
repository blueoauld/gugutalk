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
        (2...10).contains(nickname.trimmingCharacters(in: .whitespaces).count) && birthYear.count == 4 && region != nil
    }

    func setup() async -> Bool {
        guard !isLoading else { return false }

        let trimmedNickname = nickname.trimmingCharacters(in: .whitespaces)
        guard (2...10).contains(trimmedNickname.count) else {
            ToastManager.shared.show("닉네임은 2자 이상 10자 이하여야 합니다.", style: .error)
            return false
        }
        guard let region else {
            ToastManager.shared.show("지역을 선택해주시길 바랍니다.", style: .error)
            return false
        }
        guard bio.count < 500 else {
            ToastManager.shared.show("자기소개는 500자 이하여야 합니다.", style: .error)
            return false
        }

        isLoading = true
        defer { isLoading = false }

        do {
            try await authenticationService.setup(nickname: trimmedNickname, birthYear: birthYear, region: region, bio: bio)

            ToastManager.shared.show("계정이 활성화되었습니다.", style: .info)
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
