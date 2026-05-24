import SwiftUI

enum LocationViewState {

    case idle
    case loading
    case empty
    case data
    case error(String)
}

@MainActor
@Observable
final class LocationViewModel {

    private let memberService = MemberService.shared

    var state: LocationViewState = .idle
    var members: [MemberRowResponse] = []
    var gender = "ALL"
    var comment = ""

    private(set) var isPaging = false
    private(set) var isLoading = false
    private var hasLoad = false
    private var cursor = CursorRequest()
    var hasNext: Bool { cursor.hasNext }

    func load() async {
        guard !hasLoad else { return }

        state = .loading
        await fetch()
        hasLoad = true
    }

    func loadNext() async {
        guard !isPaging, hasNext else { return }

        isPaging = true
        defer { isPaging = false }

        do {
            let response = try await memberService.getsByRegion(
                gender: gender,
                cursorId: cursor.cursorId,
                cursorDateAt: cursor.cursorDateAt,
                size: 20
            )

            cursor.update(cursorId: response.nextId, cursorDateAt: response.nextDateAt, hasNext: response.hasNext)
            members.append(contentsOf: response.payload)
        } catch let error as APIError {
            ToastManager.shared.show(error.message, style: .error)
        } catch {
            ToastManager.shared.show(error.localizedDescription, style: .error)
        }
    }

    func reload() async {
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
        cursor.reset()
        members = []

        do {
            let response = try await memberService.getsByRegion(
                gender: gender,
                cursorId: cursor.cursorId,
                cursorDateAt: cursor.cursorDateAt,
                size: 20
            )

            cursor.update(cursorId: response.nextId, cursorDateAt: response.nextDateAt, hasNext: response.hasNext)
            members = response.payload
            state = members.isEmpty ? .empty : .data
        } catch let error as APIError {
            state = .error(error.message)
        } catch {
            state = .error(error.localizedDescription)
        }
    }
}
