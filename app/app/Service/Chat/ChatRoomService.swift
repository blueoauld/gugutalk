import Alamofire

final class ChatRoomService {

    static let shared = ChatRoomService()

    func delete(
        chatRoomId: Int64,
    ) async throws {
        try await PrivateNetworkManager.shared.requestVoid(
            "/chat-rooms/\(chatRoomId)",
            method: .delete,
            encoding: JSONEncoding.default
        )
    }

    func gets(
        status: String,
        cursorId: Int64?,
        cursorDateAt: String?,
        size: Int = 20,
    ) async throws -> CursorResponse<ChatRoomRowResponse> {
        var parameters: [String: Any] = [
            "status": status,
            "size": size,
        ]

        if let cursorId {
            parameters["cursorId"] = cursorId
        }
        if let cursorDateAt {
            parameters["cursorDateAt"] = cursorDateAt
        }

        return try await PrivateNetworkManager.shared.request(
            "/chat-rooms",
            method: .get,
            parameters: parameters,
            encoding: URLEncoding.default,
            as: CursorResponse<ChatRoomRowResponse>.self
        )
    }
}
