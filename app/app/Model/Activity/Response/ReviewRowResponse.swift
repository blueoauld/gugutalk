struct ReviewRowResponse: Decodable, Identifiable {
    
    let reviewId: Int64
    let fromId: Int64
    let toId: Int64
    let nickname: String
    let content: String
    let createdAt: String
    
    var id: Int64 { reviewId }
    
    init(from response: ReviewCreateResponse) {
        self.reviewId = response.reviewId
        self.fromId = response.fromId
        self.toId = response.toId
        self.nickname = response.nickname
        self.content = response.content
        self.createdAt = response.createdAt
    }
}
