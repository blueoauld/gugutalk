import SwiftUI

struct MemberProfileView: View {

    @Environment(AppRouter.self) private var router

    @State private var vm = MemberProfileViewModel()

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
        .trackScreen("member_profile")
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
                }
                .padding()
            }
            .safeAreaBar(edge: .bottom) {
                SubmitButton(title: "편집하기", disabled: !vm.enabled || vm.isLoading) {
                    Task {
                        guard let result = await vm.update() else { return }

                        switch result {
                        case .success():
                            ToastManager.shared.show("프로필 편집이 완료되었습니다.", style: .info)
                            router.pop()
                        case .failure(let error):
                            ToastManager.shared.show(error.userMessage, style: .error)
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
