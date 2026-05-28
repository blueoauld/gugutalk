import SwiftUI

enum MainViewState {

    case idle
    case loading
    case empty
    case data
    case error(String)
}

@MainActor
@Observable
final class MainViewModel {

    private let memberService = MemberService.shared

    var state: MainViewState = .idle
    var members: [MemberRowResponse] = []
    var gender: GenderFilter
    var view: ViewFilter
    var comment = ""

    private(set) var isLoading = false

    private var isPaging = false

    private var fetchCount = 0
    private var isFetching: Bool { fetchCount > 0 }

    private var generation = 0

    private var hasLoad = false
    private var cursor = CursorRequest()
    var hasNext: Bool { cursor.hasNext }

    init() {
        let viewRaw = UserDefaults.standard.string(forKey: StorageKey.view)
        self.view = viewRaw.flatMap(ViewFilter.init(rawValue:)) ?? .recent

        let genderRaw = UserDefaults.standard.string(forKey: StorageKey.gender)
        self.gender = genderRaw.flatMap(GenderFilter.init(rawValue:)) ?? .all
    }

    func load() async {
        guard !hasLoad else { return }
        hasLoad = true

        state = .loading
        await fetch()
    }

    func loadNext() async {
        guard !isPaging, !isFetching, hasNext else { return }

        let token = generation

        isPaging = true
        defer { isPaging = false }

        do {
            let response = try await requestMembers()

            guard token == generation else { return }

            cursor.update(cursorId: response.nextId, cursorDateAt: response.nextDateAt, hasNext: response.hasNext)
            members.append(contentsOf: response.payload)
        } catch let error as APIError {
            ToastManager.shared.show(error.message, style: .error)
        } catch {
            ToastManager.shared.show(error.localizedDescription, style: .error)
        }
    }

    func reload() async {
        await fetch()
    }

    func switchView() async {
        state = .loading
        await fetch()
    }

    func updateComment() async -> Bool {
        guard !isLoading else { return false }
        guard !comment.isEmpty else {
            ToastManager.shared.show("코멘트 내용을 작성해주시길 바랍니다.", style: .error)
            return false
        }
        guard comment.count < 50 else {
            ToastManager.shared.show("코멘트는 50자 이하여야 합니다.", style: .error)
            return false
        }

        isLoading = true
        defer { isLoading = false }

        do {
            try await memberService.updateComment(content: comment)
            
            ToastManager.shared.show("코멘트를 작성하셨습니다.", style: .info)
            return true
        } catch let error as APIError {
            ToastManager.shared.show(error.message, style: .error)
            return false
        } catch {
            ToastManager.shared.show(error.localizedDescription, style: .error)
            return false
        }
    }

    func bump() async {
        try? await memberService.bump()
    }

    private func fetch() async {
        generation &+= 1

        let token = generation

        fetchCount += 1
        defer { fetchCount -= 1 }

        cursor.reset()

        do {
            let response = try await requestMembers()

            guard token == generation else { return }

            cursor.update(cursorId: response.nextId, cursorDateAt: response.nextDateAt, hasNext: response.hasNext)
            members = response.payload
            state = members.isEmpty ? .empty : .data
        } catch let error as APIError {
            guard token == generation else { return }

            state = .error(error.message)
        } catch {
            guard token == generation else { return }

            state = .error(error.localizedDescription)
        }
    }

    private func requestMembers() async throws -> CursorResponse<MemberRowResponse> {
        switch view {
        case .recent:
            return try await memberService.gets(
                gender: gender.rawValue,
                cursorId: cursor.cursorId,
                cursorDateAt: cursor.cursorDateAt,
                size: 20
            )
        case .region:
            return try await memberService.getsByRegion(
                gender: gender.rawValue,
                cursorId: cursor.cursorId,
                cursorDateAt: cursor.cursorDateAt,
                size: 20
            )
        }
    }
}
