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
            .frame(height: 50)
            .background(
                Color(.systemGray6),
                in: RoundedRectangle(cornerRadius: 16)
            )
            .keyboardType(keyboardType)
            .focused(focusedField, equals: field)
    }
}
