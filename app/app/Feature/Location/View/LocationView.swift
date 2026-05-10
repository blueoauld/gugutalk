import SwiftUI

struct LocationView: View {

    @Environment(LocationRouter.self) private var router

    var body: some View {
        VStack {
            Button {
                router.push(.member(2))
            } label: {
                Text("테스트")
            }
        }
    }
}
