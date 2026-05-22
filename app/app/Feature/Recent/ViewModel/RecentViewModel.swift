import SwiftUI

enum RecentViewState {

    case idle
    case loading
    case empty
    case data
    case error(String)
}

@MainActor
@Observable
final class RecentViewModel {

    private let memberService = MemberService.shared

    var state: RecentViewState = .idle
    var isPaging = false
    var isLoading = false
    var hasLoad = false
    var hasNext = false

    var members: [MemberRowResponse] = []
    var gender = "ALL"
    var comment = ""

    private var cursorId: Int64? = nil
    private var cursorDateAt: String? = nil

    func gets() async {
        guard !hasLoad else { return }

        state = .loading
        await fetch()
        hasLoad = true
    }

    func getsNext() async {
        guard !isPaging, hasNext else { return }

        isPaging = true
        defer { isPaging = false }

        do {
            let response = try await memberService.gets(
                gender: gender,
                cursorId: cursorId,
                cursorDateAt: cursorDateAt,
                size: 20
            )

            members.append(contentsOf: response.payload)
            cursorId = response.nextId
            cursorDateAt = response.nextDateAt
            hasNext = response.hasNext

            state = members.isEmpty ? .empty : .data
        } catch let error as APIError {
            state = .error(error.message)
        } catch {
            state = .error(error.localizedDescription)
        }
    }

    func refresh() async {
        guard !isLoading else { return }

        isLoading = true
        defer { isLoading = false }
        
        await fetch()
    }

    func updateComment() async {
        guard !isLoading else { return }
        guard !comment.isEmpty else {
            ToastManager.shared.show("코멘트 내용을 작성해주시길 바랍니다.", style: .error)
            return
        }

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

    private func fetch() async {
        members = []
        cursorId = nil
        cursorDateAt = nil
        hasNext = false

        do {
            let response = try await memberService.gets(
                gender: gender,
                cursorId: cursorId,
                cursorDateAt: cursorDateAt,
                size: 20
            )

            members = response.payload
            cursorId = response.nextId
            cursorDateAt = response.nextDateAt
            hasNext = response.hasNext

            state = members.isEmpty ? .empty : .data
        } catch let error as APIError {
            state = .error(error.message)
        } catch {
            state = .error(error.localizedDescription)
        }
    }
}
