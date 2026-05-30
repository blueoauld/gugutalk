import SwiftUI

struct ChatRoomList: View {

    let chatRooms: [ChatRoomRowResponse]
    let hasNext: Bool
    var onNext: () async -> Void
    var onRead: (Int64) async -> Void
    var onDelete: (Int64) async -> Void

    @Environment(\.colorScheme) private var colorScheme

    @State private var readTrigger = false
    @State private var deleteTrigger = false

    var body: some View {
        List {
            ForEach(chatRooms) { it in
                NavigationLink(value: AppRoute.chatMessage(it.chatRoomId, it.memberId, it.nickname, it.profileUrl)) {
                    ChatRoomListRow(
                        nickname: it.nickname,
                        profileUrl: it.profileUrl,
                        updatedAt: it.lastMessageAt,
                        message: it.lastMessagePreview,
                        unreadCount: it.unreadCount
                    )
                }
                .listRowSeparator(.hidden)
                .listRowInsets(EdgeInsets())
                .swipeActions(edge: .leading, allowsFullSwipe: false) {
                    Button {
                        readTrigger.toggle()

                        Task {
                            await onRead(it.chatRoomId)
                        }
                    } label: {
                        Image(systemName: colorScheme == .dark ? "eyes" : "eyes.inverse")
                    }
                    .tint(.gray)
                }
                .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                    Button {
                        deleteTrigger.toggle()

                        Task {
                            await onDelete(it.chatRoomId)
                        }
                    } label: {
                        Image(systemName: "trash.fill")
                    }
                    .tint(.red)
                }
            }

            if hasNext {
                HStack {
                    Spacer()
                    ProgressView()
                    Spacer()
                }
                .listRowSeparator(.hidden)
                .task {
                    await onNext()
                }
            }
        }
        .listStyle(.plain)
        .scrollContentBackground(.hidden)
        .navigationLinkIndicatorVisibility(.hidden)
        .sensoryFeedback(.selection, trigger: readTrigger)
        .sensoryFeedback(.selection, trigger: deleteTrigger)
    }
}
