struct ReviewCreateResponse: Decodable {

    let reviewId: Int64
    let fromId: Int64
    let toId: Int64
    let nickname: String
    let content: String
    let createdAt: String
}
