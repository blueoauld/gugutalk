enum AppRoute: Hashable {
    
    case recent
    case location
    case chat
    case setting
    case memberSearch
    case member(Int64)
    
    var hideTabBar: Bool {
        switch self {
        case .member: return true
        default: return false
        }
    }
}
