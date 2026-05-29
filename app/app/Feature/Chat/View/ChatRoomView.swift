import SwiftUI

struct ChatRoomView: View {

    @Environment(AppRouter.self) private var router

    @State private var vm = ChatRoomViewModel()
    @State private var toggleChatTrigger = false

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
                    toggleChatTrigger.toggle()

                    Task {
                        await vm.toggleChat()
                    }
                } label: {
                    Image(systemName: vm.isChat ? "bell" : "bell.slash")
                        .font(.body)
                        .foregroundStyle(.primary)
                        .contentTransition(.symbolEffect(.replace))
                }
                .sensoryFeedback(.selection, trigger: toggleChatTrigger)
            }
        }
        .task {
            async let i: Void = vm.isChat()
            async let l: Void = vm.load()

            _ = await (i, l)
        }
        .onChange(of: vm.status) { _, newValue in
            Task {
                await vm.switchView()
            }
        }
        .overlay {
            if vm.isLoading {
                LoadingOverlay()
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
                onRead: vm.read,
                onDelete: vm.delete,
            )
        case .error(let message):
            ErrorRetryView(message: message, retry: vm.switchView)
        }
    }
}
