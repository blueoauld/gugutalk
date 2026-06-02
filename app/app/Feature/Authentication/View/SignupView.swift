import SwiftUI

struct SignupView: View {

    @Environment(AuthenticationRouter.self) private var router

    @State private var vm = SignupViewModel()
    @State private var showAlert = false

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

                        VerificationCodeSendButton(title: "전송", disabled: !vm.sendCodeEnabled) {
                            Task {
                                await vm.sendVerificationCode()
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
            }
            .padding()
        }
        .onTapGesture {
            hideKeyboard()
        }
        .safeAreaBar(edge: .bottom) {
            SubmitButton(title: "회원가입", disabled: !vm.enabled) {
                Task {
                    if await vm.signup() {
                        router.push(.setup)
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
    }
}
