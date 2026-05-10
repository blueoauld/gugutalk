import SwiftUI

struct SignupView: View {
    
    enum Field {
        case phone
        case verificationCode
        case password
        case passwordConfirm
        case birthYear
    }
    
    @Environment(AppRouter.self) private var router
    
    @FocusState private var focusedField: Field?
    
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
                            field: Field.phone,
                            focusedField: $focusedField,
                            keyboardType: .phonePad
                        )
                        
                        VerificationCodeSendButton(title: "전송", disabled: !vm.sendCodeEnabled) {
                            vm.isSendCode = true
                        }
                    }
                    
                    CustomTextField(
                        placeholder: "인증번호",
                        text: $vm.verificationCode,
                        field: Field.verificationCode,
                        focusedField: $focusedField,
                        keyboardType: .numberPad
                    )
                }
                
                VStack {
                    CustomSecureField(
                        placeholder: "비밀번호",
                        text: $vm.password,
                        field: Field.password,
                        focusedField: $focusedField
                    )
                    
                    CustomSecureField(
                        placeholder: "비밀번호 확인",
                        text: $vm.passwordConfirm,
                        field: Field.passwordConfirm,
                        focusedField: $focusedField
                    )
                }
                
                GenderSelector(gender: $vm.gender)
            }
            .padding()
        }
        .onTapGesture {
            focusedField = nil
        }
        .safeAreaBar(edge: .bottom) {
            SubmitButton(title: "회원가입", disabled: !vm.enabled) {
                router.push(.setup)
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
