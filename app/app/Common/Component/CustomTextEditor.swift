import SwiftUI

struct CustomTextEditor<Field: Hashable>: View {

    let placeholder: String
    @Binding var text: String
    let field: Field
    var focusedField: FocusState<Field>.Binding
    var keyboardType: UIKeyboardType = .default

    var body: some View {
        TextField(placeholder, text: $text, axis: .vertical)
            .padding(14)
            .lineLimit(5, reservesSpace: true)
            .background(
                Color(.systemGray6),
                in: RoundedRectangle(cornerRadius: 16)
            )
            .focused(focusedField, equals: field)
            .keyboardType(keyboardType)
    }
}
