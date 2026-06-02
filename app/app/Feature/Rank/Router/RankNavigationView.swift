import SwiftUI

struct RankNavigationView: View {
    
    @Bindable var router: AppRouter
    
    var body: some View {
        NavigationStack(path: $router.path) {
            RankView()
                .appDestination()
        }
        .environment(router)
    }
}
