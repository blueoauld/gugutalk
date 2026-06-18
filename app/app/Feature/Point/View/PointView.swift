import SwiftUI

struct PointView: View {
    
    @State private var vm = PointViewModel()
    
    var body: some View {
        VStack(alignment: .leading) {
            PointHeader(balance: vm.balance)
            
            content
        }
        .navigationTitle("포인트")
        .navigationBarTitleDisplayMode(.inline)
        .task {
            async let b: Void = vm.getBalance()
            async let l: Void = vm.load()

            _ = await (b, l)
        }
        .trackScreen("point")
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
            PointHistoryList(
                items: vm.pointHistories,
                hasNext: vm.hasNext,
                onNext: {
                    if case .failure(let error) = await vm.loadNext() {
                        ToastManager.shared.show(error.userMessage, style: .error)
                    }
                }
            )
        case .error(let message):
            ErrorRetryView(message: message, retry: vm.load)
        }
    }
}
