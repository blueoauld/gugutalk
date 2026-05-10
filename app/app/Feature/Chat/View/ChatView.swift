import SwiftUI

struct ChatView: View {
    
    @Environment(AppRouter.self) private var router
    
    var body: some View {
        VStack {
            Button {
                router.push(.member(3))
            } label: {
                Text("테스트")
            }
        }
        .navigationTitle("채팅")
        .navigationBarTitleDisplayMode(.inline)
    }
}
