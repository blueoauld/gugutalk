import SwiftUI

struct MemberView: View {

    let memberId: Int64

    @Environment(AppRouter.self) private var router

    @State private var vm = MemberViewModel()
    @State private var showMenu = false

    private var isMe: Bool {
        memberId == TokenStorage.shared.memberId
    }

    var body: some View {
        VStack {
            content
        }
        .navigationTitle(isMe ? "내 프로필" : "프로필")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            toolbarContent
        }
        .task {
            await vm.get(memberId: memberId)
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
            if let member = vm.member {
                ScrollView {
                    MemberProfile(member: member)
                }
                .safeAreaBar(edge: .bottom) {
                    if !isMe {
                        MemberActionBar(memberId: memberId, member: member, vm: vm)
                    }
                }
                .ignoresSafeArea(.keyboard, edges: .bottom)
            }
        case .error(let message):
            ErrorRetryView(message: message, retry: { await vm.get(memberId: memberId) })
        }
    }

    @ToolbarContentBuilder
    private var toolbarContent: some ToolbarContent {
        if let member = vm.member {
            if isMe {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        showMenu = true
                    } label: {
                        Image(systemName: "ellipsis")
                            .font(.body)
                            .foregroundStyle(.primary)
                    }
                    .confirmationDialog("메뉴", isPresented: $showMenu) {
                        Button("편집") {
                            router.push(AppRoute.memberProfile)
                        }

                        Button("리뷰") {
                            router.push(AppRoute.review(memberId, member.nickname))
                        }
                    }
                }
            } else {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        showMenu = true
                    } label: {
                        Image(systemName: "ellipsis")
                            .font(.body)
                            .foregroundStyle(.primary)
                    }
                    .confirmationDialog("메뉴", isPresented: $showMenu) {
                        Button(member.isPrivateImageGrant ? "비밀 사진 닫기" : "비밀 사진 공개") {
                            Task {
                                if member.isPrivateImageGrant {
                                    if case .failure(let error) = await vm.deletePrivateImageGrant(memberId: memberId) {
                                        ToastManager.shared.show(error.userMessage, style: .error)
                                    }
                                } else {
                                    if case .failure(let error) = await vm.createPrivateImageGrant(memberId: memberId) {
                                        ToastManager.shared.show(error.userMessage, style: .error)
                                    }
                                }
                            }
                        }

                        Button("신고", role: .destructive) {
                            router.push(AppRoute.report(memberId, member.nickname))
                        }
                    }
                }
            }
        }
    }
}
