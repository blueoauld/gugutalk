import SwiftUI

struct LoginView: View {
    
    enum Field {
        case phone
        case password
    }
    
    @Environment(AppRouter.self) private var router
    
    @FocusState private var focusedField: Field?
    
    @State private var vm = LoginViewModel()
    
    var body: some View {
        ScrollView {
            VStack {
                VStack {
                    CustomTextField(
                        placeholder: "휴대폰",
                        text: $vm.phone,
                        field: Field.phone,
                        focusedField: $focusedField,
                        keyboardType: .phonePad
                    )
                    
                    CustomSecureField(
                        placeholder: "비밀번호",
                        text: $vm.password,
                        field: Field.password,
                        focusedField: $focusedField
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
            focusedField = nil
        }
        .safeAreaBar(edge: .bottom) {
            SubmitButton(title: "로그인", disabled: !vm.enabled) {
                
            }
        }
        .navigationTitle("로그인")
        .navigationBarTitleDisplayMode(.inline)
    }
}
