import Alamofire

final class ChatMessageReactionService {

    static let shared = ChatMessageReactionService()

    func react(
        chatRoomId: Int64,
        chatMessageId: Int64,
        type: ReactionType,
    ) async throws {
        try await PrivateNetworkManager.shared.requestVoid(
            "/chat-rooms/\(chatRoomId)/messages/\(chatMessageId)/reactions?type=\(type.rawValue)",
            method: .put,
            encoding: JSONEncoding.default
        )
    }
}
