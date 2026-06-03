import SwiftUI

enum ChatRoomSearchViewState {

    case idle
    case loading
    case empty
    case data
    case error(String)
}

@MainActor
@Observable
final class ChatRoomSearchViewModel {

    private let chatRoomService = ChatRoomService.shared

    var state: ChatRoomSearchViewState = .idle
    var chatRooms: [ChatRoomSearchRowResponse] = []

    var nickname = ""
    private var searchedNickname = ""

    private(set) var isPaging = false
    private var cursor = CursorRequest()
    var hasNext: Bool { cursor.hasNext }

    func search() async -> Result<Void, Error> {
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

        searchedNickname = trimmedNickname
        state = .loading
        await fetch()

        return .success(())
    }

    func loadNext() async -> Result<Void, Error>? {
        guard !isPaging, hasNext else { return nil }

        isPaging = true
        defer { isPaging = false }

        do {
            let response = try await chatRoomService.search(
                nickname: searchedNickname,
                cursorId: cursor.cursorId,
                cursorDateAt: cursor.cursorDateAt,
                size: 20
            )

            cursor.update(cursorId: response.nextId, cursorDateAt: response.nextDateAt, hasNext: response.hasNext)
            chatRooms.append(contentsOf: response.payload)
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    private func fetch() async {
        cursor.reset()
        chatRooms = []

        do {
            let response = try await chatRoomService.search(
                nickname: searchedNickname,
                cursorId: cursor.cursorId,
                cursorDateAt: cursor.cursorDateAt,
                size: 20
            )

            cursor.update(cursorId: response.nextId, cursorDateAt: response.nextDateAt, hasNext: response.hasNext)
            chatRooms = response.payload
            state = chatRooms.isEmpty ? .empty : .data
        } catch {
            state = .error(error.userMessage)
        }
    }
}
