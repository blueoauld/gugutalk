import SwiftUI

struct UnlikeListView: View {

    @State private var vm = UnlikeListViewModel()

    var body: some View {
        VStack {
            content
        }
        .navigationTitle("싫어요 목록")
        .navigationBarTitleDisplayMode(.inline)
        .task {
            await vm.load()
        }
        .overlay {
            if vm.isLoading {
                LoadingOverlay()
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
            ActivityList(
                items: vm.unlikes,
                hasNext: vm.hasNext,
                onNext: vm.loadNext,
                onDelete: vm.delete
            )
        case .error(let message):
            ErrorRetryView(message: message, retry: {
                await vm.load()
            })
        }
    }
}
