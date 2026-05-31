import SwiftUI

struct ChatMessageList: View {

    let chatMessage: [ChatMessageRowResponse]
    let hasNext: Bool
    var onNext: () async -> Void
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

                        ChatMessageBubble(message: message)
                            .padding(.horizontal)
                            .padding(.vertical, 2)
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
