struct MemberUpdateProfileRequest: Encodable {

    let publicImages: [MemberImageCreateRequest]
    let privateImages: [MemberImageCreateRequest]
    let nickname: String
    let birthYear: Int
    let region: Region
    let bio: String
}
