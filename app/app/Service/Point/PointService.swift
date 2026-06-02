import Alamofire

final class PointService {

    static let shared = PointService()

    func getBalance() async throws -> PointGetBalanceResponse {
        return try await PrivateNetworkManager.shared.request(
            "/points/balance",
            method: .get,
            encoding: URLEncoding.default,
            as: PointGetBalanceResponse.self
        )
    }

    func rewardAttendance() async throws {
        try await PrivateNetworkManager.shared.requestVoid(
            "/points/attendance",
            method: .post,
            encoding: JSONEncoding.default
        )
    }

    func rewardAdvertisement() async throws {
        try await PrivateNetworkManager.shared.requestVoid(
            "/points/advertisement",
            method: .post,
            encoding: JSONEncoding.default
        )
    }

    func gets(
        cursorId: Int64?,
        cursorDateAt: String?,
        size: Int = 20,
    ) async throws -> CursorResponse<PointHistoryRowResponse> {
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
            "/points",
            method: .get,
            parameters: parameters,
            encoding: URLEncoding.default,
            as: CursorResponse<PointHistoryRowResponse>.self
        )
    }
}
