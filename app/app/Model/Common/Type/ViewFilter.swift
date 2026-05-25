enum ViewFilter: String, CaseIterable {
    
    case recent = "RECENT"
    case region = "REGION"
    
    var label: String {
        switch self {
        case .recent: return "최근"
        case .region: return "지역"
        }
    }
}
