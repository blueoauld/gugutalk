struct BanInfo: Identifiable, Equatable {

    let uuid: String
    let reason: String
    let expiredAt: String

    var id: String { uuid }
}
