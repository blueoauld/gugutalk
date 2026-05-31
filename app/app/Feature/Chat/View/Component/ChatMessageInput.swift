import SwiftUI

struct ChatMessageInput: View {

    @Binding var message: String
    var onSend: () async -> Void
    var isLoading: Bool

    private var enabled: Bool {
        !message.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty && !isLoading
    }

    var body: some View {
        TextField("메세지 입력", text: $message, axis: .vertical)
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
                        await onSend()
                    }
                } label: {
                    Group {
                        if isLoading {
                            ProgressView()
                                .tint(.white)
                        } else {
                            Image(systemName: "paperplane.fill")
                                .foregroundColor(.white)
                        }
                    }
                    .frame(width: 36, height: 36)
                    .background(enabled ? Color.blue : Color(.systemGray3))
                    .clipShape(Circle())
                }
                .padding(.trailing, 4)
                .padding(.bottom, 4)
                .disabled(!enabled)
            }
            .glassEffect(
                .regular.tint(.clear).interactive(),
                in: .rect(cornerRadius: 20)
            )
            .autocorrectionDisabled(true)
            .textInputAutocapitalization(.never)
            .padding()
    }
}
