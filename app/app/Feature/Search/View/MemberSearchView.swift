import SwiftUI

struct MemberSearchView: View {

    @Environment(AppRouter.self) private var router

    @State private var vm = MemberSearchViewModel()

    var body: some View {
        VStack {
            content
        }
        .navigationTitle("회원 검색")
        .navigationBarTitleDisplayMode(.inline)
        .searchable(
            text: $vm.nickname,
            placement: .navigationBarDrawer(displayMode: .always),
            prompt: "닉네임 입력"
        )
        .onSubmit(of: .search) {
            Task {
                await vm.search()
            }
        }
    }

    @ViewBuilder
    private var content: some View {
        switch vm.state {
        case .idle:
            Spacer()
        case .loading:
            ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
        case .empty:
            ScrollView {
                ContentUnavailableView("내역 없음", systemImage: "tray")
                    .containerRelativeFrame([.horizontal, .vertical])
            }
        case .data:
            MemberSearchList(items: vm.members, hasNext: vm.hasNext, onNext: vm.loadNext)
        case .error(let message):
            ErrorRetryView(message: message, retry: vm.search)
        }
    }
}
