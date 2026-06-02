import SwiftUI

struct LoginView: View {

    @Environment(AuthenticationRouter.self) private var router
    @Environment(SessionStore.self) private var session

    @State private var vm = LoginViewModel()

    var body: some View {
        ScrollView {
            VStack {
                VStack {
                    CustomTextField(
                        placeholder: "휴대폰",
                        text: $vm.phone,
                        keyboardType: .phonePad,
                    )

                    CustomSecureField(
                        placeholder: "비밀번호",
                        text: $vm.password,
                    )
                }
                .padding(.bottom)

                Button {
                    router.push(.signup)
                } label: {
                    Text("회원가입")
                        .font(.default)
                }
            }
            .padding()
        }
        .onTapGesture {
            hideKeyboard()
        }
        .safeAreaBar(edge: .bottom) {
            SubmitButton(title: "로그인", disabled: !vm.enabled || vm.isLoading) {
                Task {
                    guard let result = await vm.login() else { return }

                    switch result {
                    case .success(let response):
                        ToastManager.shared.show("로그인이 완료되었습니다.", style: .info)
                        session.login(response)
                    case .failure(let error):
                        ToastManager.shared.show(error.userMessage, style: .error)
                    }
                }
            }
        }
        .navigationTitle("로그인")
        .navigationBarTitleDisplayMode(.inline)
        .overlay {
            if vm.isLoading {
                LoadingOverlay()
            }
        }
    }
}
