import SwiftUI

struct ChatDateSeparator: View {

    let date: String

    var body: some View {
        if let date = date.toISO8601Date() {
            Text(date.formatted(.dateTime.year().month().day()))
                .font(.caption2)
                .foregroundStyle(.secondary)
                .padding(.horizontal, 12)
                .padding(.vertical, 4)
                .background(Color(.systemGray5), in: Capsule())
                .frame(maxWidth: .infinity)
                .padding(.vertical, 8)
        }
    }
}
