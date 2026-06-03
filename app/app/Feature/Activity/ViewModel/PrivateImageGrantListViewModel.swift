import SwiftUI

enum PrivateImageGrantListViewState {

    case idle
    case loading
    case empty
    case data
    case error(String)
}

@MainActor
@Observable
final class PrivateImageGrantListViewModel {

    private let privateImageGrantService = PrivateImageGrantService.shared

    var state: PrivateImageGrantListViewState = .idle
    var privateImageGrants: [ActivityRowResponse] = []

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
            let response = try await privateImageGrantService.gets(
                cursorId: cursor.cursorId,
                cursorDateAt: cursor.cursorDateAt,
                size: 20
            )

            cursor.update(cursorId: response.nextId, cursorDateAt: response.nextDateAt, hasNext: response.hasNext)
            privateImageGrants.append(contentsOf: response.payload)
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    func delete(memberId: Int64) async -> Result<Void, Error>? {
        guard !isLoading else { return nil }

        isLoading = true
        defer { isLoading = false }

        do {
            try await privateImageGrantService.delete(memberId: memberId)

            withAnimation {
                privateImageGrants.removeAll { $0.toId == memberId }
            }
            state = privateImageGrants.isEmpty ? .empty : .data
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    private func fetch() async {
        cursor.reset()
        privateImageGrants = []

        do {
            let response = try await privateImageGrantService.gets(
                cursorId: cursor.cursorId,
                cursorDateAt: cursor.cursorDateAt,
                size: 20
            )

            cursor.update(cursorId: response.nextId, cursorDateAt: response.nextDateAt, hasNext: response.hasNext)
            privateImageGrants = response.payload
            state = privateImageGrants.isEmpty ? .empty : .data
        } catch {
            state = .error(error.userMessage)
        }
    }
}
