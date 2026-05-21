struct SignupResponse: Decodable {

    let memberId: Int64
    let accessToken: String
    let refreshToken: String
}
