import SwiftUI

struct RootTabView: View {

    enum TabType: Hashable {
        case main
        case chat
        case setting
    }

    @State private var mainRouter = AppRouter()
    @State private var chatRouter = AppRouter()
    @State private var settingRouter = AppRouter()

    @State private var chatVM = ChatRoomViewModel()
    @State private var chatBadgeManager = ChatBadgeManager.shared

    @State private var selectedTab: TabType = .main
    @State private var hideTabBar = false

    var body: some View {
        tabContent
            .task {
                chatVM.subscribe()
            }
            .onChange(of: mainRouter.path) { _, path in
                hideTabBar = path.last?.hideTabBar ?? false
            }
            .onChange(of: chatRouter.path) { _, path in
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

            Tab("설정", systemImage: "gearshape", value: TabType.setting) {
                SettingNavigationView(router: settingRouter)
                    .toolbarVisibility(hideTabBar ? .hidden : .visible, for: .tabBar)
            }
        }
        .tabViewStyle(.automatic)
        .sensoryFeedback(.selection, trigger: selectedTab)
    }
}
