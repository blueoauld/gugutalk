enum AppRoute: Hashable {
    
    case main
    case chat
    case setting
    case memberSearch
    case member(Int64)
    case memberProfile
    case review(Int64, String)
    case report(Int64, String)
    case likeList
    case unlikeList
    case privateImageGrantList
    case blockList
    case chatMessage(Int64, Int64, String, String?)
    case chatRoomSearch

    var hideTabBar: Bool {
        switch self {
        case .member: return true
        case .memberProfile: return true
        case .review: return true
        case .report: return true
        case .likeList: return true
        case .unlikeList: return true
        case .privateImageGrantList: return true
        case .blockList: return true
        case .chatMessage: return true
        case .chatRoomSearch: return true
        default: return false
        }
    }
}
