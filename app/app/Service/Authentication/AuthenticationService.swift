import Alamofire

final class AuthenticationService {

    static let shared = AuthenticationService()

    func sendVerificationCode(phone: String, deviceId: String) async throws {
        try await PublicNetworkManager.shared.requestVoid(
            "/authentications/verification-code",
            method: .post,
            parameters: ["phone": phone, "deviceId": deviceId],
            encoding: JSONEncoding.default
        )
    }
}
