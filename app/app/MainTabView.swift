import SwiftUI

struct MainTabView: View {

    var body: some View {
        TabView {
            RecentNavigationView()
                .tabItem {
                    Label("최근", systemImage: "clock")
                }

            LocationNavigationView()
                .tabItem {
                    Label("위치", systemImage: "location")
                }
        }
    }
}
