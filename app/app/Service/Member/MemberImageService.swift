import Alamofire

final class MemberImageService {

    static let shared = MemberImageService()

    func createPublicUploadUrls(
        urls: UploadUrlRequests
    ) async throws -> UploadUrlResponses {
        try await PrivateNetworkManager.shared.request(
            "/members/images/public/urls",
            method: .post,
            parameters: urls,
            encoder: JSONParameterEncoder.default,
            as: UploadUrlResponses.self
        )
    }

    func createPrivateUploadUrls(
        urls: UploadUrlRequests
    ) async throws -> UploadUrlResponses {
        try await PrivateNetworkManager.shared.request(
            "/members/images/private/urls",
            method: .post,
            parameters: urls,
            encoder: JSONParameterEncoder.default,
            as: UploadUrlResponses.self
        )
    }
}
