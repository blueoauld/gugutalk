import SwiftUI

struct LocationView: View {
    
    @Environment(AppRouter.self) private var router
    
    var body: some View {
        VStack {
            Button {
                router.push(.member(2))
            } label: {
                Text("테스트")
            }
        }
        .navigationTitle("위치")
        .navigationBarTitleDisplayMode(.inline)
    }
}
