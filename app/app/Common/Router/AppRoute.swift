enum AppRoute: Hashable {
    
    case recent
    case location
    case chat
    case setting
    case memberSearch
    case member(Int64)
    case review(Int64, String)

    var hideTabBar: Bool {
        switch self {
        case .member: return true
        case .review: return true
        default: return false
        }
    }
}
