import SwiftUI

struct CustomSecureField<Field: Hashable>: View {

    let placeholder: String
    @Binding var text: String
    let field: Field
    var focusedField: FocusState<Field>.Binding

    var body: some View {
        SecureField(placeholder, text: $text)
            .padding(.horizontal, 14)
            .frame(height: 50)
            .background(
                Color(.systemGray6),
                in: RoundedRectangle(cornerRadius: 16)
            )
            .focused(focusedField, equals: field)
    }
}
