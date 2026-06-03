import SwiftUI

struct RankView: View {

    @AppStorage(StorageKey.rank) private var savedRank: RankFilter = .like
    @AppStorage(StorageKey.rankGender) private var savedGender: GenderFilter = .all

    @Environment(AppRouter.self) private var router

    @State private var vm = RankViewModel()

    var body: some View {
        VStack {
            RankViewPicker(selectedRank: $vm.rank)

            content
        }
        .navigationTitle("랭킹")
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
        .onChange(of: vm.rank) { _, newValue in
            savedRank = newValue

            Task {
                await vm.switchView()
            }
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
        case .data:
            RankList(
                members: vm.members,
                hasNext: vm.hasNext,
                onNext: vm.loadNext,
                onTap: {
                    router.push(.member($0))
                },
                onSend: { memberId, message in
                    await vm.createChatRoom(memberId: memberId, message: message)
                }
            )
        case .error(let message):
            ErrorRetryView(message: message, retry: vm.load)
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
        }
    }
}
