import SwiftUI

struct MemberList: View {

    let members: [MemberRowResponse]
    let hasNext: Bool
    let onNext: () async -> Void
    let onRefresh: () async -> Void
    let onTap: (Int64) -> Void
    let onSend: (_ memberId: Int64, _ message: String) async -> Void

    @AppStorage(StorageKey.message) private var savedMessage = ""

    @State private var showMessage = false
    @State private var message = ""
    @State private var targetId: Int64?

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
                        message = savedMessage
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
            TextField("내용 입력 (15P)", text: $message)

            Button("전송") {
                guard let targetId = targetId else { return }

                Task {
                    await onSend(targetId, message)

                    savedMessage = message
                }
            }

            Button("취소", role: .cancel) { }
        }
    }
}
