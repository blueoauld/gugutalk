import SwiftUI
import Alamofire

final class CommonInterceptor: RequestInterceptor {

    func adapt(
        _ urlRequest: URLRequest,
        for session: Session,
        completion: @escaping (Result<URLRequest, Error>) -> Void
    ) {
        var request = urlRequest

        if let deviceId = TokenStorage.shared.deviceId {
            request.setValue(deviceId, forHTTPHeaderField: "X-Device-Id")
        }
        completion(.success(request))
    }
}
