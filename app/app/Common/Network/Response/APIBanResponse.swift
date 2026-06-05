struct APIBanResponse: Decodable {

    let code: String
    let uuid: String
    let reason: String
    let expiredAt: String
}
