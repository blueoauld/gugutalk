import SwiftUI

struct MemberList: View {

    let members: [MemberRowResponse]
    let hasNext: Bool
    var onNext: () async -> Void
    var onRefresh: () async -> Void

    @State private var showMessage = false
    @State private var message = ""

    var body: some View {
        List {
            ForEach(members) { it in
                NavigationLink(value: AppRoute.member(it.memberId)) {
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
                        region: it.region
                    )
                }
                .buttonStyle(.plain)
                .listRowSeparator(.hidden)
                .listRowInsets(EdgeInsets())
                .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                    Button {
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
        .listStyle(.plain)
        .navigationLinkIndicatorVisibility(.hidden)
        .refreshable {
            await onRefresh()
        }
        .alert("쪽지", isPresented: $showMessage) {
            TextField("내용", text: $message)
            Button("전송") { }
            Button("취소", role: .cancel) { }
        }
    }
}
