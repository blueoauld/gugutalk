import SwiftUI

enum ChatRoomViewState {

    case idle
    case loading
    case empty
    case data
    case error(String)
}

@MainActor
@Observable
final class ChatRoomViewModel {

    private let chatRoomService = ChatRoomService.shared
    private let memberService = MemberService.shared

    var state: ChatRoomViewState = .idle
    var chatRooms: [ChatRoomRowResponse] = []
    var status: ChatRoomStatusFilter = .all
    var isChat = true

    private(set) var isPaging = false
    private(set) var isLoading = false
    private(set) var isMutating = false

    private var hasLoad = false
    private var cursor = CursorRequest()
    var hasNext: Bool { cursor.hasNext }

    func load() async {
        guard !hasLoad else { return }
        hasLoad = true
        
        state = .loading
        await fetch()
    }

    func switchView() async {
        state = .loading
        await fetch()
    }

    func loadNext() async {
        guard !isPaging, hasNext else { return }

        isPaging = true
        defer { isPaging = false }

        do {
            let response = try await chatRoomService.gets(
                status: status.rawValue,
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

    func read(chatRoomId: Int64) async {
        guard !isLoading else { return }

        isLoading = true
        defer { isLoading = false }

        do {
            try await chatRoomService.read(chatRoomId: chatRoomId)

            if let index = chatRooms.firstIndex(where: { $0.chatRoomId == chatRoomId }) {
                chatRooms[index].unreadCount = 0
            }
        } catch let error as APIError {
            ToastManager.shared.show(error.message, style: .error)
        } catch {
            ToastManager.shared.show(error.localizedDescription, style: .error)
        }
    }

    func delete(chatRoomId: Int64) async {
        guard !isLoading else { return }

        isLoading = true
        defer { isLoading = false }

        do {
            try await chatRoomService.delete(chatRoomId: chatRoomId)

            withAnimation {
                chatRooms.removeAll { $0.chatRoomId == chatRoomId }
            }

            state = chatRooms.isEmpty ? .empty : .data
        } catch let error as APIError {
            ToastManager.shared.show(error.message, style: .error)
        } catch {
            ToastManager.shared.show(error.localizedDescription, style: .error)
        }
    }

    func isChat() async {
        guard let response = try? await memberService.isChat() else { return }

        isChat = response.isChat
    }

    func toggleChat() async {
        guard !isMutating else { return }

        isMutating = true
        defer { isMutating = false }

        isChat.toggle()

        do {
            try await memberService.toggleChat()
        } catch let error as APIError {
            isChat.toggle()

            ToastManager.shared.show(error.message, style: .error)
        } catch {
            isChat.toggle()

            ToastManager.shared.show(error.localizedDescription, style: .error)
        }
    }

    private func fetch() async {
        cursor.reset()
        chatRooms = []

        do {
            let response = try await chatRoomService.gets(
                status: status.rawValue,
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
