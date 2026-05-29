struct ChatRoomRowResponse: Decodable, Identifiable {

    let chatRoomId: Int64
    let memberId: Int64
    let nickname: String
    let profileUrl: String?
    let unreadCount: Int64
    let lastMessagePreview: String
    let lastMessageAt: String

    var id: Int64 { chatRoomId }
}
