import Alamofire

final class LikeService {

    static let shared = LikeService()

    func create(
        memberId: Int64,
    ) async throws {
        try await PrivateNetworkManager.shared.requestVoid(
            "/likes/\(memberId)",
            method: .post,
            encoding: JSONEncoding.default
        )
    }

    func delete(
        memberId: Int64,
    ) async throws {
        try await PrivateNetworkManager.shared.requestVoid(
            "/likes/\(memberId)",
            method: .delete,
            encoding: JSONEncoding.default
        )
    }
}
