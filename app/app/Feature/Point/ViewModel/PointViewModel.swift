import SwiftUI

enum PointViewState {

    case idle
    case loading
    case empty
    case data
    case error(String)
}

@MainActor
@Observable
final class PointViewModel {

    private let pointService = PointService.shared

    var state: PointViewState = .idle
    var balance: Int64 = 0
    var pointHistories: [PointHistoryRowResponse] = []

    private(set) var isLoading = false
    private(set) var isPaging = false
    private var cursor = CursorRequest()
    var hasNext: Bool { cursor.hasNext }

    func load() async {
        state = .loading
        await fetch()
    }

    func loadNext() async -> Result<Void, Error>? {
        guard !isPaging, hasNext else { return nil }

        isPaging = true
        defer { isPaging = false }

        do {
            let response = try await pointService.gets(
                cursorId: cursor.cursorId,
                cursorDateAt: cursor.cursorDateAt,
                size: 20
            )

            cursor.update(cursorId: response.nextId, cursorDateAt: response.nextDateAt, hasNext: response.hasNext)
            pointHistories.append(contentsOf: response.payload)
            return .success(())
        } catch {
            if let apiError = error as? APIError, case .cancelled = apiError {
                return nil
            }
            return .failure(error)
        }
    }

    func getBalance() async {
        guard !isLoading else { return }

        isLoading = true
        defer { isLoading = false }

        guard let response = try? await pointService.getBalance() else { return }
        balance = response.balance
    }
    
    private func fetch() async {
        cursor.reset()
        pointHistories = []

        do {
            let response = try await pointService.gets(
                cursorId: cursor.cursorId,
                cursorDateAt: cursor.cursorDateAt,
                size: 20
            )

            cursor.update(cursorId: response.nextId, cursorDateAt: response.nextDateAt, hasNext: response.hasNext)
            pointHistories = response.payload
            state = pointHistories.isEmpty ? .empty : .data
        } catch {
            if let apiError = error as? APIError, case .cancelled = apiError {
                return
            }
            state = .error(error.userMessage)
        }
    }
}
