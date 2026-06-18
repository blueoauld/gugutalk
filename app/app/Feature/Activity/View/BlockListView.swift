import SwiftUI

struct BlockListView: View {

    @Environment(AppRouter.self) private var router

    @State private var vm = BlockListViewModel()

    var body: some View {
        VStack {
            content
        }
        .navigationTitle("차단 목록")
        .navigationBarTitleDisplayMode(.inline)
        .task {
            await vm.load()
        }
        .overlay {
            if vm.isLoading {
                LoadingOverlay()
            }
        }
        .trackScreen("block_list")
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
            ActivityList(
                items: vm.blocks,
                hasNext: vm.hasNext,
                onNext: {
                    if case .failure(let error) = await vm.loadNext() {
                        ToastManager.shared.show(error.userMessage, style: .error)
                    }
                },
                onTap: {
                    router.push(.member($0))
                },
                onDelete: { memberId in
                    if case .failure(let error) = await vm.delete(memberId: memberId) {
                        ToastManager.shared.show(error.userMessage, style: .error)
                    }
                }
            )
        case .error(let message):
            ErrorRetryView(message: message, retry: {
                await vm.load()
            })
        }
    }
}
