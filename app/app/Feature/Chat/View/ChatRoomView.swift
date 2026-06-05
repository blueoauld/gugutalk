import SwiftUI

struct ChatRoomView: View {
    
    @Environment(AppRouter.self) private var router
    @Environment(ChatRoomViewModel.self) private var vm
    @Environment(\.scenePhase) private var scenePhase
    
    @State private var toggleChatTrigger = false
    
    var body: some View {
        @Bindable var vm = vm
        
        VStack {
            ChatStatusPicker(selectedStatus: $vm.status)
            
            content
        }
        .navigationTitle("채팅")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            toolbarContent
        }
        .task {
            await vm.isChat()
            await vm.load()
        }
        .onChange(of: vm.status) { _, newValue in
            Task {
                await vm.switchView()
            }
        }
        .onChange(of: scenePhase) { _, newPhase in
            if newPhase == .active {
                Task {
                    await vm.switchView()
                }
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
            ChatRoomList(
                chatRooms: vm.chatRooms,
                hasNext: vm.hasNext,
                onNext: {
                    if case .failure(let error) = await vm.loadNext() {
                        ToastManager.shared.show(error.userMessage, style: .error)
                    }
                },
                onTap: {
                    router.push(.chatMessage($0, $1, $2, $3))
                },
                onRead: {
                    if case .failure(let error) = await vm.read(chatRoomId: $0) {
                        ToastManager.shared.show(error.userMessage, style: .error)
                    }
                },
                onDelete: {
                    if case .failure(let error) = await vm.delete(chatRoomId: $0) {
                        ToastManager.shared.show(error.userMessage, style: .error)
                    }
                },
            )
        case .error(let message):
            ErrorRetryView(message: message, retry: vm.switchView)
        }
    }
    
    @ToolbarContentBuilder
    private var toolbarContent: some ToolbarContent {
        ToolbarItem(placement: .topBarLeading) {
            Button {
                router.push(AppRoute.chatRoomSearch)
            } label: {
                Image(systemName: "magnifyingglass")
                    .font(.body)
                    .foregroundStyle(.primary)
            }
        }
        
        ToolbarItem(placement: .topBarTrailing) {
            Button {
                toggleChatTrigger.toggle()
                
                Task {
                    if case .failure(let error) = await vm.toggleChat() {
                        ToastManager.shared.show(error.userMessage, style: .error)
                    }
                }
            } label: {
                Image(systemName: vm.isChat ? "bell" : "bell.slash")
                    .font(.body)
                    .foregroundStyle(.primary)
                    .contentTransition(.symbolEffect(.replace))
            }
            .sensoryFeedback(.selection, trigger: toggleChatTrigger)
        }
    }
}
