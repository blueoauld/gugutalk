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
            "/unlikes",
            method: .get,
            parameters: parameters,
            encoding: URLEncoding.default,
            as: CursorResponse<ActivityRowResponse>.self
        )
    }

    func getsByRank(
        gender: String,
        cursorId: Int64?,
        cursorScore: Int64?,
        size: Int = 20,
    ) async throws -> CursorScoreResponse<RankRowResponse> {
        var parameters: [String: Any] = [
            "gender": gender,
            "size": size,
        ]

        if let cursorId {
            parameters["cursorId"] = cursorId
        }
        if let cursorScore {
            parameters["cursorScore"] = cursorScore
        }

        return try await PrivateNetworkManager.shared.request(
            "/unlikes/rank",
            method: .get,
            parameters: parameters,
            encoding: URLEncoding.default,
            as: CursorScoreResponse<RankRowResponse>.self
        )
    }
}
