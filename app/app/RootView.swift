import SwiftUI

struct RootView: View {

    @Environment(SessionStore.self) private var session

    var body: some View {

        Group {
            if let banInfo = session.banInfo {
                BanView(banInfo: banInfo)
            } else if session.isLoggedIn {
                RootTabView()
            } else {
                AuthenticationNavigationView()
            }
        }
        .animation(.default, value: session.isLoggedIn)
        .animation(.default, value: session.banInfo)
        .onChange(of: session.isLoggedIn, initial: true) { _, loggedIn in
            if loggedIn, session.banInfo == nil {
                StompManager.shared.connect()
            } else {
                StompManager.shared.disconnect()
            }
        }
        .onChange(of: session.banInfo) { _, info in
            if info != nil {
                StompManager.shared.disconnect()
            }
        }
    }
}
