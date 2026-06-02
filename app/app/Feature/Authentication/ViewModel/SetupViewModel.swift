import SwiftUI

@MainActor
@Observable
final class SetupViewModel {

    private let authenticationService = AuthenticationService.shared

    private(set) var isLoading = false

    var nickname = ""
    var birthYear = ""
    var region: Region? = nil
    var bio = ""

    var enabled: Bool {
        (2...15).contains(nickname.trimmingCharacters(in: .whitespaces).count) && birthYear.count == 4 && region != nil
    }

    func setup() async -> Result<Void, Error>? {
        guard !isLoading else { return nil }

        let trimmedNickname = nickname.trimmingCharacters(in: .whitespaces)
        guard (2...15).contains(trimmedNickname.count) else {
            return .failure(
                APIError.server(
                    code: "INTERNAL_CLIENT_ERROR",
                    message: "닉네임은 2자 이상 15자 이하여야 합니다.",
                    statusCode: 400
                )
            )
        }
        guard let region else {
            return .failure(
                APIError.server(
                    code: "INTERNAL_CLIENT_ERROR",
                    message: "지역을 선택해주시길 바랍니다.",
                    statusCode: 400
                )
            )
        }
        guard bio.count < 500 else {
            return .failure(
                APIError.server(
                    code: "INTERNAL_CLIENT_ERROR",
                    message: "자기소개는 500자 이하여야 합니다.",
                    statusCode: 400
                )
            )
        }

        isLoading = true
        defer { isLoading = false }

        do {
            try await authenticationService.setup(nickname: trimmedNickname, birthYear: birthYear, region: region, bio: bio)
            return .success(())
        } catch {
            return .failure(error)
        }
    }
}
