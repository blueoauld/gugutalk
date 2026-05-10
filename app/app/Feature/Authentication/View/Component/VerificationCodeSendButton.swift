import SwiftUI

struct VerificationCodeSendButton: View {

    let title: String
    let disabled: Bool
    var action: () -> Void

    var body: some View {
        Button(action: action) {
            Text("전송")
                .font(.default.bold())
                .foregroundColor(.white)
                .padding(.horizontal, 20)
                .frame(height: 50)
                .background(
                    disabled ? .gray : .blue,
                    in: RoundedRectangle(cornerRadius: 16)
                )
        }
        .disabled(disabled)
    }
}
