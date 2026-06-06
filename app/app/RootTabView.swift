import SwiftUI

struct RootTabView: View {

    enum TabType: Hashable {
        case main
        case chat
        case rank
        case setting
    }

    @State private var mainRouter = AppRouter()
    @State private var chatRouter = AppRouter()
    @State private var rankRouter = AppRouter()
    @State private var settingRouter = AppRouter()

    @State private var chatVM = ChatRoomViewModel()
    @State private var chatBadgeManager = ChatBadgeManager.shared
    @State private var pushRouter = PushRouter.shared

    @State private var selectedTab: TabType = .main
    @State private var hideTabBar = false

    var body: some View {
        tabContent
            .task {
                await chatVM.load()

                chatVM.subscribe()
                handlePendingDeepLink()
            }
            .onChange(of: pushRouter.pending) { _, _ in
                handlePendingDeepLink()
            }
            .onChange(of: mainRouter.path) { _, path in
                hideTabBar = path.last?.hideTabBar ?? false
            }
            .onChange(of: chatRouter.path) { _, path in
                hideTabBar = path.last?.hideTabBar ?? false
            }
            .onChange(of: rankRouter.path) { _, path in
                hideTabBar = path.last?.hideTabBar ?? false
            }
            .onChange(of: settingRouter.path) { _, path in
                hideTabBar = path.last?.hideTabBar ?? false
            }
    }

    private var tabContent: some View {
        TabView(selection: $selectedTab) {
            Tab("메인", systemImage: "house", value: TabType.main) {
                MainNavigationView(router: mainRouter)
                    .toolbarVisibility(hideTabBar ? .hidden : .visible, for: .tabBar)
            }

            Tab("채팅", systemImage: "message", value: TabType.chat) {
                ChatNavigationView(router: chatRouter)
                    .environment(chatVM)
                    .toolbarVisibility(hideTabBar ? .hidden : .visible, for: .tabBar)
            }
            .badge(chatBadgeManager.unreadCount)

            Tab("랭킹", systemImage: "trophy", value: TabType.rank) {
                RankNavigationView(router: rankRouter)
                    .toolbarVisibility(hideTabBar ? .hidden : .visible, for: .tabBar)
            }

            Tab("설정", systemImage: "gearshape", value: TabType.setting) {
                SettingNavigationView(router: settingRouter)
                    .toolbarVisibility(hideTabBar ? .hidden : .visible, for: .tabBar)
            }
        }
        .tabViewStyle(.automatic)
        .sensoryFeedback(.selection, trigger: selectedTab)
    }

    private func handlePendingDeepLink() {
        guard let route = pushRouter.pending else { return }

        pushRouter.pending = nil
        selectedTab = tab(for: route)

        let targetRouter = router(for: route)

        guard targetRouter.path.last != route else { return }
        targetRouter.push(route)
    }

    private func tab(for route: AppRoute) -> TabType {
        switch route {
        case .chat, .chatMessage, .chatRoomSearch, .chatMessageVideo:
            return .chat
        case .rank:
            return .rank
        case .setting, .point:
            return .setting
        default:
            return .main
        }
    }

    private func router(for route: AppRoute) -> AppRouter {
        switch tab(for: route) {
        case .main: return mainRouter
        case .chat: return chatRouter
        case .rank: return rankRouter
        case .setting: return settingRouter
        }
    }
}
