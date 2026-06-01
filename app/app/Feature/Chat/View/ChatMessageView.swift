import SwiftUI
import Kingfisher

struct ChatMessageView: View {

    let chatRoomId: Int64
    let memberId: Int64
    let nickname: String
    let profileUrl: String?

    @Environment(AppRouter.self) private var router

    @State private var vm = ChatMessageViewModel()
    @State private var scrollToBottomTrigger = 0

    private let imageSize: CGFloat = 35

    var body: some View {
        VStack {
            content
        }
        .onTapGesture {
            hideKeyboard()
        }
        .safeAreaBar(edge: .bottom) {
            ChatMessageInput(
                message: $vm.message,
                onSend: {
                    await vm.send(chatRoomId: chatRoomId)

                    scrollToBottomTrigger += 1
                },
                onSendMedia: { media in
                    await vm.upload(chatRoomId: chatRoomId, media: media)

                    scrollToBottomTrigger += 1
                },
                isLoading: vm.isLoading,
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
        .onChange(of: vm.shouldDismiss) { _, newValue in
            if newValue {
                router.pop()
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
                    await vm.loadNext(chatRoomId: chatRoomId)
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
