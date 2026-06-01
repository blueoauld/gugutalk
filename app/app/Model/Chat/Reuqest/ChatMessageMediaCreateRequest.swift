struct ChatMessageMediaCreateRequest: Encodable {

    let type: MessageType
    let url: String
    let key: String
    let thumbnailUrl: String?
    let thumbnailKey: String?
}
