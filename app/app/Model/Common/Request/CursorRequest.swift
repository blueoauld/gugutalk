struct CursorRequest {

    var cursorId: Int64? = nil
    var cursorDateAt: String? = nil
    var hasNext = false

    mutating func reset() {
        self = .init()
    }

    mutating func update(cursorId: Int64?, cursorDateAt: String?, hasNext: Bool) {
        self.cursorId = cursorId
        self.cursorDateAt = cursorDateAt
        self.hasNext = hasNext
    }
}
