import SwiftUI

struct MainView: View {

    @Environment(AppRouter.self) private var router

    @State private var vm = MainViewModel()

    @State private var showComment = false

    var body: some View {
        VStack {
            MainViewPicker(selectedView: $vm.view)

            content
        }
        .navigationTitle("메인")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            toolbarContent
        }
        .task {
            await vm.load()
        }
        .onChange(of: vm.gender) { _, _ in
            Task {
                await vm.switchView()
            }
        }
        .onChange(of: vm.view) { _, _ in
            Task {
                await vm.switchView()
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

    @ViewBuilder
    private var content: some View {
        switch vm.state {
        case .idle:
            Spacer()
        case .loading:
            ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
        case .empty:
            ScrollView {
                ContentUnavailableView("내역 없음", systemImage: "tray")
                    .containerRelativeFrame([.horizontal, .vertical])
            }
            .refreshable {
                await vm.reload()
            }
        case .data:
            MemberList(
                members: vm.members,
                hasNext: vm.hasNext,
                onNext: vm.loadNext,
                onRefresh: {
                    await vm.bump()
                    await vm.reload()
                }
            )
        case .error(let message):
            ErrorRetryView(message: message, retry: vm.reload)
        }
    }

    @ToolbarContentBuilder
    private var toolbarContent: some ToolbarContent {
        ToolbarItemGroup(placement: .topBarTrailing) {
            Menu {
                GenderPicker(selectedGender: $vm.gender)
            } label: {
                Image(systemName: "line.3.horizontal.decrease")
                    .font(.body)
                    .foregroundStyle(.primary)
            }

            Button {
                showComment = true
            } label: {
                Image(systemName: "square.and.pencil")
                    .font(.body)
                    .foregroundStyle(.primary)
            }
        }

        ToolbarItem(placement: .topBarLeading) {
            Button {
                router.push(AppRoute.memberSearch)
            } label: {
                Image(systemName: "magnifyingglass")
                    .font(.body)
                    .foregroundStyle(.primary)
            }
        }
    }
}
