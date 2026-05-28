import SwiftUI

struct MainView: View {

    @AppStorage(StorageKey.comment) private var savedComment = ""
    @AppStorage(StorageKey.view) private var savedView: ViewFilter = .recent
    @AppStorage(StorageKey.gender) private var savedGender: GenderFilter = .all

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
        .onChange(of: vm.gender) { _, newValue in
            savedGender = newValue

            Task {
                await vm.switchView()
            }
        }
        .onChange(of: vm.view) { _, newValue in
            savedView = newValue

            Task {
                await vm.switchView()
            }
        }
        .alert("코멘트", isPresented: $showComment) {
            TextField("내용", text: $vm.comment)

            Button("작성") {
                Task {
                    if await vm.updateComment() {
                        savedComment = vm.comment
                    }
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
                async let b: Void = vm.bump()
                async let r: Void = vm.reload()

                _ = await (b, r)
            }
        case .data:
            MemberList(
                members: vm.members,
                hasNext: vm.hasNext,
                onNext: vm.loadNext,
                onRefresh: {
                    async let b: Void = vm.bump()
                    async let r: Void = vm.reload()

                    _ = await (b, r)
                }
            )
        case .error(let message):
            ErrorRetryView(message: message, retry: vm.switchView)
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
                vm.comment = savedComment
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
