struct CursorScoreRequest {
    
    var cursorId: Int64? = nil
    var cursorScore: Int64? = nil
    var hasNext = false
    
    mutating func reset() {
        self = .init()
    }
    
    mutating func update(cursorId: Int64?, cursorScore: Int64?, hasNext: Bool) {
        self.cursorId = cursorId
        self.cursorScore = cursorScore
        self.hasNext = hasNext
    }
}
