import Alamofire

final class ReportService {

    static let shared = ReportService()

    func create(
        memberId: Int64,
        request: ReportCreateRequest
    ) async throws {
        try await PrivateNetworkManager.shared.requestVoid(
            "/reports/\(memberId)",
            method: .post,
            parameters: request,
            encoder: JSONParameterEncoder.default
        )
    }
}
