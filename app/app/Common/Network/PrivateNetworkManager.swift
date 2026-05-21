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
        
        return try await session.request(
            url,
            method: method,
            parameters: parameters,
            encoding: encoding,
            headers: headers
        )
        .validate(statusCode: 200..<300)
        .serializingDecodable(T.self)
        .value
    }
}
