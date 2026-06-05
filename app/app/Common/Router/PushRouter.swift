import Foundation

@Observable
final class PushRouter {

    static let shared = PushRouter()

    private init() {}

    var pending: AppRoute?

    func handle(userInfo: [AnyHashable: Any]) {
        guard let type = userInfo["type"] as? String else { return }

        switch type {
        case "MESSAGE":
            guard
                let chatRoomId = (userInfo["chatRoomId"] as? String).flatMap(Int64.init),
                let memberId = (userInfo["memberId"] as? String).flatMap(Int64.init),
                let nickname = userInfo["nickname"] as? String
            else { return }

            let profileUrl = userInfo["profileUrl"] as? String
            pending = .chatMessage(chatRoomId, memberId, nickname, profileUrl)
        default:
            break
        }
    }
}
