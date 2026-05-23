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

    private(set) var isPaging = false
    private(set) var isLoading = false
    private var cursor = CursorRequest()
    var hasNext: Bool { cursor.hasNext }

    func load(memberId: Int64) async {
        state = .loading
        await fetch(memberId: memberId)
    }

    func loadNext(memberId: Int64) async {
        guard !isPaging, hasNext else { return }

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
        } catch let error as APIError {
            ToastManager.shared.show(error.message, style: .error)
        } catch {
            ToastManager.shared.show(error.localizedDescription, style: .error)
        }
    }

    func createReview(memberId: Int64) async {
        guard !isLoading else { return }

        isLoading = true
        defer { isLoading = false }

        do {
            let response = try await reviewService.create(memberId: memberId, content: review)

            reviews.insert(ReviewRowResponse(from: response), at: 0)
            review = ""
            state = .data

            ToastManager.shared.show("리뷰를 작성하셨습니다.", style: .info)
        } catch let error as APIError {
            ToastManager.shared.show(error.message, style: .error)
        } catch {
            ToastManager.shared.show(error.localizedDescription, style: .error)
        }
    }

    func deleteReview(reviewId: Int64) async {
        guard !isLoading else { return }

        isLoading = true
        defer { isLoading = false }

        do {
            try await reviewService.delete(reviewId: reviewId)

            reviews.removeAll { $0.reviewId == reviewId }
            state = reviews.isEmpty ? .empty : .data

            ToastManager.shared.show("리뷰를 삭제하셨습니다.", style: .info)
        } catch let error as APIError {
            ToastManager.shared.show(error.message, style: .error)
        } catch {
            ToastManager.shared.show(error.localizedDescription, style: .error)
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
        } catch let error as APIError {
            state = .error(error.message)
        } catch {
            state = .error(error.localizedDescription)
        }
    }
}
