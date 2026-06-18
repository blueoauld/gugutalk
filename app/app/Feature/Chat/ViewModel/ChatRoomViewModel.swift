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
    
    private let upsertQueue = "/user/queue/chat-rooms/upsert"
    private let deleteQueue = "/user/queue/chat-rooms/delete"
    private let readQueue = "/user/queue/chat-rooms/read"
    
    var state: ChatRoomViewState = .idle
    var chatRooms: [ChatRoomRowResponse] = [] {
        didSet { syncBadge() }
    }
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

    func silentRefresh() async {
        await fetch(silent: true)
    }
    
    func loadNext() async -> Result<Void, Error>? {
        guard !isPaging, hasNext else { return nil }

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
            return .success(())
        } catch {
            if let apiError = error as? APIError, case .cancelled = apiError {
                return nil
            }
            
            return .failure(error)
        }
    }
    
    func read(chatRoomId: Int64) async -> Result<Void, Error>? {
        guard !isLoading else { return nil }

        isLoading = true
        defer { isLoading = false }
        
        do {
            try await chatRoomService.read(chatRoomId: chatRoomId)
            return .success(())
        } catch {
            if let apiError = error as? APIError, case .cancelled = apiError {
                return nil
            }

            return .failure(error)
        }
    }
    
    func delete(chatRoomId: Int64) async -> Result<Void, Error>? {
        guard !isLoading else { return nil }

        isLoading = true
        defer { isLoading = false }
        
        do {
            try await chatRoomService.delete(chatRoomId: chatRoomId)
            return .success(())
        } catch {
            if let apiError = error as? APIError, case .cancelled = apiError {
                return nil
            }

            return .failure(error)
        }
    }
    
    func isChat() async {
        guard let response = try? await memberService.isChat() else { return }
        
        isChat = response.isChat
    }
    
    func toggleChat() async -> Result<Void, Error>? {
        guard !isMutating else { return nil }
        
        isMutating = true
        defer { isMutating = false }
        
        isChat.toggle()
        
        do {
            try await memberService.toggleChat()
            return .success(())
        } catch {
            isChat.toggle()

            if let apiError = error as? APIError, case .cancelled = apiError {
                return nil
            }

            return .failure(error)
        }
    }
    
    private func fetch(silent: Bool = false) async {
        cursor.reset()

        if !silent {
            chatRooms = []
        }

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
        } catch {
            if let apiError = error as? APIError, case .cancelled = apiError {
                return
            }

            if !silent {
                state = .error(error.userMessage)
            }
        }
    }
    
    private func syncBadge() {
        ChatBadgeManager.shared.unreadCount = chatRooms.filter { $0.unreadCount > 0 }.count
    }
    
    // MARK: - STOMP
    func subscribe() {
        StompManager.shared.subscribe(
            to: upsertQueue,
            as: ChatRoomUpsertResponse.self
        ) { [weak self] room in
            guard let self else { return }
            
            self.receive(room)
        }
        
        StompManager.shared.subscribe(
            to: deleteQueue,
            as: ChatRoomDeleteResponse.self
        ) { [weak self] room in
            guard let self else { return }
            
            self.receive(room)
        }
        
        StompManager.shared.subscribe(
            to: readQueue,
            as: ChatRoomReadResponse.self
        ) { [weak self] room in
            guard let self else { return }
            
            self.receive(room)
        }
    }
    
    func unsubscribe() {
        StompManager.shared.unsubscribe(from: upsertQueue)
        StompManager.shared.unsubscribe(from: deleteQueue)
    }
    
    private func receive(_ room: ChatRoomUpsertResponse) {
        let isMine = room.senderId == TokenStorage.shared.memberId
        
        if let index = chatRooms.firstIndex(where: { $0.chatRoomId == room.chatRoomId }) {
            var updated = chatRooms.remove(at: index)
            
            updated.nickname = room.nickname
            updated.profileUrl = room.profileUrl
            if !isMine {
                updated.unreadCount += 1
            }
            updated.lastMessagePreview = room.lastMessagePreview
            updated.lastMessageAt = room.lastMessageAt
            
            chatRooms.insert(updated, at: 0)
        } else {
            var newRoom = ChatRoomRowResponse(from: room)
            
            newRoom.unreadCount = isMine ? 0 : 1
            chatRooms.insert(newRoom, at: 0)
            state = .data
        }
    }
    
    private func receive(_ room: ChatRoomDeleteResponse) {
        chatRooms.removeAll { $0.chatRoomId == room.chatRoomId }
        state = chatRooms.isEmpty ? .empty : .data
    }
    
    private func receive(_ room: ChatRoomReadResponse) {
        guard let index = chatRooms.firstIndex(where: { $0.chatRoomId == room.chatRoomId }) else { return }

        if status == .unread {
            chatRooms.remove(at: index)
            state = chatRooms.isEmpty ? .empty : .data
        } else {
            chatRooms[index].unreadCount = 0
        }
    }
}
