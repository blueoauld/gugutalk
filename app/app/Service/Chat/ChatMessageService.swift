import Alamofire

final class ChatMessageService {

    static let shared = ChatMessageService()

    func gets(
        chatRoomId: Int64,
        cursorId: Int64?,
        cursorDateAt: String?,
        size: Int = 20,
    ) async throws -> CursorResponse<ChatMessageRowResponse> {
        var parameters: [String: Any] = [
            "size": size,
        ]

        if let cursorId {
            parameters["cursorId"] = cursorId
        }
        if let cursorDateAt {
            parameters["cursorDateAt"] = cursorDateAt
        }

        return try await PrivateNetworkManager.shared.request(
            "/chat-rooms/\(chatRoomId)/messages",
            method: .get,
            parameters: parameters,
            encoding: URLEncoding.default,
            as: CursorResponse<ChatMessageRowResponse>.self
        )
    }
}
