import SwiftUI

struct MemberProfileView: View {

    enum Field {
        case nickname
        case birthYear
        case bio
    }

    @Environment(AppRouter.self) private var router

    @State private var vm = MemberProfileViewModel()

    @FocusState private var focusedField: Field?

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                VStack(alignment: .leading) {
                    Text("프로필 사진")
                        .font(.subheadline)
                        .foregroundStyle(.primary)

                    ImagePicker(selectImages: $vm.selectPublicImages, maxCount: 5)
                }

                VStack(alignment: .leading) {
                    Text("비밀 사진")
                        .font(.subheadline)
                        .foregroundStyle(.primary)

                    ImagePicker(selectImages: $vm.selectPrivateImages, maxCount: 5)
                }

                VStack(alignment: .leading) {
                    Text("정보")
                        .font(.subheadline)
                        .foregroundStyle(.primary)

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
            }
            .padding()
        }
        .safeAreaBar(edge: .bottom) {
            SubmitButton(title: "편집하기", disabled: $vm.selectPublicImages.isEmpty) {
                Task {
                    if await vm.update() {
                        router.pop()
                    }
                }
            }
        }
        .onTapGesture {
            hideKeyboard()
        }
        .navigationTitle("프로필 편집")
        .navigationBarTitleDisplayMode(.inline)
        .overlay {
            if vm.isLoading {
                LoadingOverlay()
            }
        }
    }
}
