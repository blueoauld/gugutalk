import SwiftUI

struct AppDestination: ViewModifier {

    func body(content: Content) -> some View {
        content
            .navigationDestination(for: AppRoute.self) { route in
                switch route {
                case .recent:
                    RecentView()
                case .location:
                    LocationView()
                case .chat:
                    ChatView()
                case .setting:
                    SettingView()
                case .memberSearch:
                    MemberSearchView()
                case .member(let memberId):
                    MemberView(memberId: memberId)
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
                }
            }
    }
}

extension View {

    func appDestination() -> some View {
        modifier(AppDestination())
    }
}
