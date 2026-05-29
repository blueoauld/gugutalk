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

    func toggleChat() async throws {
        try await PrivateNetworkManager.shared.requestVoid(
            "/members/is-chat",
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

    func updateProfile(
        request: MemberUpdateProfileRequest
    ) async throws {
        try await PrivateNetworkManager.shared.requestVoid(
            "/members/profile",
            method: .put,
            parameters: request,
            encoder: JSONParameterEncoder.default
        )
    }

    func getMe() async throws -> MemberGetMeResponse {
        return try await PrivateNetworkManager.shared.request(
            "/members/me",
            method: .get,
            encoding: URLEncoding.default,
            as: MemberGetMeResponse.self
        )
    }
    
    func getPrivateImages(
        memberId: Int64
    ) async throws -> MemberGetPrivateImagesResponse {
        return try await PrivateNetworkManager.shared.request(
            "/members/\(memberId)/private-images",
            method: .get,
            encoding: URLEncoding.default,
            as: MemberGetPrivateImagesResponse.self
        )
    }

    func isChat() async throws -> MemberIsChatResponse {
        return try await PrivateNetworkManager.shared.request(
            "/members/is-chat",
            method: .get,
            encoding: URLEncoding.default,
            as: MemberIsChatResponse.self
        )
    }
}
