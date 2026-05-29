import SwiftUI

struct ChatRoomView: View {

    @Environment(AppRouter.self) private var router

    @State private var vm = ChatRoomViewModel()
    @State private var isMute = false

    var body: some View {
        VStack {
            ChatStatusPicker(selectedStatus: $vm.status)

            content
        }
        .navigationTitle("채팅")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                Button {
                } label: {
                    Image(systemName: "magnifyingglass")
                        .font(.body)
                        .foregroundStyle(.primary)
                }
            }

            ToolbarItem(placement: .topBarTrailing) {
                Button {
                    isMute.toggle()
                } label: {
                    Image(systemName: isMute ? "bell.slash" : "bell")
                        .font(.body)
                        .foregroundStyle(.primary)
                        .contentTransition(.symbolEffect(.replace))
                }
                .sensoryFeedback(.selection, trigger: isMute)
            }
        }
        .task {
            await vm.load()
        }
        .onChange(of: vm.status) { _, newValue in
            Task {
                await vm.switchView()
            }
        }
    }

    @ViewBuilder
    private var content: some View {
        switch vm.state {
        case .idle:
            Spacer()
        case .loading:
            ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
        case .empty:
            ScrollView {
                ContentUnavailableView("내역 없음", systemImage: "tray")
                    .containerRelativeFrame([.horizontal, .vertical])
            }
        case .data:
            ChatList(
                chatRooms: vm.chatRooms,
                hasNext: vm.hasNext,
                onNext: vm.loadNext,
            )
        case .error(let message):
            ErrorRetryView(message: message, retry: vm.switchView)
        }
    }
}
