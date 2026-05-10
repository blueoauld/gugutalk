import SwiftUI

struct RecentView: View {

    @Environment(AppRouter.self) private var router

    var body: some View {
        VStack {
            Button {
                router.push(.member(1))
            } label: {
                Text("테스트")
            }
        }
        .navigationTitle("최근")
        .navigationBarTitleDisplayMode(.inline)
    }
}
