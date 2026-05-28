import SwiftUI

struct MemberSearchList: View {

    let items: [MemberSearchRowResponse]
    let hasNext: Bool
    var onNext: () async -> Void

    var body: some View {
        List {
            ForEach(items) { it in
                MemberSearchListRow(
                    memberId: it.memberId,
                    nickname: it.nickname,
                    profileUrl: it.profileUrl,
                    gender: it.gender,
                    age: it.age,
                    region: it.region,
                    updatedAt: it.updatedAt
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
    }
}
