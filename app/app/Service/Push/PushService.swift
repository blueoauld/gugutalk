import Alamofire

final class PushService {

    static let shared = PushService()

    func upsert(token: String) async throws {
        try await PrivateNetworkManager.shared.requestVoid(
            "/push?token=\(token)",
            method: .post,
            encoding: JSONEncoding.default
        )
    }

    func delete(token: String) async throws {
        try await PublicNetworkManager.shared.requestVoid(
            "/push?token=\(token)",
            method: .delete,
            encoding: JSONEncoding.default
        )
    }
}
