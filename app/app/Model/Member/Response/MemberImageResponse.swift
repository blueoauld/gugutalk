struct MemberImageResponse: Decodable, Equatable {

    let imageId: Int64
    let type: String
    let key: String
    let url: String
    let sortOrder: Int
}
