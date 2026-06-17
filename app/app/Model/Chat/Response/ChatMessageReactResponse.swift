struct ChatMessageReactResponse: Decodable {

    let chatMessageId: Int64
    let memberId: Int64
    var type: ReactionType? = nil
}
