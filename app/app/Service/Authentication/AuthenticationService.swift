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

    func signup(
        phone: String,
        deviceId: String,
        verificationCode: String,
        password: String,
        confirmPassword: String,
        gender: Gender
    ) async throws -> SignupResponse {
        try await PublicNetworkManager.shared.request(
            "/authentications/signup",
            method: .post,
            parameters: [
                "phone": phone,
                "deviceId": deviceId,
                "verificationCode": verificationCode,
                "password": password,
                "confirmPassword": confirmPassword,
                "gender": gender.rawValue,
            ],
            encoding: JSONEncoding.default,
            as: SignupResponse.self
        )
    }

    func setup(
        nickname: String,
        birthYear: String,
        region: Region,
        bio: String,
    ) async throws {
        try await PrivateNetworkManager.shared.requestVoid(
            "/authentications/setup",
            method: .put,
            parameters: [
                "nickname": nickname,
                "birthYear": Int(birthYear),
                "region": region.rawValue,
                "bio": bio,
            ],
            encoding: JSONEncoding.default
        )
    }

    func logout(
        accessToken: String,
        refreshToken: String,
    ) async throws {
        try await PrivateNetworkManager.shared.requestVoid(
            "/authentications/logout",
            method: .delete,
            parameters: [
                "accessToken": accessToken,
                "refreshToken": refreshToken
            ],
            encoding: JSONEncoding.default
        )
    }
}
