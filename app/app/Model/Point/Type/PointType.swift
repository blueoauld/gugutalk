enum PointType: String, CaseIterable, Identifiable, Codable {
    
    case earn = "EARN"
    case use = "USE"
    
    var id: String { self.rawValue }
}
