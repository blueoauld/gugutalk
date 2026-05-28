import SwiftUI

enum PrivateImageFullScreenViewState {

    case idle
    case loading
    case data
    case error(String)
}

@MainActor
@Observable
final class PrivateImageFullScreenViewModel {

    private let memberService = MemberService.shared

    var state: PrivateImageFullScreenViewState = .idle
    var phone: String? = nil
    var images: [MemberImageResponse]? = nil

    private(set) var isLoading = false

    func getPrivateImages(memberId: Int64) async {
        state = .loading

        do {
            let response = try await memberService.getPrivateImages(memberId: memberId)

            phone = response.phone
            images = response.images
            state = .data
        } catch let error as APIError {
            state = .error(error.message)
        } catch {
            state = .error(error.localizedDescription)
        }
    }
}
