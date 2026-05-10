import SwiftUI

struct MainTabView: View {

    enum Tab {
        case recent
        case location
    }

    @State private var recentRouter = RecentRouter()
    @State private var locationRouter = LocationRouter()

    @State private var selectedTab: Tab = .recent

    var body: some View {
        TabView(selection: $selectedTab) {
            RecentNavigationView(router: recentRouter)
                .tabItem {
                    Label("최근", systemImage: "clock")
                }
                .tag(Tab.recent)

            LocationNavigationView(router: locationRouter)
                .tabItem {
                    Label("위치", systemImage: "location")
                }
                .tag(Tab.location)
        }
        .tabViewStyle(.automatic)
        .sensoryFeedback(.selection, trigger: selectedTab)
    }
}
