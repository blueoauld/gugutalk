import SwiftUI

struct ChatNavigationView: View {
    
    @Bindable var router: AppRouter
    
    var body: some View {
        NavigationStack(path: $router.path) {
            ChatView()
                .appDestination()
        }
        .environment(router)
    }
}
