import SwiftUI

struct ChatList: View {

    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        List {
            ForEach(1...1000, id: \.self) { it in
                ChatListRow(
                    nickname: "홍길동 \(it)",
                    updatedAt: "오후 12:00",
                    message: String(repeating: "메시지 ", count: it % 15 + 1),
                    unreads: it
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
        }
        .listStyle(.plain)
    }
}
