enum ReportType: String, CaseIterable, Identifiable, Codable {

    case abuse = "ABUSE"
    case spam = "SPAM"
    case minor = "MINOR"
    case sexual = "SEXUAL"
    case fake = "FAKE"
    case etc = "ETC"

    var id: String { self.rawValue }

    var label: String {
        switch self {
        case .abuse: return "욕설 / 비방"
        case .spam: return "스팸 / 광고"
        case .minor: return "미성년자"
        case .sexual: return "음란물"
        case .fake: return "도용"
        case .etc: return "기타"
        }
    }
}
