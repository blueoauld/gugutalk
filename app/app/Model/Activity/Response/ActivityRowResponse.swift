struct ActivityRowResponse: Decodable, Identifiable {

    let activityId: Int64
    let toId: Int64
    let profileUrl: String?
    let nickname: String
    let gender: Gender
    let age: Int
    let region: Region
    let createdAt: String

    var id: Int64 { activityId }
}
