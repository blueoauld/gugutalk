struct ChatRoomUpsertResponse: Decodable, Identifiable {

    let chatRoomId: Int64
    let targetId: Int64
    let memberId: Int64
    let senderId: Int64
    var nickname: String
    var profileUrl: String?
    var unreadCount: Int64
    var lastMessagePreview: String
    var lastMessageAt: String

    var id: Int64 { chatRoomId }
}
