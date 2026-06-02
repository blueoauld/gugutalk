enum RankFilter: String, CaseIterable {

    case like = "LIKE"
    case unlike = "UNLIKE"
    case review = "REVIEW"

    var label: String {
        switch self {
        case .like: return "좋아요"
        case .unlike: return "싫어요"
        case .review: return "리뷰"
        }
    }
}
