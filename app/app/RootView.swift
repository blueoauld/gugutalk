import SwiftUI

struct RootView: View {

    @Environment(SessionStore.self) private var session

    var body: some View {

        Group {
            if session.isLoggedIn {
                RootTabView()
            } else {
                AuthenticationNavigationView()
            }
        }
        .animation(.default, value: session.isLoggedIn)
        .onChange(of: session.isLoggedIn, initial: true) { _, loggedIn in
            if loggedIn {
                StompManager.shared.connect()
            } else {
                StompManager.shared.disconnect()
            }
        }
    }
}
