import SwiftUI

struct MemberView: View {
    
    let memberId: Int64

    @Environment(AppRouter.self) private var router

    @State private var vm = MemberViewModel()
    
    @State private var currentPage = 0
    @State private var showMenu = false
    
    var body: some View {
        VStack {
            content
        }
        .navigationTitle("프로필")
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
                    MemberActionBar(memberId: memberId, member: member, vm: vm)
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
            ToolbarItem(placement: .topBarTrailing) {
                Button {
                    showMenu = true
                } label: {
                    Image(systemName: "ellipsis")
                        .font(.default)
                        .foregroundStyle(.primary)
                }
                .confirmationDialog("메뉴", isPresented: $showMenu) {
                    Button(member.isPrivateImageGrant ? "비밀 사진 닫기" : "비밀 사진 공개") {
                        Task {
                            if member.isPrivateImageGrant {
                                await vm.deletePrivateImageGrant(memberId: memberId)
                            } else {
                                await vm.createPrivateImageGrant(memberId: memberId)
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
