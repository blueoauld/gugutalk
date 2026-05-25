import Alamofire

final class MemberService {

    static let shared = MemberService()

    func updateComment(
        content: String,
    ) async throws {
        try await PrivateNetworkManager.shared.requestVoid(
            "/members/comment",
            method: .patch,
            parameters: [
                "content": content,
            ],
            encoding: JSONEncoding.default
        )
    }

    func bump() async throws {
        try await PrivateNetworkManager.shared.requestVoid(
            "/members/bump",
            method: .patch,
            encoding: JSONEncoding.default
        )
    }

    func get(
        memberId: Int64
    ) async throws -> MemberGetResponse {
        return try await PrivateNetworkManager.shared.request(
            "/members/\(memberId)",
            method: .get,
            encoding: URLEncoding.default,
            as: MemberGetResponse.self
        )
    }

    func gets(
        gender: String,
        cursorId: Int64?,
        cursorDateAt: String?,
        size: Int = 20,
    ) async throws -> CursorResponse<MemberRowResponse> {
        var parameters: [String: Any] = [
            "gender": gender,
            "size": size,
        ]

        if let cursorId {
            parameters["cursorId"] = cursorId
        }
        if let cursorDateAt {
            parameters["cursorDateAt"] = cursorDateAt
        }

        return try await PrivateNetworkManager.shared.request(
            "/members",
            method: .get,
            parameters: parameters,
            encoding: URLEncoding.default,
            as: CursorResponse<MemberRowResponse>.self
        )
    }

    func getsByRegion(
        gender: String,
        cursorId: Int64?,
        cursorDateAt: String?,
        size: Int = 20,
    ) async throws -> CursorResponse<MemberRowResponse> {
        var parameters: [String: Any] = [
            "gender": gender,
            "size": size,
        ]

        if let cursorId {
            parameters["cursorId"] = cursorId
        }
        if let cursorDateAt {
            parameters["cursorDateAt"] = cursorDateAt
        }

        return try await PrivateNetworkManager.shared.request(
            "/members/region",
            method: .get,
            parameters: parameters,
            encoding: URLEncoding.default,
            as: CursorResponse<MemberRowResponse>.self
        )
    }

    func search(
        nickname: String,
        cursorId: Int64?,
        cursorDateAt: String?,
        size: Int = 20,
    ) async throws -> CursorResponse<MemberSearchRowResponse> {
        var parameters: [String: Any] = [
            "nickname": nickname,
            "size": size,
        ]

        if let cursorId {
            parameters["cursorId"] = cursorId
        }
        if let cursorDateAt {
            parameters["cursorDateAt"] = cursorDateAt
        }

        return try await PrivateNetworkManager.shared.request(
            "/members/search",
            method: .get,
            parameters: parameters,
            encoding: URLEncoding.default,
            as: CursorResponse<MemberSearchRowResponse>.self
        )
    }
}
