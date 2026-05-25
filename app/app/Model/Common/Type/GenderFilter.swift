enum GenderFilter: String, CaseIterable {

    case all = "ALL"
    case female = "FEMALE"
    case male = "MALE"

    var label: String {
        switch self {
        case .all: return "전체"
        case .female: return "여자"
        case .male: return "남자"
        }
    }
}
