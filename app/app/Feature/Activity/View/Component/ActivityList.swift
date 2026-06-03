import SwiftUI

struct ActivityList: View {

    let items: [ActivityRowResponse]
    let hasNext: Bool
    let onNext: () async -> Void
    let onTap: (Int64) -> Void
    let onDelete: (Int64) async -> Void

    var body: some View {
        List {
            ForEach(items) { it in
                ActivityListRow(
                    memberId: it.toId,
                    nickname: it.nickname,
                    profileUrl: it.profileUrl,
                    gender: it.gender,
                    age: it.age,
                    region: it.region,
                    onTap: {
                        onTap(it.toId)
                    },
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
