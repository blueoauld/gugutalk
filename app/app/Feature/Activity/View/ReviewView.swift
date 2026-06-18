import SwiftUI

struct ReviewView: View {

    let memberId: Int64
    let nickname: String

    @State private var vm = ReviewViewModel()
    @State private var showAlert = false

    var body: some View {
        VStack {
            content
        }
        .onTapGesture {
            hideKeyboard()
        }
        .safeAreaBar(edge: .bottom) {
            ReviewInputBar(review: $vm.review) {
                guard let result = await vm.create(memberId: memberId) else { return }

                switch result {
                case .success():
                    ToastManager.shared.show("리뷰를 작성하셨습니다.", style: .info)
                case .failure(let error):
                    ToastManager.shared.show(error.userMessage, style: .error)
                }
            }
        }
        .navigationTitle("리뷰 (\(nickname))")
        .navigationBarTitleDisplayMode(.inline)
        .task {
            await vm.load(memberId: memberId)

            showAlert = true
        }
        .alert("경고", isPresented: $showAlert) {
            Button("확인", role: .cancel) {}
        } message: {
            Text("신상 공개, 음란, 비방 등 부적절한 내용이 포함된 리뷰는 서비스 이용이 제한될 수 있습니다. 작성하신 리뷰는 삭제가 불가능하니 신중히 작성해 주세요.")
        }
        .overlay {
            if vm.isLoading {
                LoadingOverlay()
            }
        }
        .trackScreen("review")
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
            ReviewList(
                reviews: vm.reviews,
                hasNext: vm.hasNext,
                onNext: {
                    if case .failure(let error) = await vm.loadNext(memberId: memberId) {
                        ToastManager.shared.show(error.userMessage, style: .error)
                    }
                },
                onDelete: { reviewId in
                    if case .failure(let error) = await vm.delete(reviewId: reviewId) {
                        ToastManager.shared.show(error.userMessage, style: .error)
                    }
                }
            )
        case .error(let message):
            ErrorRetryView(message: message, retry: {
                await vm.load(memberId: memberId)
            })
        }
    }
}
