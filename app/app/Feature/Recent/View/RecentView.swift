import SwiftUI

struct RecentView: View {

    @Environment(RecentRouter.self) private var router

    var body: some View {
        VStack {
            Button {
                router.push(.member(1))
            } label: {
                Text("테스트")
            }
        }
    }
}
