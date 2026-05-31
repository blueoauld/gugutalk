struct ChatRoomSearchRowResponse: Decodable, Identifiable {
    
    let chatRoomId: Int64
    let memberId: Int64
    let nickname: String
    let profileUrl: String?
    var lastMessagePreview: String
    var lastMessageAt: String
    
    var id: Int64 { chatRoomId }
}
