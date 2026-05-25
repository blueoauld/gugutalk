struct MemberSearchRowResponse: Decodable, Identifiable {
    
    let memberId: Int64
    let nickname: String
    let profileUrl: String?
    let gender: Gender
    let age: Int
    let region: Region
    let updatedAt: String
    
    var id: Int64 { memberId }
}
