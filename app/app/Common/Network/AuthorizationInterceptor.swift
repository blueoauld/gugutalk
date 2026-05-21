import SwiftUI
import Alamofire

final class AuthorizationInterceptor: RequestInterceptor {

    func adapt(
        _ urlRequest: URLRequest,
        for session: Session,
        completion: @escaping (Result<URLRequest, Error>) -> Void
    ) {
        var request = urlRequest

        if let deviceId = TokenStorage.shared.deviceId {
            request.setValue(deviceId, forHTTPHeaderField: "X-Device-Id")
        }
        if let accessToken = TokenStorage.shared.accessToken {
            request.setValue("Bearer \(accessToken)", forHTTPHeaderField: "Authorization")
        }
        completion(.success(request))
    }
}
