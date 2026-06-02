import Alamofire

final class ReviewService {

    static let shared = ReviewService()

    func create(
        memberId: Int64,
        content: String,
    ) async throws -> ReviewCreateResponse {
        return try await PrivateNetworkManager.shared.request(
            "/reviews/\(memberId)",
            method: .post,
            parameters: [
                "content": content,
            ],
            encoding: JSONEncoding.default,
            as: ReviewCreateResponse.self
        )
    }

    func delete(
        reviewId: Int64,
    ) async throws {
        try await PrivateNetworkManager.shared.requestVoid(
            "/reviews/\(reviewId)",
            method: .delete,
            encoding: JSONEncoding.default,
        )
    }

    func gets(
        memberId: Int64,
        cursorId: Int64?,
        cursorDateAt: String?,
        size: Int = 20,
    ) async throws -> CursorResponse<ReviewRowResponse> {
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
            "/reviews/\(memberId)",
            method: .get,
            parameters: parameters,
            encoding: URLEncoding.default,
            as: CursorResponse<ReviewRowResponse>.self
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
            "/reviews/rank",
            method: .get,
            parameters: parameters,
            encoding: URLEncoding.default,
            as: CursorScoreResponse<RankRowResponse>.self
        )
    }
}
