import SwiftUI

struct ChatRoomSearchList: View {

    let chatRooms: [ChatRoomSearchRowResponse]
    let hasNext: Bool
    let onTap: (Int64, Int64, String, String?) -> Void
    let onNext: () async -> Void

    var body: some View {
        List {
            ForEach(chatRooms) { it in
                ChatRoomListRow(
                    nickname: it.nickname,
                    profileUrl: it.profileUrl,
                    updatedAt: it.lastMessageAt,
                    message: it.lastMessagePreview,
                    unreadCount: 0,
                    onTap: {
                        onTap(it.chatRoomId, it.memberId, it.nickname, it.profileUrl)
                    },
                )
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
