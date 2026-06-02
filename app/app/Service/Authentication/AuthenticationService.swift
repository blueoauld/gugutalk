import Alamofire

final class AuthenticationService {

    static let shared = AuthenticationService()

    func sendVerificationCode(phone: String, deviceId: String) async throws {
        try await PublicNetworkManager.shared.requestVoid(
            "/authentication/verify",
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
        return try await PublicNetworkManager.shared.request(
            "/authentication/signup",
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
        return try await PrivateNetworkManager.shared.requestVoid(
            "/authentication/setup",
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

    func login(
        phone: String,
        password: String,
        deviceId: String,
    ) async throws -> LoginResponse {
        return try await PublicNetworkManager.shared.request(
            "/authentication/login",
            method: .post,
            parameters: [
                "phone": phone,
                "password": password,
                "deviceId": deviceId,
            ],
            encoding: JSONEncoding.default,
            as: LoginResponse.self
        )
    }

    func logout(
        accessToken: String,
        refreshToken: String,
    ) async throws {
        try await PrivateNetworkManager.shared.requestVoid(
            "/authentication/logout",
            method: .delete,
            parameters: [
                "accessToken": accessToken,
                "refreshToken": refreshToken
            ],
            encoding: JSONEncoding.default
        )
    }
}
