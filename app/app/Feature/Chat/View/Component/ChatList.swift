import SwiftUI

struct ChatList: View {

    var body: some View {
        List {
            ForEach(1...1000, id: \.self) { it in
                ChatListRow(
                    nickname: "홍길동 \(it)",
                    updatedAt: "오후 12:00",
                    message: String(repeating: "메시지 ", count: it % 15 + 1),
                    unreads: it
                )
            }
        }
        .listStyle(.plain)
    }
}
