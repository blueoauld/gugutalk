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
        VStack {
            content
        }
        .navigationTitle("프로필 편집")
        .navigationBarTitleDisplayMode(.inline)
        .task {
            await vm.getMe()
        }
        .overlay {
            if vm.isLoading {
                LoadingOverlay()
            }
        }
    }

    @ViewBuilder
    private var content: some View {
        switch vm.state {
        case .idle:
            Spacer()
        case .loading:
            ProgressView()
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        case .data:
            ScrollView {
                VStack(spacing: 20) {
                    VStack(alignment: .leading) {
                        Text("프로필 사진")
                            .font(.subheadline)
                            .foregroundStyle(.primary)

                        MemberImagePicker(selectImages: $vm.publicImages, maxCount: 5)
                    }

                    VStack(alignment: .leading) {
                        Text("비밀 사진")
                            .font(.subheadline)
                            .foregroundStyle(.primary)

                        MemberImagePicker(selectImages: $vm.privateImages, maxCount: 5)
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
                SubmitButton(title: "편집하기", disabled: !vm.enabled) {
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
        case .error(let message):
            ErrorRetryView(message: message, retry: vm.getMe)
        }
    }
}
