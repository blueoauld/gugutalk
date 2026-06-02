import SwiftUI

struct PointView: View {

    @State private var vm = PointViewModel()

    var body: some View {
        VStack(alignment: .leading) {
            VStack(alignment: .leading) {
                Text("보유 포인트")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)

                HStack(alignment: .firstTextBaseline, spacing: 4) {
                    Text("\(vm.balance)")
                        .font(.largeTitle)
                        .fontWeight(.bold)

                    Text("P")
                        .font(.title3)
                        .foregroundStyle(.secondary)
                }
            }
            .padding()

            content
        }
        .navigationTitle("포인트")
        .navigationBarTitleDisplayMode(.inline)
        .task {
            async let b: Void = vm.getBalance()
            async let l: Void = vm.load()

            _ = await (b, l)
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
            VStack {
                PointHistoryList(
                    items: vm.pointHistories,
                    hasNext: vm.hasNext,
                    onNext: vm.loadNext
                )
            }
        case .error(let message):
            ErrorRetryView(message: message, retry: vm.load)
        }
    }
}
