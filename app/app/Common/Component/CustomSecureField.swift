import SwiftUI

struct CustomSecureField: View {
    
    let placeholder: String
    @Binding var text: String
    
    var body: some View {
        SecureField(placeholder, text: $text)
            .padding(.horizontal, 14)
            .frame(height: 50)
            .background(
                Color(.systemGray6),
                in: RoundedRectangle(cornerRadius: 16)
            )
    }
}
