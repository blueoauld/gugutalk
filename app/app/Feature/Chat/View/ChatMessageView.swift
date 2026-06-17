import SwiftUI
import Kingfisher

struct ChatMessageView: View {

    let chatRoomId: Int64
    let memberId: Int64
    let nickname: String
    let profileUrl: String?

    @Environment(AppRouter.self) private var router
    @Environment(\.scenePhase) private var scenePhase

    @State private var vm = ChatMessageViewModel()
    @State private var scrollToBottomTrigger = 0
    @State private var message = ""

    private let imageSize: CGFloat = 35

    var body: some View {
        VStack {
            content
        }
        .onTapGesture {
            hideKeyboard()
        }
        .safeAreaBar(edge: .bottom) {
            ChatMessageInputBar(
                message: $message,
                onSend: {
                    vm.message = message
                    message = ""

                    if case .failure(let error) = await vm.send(chatRoomId: chatRoomId) {
                        if let apiError = error as? APIError, case .server(let code, _, _) = apiError, code == "CHAT_03" {
                            router.pop()
                        }
                        ToastManager.shared.show(error.userMessage, style: .error)
                    }

                    scrollToBottomTrigger += 1
                },
                onSendMedia: { media in
                    if case .failure(let error) = await vm.upload(chatRoomId: chatRoomId, media: media) {
                        if let apiError = error as? APIError, case .server(let code, _, _) = apiError, code == "CHAT_03" {
                            router.pop()
                        }
                        ToastManager.shared.show(error.userMessage, style: .error)
                    }

                    scrollToBottomTrigger += 1
                },
                isUploading: vm.isUploading
            )
        }
        .navigationTitle(nickname)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            toolbarContent
        }
        .task {
            vm.subscribe(chatRoomId: chatRoomId)

            await vm.load(chatRoomId: chatRoomId)
            await vm.read(chatRoomId: chatRoomId)
        }
        .onDisappear {
            vm.unsubscribe(chatRoomId: chatRoomId)
        }
        .onChange(of: scenePhase) { _, newPhase in
            if newPhase == .active {
                Task {
                    await vm.syncLatest(chatRoomId: chatRoomId)
                    await vm.read(chatRoomId: chatRoomId)
                }
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
            ChatMessageList(
                chatMessage: vm.chatMessages,
                hasNext: vm.hasNext,
                onNext: {
                    if case .failure(let error) = await vm.loadNext(chatRoomId: chatRoomId) {
                        ToastManager.shared.show(error.userMessage, style: .error)
                    }
                },
                onReact: { message, emoji in
                    Task {
                        guard let type = ReactionType(emoji: emoji) else { return }

                        if case .failure(let error) = await vm.react(
                            chatRoomId: chatRoomId,
                            chatMessageId: message.chatMessageId,
                            type: type
                        ) {
                            ToastManager.shared.show(error.userMessage, style: .error)
                        }
                    }
                },
                scrollToBottomTrigger: scrollToBottomTrigger
            )
        case .error(let message):
            ErrorRetryView(message: message, retry: {
                await vm.load(chatRoomId: chatRoomId)
            })
        }
    }

    @ToolbarContentBuilder
    private var toolbarContent: some ToolbarContent {
        ToolbarItem(placement: .topBarTrailing) {
            KFImage(profileUrl.flatMap { URL(string: $0) })
                .placeholder {
                    Image(systemName: "person.fill")
                        .font(.footnote)
                        .foregroundColor(Color(.systemGray4))
                        .frame(width: imageSize, height: imageSize)
                        .background(Color(.systemGray6))
                        .clipShape(Circle())
                }
                .retry(maxCount: 3, interval: .seconds(2))
                .fade(duration: 0.25)
                .resizable()
                .scaledToFill()
                .frame(width: imageSize, height: imageSize)
                .clipShape(Circle())
                .onTapGesture {
                    router.push(AppRoute.member(memberId))
                }
        }
    }
}
