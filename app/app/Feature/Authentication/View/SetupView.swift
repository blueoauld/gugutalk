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
            SubmitButton(title: "들어가기", disabled: !vm.enabled) {
                Task {
                    if await vm.setup() {
                        session.isLoggedIn = true
                    }
                }
            }
        }
        .navigationTitle("프로필")
        .navigationBarTitleDisplayMode(.inline)
        .navigationBarBackButtonHidden(true)
    }
}
