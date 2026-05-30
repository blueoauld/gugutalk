import SwiftUI

struct ChatMessageList: View {
    
    let chatMessage: [ChatMessageRowResponse]
    let hasNext: Bool
    var onNext: () async -> Void
    
    var body: some View {
        List {
            ForEach(Array(chatMessage.enumerated()), id: \.element.id) { index, message in
                VStack {
                    ChatMessageBubble(message: message)
                        .padding(.horizontal)
                        .padding(.vertical, 2)
                }
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
    }
}
