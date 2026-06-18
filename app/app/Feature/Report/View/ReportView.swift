import SwiftUI

struct ReportView: View {

    let memberId: Int64
    let nickname: String

    @Environment(AppRouter.self) private var router

    @State private var vm = ReportViewModel()
    @State private var showAlert = false

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                ReportMenu(reportType: $vm.reportType)

                ImagePicker(selectImages: $vm.selectImages, maxCount: 5)

                ReportReason(reason: $vm.reason)
            }
            .padding()
        }
        .safeAreaBar(edge: .bottom) {
            SubmitButton(title: "신고하기", disabled: vm.reportType == nil || vm.isLoading) {
                Task {
                    guard let result = await vm.create(memberId: memberId) else { return }

                    switch result {
                    case .success():
                        ToastManager.shared.show("신고가 접수되었습니다.", style: .info)
                        router.pop()
                    case .failure(let error):
                        ToastManager.shared.show(error.userMessage, style: .error)
                    }
                }
            }
        }
        .onTapGesture {
            hideKeyboard()
        }
        .task {
            showAlert = true
        }
        .navigationTitle("신고 (\(nickname))")
        .navigationBarTitleDisplayMode(.inline)
        .alert("경고", isPresented: $showAlert) {
            Button("확인", role: .cancel) {}
        } message: {
            Text("부정한 목적의 신고 기능 사용이 확인될 경우 서비스 이용이 제한될 수 있습니다.")
        }
        .overlay {
            if vm.isLoading {
                LoadingOverlay()
            }
        }
        .trackScreen("report")
    }
}
