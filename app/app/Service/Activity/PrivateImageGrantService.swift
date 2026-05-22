import Alamofire

final class PrivateImageGrantService {

    static let shared = PrivateImageGrantService()

    func create(
        memberId: Int64,
    ) async throws {
        try await PrivateNetworkManager.shared.requestVoid(
            "/private-image-grants/\(memberId)",
            method: .post,
            encoding: JSONEncoding.default
        )
    }
    
    func delete(
        memberId: Int64,
    ) async throws {
        try await PrivateNetworkManager.shared.requestVoid(
            "/private-image-grants/\(memberId)",
            method: .delete,
            encoding: JSONEncoding.default
        )
    }
}
