import SwiftUI

enum ChatMessageViewState {

    case idle
    case loading
    case empty
    case data
    case error(String)
}

@MainActor
@Observable
final class ChatMessageViewModel {

    private let chatMessageService = ChatMessageService.shared

    var state: ChatMessageViewState = .idle
    var chatMessages: [ChatMessageRowResponse] = []
    var message: String = ""

    private(set) var isPaging = false

    private var hasLoad = false
    private var cursor = CursorRequest()
    var hasNext: Bool { cursor.hasNext }

    func load(chatRoomId: Int64) async {
        guard !hasLoad else { return }
        hasLoad = true

        state = .loading
        await fetch(chatRoomId: chatRoomId)
    }

    func loadNext(chatRoomId: Int64) async {
        guard !isPaging, hasNext else { return }

        isPaging = true
        defer { isPaging = false }

        do {
            let response = try await chatMessageService.gets(
                chatRoomId: chatRoomId,
                cursorId: cursor.cursorId,
                cursorDateAt: cursor.cursorDateAt,
                size: 20
            )

            cursor.update(cursorId: response.nextId, cursorDateAt: response.nextDateAt, hasNext: response.hasNext)
            chatMessages.append(contentsOf: response.payload)
        } catch let error as APIError {
            ToastManager.shared.show(error.message, style: .error)
        } catch {
            ToastManager.shared.show(error.localizedDescription, style: .error)
        }
    }

    private func fetch(chatRoomId: Int64) async {
        cursor.reset()
        chatMessages = []

        do {
            let response = try await chatMessageService.gets(
                chatRoomId: chatRoomId,
                cursorId: cursor.cursorId,
                cursorDateAt: cursor.cursorDateAt,
                size: 20
            )

            cursor.update(cursorId: response.nextId, cursorDateAt: response.nextDateAt, hasNext: response.hasNext)
            chatMessages = response.payload
            state = chatMessages.isEmpty ? .empty : .data
        } catch let error as APIError {
            state = .error(error.message)
        } catch {
            state = .error(error.localizedDescription)
        }
    }
}
