enum AppRoute: Hashable {
    
    case recent
    case location
    case chat
    case setting
    case memberSearch
    case member(Int64)
    case review(Int64, String)
    case report(Int64, String)
    case likeList
    case unlikeList
    case privateImageGrantList
    case blockList

    var hideTabBar: Bool {
        switch self {
        case .member: return true
        case .review: return true
        case .report: return true
        case .likeList: return true
        case .unlikeList: return true
        case .privateImageGrantList: return true
        case .blockList: return true
        default: return false
        }
    }
}
