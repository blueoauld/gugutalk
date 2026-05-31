import Foundation

@MainActor
@Observable
final class ChatBadgeManager {

    static let shared = ChatBadgeManager()

    private init() {}

    var unreadCount = 0
}
