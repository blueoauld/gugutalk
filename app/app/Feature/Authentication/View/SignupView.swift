import SwiftUI

struct SignupView: View {

    @Environment(AuthenticationRouter.self) private var router
    @Environment(SessionStore.self) private var session

    @State private var vm = SignupViewModel()
    @State private var showAlert = false
    @State private var showService = false
    @State private var showPrivate = false

    var body: some View {
        ScrollView {
            VStack(spacing: 24) {
                VStack {
                    HStack {
                        CustomTextField(
                            placeholder: "휴대폰",
                            text: $vm.phone,
                            keyboardType: .phonePad
                        )

                        VerificationCodeSendButton(title: "전송", disabled: !vm.sendCodeEnabled || vm.isLoading) {
                            Task {
                                guard let result = await vm.sendVerificationCode() else { return }

                                switch result {
                                case .success():
                                    ToastManager.shared.show("인증 번호가 전송되었습니다.", style: .info)
                                case .failure:
                                    ToastManager.shared.show("인증 번호 전송에 실패했습니다.", style: .error)
                                }
                            }
                        }
                    }

                    CustomTextField(
                        placeholder: "인증번호",
                        text: $vm.verificationCode,
                        keyboardType: .numberPad
                    )
                }

                VStack {
                    CustomSecureField(
                        placeholder: "비밀번호",
                        text: $vm.password,
                    )

                    CustomSecureField(
                        placeholder: "비밀번호 확인",
                        text: $vm.confirmPassword,
                    )
                }

                GenderSelector(gender: $vm.gender)

                Spacer()

                HStack {
                    Button {
                        showService = true
                    } label: {
                        Text("서비스 이용약관")
                    }
                    .sheet(isPresented: $showService) {
                        SafariView(url: URL(string: "https://pidulgi.com/service.html")!)
                    }

                    Button {
                        showPrivate = true
                    } label: {
                        Text("개인정보 취급방침")
                    }
                    .sheet(isPresented: $showPrivate) {
                        SafariView(url: URL(string: "https://pidulgi.com/private.html")!)
                    }
                }
                .font(.footnote)
            }
            .padding()
        }
        .onTapGesture {
            hideKeyboard()
        }
        .safeAreaBar(edge: .bottom) {
            SubmitButton(title: "회원가입", disabled: !vm.enabled || vm.isLoading) {
                Task {
                    guard let result = await vm.signup() else { return }

                    switch result {
                    case .success():
                        ToastManager.shared.show("회원가입이 완료되었습니다.", style: .info)
                        router.push(.setup)
                    case .failure(let error):
                        ToastManager.shared.show(error.userMessage, style: .error)
                    }
                }
            }
        }
        .task {
            showAlert = true
        }
        .navigationTitle("회원가입")
        .navigationBarTitleDisplayMode(.inline)
        .alert("경고", isPresented: $showAlert) {
            Button("확인", role: .cancel) {}
        } message: {
            Text("미성년자는 이용할 수 없습니다.\n적발 시 서비스 이용이 제한됩니다.")
        }
        .overlay {
            if vm.isLoading {
                LoadingOverlay()
            }
        }
        .trackScreen("signup")
    }
}
