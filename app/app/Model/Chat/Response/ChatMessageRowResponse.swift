import SwiftUI

struct ChatMessageRowResponse: Decodable, Identifiable {

    let chatMessageId: Int64
    let clientMessageId: String?
    let senderId: Int64
    let content: String
    let type: MessageType
    let createdAt: String
    var reactions: [ChatMessageReactResponse]

    var id: String { clientMessageId ?? "\(chatMessageId)" }
    var status: SendStatus = .sent

    enum CodingKeys: String, CodingKey {
        case chatMessageId, clientMessageId, senderId, content, type, createdAt, reactions
    }
}

extension ChatMessageRowResponse {

    private static let iso8601: ISO8601DateFormatter = {
        let f = ISO8601DateFormatter()
        f.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        return f
    }()

    static func optimistic(clientMessageId: String, senderId: Int64, content: String) -> Self {
        ChatMessageRowResponse(
            chatMessageId: Int64.max,
            clientMessageId: clientMessageId,
            senderId: senderId,
            content: content,
            type: .text,
            createdAt: iso8601.string(from: Date()),
            reactions: [],
            status: .pending
        )
    }
}
