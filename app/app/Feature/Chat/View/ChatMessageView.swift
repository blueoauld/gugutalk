import SwiftUI

struct ChatMessage: Identifiable {

    let id = UUID()
    let text: String
    let isFromCurrentUser: Bool
    let date: Date
}

struct ChatMessageView: View {

    let chatRoomId: Int64
    let memberId: Int64
    let nickname: String
    let profileUrl: String?

    @State private var review = ""
    @State private var messages: [ChatMessage] = [
        ChatMessage(text: "안녕하세요! 오늘 회의 시간 확인차 연락드려요.", isFromCurrentUser: false, date: .now.addingTimeInterval(-600)),
        ChatMessage(text: "네 안녕하세요 오후 2시 맞습니다.", isFromCurrentUser: true, date: .now.addingTimeInterval(-540)),
        ChatMessage(text: "장소는 어디인가요?", isFromCurrentUser: false, date: .now.addingTimeInterval(-480)),
        ChatMessage(text: "3층 대회의실이에요. 자료는 미리 공유드릴게요!", isFromCurrentUser: true, date: .now.addingTimeInterval(-420)),
        ChatMessage(text: "감사합니다 😊 그때 뵙겠습니다.", isFromCurrentUser: false, date: .now.addingTimeInterval(-360))
    ]

    var body: some View {
        List {
            ForEach(messages) { message in
                MessageBubble(message: message)
                    .listRowSeparator(.hidden)
                    .listRowInsets(EdgeInsets(top: 4, leading: 12, bottom: 4, trailing: 12))
            }
        }
        .listStyle(.plain)
        .safeAreaBar(edge: .bottom) {
            TextField("메시지 입력", text: $review, axis: .vertical)
                .font(.subheadline)
                .lineLimit(1...5)
                .multilineTextAlignment(.leading)
                .padding(.leading)
                .padding(.trailing, 50)
                .padding(.vertical, 8)
                .frame(minHeight: 44)
                .overlay(alignment: .bottomTrailing) {
                    Button {
                        Task {
                            sendMessage()
                        }
                    } label: {
                        Image(systemName: "paperplane.fill")
                            .foregroundColor(.white)
                            .frame(width: 36, height: 36)
                            .background(review.isEmpty ? Color(.systemGray3) : .blue)
                            .clipShape(Circle())
                    }
                    .padding(.trailing, 4)
                    .padding(.bottom, 4)
                    .disabled(review.isEmpty)
                }
                .glassEffect(
                    .regular.tint(.clear).interactive(),
                    in: .rect(cornerRadius: 20)
                )
                .autocorrectionDisabled(true)
                .textInputAutocapitalization(.never)
                .padding()
        }
        .navigationTitle("홍길동")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Image(systemName: "person.fill")
                    .font(.footnote)
                    .foregroundColor(Color(.systemGray5))
                    .frame(width: 35, height: 35)
                    .background(Color(.systemGray4))
                    .clipShape(Circle())
            }
        }
    }

    private func sendMessage() {
        let trimmed = review.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return }
        messages.append(ChatMessage(text: trimmed, isFromCurrentUser: true, date: .now))
        review = ""
    }
}

struct MessageBubble: View {

    let message: ChatMessage

    private var timeText: String {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "ko_KR")
        formatter.dateFormat = "a h:mm"
        return formatter.string(from: message.date)
    }

    var body: some View {
        if message.isFromCurrentUser {
            VStack(alignment: .trailing) {
                HStack(alignment: .bottom, spacing: 4) {
                    Spacer()

                    timeLabel

                    Text(message.text)
                        .font(.subheadline)
                        .foregroundColor(.white)
                        .padding(.horizontal, 14)
                        .padding(.vertical, 10)
                        .background(.blue)
                        .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
                }
            }
        }

        if !message.isFromCurrentUser {
            VStack(alignment: .trailing) {
                HStack(alignment: .bottom, spacing: 4) {
                    Text(message.text)
                        .font(.subheadline)
                        .foregroundColor(.primary)
                        .padding(.horizontal, 14)
                        .padding(.vertical, 10)
                        .background(Color(.systemGray5))
                        .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))

                    timeLabel

                    Spacer()
                }
            }
        }
    }

    private var timeLabel: some View {
        Text(timeText)
            .font(.caption2)
            .foregroundColor(.secondary)
    }
}

#Preview {
    NavigationStack {
        ChatMessageView(chatRoomId: 1, memberId: 1, nickname: "홍길동", profileUrl: nil)
    }
}
