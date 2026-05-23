import SwiftUI

struct ReportView: View {

    let memberId: Int64
    let nickname: String

    @State private var showAlert = false
    @State private var selectImages: [PickedImage] = []
    @State private var reason = ""

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                ReportMenu()

                ImagePicker(selectImages: $selectImages, maxCount: 5)

                ReportReason(reason: $reason)
            }
            .padding()
        }
        .safeAreaBar(edge: .bottom) {
            SubmitButton(title: "신고하기", disabled: reason.isEmpty) {

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
    }
}
