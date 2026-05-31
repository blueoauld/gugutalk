import SwiftUI

struct CustomTextField<Field: Hashable>: View {
    
    let placeholder: String
    @Binding var text: String
    let field: Field
    var focusedField: FocusState<Field>.Binding
    var keyboardType: UIKeyboardType = .default
    
    var body: some View {
        TextField(placeholder, text: $text)
            .padding(.horizontal, 14)
            .padding(.trailing, 28)
            .frame(height: 50)
            .background(
                Color(.systemGray6),
                in: RoundedRectangle(cornerRadius: 16)
            )
            .keyboardType(keyboardType)
            .focused(focusedField, equals: field)
            .overlay(alignment: .trailing) {
                if !text.isEmpty {
                    Button {
                        text = ""
                    } label: {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundStyle(.gray)
                    }
                    .padding(.trailing, 12)
                }
            }
    }
}
