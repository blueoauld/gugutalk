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
}
