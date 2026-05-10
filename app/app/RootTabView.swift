import SwiftUI

struct RootTabView: View {
    
    enum Tab {
        case recent
        case location
        case chat
        case setting
    }
    
    @State private var recentRouter = AppRouter()
    @State private var locationRouter = AppRouter()
    @State private var chatRouter = AppRouter()
    @State private var settingRouter = AppRouter()
    
    @State private var selectedTab: Tab = .recent
    @State private var hideTabBar = false
    
    var body: some View {
        TabView(selection: $selectedTab) {
            RecentNavigationView(router: recentRouter)
                .tabItem {
                    Label("최근", systemImage: "clock")
                }
                .tag(Tab.recent)
                .toolbarVisibility(hideTabBar ? .hidden : .visible, for: .tabBar)
                .animation(.easeInOut, value: hideTabBar)
            
            LocationNavigationView(router: locationRouter)
                .tabItem {
                    Label("위치", systemImage: "location")
                }
                .tag(Tab.location)
                .toolbarVisibility(hideTabBar ? .hidden : .visible, for: .tabBar)
                .animation(.easeInOut, value: hideTabBar)
            
            ChatNavigationView(router: chatRouter)
                .tabItem {
                    Label("채팅", systemImage: "message")
                }
                .tag(Tab.chat)
                .toolbarVisibility(hideTabBar ? .hidden : .visible, for: .tabBar)
                .animation(.easeInOut, value: hideTabBar)
            
            SettingNavigationView(router: settingRouter)
                .tabItem {
                    Label("설정", systemImage: "gearshape")
                }
                .tag(Tab.setting)
                .toolbarVisibility(hideTabBar ? .hidden : .visible, for: .tabBar)
                .animation(.easeInOut, value: hideTabBar)
        }
        .tabViewStyle(.automatic)
        .sensoryFeedback(.selection, trigger: selectedTab)
        .onChange(of: recentRouter.path) { _, newPath in
            hideTabBar = newPath.last?.hideTabBar ?? false
        }
        .onChange(of: locationRouter.path) { _, newPath in
            hideTabBar = newPath.last?.hideTabBar ?? false
        }
        .onChange(of: chatRouter.path) { _, newPath in
            hideTabBar = newPath.last?.hideTabBar ?? false
        }
        .onChange(of: settingRouter.path) { _, newPath in
            hideTabBar = newPath.last?.hideTabBar ?? false
        }
    }
}
