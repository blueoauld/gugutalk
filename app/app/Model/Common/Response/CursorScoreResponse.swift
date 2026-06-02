import Foundation

struct CursorScoreResponse<T: Decodable>: Decodable {

    let payload: [T]
    let nextId: Int64?
    let nextScore: Int64?
    let hasNext: Bool
}
