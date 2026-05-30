struct ChatMessageRowResponse: Decodable, Identifiable {

    let chatMessageId: Int64
    let senderId: Int64
    let content: String
    let type: MessageType
    let createdAt: String

    var id: Int64 { chatMessageId }
}
