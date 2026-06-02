import SwiftUI

struct CustomTextEditor: View {
    
    let placeholder: String
    @Binding var text: String
    var keyboardType: UIKeyboardType = .default
    
    var body: some View {
        TextField(placeholder, text: $text, axis: .vertical)
            .padding(14)
            .lineLimit(5, reservesSpace: true)
            .background(
                Color(.systemGray6),
                in: RoundedRectangle(cornerRadius: 16)
            )
            .keyboardType(keyboardType)
    }
}
