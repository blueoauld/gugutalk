import SwiftUI

struct SetupView: View {

    @Environment(SessionStore.self) private var session

    @State private var vm = SetupViewModel()

    var body: some View {
        ScrollView {
            VStack {
                CustomTextField(
                    placeholder: "닉네임",
                    text: $vm.nickname,
                    keyboardType: .default
                )

                CustomTextField(
                    placeholder: "출생연도",
                    text: $vm.birthYear,
                    keyboardType: .numberPad
                )

                RegionPicker(region: $vm.region)

                CustomTextEditor(
                    placeholder: "자기소개",
                    text: $vm.bio,
                    keyboardType: .default
                )
            }
            .padding()
        }
        .onTapGesture {
            hideKeyboard()
        }
        .safeAreaBar(edge: .bottom) {
            SubmitButton(title: "들어가기", disabled: !vm.enabled || vm.isLoading) {
                Task {
                    guard let result = await vm.setup() else { return }

                    switch result {
                    case .success():
                        ToastManager.shared.show("계정이 활성화되었습니다.", style: .info)
                        session.isLoggedIn = true
                    case .failure(let error):
                        ToastManager.shared.show(error.userMessage, style: .error)
                    }
                }
            }
        }
        .navigationTitle("프로필")
        .navigationBarTitleDisplayMode(.inline)
        .navigationBarBackButtonHidden(true)
        .overlay {
            if vm.isLoading {
                LoadingOverlay()
            }
        }
        .trackScreen("setup")
    }
}
