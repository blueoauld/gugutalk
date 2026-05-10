import SwiftUI

struct SettingView: View {
    
    @Environment(AppRouter.self) private var router
    
    var body: some View {
        VStack {
            Button {
                router.push(.member(4))
            } label: {
                Text("테스트")
            }
        }
        .navigationTitle("설정")
        .navigationBarTitleDisplayMode(.inline)
    }
}
