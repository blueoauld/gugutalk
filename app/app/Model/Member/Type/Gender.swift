enum Gender: String, CaseIterable, Identifiable, Codable {
    
    case male = "MALE"
    case female = "FEMALE"
    
    var id: String { self.rawValue }
    
    var label: String {
        switch self {
        case .male: return "남자"
        case .female: return "여자"
        }
    }
}
