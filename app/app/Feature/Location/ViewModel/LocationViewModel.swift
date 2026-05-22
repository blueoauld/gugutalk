import SwiftUI

@MainActor
@Observable
final class LocationViewModel {

    private let memberService = MemberService.shared

    var isLoading = false

    var comment = ""

    func updateComment() async {
        guard !isLoading else { return }

        isLoading = true
        defer { isLoading = false }

        do {
            try await memberService.updateComment(
                content: comment,
            )

            ToastManager.shared.show("코멘트를 작성하셨습니다.", style: .info)
        } catch let error as APIError {
            ToastManager.shared.show(error.message, style: .error)
        } catch {
            ToastManager.shared.show(error.localizedDescription, style: .error)
        }
    }

    func bump() async {
        try? await memberService.bump()
    }
}
