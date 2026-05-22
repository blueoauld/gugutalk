import Alamofire

final class BlockService {
    
    static let shared = BlockService()
    
    func create(
        memberId: Int64,
    ) async throws {
        try await PrivateNetworkManager.shared.requestVoid(
            "/blocks/\(memberId)",
            method: .post,
            encoding: JSONEncoding.default
        )
    }
    
    func delete(
        memberId: Int64,
    ) async throws {
        try await PrivateNetworkManager.shared.requestVoid(
            "/blocks/\(memberId)",
            method: .delete,
            encoding: JSONEncoding.default
        )
    }
}
