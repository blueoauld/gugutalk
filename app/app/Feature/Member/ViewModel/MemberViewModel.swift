import SwiftUI

enum MemberViewState {

    case idle
    case loading
    case data
    case error(String)
}

@MainActor
@Observable
final class MemberViewModel {

    private let memberService = MemberService.shared
    private let likeService = LikeService.shared
    private let unlikeService = UnlikeService.shared
    private let blockService = BlockService.shared
    private let privateImageGrantService = PrivateImageGrantService.shared

    var state: MemberViewState = .idle
    var member: MemberGetResponse? = nil

    private(set) var isLoading = false
    private(set) var isMutating = false

    func get(memberId: Int64) async {
        state = .loading

        do {
            member = try await memberService.get(memberId: memberId)
            state = .data
        } catch let error as APIError {
            state = .error(error.message)
        } catch {
            state = .error(error.localizedDescription)
        }
    }

    func createLike(memberId: Int64) async {
        guard !isMutating else { return }

        isMutating = true
        defer { isMutating = false }

        member?.isLike = true
        member?.likes += 1

        do {
            try await likeService.create(memberId: memberId)
        } catch let error as APIError {
            member?.isLike = false
            member?.likes -= 1

            ToastManager.shared.show(error.message, style: .error)
        } catch {
            member?.isLike = false
            member?.likes -= 1

            ToastManager.shared.show(error.localizedDescription, style: .error)
        }
    }

    func deleteLike(memberId: Int64) async {
        guard !isMutating else { return }

        isMutating = true
        defer { isMutating = false }

        member?.isLike = false
        member?.likes -= 1

        do {
            try await likeService.delete(memberId: memberId)
        } catch let error as APIError {
            member?.isLike = true
            member?.likes += 1

            ToastManager.shared.show(error.message, style: .error)
        } catch {
            member?.isLike = true
            member?.likes += 1

            ToastManager.shared.show(error.localizedDescription, style: .error)
        }
    }

    func createUnlike(memberId: Int64) async {
        guard !isMutating else { return }

        isMutating = true
        defer { isMutating = false }

        member?.isUnlike = true
        member?.unlikes += 1

        do {
            try await unlikeService.create(memberId: memberId)
        } catch let error as APIError {
            member?.isUnlike = false
            member?.unlikes -= 1

            ToastManager.shared.show(error.message, style: .error)
        } catch {
            member?.isUnlike = false
            member?.unlikes -= 1

            ToastManager.shared.show(error.localizedDescription, style: .error)
        }
    }

    func deleteUnlike(memberId: Int64) async {
        guard !isMutating else { return }

        isMutating = true
        defer { isMutating = false }

        member?.isUnlike = false
        member?.unlikes -= 1

        do {
            try await unlikeService.delete(memberId: memberId)
        } catch let error as APIError {
            member?.isUnlike = false
            member?.unlikes += 1

            ToastManager.shared.show(error.message, style: .error)
        } catch {
            member?.isUnlike = false
            member?.unlikes += 1

            ToastManager.shared.show(error.localizedDescription, style: .error)
        }
    }

    func createBlock(memberId: Int64) async {
        guard !isLoading else { return }

        isLoading = true
        defer { isLoading = false }

        do {
            try await blockService.create(memberId: memberId)

            member?.isBlock = true
            ToastManager.shared.show("차단하셨습니다.", style: .info)
        } catch let error as APIError {
            ToastManager.shared.show(error.message, style: .error)
        } catch {
            ToastManager.shared.show(error.localizedDescription, style: .error)
        }
    }

    func deleteBlock(memberId: Int64) async {
        guard !isLoading else { return }

        isLoading = true
        defer { isLoading = false }

        do {
            try await blockService.delete(memberId: memberId)

            member?.isBlock = false
            ToastManager.shared.show("차단을 해제하셨습니다.", style: .info)
        } catch let error as APIError {
            ToastManager.shared.show(error.message, style: .error)
        } catch {
            ToastManager.shared.show(error.localizedDescription, style: .error)
        }
    }

    func createPrivateImageGrant(memberId: Int64) async {
        guard !isLoading else { return }

        isLoading = true
        defer { isLoading = false }

        do {
            try await privateImageGrantService.create(memberId: memberId)

            member?.isPrivateImageGrant = true
            ToastManager.shared.show("비밀 사진을 공개하셨습니다.", style: .info)
        } catch let error as APIError {
            ToastManager.shared.show(error.message, style: .error)
        } catch {
            ToastManager.shared.show(error.localizedDescription, style: .error)
        }
    }

    func deletePrivateImageGrant(memberId: Int64) async {
        guard !isLoading else { return }

        isLoading = true
        defer { isLoading = false }

        do {
            try await privateImageGrantService.delete(memberId: memberId)

            member?.isPrivateImageGrant = false
            ToastManager.shared.show("비밀 사진을 닫으셨습니다.", style: .info)
        } catch let error as APIError {
            ToastManager.shared.show(error.message, style: .error)
        } catch {
            ToastManager.shared.show(error.localizedDescription, style: .error)
        }
    }
}
