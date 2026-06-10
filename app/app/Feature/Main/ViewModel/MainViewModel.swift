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
    private let chatRoomService = ChatRoomService.shared

    var state: MainViewState = .idle
    var members: [MemberRowResponse] = []
    var gender: GenderFilter
    var view: ViewFilter
    var comment = ""

    private(set) var isLoading = false
    private(set) var isPaging = false

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

    func loadNext() async -> Result<Void, Error>? {
        guard !isPaging, !isFetching, hasNext else { return nil }

        let token = generation

        isPaging = true
        defer { isPaging = false }

        do {
            let response = try await requestMembers()

            guard token == generation else { return nil }

            cursor.update(cursorId: response.nextId, cursorDateAt: response.nextDateAt, hasNext: response.hasNext)
            members.append(contentsOf: response.payload)
            return .success(())
        } catch {
            if let apiError = error as? APIError, case .cancelled = apiError {
                return nil
            }

            return .failure(error)
        }
    }

    func reload() async {
        await fetch()
    }

    func switchView() async {
        state = .loading
        await fetch()
    }

    func updateComment() async -> Result<Void, Error>? {
        guard !isLoading else { return nil }
        guard !comment.isEmpty else {
            return .failure(
                APIError.server(
                    code: "INTERNAL_CLIENT_ERROR",
                    message: "코멘트 내용을 작성해주시길 바랍니다.",
                    statusCode: 400
                )
            )
        }
        guard comment.count < 50 else {
            return .failure(
                APIError.server(
                    code: "INTERNAL_CLIENT_ERROR",
                    message: "코멘트는 50자 이하여야 합니다.",
                    statusCode: 400
                )
            )
        }

        isLoading = true
        defer { isLoading = false }

        do {
            try await memberService.updateComment(content: comment)
            return .success(())
        } catch {
            if let apiError = error as? APIError, case .cancelled = apiError {
                return nil
            }

            return .failure(error)
        }
    }

    func bump() async {
        try? await memberService.bump()
    }

    func createChatRoom(memberId: Int64, message: String) async -> Result<Void, Error>? {
        guard !isLoading else { return nil }

        isLoading = true
        defer { isLoading = false }

        do {
            try await chatRoomService.create(targetId: memberId, content: message)
            return .success(())
        } catch {
            if let apiError = error as? APIError, case .cancelled = apiError {
                return nil
            }

            return .failure(error)
        }
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
        } catch {
            guard token == generation else { return }

            if let apiError = error as? APIError, case .cancelled = apiError {
                return
            }

            state = .error(error.userMessage)
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
