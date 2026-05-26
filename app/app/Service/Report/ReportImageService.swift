import Alamofire

final class ReportImageService {
    
    static let shared = ReportImageService()
    
    func createUploadUrls(
        urls: UploadUrlRequests
    ) async throws -> UploadUrlResponses {
        try await PrivateNetworkManager.shared.request(
            "/reports/images/urls",
            method: .post,
            parameters: urls,
            encoder: JSONParameterEncoder.default,
            as: UploadUrlResponses.self
        )
    }
}
