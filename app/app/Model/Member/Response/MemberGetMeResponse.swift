struct MemberGetMeResponse: Decodable {

    let memberId: Int64
    let images: [MemberImageResponse]
    let nickname: String
    let birthYear: Int
    let region: Region
    let bio: String
}
