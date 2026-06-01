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

    func send(
        chatRoomId: Int64,
        content: String,
    ) async throws {
        try await PrivateNetworkManager.shared.requestVoid(
            "/chat-rooms/\(chatRoomId)/messages",
            method: .post,
            parameters: [
                "content": content,
            ],
            encoding: JSONEncoding.default
        )
    }

    func upload(
        chatRoomId: Int64,
        request: ChatMessageMediaUploadRequest
    ) async throws {
        try await PrivateNetworkManager.shared.requestVoid(
            "/chat-rooms/\(chatRoomId)/media",
            method: .post,
            parameters: request,
            encoder: JSONParameterEncoder.default
        )
    }

    func createUploadUrls(
        chatRoomId: Int64,
        urls: UploadUrlRequests
    ) async throws -> UploadUrlResponses {
        try await PrivateNetworkManager.shared.request(
            "/chat-rooms/\(chatRoomId)/urls",
            method: .post,
            parameters: urls,
            encoder: JSONParameterEncoder.default,
            as: UploadUrlResponses.self
        )
    }

    func getVideo(
        chatMessageId: Int64
    ) async throws -> ChatMessageGetVideoResponse {
        return try await PrivateNetworkManager.shared.request(
            "/chat-messages/\(chatMessageId)/video",
            method: .get,
            encoding: URLEncoding.default,
            as: ChatMessageGetVideoResponse.self
        )
    }
}
