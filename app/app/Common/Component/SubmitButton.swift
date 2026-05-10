import SwiftUI

struct SubmitButton: View {

    let title: String
    let disabled: Bool
    var action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.default.bold())
                .padding(.vertical, 7)
                .frame(maxWidth: .infinity)
        }
        .buttonStyle(.glassProminent)
        .disabled(disabled)
        .animation(.smooth, value: disabled)
        .padding()
    }
}
