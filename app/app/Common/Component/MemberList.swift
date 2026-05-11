import SwiftUI

struct MemberList: View {

    var body: some View {
        List {
            ForEach(0..<100, id: \.self) { _ in
                MemberListRow(
                    nickname: "홍길동",
                    updatedAt: "방금전",
                    comment: "코멘트",
                    gender: Gender.male,
                    age: 20,
                    likes: 100,
                    unlikes: 200,
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
