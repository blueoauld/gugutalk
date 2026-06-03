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
    private let chatRoomService = ChatRoomService.shared

    var state: MemberViewState = .idle
    var member: MemberGetResponse? = nil

    private(set) var isLoading = false
    private(set) var isMutating = false

    func get(memberId: Int64) async {
        state = .loading

        do {
            member = try await memberService.get(memberId: memberId)
            state = .data
        } catch {
            state = .error(error.userMessage)
        }
    }

    func createLike(memberId: Int64) async -> Result<Void, Error>? {
        guard !isMutating else { return nil }

        isMutating = true
        defer { isMutating = false }

        member?.isLike = true
        member?.likes += 1

        do {
            try await likeService.create(memberId: memberId)
            return .success(())
        } catch {
            member?.isLike = false
            member?.likes -= 1
            return .failure(error)
        }
    }

    func deleteLike(memberId: Int64) async -> Result<Void, Error>? {
        guard !isMutating else { return nil }

        isMutating = true
        defer { isMutating = false }

        member?.isLike = false
        member?.likes -= 1

        do {
            try await likeService.delete(memberId: memberId)
            return .success(())
        } catch {
            member?.isLike = true
            member?.likes += 1
            return .failure(error)
        }
    }

    func createUnlike(memberId: Int64) async -> Result<Void, Error>? {
        guard !isMutating else { return nil }

        isMutating = true
        defer { isMutating = false }

        member?.isUnlike = true
        member?.unlikes += 1

        do {
            try await unlikeService.create(memberId: memberId)
            return .success(())
        } catch {
            member?.isUnlike = false
            member?.unlikes -= 1
            return .failure(error)
        }
    }

    func deleteUnlike(memberId: Int64) async -> Result<Void, Error>? {
        guard !isMutating else { return nil }

        isMutating = true
        defer { isMutating = false }

        member?.isUnlike = false
        member?.unlikes -= 1

        do {
            try await unlikeService.delete(memberId: memberId)
            return .success(())
        } catch {
            member?.isUnlike = true
            member?.unlikes += 1
            return .failure(error)
        }
    }

    func createBlock(memberId: Int64) async -> Result<Void, Error>? {
        guard !isLoading else { return nil }

        isLoading = true
        defer { isLoading = false }

        do {
            try await blockService.create(memberId: memberId)

            member?.isBlock = true
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    func deleteBlock(memberId: Int64) async -> Result<Void, Error>? {
        guard !isLoading else { return nil }

        isLoading = true
        defer { isLoading = false }

        do {
            try await blockService.delete(memberId: memberId)

            member?.isBlock = false
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    func createPrivateImageGrant(memberId: Int64) async -> Result<Void, Error>? {
        guard !isLoading else { return nil }

        isLoading = true
        defer { isLoading = false }

        do {
            try await privateImageGrantService.create(memberId: memberId)

            member?.isPrivateImageGrant = true
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    func deletePrivateImageGrant(memberId: Int64) async -> Result<Void, Error>? {
        guard !isLoading else { return nil }

        isLoading = true
        defer { isLoading = false }

        do {
            try await privateImageGrantService.delete(memberId: memberId)

            member?.isPrivateImageGrant = false
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    func createChatRoom(memberId: Int64, message: String) async -> Result<Void, Error>? {
        guard !isLoading else { return nil }

        isLoading = true
        defer { isLoading = false }
        
        do {
            try await chatRoomService.create(targetId: memberId, content: message)
            return .success(())
        } catch {
            return .failure(error)
        }
    }
}
