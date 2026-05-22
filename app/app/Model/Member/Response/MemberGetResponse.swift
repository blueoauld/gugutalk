struct MemberGetResponse: Decodable {

    let memberId: Int64
    let nickname: String
    let gender: Gender
    let age: Int
    let region: Region
    let bio: String
    let isChat: Bool
    let updatedAt: String
    var likes: Int
    var unlikes: Int
    let reviews: Int
    var isLike: Bool
    var isUnlike: Bool
    var isPrivateImageGrant: Bool
    let hasPrivateImageGrant: Bool
    var isBlock: Bool
}
