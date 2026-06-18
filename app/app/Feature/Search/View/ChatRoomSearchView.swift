import SwiftUI

struct ChatRoomSearchView: View {

    @Environment(AppRouter.self) private var router

    @State private var vm = ChatRoomSearchViewModel()

    var body: some View {
        VStack {
            content
        }
        .navigationTitle("채팅방 검색")
        .navigationBarTitleDisplayMode(.inline)
        .searchable(
            text: $vm.nickname,
            placement: .navigationBarDrawer(displayMode: .always),
            prompt: "닉네임 입력"
        )
        .onSubmit(of: .search) {
            Task {
                if case .failure(let error) = await vm.search() {
                    ToastManager.shared.show(error.userMessage, style: .error)
                }
            }
        }
        .trackScreen("chat_room_search")
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
            ChatRoomSearchList(
                chatRooms: vm.chatRooms,
                hasNext: vm.hasNext,
                onTap: {
                    router.push(.chatMessage($0, $1, $2, $3))
                },
                onNext: {
                    if case .failure(let error) = await vm.loadNext() {
                        ToastManager.shared.show(error.userMessage, style: .error)
                    }
                }
            )
        case .error(let message):
            ErrorRetryView(message: message, retry: {
                if case .failure(let error) = await vm.search() {
                    ToastManager.shared.show(error.userMessage, style: .error)
                }
            })
        }
    }
}
