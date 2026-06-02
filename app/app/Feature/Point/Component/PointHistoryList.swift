import SwiftUI

struct PointHistoryList: View {

    let items: [PointHistoryRowResponse]
    let hasNext: Bool
    var onNext: () async -> Void

    var body: some View {
        List {
            ForEach(items) { it in
                PointHistoryListRow(
                    description: it.description,
                    type: it.type,
                    point: it.point,
                    createdAt: it.createdAt
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
