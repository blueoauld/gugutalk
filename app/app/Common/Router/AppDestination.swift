import SwiftUI

struct AppDestination: ViewModifier {

    func body(content: Content) -> some View {
        content
            .navigationDestination(for: AppRoute.self) { route in
                switch route {
                case .main:
                    MainView()
                case .chat:
                    ChatRoomView()
                case .rank:
                    RankView()
                case .setting:
                    SettingView()
                case .memberSearch:
                    MemberSearchView()
                case .member(let memberId):
                    MemberView(memberId: memberId)
                case .memberProfile:
                    MemberProfileView()
                case .review(let memberId, let nickname):
                    ReviewView(memberId: memberId, nickname: nickname)
                case .report(let memberId, let nickname):
                    ReportView(memberId: memberId, nickname: nickname)
                case .likeList:
                    LikeListView()
                case .unlikeList:
                    UnlikeListView()
                case .privateImageGrantList:
                    PrivateImageGrantListView()
                case .blockList:
                    BlockListView()
                case .chatMessage(let chatRoomId, let memberId, let nickname, let profileUrl):
                    ChatMessageView(
                        chatRoomId: chatRoomId,
                        memberId: memberId,
                        nickname: nickname,
                        profileUrl: profileUrl
                    )
                case .chatRoomSearch:
                    ChatRoomSearchView()
                case .chatMessageVideo(let chatMessageId, let thumbnailUrl):
                    ChatMessageVideoView(chatMessageId: chatMessageId, thumbnailUrl: thumbnailUrl)
                case .point:
                    PointView()
                }
            }
    }
}

extension View {

    func appDestination() -> some View {
        modifier(AppDestination())
    }
}
