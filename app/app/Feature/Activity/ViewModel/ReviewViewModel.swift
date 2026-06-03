import SwiftUI

enum ReviewViewState {

    case idle
    case loading
    case empty
    case data
    case error(String)
}

@MainActor
@Observable
final class ReviewViewModel {

    private let reviewService = ReviewService.shared

    var state: ReviewViewState = .idle
    var reviews: [ReviewRowResponse] = []
    var review = ""

    private(set) var isLoading = false
    private(set) var isPaging = false
    private var cursor = CursorRequest()
    var hasNext: Bool { cursor.hasNext }

    func load(memberId: Int64) async {
        state = .loading
        await fetch(memberId: memberId)
    }

    func loadNext(memberId: Int64) async -> Result<Void, Error>? {
        guard !isPaging, hasNext else { return nil }

        isPaging = true
        defer { isPaging = false }

        do {
            let response = try await reviewService.gets(
                memberId: memberId,
                cursorId: cursor.cursorId,
                cursorDateAt: cursor.cursorDateAt,
                size: 20
            )

            cursor.update(cursorId: response.nextId, cursorDateAt: response.nextDateAt, hasNext: response.hasNext)
            reviews.append(contentsOf: response.payload)
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    func create(memberId: Int64) async -> Result<Void, Error>? {
        guard !isLoading else { return nil }

        isLoading = true
        defer { isLoading = false }

        do {
            let response = try await reviewService.create(memberId: memberId, content: review)

            withAnimation {
                reviews.insert(ReviewRowResponse(from: response), at: 0)
                review = ""
            }
            state = .data
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    func delete(reviewId: Int64) async -> Result<Void, Error>? {
        guard !isLoading else { return nil }

        isLoading = true
        defer { isLoading = false }

        do {
            try await reviewService.delete(reviewId: reviewId)

            withAnimation {
                reviews.removeAll { $0.reviewId == reviewId }
            }
            state = reviews.isEmpty ? .empty : .data
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    private func fetch(memberId: Int64) async {
        cursor.reset()
        reviews = []
        
        do {
            let response = try await reviewService.gets(
                memberId: memberId,
                cursorId: cursor.cursorId,
                cursorDateAt: cursor.cursorDateAt,
                size: 20
            )

            cursor.update(cursorId: response.nextId, cursorDateAt: response.nextDateAt, hasNext: response.hasNext)
            reviews = response.payload
            state = reviews.isEmpty ? .empty : .data
        } catch {
            state = .error(error.userMessage)
        }
    }
}
