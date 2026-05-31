struct ChatRoomDeleteResponse: Decodable, Identifiable {
    
    let chatRoomId: Int64
    
    var id: Int64 { chatRoomId }
}
