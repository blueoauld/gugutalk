import SwiftUI

enum RankViewState {
    
    case idle
    case loading
    case empty
    case data
    case error(String)
}

@MainActor
@Observable
final class RankViewModel {
    
    private let likeService = LikeService.shared
    private let unlikeService = UnlikeService.shared
    private let reviewService = ReviewService.shared
    private let chatRoomService = ChatRoomService.shared
    
    var state: RankViewState = .idle
    var members: [RankRowResponse] = []
    var gender: GenderFilter
    var rank: RankFilter
    
    private(set) var isLoading = false
    private(set) var isPaging = false
    
    private var hasLoad = false
    private var cursor = CursorScoreRequest()
    var hasNext: Bool { cursor.hasNext }
    
    init() {
        let rankRaw = UserDefaults.standard.string(forKey: StorageKey.rank)
        self.rank = rankRaw.flatMap(RankFilter.init(rawValue:)) ?? .like
        
        let genderRaw = UserDefaults.standard.string(forKey: StorageKey.rankGender)
        self.gender = genderRaw.flatMap(GenderFilter.init(rawValue:)) ?? .all
    }
    
    func load() async {
        guard !hasLoad else { return }
        
        hasLoad = true
        state = .loading
        
        await fetch()
    }
    
    func loadNext() async -> Result<Void, Error>? {
        guard !isPaging, hasNext else { return nil }
        
        isPaging = true
        defer { isPaging = false }
        
        do {
            let response = try await requestMembers()
            
            cursor.update(cursorId: response.nextId, cursorScore: response.nextScore, hasNext: response.hasNext)
            members.append(contentsOf: response.payload)
            return .success(())
        } catch {
            if let apiError = error as? APIError, case .cancelled = apiError {
                return nil
            }
            return .failure(error)
        }
    }
    
    func switchView() async {
        state = .loading
        await fetch()
    }
    
    func createChatRoom(memberId: Int64, message: String) async -> Result<Void, Error>? {
        guard !isLoading else { return nil }
        
        isLoading = true
        defer { isLoading = false }
        
        do {
            try await chatRoomService.create(targetId: memberId, content: message)
            return .success(())
        } catch {
            if let apiError = error as? APIError, case .cancelled = apiError {
                return nil
            }
            return .failure(error)
        }
    }
    
    private func fetch() async {
        cursor.reset()
        members = []
        
        do {
            let response = try await requestMembers()
            
            cursor.update(cursorId: response.nextId, cursorScore: response.nextScore, hasNext: response.hasNext)
            members = response.payload
            state = members.isEmpty ? .empty : .data
        } catch {
            if let apiError = error as? APIError, case .cancelled = apiError {
                return
            }
            state = .error(error.userMessage)
        }
    }
    
    private func requestMembers() async throws -> CursorScoreResponse<RankRowResponse> {
        switch rank {
        case .like:
            return try await likeService.getsByRank(
                gender: gender.rawValue,
                cursorId: cursor.cursorId,
                cursorScore: cursor.cursorScore,
                size: 20
            )
        case .unlike:
            return try await unlikeService.getsByRank(
                gender: gender.rawValue,
                cursorId: cursor.cursorId,
                cursorScore: cursor.cursorScore,
                size: 20
            )
        case .review:
            return try await reviewService.getsByRank(
                gender: gender.rawValue,
                cursorId: cursor.cursorId,
                cursorScore: cursor.cursorScore,
                size: 20
            )
        }
    }
}
