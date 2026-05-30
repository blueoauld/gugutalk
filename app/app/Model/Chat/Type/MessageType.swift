enum MessageType: String, CaseIterable, Identifiable, Codable {

    case text = "TEXT"
    case image = "IMAGE"
    case video = "VIDEO"

    var id: String { self.rawValue }
}
