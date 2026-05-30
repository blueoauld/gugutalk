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
    var chatRooms: [ChatRoomRowResponse] = []

    var nickname = ""
    private var searchedNickname = ""

    private(set) var isPaging = false
    private var cursor = CursorRequest()
    var hasNext: Bool { cursor.hasNext }

    func search() async {
        let trimmedNickname = nickname.trimmingCharacters(in: .whitespaces)
        guard (2...10).contains(trimmedNickname.count) else {
            ToastManager.shared.show("닉네임 검색은 2자 이상 10자 이하여야 합니다.", style: .error)
            return
        }

        searchedNickname = trimmedNickname
        state = .loading
        await fetch()
    }

    func loadNext() async {
        guard !isPaging, hasNext else { return }

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
        } catch let error as APIError {
            ToastManager.shared.show(error.message, style: .error)
        } catch {
            ToastManager.shared.show(error.localizedDescription, style: .error)
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
        } catch let error as APIError {
            state = .error(error.message)
        } catch {
            state = .error(error.localizedDescription)
        }
    }
}
