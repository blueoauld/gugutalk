import SwiftUI

enum APIError: Error {

    case network
    case unauthorized
    case ban(code: String, uuid: String, reason: String, expiredAt: String)
    case server(code: String, message: String, statusCode: Int)
    case decoding
    case unknown(Error)

    var message: String {
        switch self {
        case .network:
            return "네트워크 연결을 확인해주시길 바랍니다."
        case .unauthorized:
            return "로그인이 필요합니다."
        case .ban:
            return "서비스 이용이 제한된 상태입니다."
        case .server(_, let message, _):
            return message
        case .decoding:
            return "데이터를 불러오지 못했습니다."
        case .unknown:
            return "알 수 없는 오류가 발생했습니다."
        }
    }

    var banInfo: BanInfo? {
        guard case let .ban(_, uuid, reason, expiredAt) = self else { return nil }

        return BanInfo(uuid: uuid, reason: reason, expiredAt: expiredAt)
    }
}

extension Error {

    var userMessage: String {
        (self as? APIError)?.message ?? localizedDescription
    }
}
