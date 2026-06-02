struct PointHistoryRowResponse: Decodable, Identifiable {
    
    let pointHistoryId: Int64
    let description: String
    let type: PointType
    let point: Int64
    let createdAt: String
    
    var id: Int64 { pointHistoryId }
}
