import SwiftUI

struct MemberList: View {

    var body: some View {
        List {
            ForEach(1...1000, id: \.self) { it in
                MemberListRow(
                    nickname: "홍길동 \(it)",
                    updatedAt: "방금전",
                    comment: String(repeating: "코멘트 ", count: it % 15 + 1),
                    gender: Gender.male,
                    age: 20,
                    likes: it,
                    unlikes: 1000 - it,
                    reviews: it,
                    region: Region.seoul
                )
            }
        }
        .listStyle(.plain)
        .refreshable {
            try? await Task.sleep(for: .seconds(1))
        }
    }
}
