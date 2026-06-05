import SwiftUI
import Alamofire

final class PrivateNetworkManager {

    static let shared = PrivateNetworkManager()

    let session: Session

    private init() {
        let configuration = URLSessionConfiguration.default
        configuration.timeoutIntervalForRequest = 30
        configuration.timeoutIntervalForResource = 60

        self.session = Session(
            configuration: configuration,
            interceptor: AuthorizationInterceptor()
        )
    }

    func request<T: Decodable>(
        _ path: String,
        method: HTTPMethod = .get,
        parameters: Parameters? = nil,
        encoding: ParameterEncoding = JSONEncoding.default,
        headers: HTTPHeaders? = nil,
        as type: T.Type
    ) async throws -> T {
        let url = APIEnvironment.baseURL + path

        let response = await session.request(
            url,
            method: method,
            parameters: parameters,
            encoding: encoding,
            headers: headers
        )
            .validate(statusCode: 200..<300)
            .serializingDecodable(T.self)
            .response

        return try handle(response: response)
    }

    func requestVoid(
        _ path: String,
        method: HTTPMethod = .get,
        parameters: Parameters? = nil,
        encoding: ParameterEncoding = URLEncoding.default,
        headers: HTTPHeaders? = nil
    ) async throws {
        let url = APIEnvironment.baseURL + path

        let response = await session.request(
            url,
            method: method,
            parameters: parameters,
            encoding: encoding,
            headers: headers
        )
            .validate(statusCode: 200..<300)
            .serializingData(emptyResponseCodes: [200, 204, 205])
            .response

        try handleVoid(response: response)
    }

    func request<Parameters: Encodable, T: Decodable>(
        _ path: String,
        method: HTTPMethod = .get,
        parameters: Parameters,
        encoder: ParameterEncoder = JSONParameterEncoder.default,
        headers: HTTPHeaders? = nil,
        as type: T.Type
    ) async throws -> T {
        let url = APIEnvironment.baseURL + path

        let response = await session.request(
            url,
            method: method,
            parameters: parameters,
            encoder: encoder,
            headers: headers
        )
            .validate(statusCode: 200..<300)
            .serializingDecodable(T.self)
            .response

        return try handle(response: response)
    }

    func requestVoid<Parameters: Encodable>(
        _ path: String,
        method: HTTPMethod = .get,
        parameters: Parameters,
        encoder: ParameterEncoder = JSONParameterEncoder.default,
        headers: HTTPHeaders? = nil
    ) async throws {
        let url = APIEnvironment.baseURL + path

        let response = await session.request(
            url,
            method: method,
            parameters: parameters,
            encoder: encoder,
            headers: headers
        )
            .validate(statusCode: 200..<300)
            .serializingData(emptyResponseCodes: [200, 204, 205])
            .response

        try handleVoid(response: response)
    }

    private func handle<T: Decodable>(response: DataResponse<T, AFError>) throws -> T {
        switch response.result {
        case .success(let value):
            return value
        case .failure(let afError):
            throw resolveError(
                afError,
                data: response.data,
                statusCode: response.response?.statusCode
            )
        }
    }

    private func handleVoid(response: DataResponse<Data, AFError>) throws {
        if case .failure(let afError) = response.result {
            throw resolveError(
                afError,
                data: response.data,
                statusCode: response.response?.statusCode
            )
        }
    }

    private func resolveError(_ afError: AFError, data: Data?, statusCode: Int?) -> APIError {
        let apiError = error(afError, data: data, statusCode: statusCode)

        if let ban = apiError.banInfo {
            Task { @MainActor in
                SessionStore.shared.handleBan(ban)
            }
        }
        return apiError
    }

    private func error(_ error: AFError, data: Data?, statusCode: Int?) -> APIError {
        if let urlError = error.underlyingError as? URLError {
            switch urlError.code {
            case .notConnectedToInternet, .timedOut, .networkConnectionLost, .cannotConnectToHost, .cannotFindHost, .dataNotAllowed:
                return .network
            default:
                break
            }
        }

        if let statusCode {
            switch statusCode {
            case 401:
                if let data, let errorResponse = try? JSONDecoder().decode(APIBanResponse.self, from: data) {
                    if errorResponse.code == "UNAUTHORIZED_03" {
                        return .ban(
                            code: errorResponse.code,
                            uuid: errorResponse.uuid,
                            reason: errorResponse.reason,
                            expiredAt: errorResponse.expiredAt
                        )
                    }
                }
                return .unauthorized
            case 400...599:
                if let data, let errorResponse = try? JSONDecoder().decode(APIErrorResponse.self, from: data) {
                    return .server(
                        code: errorResponse.code,
                        message: errorResponse.message,
                        statusCode: statusCode
                    )
                }
                return .server(
                    code: "INTERNAL_SERVER_ERROR",
                    message: "서버 오류가 발생했습니다.",
                    statusCode: statusCode
                )
            default:
                break
            }
        }

        if case .responseSerializationFailed = error {
            return .decoding
        }
        return .unknown(error)
    }
}
