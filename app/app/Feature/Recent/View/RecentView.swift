import SwiftUI

struct RecentView: View {
    
    @Environment(AppRouter.self) private var router
    
    @State private var vm = RecentViewModel()
    
    @State private var showComment = false
    
    var body: some View {
        VStack {
            GenderPicker(selectedGender: $vm.gender)
            
            switch vm.state {
            case .idle:
                Spacer()
                EmptyView()
                Spacer()
            case .loading:
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            case .empty:
                ScrollView {
                    ContentUnavailableView("내역 없음", systemImage: "tray")
                        .containerRelativeFrame([.horizontal, .vertical])
                }
                .refreshable {
                    await vm.refresh()
                }
            case .data:
                MemberList(
                    members: vm.members,
                    hasNext: vm.hasNext,
                    onNext: vm.getsNext,
                    onRefresh: {
                        await vm.bump()
                        await vm.refresh()
                    }
                )
            case .error(let message):
                ErrorRetryView(message: message, retry: vm.refresh)
            }
        }
        .navigationTitle("최근")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button {
                    showComment = true
                } label: {
                    Image(systemName: "square.and.pencil")
                        .font(.default)
                        .foregroundStyle(.primary)
                }
            }
        }
        .task {
            await vm.gets()
        }
        .onChange(of: vm.gender) { _, _ in
            Task {
                await vm.refresh()
            }
        }
        .alert("코멘트", isPresented: $showComment) {
            TextField("내용", text: $vm.comment)
            
            Button("작성") {
                Task {
                    await vm.updateComment()
                }
            }
            Button("취소", role: .cancel) { }
        }
        .overlay {
            if vm.isLoading {
                LoadingOverlay()
            }
        }
    }
}
