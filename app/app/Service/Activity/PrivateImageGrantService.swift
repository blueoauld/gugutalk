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

    func gets(
        cursorId: Int64?,
        cursorDateAt: String?,
        size: Int = 20,
    ) async throws -> CursorResponse<ActivityRowResponse> {
        var parameters: [String: Any] = [
            "size": size,
        ]

        if let cursorId {
            parameters["cursorId"] = cursorId
        }
        if let cursorDateAt {
            parameters["cursorDateAt"] = cursorDateAt
        }

        return try await PrivateNetworkManager.shared.request(
            "/private-image-grants",
            method: .get,
            parameters: parameters,
            encoding: URLEncoding.default,
            as: CursorResponse<ActivityRowResponse>.self
        )
    }
}
