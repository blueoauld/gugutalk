import SwiftUI

struct MemberList: View {
    
    var onRefresh: () async -> Void
    
    @State private var showMessage = false
    @State private var message = ""
    
    var body: some View {
        List {
            ForEach(1...1000, id: \.self) { it in
                NavigationLink(value: AppRoute.member(Int64(it))) {
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
                .buttonStyle(.plain)
                .listRowSeparator(.hidden)
                .listRowInsets(EdgeInsets(top: 4, leading: 12, bottom: 4, trailing: 12))
                .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                    Button {
                        showMessage = true
                    } label: {
                        Image(systemName: "envelope.fill")
                    }
                    .tint(.blue)
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
