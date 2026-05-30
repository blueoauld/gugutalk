import SwiftUI

struct ChatMessageBubble: View {

    let message: ChatMessageRowResponse

    var body: some View {
        if message.senderId == TokenStorage.shared.memberId {
            VStack(alignment: .trailing) {
                HStack(alignment: .bottom, spacing: 4) {
                    Spacer()

                    if let date = message.createdAt.toISO8601Date() {
                        Text(date.formatted(.dateTime.hour().minute()))
                            .font(.caption2)
                            .foregroundStyle(.secondary)
                    }

                    Text(message.content)
                        .font(.subheadline)
                        .foregroundColor(.white)
                        .padding(.horizontal, 14)
                        .padding(.vertical, 10)
                        .background(.blue)
                        .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
                }
            }
        } else {
            VStack(alignment: .trailing) {
                HStack(alignment: .bottom, spacing: 4) {
                    Text(message.content)
                        .font(.subheadline)
                        .foregroundColor(.primary)
                        .padding(.horizontal, 14)
                        .padding(.vertical, 10)
                        .background(Color(.systemGray5))
                        .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))

                    if let date = message.createdAt.toISO8601Date() {
                        Text(date.formatted(.dateTime.hour().minute()))
                            .font(.caption2)
                            .foregroundStyle(.secondary)
                    }

                    Spacer()
                }
            }
        }
    }
}
