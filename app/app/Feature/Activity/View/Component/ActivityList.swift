import SwiftUI

struct ActivityList: View {

    let likes: [ActivityRowResponse]
    let hasNext: Bool
    var onNext: () async -> Void
    var onDelete: (Int64) async -> Void

    var body: some View {
        List {
            ForEach(likes) { it in
                ActivityListRow(
                    memberId: it.toId,
                    nickname: it.nickname,
                    gender: it.gender,
                    age: it.age,
                    region: it.region,
                    onDelete: {
                        await onDelete(it.toId)
                    }
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
