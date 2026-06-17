enum ReactionType: String, CaseIterable, Codable {

    case heart = "HEART"
    case thumbsUp = "THUMBS_UP"
    case check = "CHECK"
    case laugh = "LAUGH"
    case wow = "WOW"
    case sad = "SAD"

    var emoji: String {
        switch self {
        case .heart: "❤️"
        case .thumbsUp: "👍"
        case .check: "✅"
        case .laugh: "😂"
        case .wow: "😮"
        case .sad: "😢"
        }
    }

    init?(emoji: String) {
        guard let match = Self.allCases.first(where: { $0.emoji == emoji }) else { return nil }
        
        self = match
    }
}
