enum ChatRoomStatusFilter: String, CaseIterable {

    case all = "ALL"
    case unread = "UNREAD"

    var label: String {
        switch self {
        case .all: return "전체"
        case .unread: return "안읽음"
        }
    }
}
