import SwiftUI

struct ChatList: View {

    let chatRooms: [ChatRoomRowResponse]
    let hasNext: Bool
    var onNext: () async -> Void

    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        List {
            ForEach(chatRooms) { it in
                ChatListRow(
                    nickname: it.nickname,
                    profileUrl: it.profileUrl,
                    updatedAt: it.lastMessageAt,
                    message: it.lastMessagePreview,
                    unreadCount: it.unreadCount
                )
                .listRowSeparator(.hidden)
                .listRowInsets(EdgeInsets())
                .swipeActions(edge: .leading, allowsFullSwipe: false) {
                    Button {
                    } label: {
                        Image(systemName: colorScheme == .dark ? "eyes" : "eyes.inverse")
                    }
                    .tint(.gray)
                }
                .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                    Button {
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
    }
}
