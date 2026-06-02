struct RankRowResponse: Decodable, Identifiable {

    let memberId: Int64
    let nickname: String
    let profileUrl: String?
    let gender: Gender
    let age: Int
    let region: Region
    let comment: String
    let updatedAt: String
    let likes: Int64
    let unlikes: Int64
    let reviews: Int64

    var id: Int64 { memberId }
}
