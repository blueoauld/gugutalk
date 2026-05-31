import SwiftUI

struct ChatMessageInput: View {

    @Binding var message: String

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
                    }
                } label: {
                    Image(systemName: "paperplane.fill")
                        .foregroundColor(.white)
                        .frame(width: 36, height: 36)
                        .background(message.isEmpty ? Color(.systemGray3) : .blue)
                        .clipShape(Circle())
                }
                .padding(.trailing, 4)
                .padding(.bottom, 4)
                .disabled(message.isEmpty)
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
