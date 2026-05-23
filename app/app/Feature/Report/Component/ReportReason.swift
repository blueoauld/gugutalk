import SwiftUI

struct ReportReason: View {

    @Binding var reason: String

    var body: some View {
        TextField("신고 사유 입력", text: $reason, axis: .vertical)
            .font(.body)
            .padding()
            .lineLimit(5, reservesSpace: true)
            .background(
                Color(.systemGray6),
                in: RoundedRectangle(cornerRadius: 20)
            )
    }
}
