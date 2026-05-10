enum Gender: String, CaseIterable, Identifiable {

    case male = "남자"
    case female = "여자"

    var id: String { self.rawValue }
}
