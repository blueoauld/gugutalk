import SwiftUI

struct RootTabView: View {

    enum TabType: Hashable {
        case recent
        case location
        case chat
        case setting
    }

    @State private var recentRouter = AppRouter()
    @State private var locationRouter = AppRouter()
    @State private var chatRouter = AppRouter()
    @State private var settingRouter = AppRouter()

    @State private var selectedTab: TabType = .recent
    @State private var hideTabBar = false

    var body: some View {
        tabContent
            .onChange(of: recentRouter.path) { _, path in
                hideTabBar = path.last?.hideTabBar ?? false
            }
            .onChange(of: locationRouter.path) { _, path in
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
            Tab("최근", systemImage: "clock", value: TabType.recent) {
                RecentNavigationView(router: recentRouter)
                    .toolbarVisibility(hideTabBar ? .hidden : .visible, for: .tabBar)
            }

            Tab("위치", systemImage: "location", value: TabType.location) {
                LocationNavigationView(router: locationRouter)
                    .toolbarVisibility(hideTabBar ? .hidden : .visible, for: .tabBar)
            }

            Tab("채팅", systemImage: "message", value: TabType.chat) {
                ChatNavigationView(router: chatRouter)
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
}
