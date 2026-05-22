import Alamofire

final class UnlikeService {

    static let shared = UnlikeService()

    func create(
        memberId: Int64,
    ) async throws {
        try await PrivateNetworkManager.shared.requestVoid(
            "/unlikes/\(memberId)",
            method: .post,
            encoding: JSONEncoding.default
        )
    }

    func delete(
        memberId: Int64,
    ) async throws {
        try await PrivateNetworkManager.shared.requestVoid(
            "/unlikes/\(memberId)",
            method: .delete,
            encoding: JSONEncoding.default
        )
    }
}
