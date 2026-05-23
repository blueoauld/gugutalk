import SwiftUI

struct ReviewList: View {

    let reviews: [ReviewRowResponse]
    let hasNext: Bool
    var onNext: () async -> Void
    var onDelete: (Int64) async -> Void

    @State private var deleteTarget: ReviewRowResponse?

    var body: some View {
        List {
            ForEach(reviews) { it in
                ReviewListRow(nickname: it.nickname, content: it.content, createdAt: it.createdAt)
                    .listRowSeparator(.hidden)
                    .listRowInsets(EdgeInsets())
                    .listRowBackground(TokenStorage.shared.memberId == it.fromId ? Color.blue.opacity(0.1) : Color.clear)
                    .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                        Button {
                            deleteTarget = it
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
        .alert(
            "리뷰 삭제",
            isPresented: Binding(
                get: { deleteTarget != nil },
                set: { if !$0 { deleteTarget = nil } }
            ),
            presenting: deleteTarget
        ) { review in
            Button("삭제", role: .destructive) {
                Task {
                    await onDelete(review.reviewId)
                }
            }
            Button("취소", role: .cancel) {}
        } message: { _ in
            Text("리뷰를 삭제할 경우 10포인트가 차감됩니다.")
        }
    }
}
