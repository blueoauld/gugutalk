import SwiftUI

struct ChatMessageList: View {

    let chatMessage: [ChatMessageRowResponse]
    let hasNext: Bool
    var onNext: () async -> Void
    var onReact: (ChatMessageRowResponse, String) -> Void
    var scrollToBottomTrigger: Int

    var body: some View {
        ScrollViewReader { proxy in
            List {
                ForEach(Array(chatMessage.enumerated()), id: \.element.id) { index, message in
                    VStack {
                        if shouldShowDateDivider(at: index) {
                            ChatDateSeparator(date: message.createdAt)
                                .padding(.vertical, 12)
                        }

                        ChatMessageBubble(message: message) { emoji in
                            onReact(message, emoji)
                        }
                        .padding(.horizontal)
                        .padding(.vertical, 4)
                    }
                    .id(message.id)
                    .rotationEffect(.degrees(180))
                    .listRowSeparator(.hidden)
                    .listRowInsets(EdgeInsets())
                }

                if hasNext {
                    HStack {
                        Spacer()
                        ProgressView()
                        Spacer()
                    }
                    .rotationEffect(.degrees(180))
                    .listRowSeparator(.hidden)
                    .task {
                        await onNext()
                    }
                } else {
                    ChatYouthProtectionNotice()
                        .padding()
                        .rotationEffect(.degrees(180))
                        .listRowSeparator(.hidden)
                        .listRowInsets(EdgeInsets())
                }
            }
            .rotationEffect(.degrees(180))
            .environment(\.defaultMinListRowHeight, 0)
            .scrollIndicators(.hidden)
            .listStyle(.plain)
            .onChange(of: scrollToBottomTrigger) {
                guard let firstId = chatMessage.first?.id else { return }

                withAnimation {
                    proxy.scrollTo(firstId, anchor: .top)
                }
            }
        }
    }

    private func messageDate(_ message: ChatMessageRowResponse) -> Date {
        message.createdAt.toISO8601Date() ?? Date()
    }

    private func shouldShowDateDivider(at index: Int) -> Bool {
        let current = messageDate(chatMessage[index])
        let olderIndex = index + 1

        guard olderIndex < chatMessage.count else {
            return true
        }

        let older = messageDate(chatMessage[olderIndex])
        return !Calendar.current.isDate(current, inSameDayAs: older)
    }
}

struct ChatYouthProtectionNotice: View {

    var body: some View {
        VStack(alignment: .leading) {
            Text("안내문")
                .font(.subheadline.bold())
                .foregroundStyle(.primary)

            Text("불법 촬영물, 성착취물 등 불법 정보의 게시 및 유통, 성매매 및 성매매 알선, 유인 행위는 관련 법령에 따라 처벌받을 수 있습니다. 욕설, 비방, 음란성 대화 및 개인정보 요구는 제재 대상이며, 유해 정보 발견 시 신고해 주세요.")
                .font(.footnote)
                .foregroundStyle(.secondary)
        }
        .padding()
        .background(Color(.secondarySystemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }
}
