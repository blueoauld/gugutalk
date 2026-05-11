import SwiftUI

struct ChatView: View {

    @Environment(AppRouter.self) private var router

    @State private var status = "ALL"
    @State private var isMute = false

    var body: some View {
        VStack {
            ChatStatusPicker(selectedStatus: $status)

            ChatList()
        }
        .navigationTitle("채팅")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                Button {
                } label: {
                    Image(systemName: "magnifyingglass")
                        .font(.default)
                        .foregroundColor(.primary)
                }
            }

            ToolbarItem(placement: .topBarTrailing) {
                Button {
                    isMute.toggle()
                } label: {
                    Image(systemName: isMute ? "bell.slash" : "bell")
                        .font(.default)
                        .foregroundColor(.primary)
                        .contentTransition(.symbolEffect(.replace))
                }
                .sensoryFeedback(.selection, trigger: isMute)
            }
        }
    }
}
