import SwiftUI

struct ChatRoomSearchList: View {

    let chatRooms: [ChatRoomRowResponse]
    let hasNext: Bool
    var onNext: () async -> Void

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
    }
}
