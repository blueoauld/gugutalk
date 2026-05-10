import SwiftUI

struct SetupView: View {
    
    enum Field {
        case nickname
        case birthYear
        case bio
    }

    @Environment(SessionStore.self) private var session

    @FocusState private var focusedField: Field?
    
    @State private var vm = SetupViewModel()
    
    var body: some View {
        ScrollView {
            VStack {
                CustomTextField(
                    placeholder: "닉네임",
                    text: $vm.nickname,
                    field: Field.nickname,
                    focusedField: $focusedField,
                    keyboardType: .default
                )
                
                CustomTextField(
                    placeholder: "출생연도",
                    text: $vm.birthYear,
                    field: Field.birthYear,
                    focusedField: $focusedField,
                    keyboardType: .numberPad
                )
                
                RegionPicker(region: $vm.region)
                
                CustomTextEditor(
                    placeholder: "자기소개",
                    text: $vm.bio,
                    field: Field.bio,
                    focusedField: $focusedField,
                    keyboardType: .default
                )
            }
            .padding()
        }
        .onTapGesture {
            focusedField = nil
        }
        .safeAreaBar(edge: .bottom) {
            SubmitButton(title: "들어가기", disabled: !vm.enabled) {
                focusedField = nil
                session.isLoggedIn = true
            }
        }
        .navigationTitle("프로필")
        .navigationBarTitleDisplayMode(.inline)
        .navigationBarBackButtonHidden(true)
    }
}
