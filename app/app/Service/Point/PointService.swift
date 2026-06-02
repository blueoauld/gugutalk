import Alamofire

final class PointService {

    static let shared = PointService()

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
}
