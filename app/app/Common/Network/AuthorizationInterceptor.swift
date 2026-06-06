import SwiftUI
import Alamofire

final class AuthorizationInterceptor: RequestInterceptor {

    private let maxRetryCount = 1
    private let refresher = TokenRefresher()

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
    
    func retry(
        _ request: Request,
        for session: Session,
        dueTo error: any Error,
        completion: @escaping @Sendable (RetryResult) -> Void
    ) {
        guard let statusCode = request.response?.statusCode, statusCode == 401, request.retryCount < maxRetryCount else {
            completion(.doNotRetryWithError(error))
            return
        }

        Task {
            let success = await refresher.refreshIfNeeded()
            completion(success ? .retry : .doNotRetryWithError(error))
        }
    }
}
