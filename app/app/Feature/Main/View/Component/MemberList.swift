import SwiftUI

struct MemberList: View {

    let members: [MemberRowResponse]
    let message: String
    let hasNext: Bool
    let onNext: () async -> Void
    let onRefresh: () async -> Void
    let onTap: (Int64) -> Void
    let onSend: (_ memberId: Int64, _ message: String) async -> Void

    @State private var showMessage = false
    @State private var targetId: Int64?
    @State private var draft = ""

    var body: some View {
        List {
            ForEach(members) { it in
                MemberListRow(
                    profileUrl: it.profileUrl,
                    nickname: it.nickname,
                    updatedAt: it.updatedAt,
                    comment: it.comment,
                    gender: it.gender,
                    age: it.age,
                    likes: it.likes,
                    unlikes: it.unlikes,
                    reviews: it.reviews,
                    region: it.region,
                    onTap: {
                        onTap(it.memberId)
                    },
                )
                .listRowSeparator(.hidden)
                .listRowInsets(EdgeInsets())
                .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                    Button {
                        targetId = it.memberId
                        draft = message
                        showMessage = true
                    } label: {
                        Image(systemName: "envelope.fill")
                    }
                    .tint(.blue)
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
        .refreshable {
            await onRefresh()
        }
        .listStyle(.plain)
        .scrollContentBackground(.hidden)
        .navigationLinkIndicatorVisibility(.hidden)
        .alert("쪽지", isPresented: $showMessage) {
            TextField("내용 입력 (15P)", text: $draft)

            Button("전송") {
                guard let targetId = targetId else { return }

                Task {
                    await onSend(targetId, draft)
                }
            }

            Button("취소", role: .cancel) { }
        }
    }
}
