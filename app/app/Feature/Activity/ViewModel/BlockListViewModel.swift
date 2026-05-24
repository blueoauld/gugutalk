import SwiftUI

enum BlockListViewState {

    case idle
    case loading
    case empty
    case data
    case error(String)
}

@MainActor
@Observable
final class BlockListViewModel {

    private let blockService = BlockService.shared

    var state: BlockListViewState = .idle
    var blocks: [ActivityRowResponse] = []

    private(set) var isPaging = false
    private(set) var isLoading = false
    private var cursor = CursorRequest()
    var hasNext: Bool { cursor.hasNext }

    func load() async {
        state = .loading
        await fetch()
    }

    func loadNext() async {
        guard !isPaging, hasNext else { return }

        isPaging = true
        defer { isPaging = false }

        do {
            let response = try await blockService.gets(
                cursorId: cursor.cursorId,
                cursorDateAt: cursor.cursorDateAt,
                size: 20
            )

            cursor.update(cursorId: response.nextId, cursorDateAt: response.nextDateAt, hasNext: response.hasNext)
            blocks.append(contentsOf: response.payload)
        } catch let error as APIError {
            ToastManager.shared.show(error.message, style: .error)
        } catch {
            ToastManager.shared.show(error.localizedDescription, style: .error)
        }
    }

    func delete(memberId: Int64) async {
        guard !isLoading else { return }

        isLoading = true
        defer { isLoading = false }

        do {
            try await blockService.delete(memberId: memberId)

            withAnimation {
                blocks.removeAll { $0.toId == memberId }
            }

            state = blocks.isEmpty ? .empty : .data
        } catch let error as APIError {
            ToastManager.shared.show(error.message, style: .error)
        } catch {
            ToastManager.shared.show(error.localizedDescription, style: .error)
        }
    }

    private func fetch() async {
        cursor.reset()
        blocks = []

        do {
            let response = try await blockService.gets(
                cursorId: cursor.cursorId,
                cursorDateAt: cursor.cursorDateAt,
                size: 20
            )

            cursor.update(cursorId: response.nextId, cursorDateAt: response.nextDateAt, hasNext: response.hasNext)
            blocks = response.payload
            state = blocks.isEmpty ? .empty : .data
        } catch let error as APIError {
            state = .error(error.message)
        } catch {
            state = .error(error.localizedDescription)
        }
    }
}
